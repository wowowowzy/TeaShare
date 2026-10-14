package com.example.tea.service.impl;

import com.example.tea.ai.community.CommunityKeywordExtractor;
import com.example.tea.entity.dto.Community.*;
import com.example.tea.entity.pojo.Community.Collect;
import com.example.tea.entity.pojo.Community.Like;
import com.example.tea.entity.pojo.Community.Post;
import com.example.tea.entity.pojo.PageResult;
import com.example.tea.entity.vo.Community.MyCollectVO;
import com.example.tea.entity.vo.Community.MyPostVO;
import com.example.tea.entity.vo.Community.PostDetailVO;
import com.example.tea.entity.vo.Community.PostListPageVO;
import com.example.tea.mapper.CommunityMapper;
import com.example.tea.rabbitmq.consumer.CommunityConsumer;
import com.example.tea.service.CommunityService;
import com.example.tea.utils.ThreadLocalUserIdUtil;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.tea.config.RabbitMQCommunityConfig.COMMUNITY_EXCHANGE;
import static com.example.tea.config.RabbitMQCommunityConfig.COMMUNITY_ROUTING_KEY;

@Service
@Slf4j
public class CommunityServiceImpl implements CommunityService {
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Resource
    private VectorStore recommendVectorStore;
    @Autowired
    private CommunityKeywordExtractor keywordExtractor;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    private static final Long LIKE = 3L;
    private static final Long COMMENT = 5L;
    private static final Long COLLECT = 10L;
    private static final Long VIEW = 1L;


    /**
     * 发布新帖子
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(PostCreateDTO dto) {
        if (!SensitiveWordHelper.contains(dto.getContent())
        &&!SensitiveWordHelper.contains(dto.getTitle())) {
            Post post = Post.builder()
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .userId(ThreadLocalUserIdUtil.getCurrentId())
                    .build();
            communityMapper.insert(post);
            //更新推荐算法向量库
            try {
                Document document = Document.builder()
                        .id(post.getId().toString())
                        .text(dto.getTitle())
                        .build();
                recommendVectorStore.add(Collections.singletonList(document));
                log.info("✅ 向量库新增帖子成功：postId=" + post.getId());
            } catch (Exception e) {
                log.info("❌ 向量库新增帖子失败：postId=" + post.getId());
                e.printStackTrace();
            }
            return post.getId();
        }else {
            String word = new StringBuffer().append(SensitiveWordHelper.findAll(dto.getTitle()))
                    .append(SensitiveWordHelper.findAll(dto.getContent()))
                    .append("这些是违禁词不允许发布！").toString();
            throw new RuntimeException(word);
        }
    }

    /**
     * 查询帖子详情
     */
    @Override
    public PostDetailVO getPostDetail(Long postId) {
        // 1. 查询帖子基础信息
        PostWithUsernameDTO post = communityMapper.selectById(postId);
        List<CommentDTO> comments = communityMapper.getCommentByPostId(postId,ThreadLocalUserIdUtil.getCurrentId());

        if (post == null|| post.getStatus().equals(0)) {
            throw new RuntimeException("帖子不存在或已删除");
        }
        PostDetailVO detailVO = PostDetailVO.builder().build();
        BeanUtils.copyProperties(post,detailVO);
        detailVO.setPostId(post.getId());
        detailVO.setCommentDTOS(comments);
        isCancelDTO isCancel = communityMapper.getCancel(ThreadLocalUserIdUtil.getCurrentId(),postId);
        detailVO.setLikeCancel(
                Optional.ofNullable(isCancel)
                        .map(isCancelDTO::getLikeCancel)
                        .orElse(-1)
        );

        detailVO.setCollectCancel(
                Optional.ofNullable(isCancel)
                        .map(isCancelDTO::getCollectCancel)
                        .orElse(-1)
        );

        //异步社区算法加权
        try {
            RabbitMQDTO mqMsg = RabbitMQDTO.builder().
                    postId(postId)
                    .weight(VIEW)
                    .userId(ThreadLocalUserIdUtil.getCurrentId())
                    .build();
            rabbitTemplate.convertAndSend(COMMUNITY_EXCHANGE,
                    COMMUNITY_ROUTING_KEY,
                    mqMsg);
        } catch (AmqpException e) {
            throw new RuntimeException(e);
        }

        return detailVO;
    }

    @Override
    public PageResult getPostList(CommunityQueryDTO dto) {
        PageHelper.startPage(
                dto.getPage()==null ? 1 : dto.getPage(),
                dto.getPageSize()==null ? 10 : dto.getPageSize()
        );
        CommunityQueryWithUserIdDTO build = CommunityQueryWithUserIdDTO
                .builder().userId(ThreadLocalUserIdUtil.getCurrentId()).build();
        BeanUtils.copyProperties(dto,build);
        Page<PostListPageVO> page = communityMapper.getPostList(build);
        return PageResult.builder()
                .total(page.getTotal())
                .records(page.getResult())
                .build();
    }

    @Override
    public List<MyPostVO> getMyPost() {
        Long userId = ThreadLocalUserIdUtil.getCurrentId();
        return communityMapper.getMyPost(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyPost(UpdateDTO updateMyPost) {
        if (!SensitiveWordHelper.contains(updateMyPost.getContent())
                &&!SensitiveWordHelper.contains(updateMyPost.getTitle())) {
            UpdateMyPostDTO updateMyPostDTO = UpdateMyPostDTO.builder().build();
            BeanUtils.copyProperties(updateMyPost,updateMyPostDTO);
            updateMyPostDTO.setUserId(ThreadLocalUserIdUtil.getCurrentId());
            //删掉帖子关键词（缓存失效，下次按新标题重新抽取）
            keywordExtractor.evict(updateMyPost.getPostId());
            //更新推荐算法向量库
            try {
                String id = updateMyPost.getPostId().toString();
                recommendVectorStore.delete(Collections.singletonList(id));
                Document document = Document.builder()
                        .id(id)
                        .text(updateMyPost.getTitle())
                        .build();
                recommendVectorStore.add(Collections.singletonList(document));

                log.info("✅ 向量库更新帖子成功：postId=" + updateMyPost.getPostId());
            } catch (Exception e) {
                log.info("❌ 向量库更新帖子失败：postId=" + updateMyPost.getPostId());
                e.printStackTrace();
            }
            communityMapper.updateMyPost(updateMyPostDTO);
        }else {
            String word = new StringBuffer().append(SensitiveWordHelper.findAll(updateMyPost.getTitle()))
                    .append(SensitiveWordHelper.findAll(updateMyPost.getContent()))
                    .append("这些是违禁词不允许发布！").toString();
            throw new RuntimeException(word);
        }
    }

    @Override
    public void deleteMyPost(Long postId) {
        //删掉帖子关键词（缓存失效）
        keywordExtractor.evict(postId);
        int deleted = communityMapper.deleteMyPost(postId,ThreadLocalUserIdUtil.getCurrentId());
        if (deleted == 0) {
            //帖子不存在或不属于当前用户：幂等返回，不执行后续向量库删除
            return;
        }
        //更新推荐算法向量库
        try {
            String id = postId.toString();
            recommendVectorStore.delete(Collections.singletonList(id));
            log.info("✅ 向量库删除帖子成功：postId=" +postId);
        } catch (Exception e) {
            log.info("❌ 向量库删除帖子失败：postId=" +postId);
            e.printStackTrace();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void comment(NewCommentDTO newCommentDTO) {
        if(SensitiveWordHelper.contains(newCommentDTO.getContent())){
            String string = new StringBuilder().append("包含违禁词")
                    .append(SensitiveWordHelper.findAll(newCommentDTO.getContent())).toString();
            throw new RuntimeException(string);
        }else {
            communityMapper.comment(newCommentDTO,ThreadLocalUserIdUtil.getCurrentId());
            communityMapper.addCommentNum(newCommentDTO.getPostId());
        }
        //异步社区算法加权
        try {
            RabbitMQDTO mqMsg = RabbitMQDTO.builder().
                    postId(newCommentDTO.getPostId())
                    .weight(COMMENT)
                    .userId(ThreadLocalUserIdUtil.getCurrentId())
                    .build();
            rabbitTemplate.convertAndSend(COMMUNITY_EXCHANGE,
                    COMMUNITY_ROUTING_KEY,
                    mqMsg);
        } catch (AmqpException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        Long currentUserId = ThreadLocalUserIdUtil.getCurrentId();
        //越权校验：只有评论作者本人能删除自己的评论
        Long commentUserId = communityMapper.getCommentUserId(id);
        if (commentUserId == null) {
            //评论已不存在：幂等处理，直接返回成功（不发无效权重，也不改变前端返回结构）
            log.info("评论不存在或已删除，忽略本次删除: commentId={}", id);
            return;
        }
        if (!commentUserId.equals(currentUserId)) {
            throw new RuntimeException("只能删除自己的评论");
        }
        //先查 postId：评论删除后 getPostByCommentId 会查不到（原来的顺序拿到的必然是 null，
        //导致 -COMMENT 权重挂在 null postId 上被消费者丢弃）
        Long postId = communityMapper.getPostByCommentId(id);
        if (postId == null) {
            //评论已不存在：幂等处理，直接返回成功（不发无效权重，也不改变前端返回结构）
            log.info("评论不存在或已删除，忽略本次删除: commentId={}", id);
            return;
        }
        //级联删除：本条 + 其下所有回复
        List<Long> integers = communityMapper.getAssociationComment(id);
        integers.add(id);
        communityMapper.deleteComment(integers);
        //评论数同步扣减（与 COMMENT 权重一致，按同一批删除的条数）
        communityMapper.reduceCommentNum(postId, integers.size());
        //异步社区算法加权：删除评论 = 不喜欢，减权
        try {
            RabbitMQDTO mqMsg = RabbitMQDTO.builder().
                    postId(postId)
                    .weight(-COMMENT)//负数
                    .userId(ThreadLocalUserIdUtil.getCurrentId())
                    .build();
            rabbitTemplate.convertAndSend(COMMUNITY_EXCHANGE,
                    COMMUNITY_ROUTING_KEY,
                    mqMsg);
        } catch (AmqpException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchLikeComment(Long commentId, Integer cancel) {
        //cancel=-1时为取消点赞
        //查看是否是新纪录
        Like like =  communityMapper.check(commentId,Like.TYPE_COMMENT,ThreadLocalUserIdUtil.getCurrentId());
        if(like == null){
            //新纪录一定是点赞 cancel=1
            communityMapper.addLike(Like.builder()
                    .userId(ThreadLocalUserIdUtil.getCurrentId())
                    .targetId(commentId)
                    .targetType(Like.TYPE_COMMENT)
                    .createTime(LocalDateTime.now())
                    .isCancel(1).build());

            communityMapper.likeComment(commentId,cancel);
        }else {
            //老记录则传cancel=1为点赞,-1为取消
            communityMapper.updateLike(commentId, ThreadLocalUserIdUtil.getCurrentId(), Like.TYPE_COMMENT, cancel);
            communityMapper.likeComment(commentId,cancel);
        }

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchLikePost(Long postId, Integer cancel) {
        //查看是否是新纪录
        Like like =  communityMapper.check(postId,Like.TYPE_POST,ThreadLocalUserIdUtil.getCurrentId());
        if(like == null){
            //新纪录一定是点赞 cancel=1
            communityMapper.addLike(Like.builder()
                    .userId(ThreadLocalUserIdUtil.getCurrentId())
                    .targetId(postId)
                    .targetType(Like.TYPE_POST)
                    .createTime(LocalDateTime.now())
                    .isCancel(1).build());
            communityMapper.likePost(postId,cancel);
            //异步社区算法加权
            try {
                RabbitMQDTO mqMsg = RabbitMQDTO.builder().
                        postId(postId)
                        .weight(LIKE)
                        .userId(ThreadLocalUserIdUtil.getCurrentId())
                        .build();
                rabbitTemplate.convertAndSend(COMMUNITY_EXCHANGE,
                        COMMUNITY_ROUTING_KEY,
                        mqMsg);
            } catch (AmqpException e) {
                throw new RuntimeException(e);
            }
        }else {
            //老记录则传cancel=1为点赞,-1为取消
            communityMapper.updateLike(postId, ThreadLocalUserIdUtil.getCurrentId(), Like.TYPE_POST, cancel);
            communityMapper.likePost(postId,cancel);
            //异步社区算法加权：按本次动作方向发权重（cancel=1 点赞 +LIKE，-1 取消 -LIKE）
            try {
                RabbitMQDTO mqMsg = RabbitMQDTO.builder().
                        postId(postId)
                        .weight(cancel > 0 ? LIKE : -LIKE)
                        .userId(ThreadLocalUserIdUtil.getCurrentId())
                        .build();
                rabbitTemplate.convertAndSend(COMMUNITY_EXCHANGE,
                        COMMUNITY_ROUTING_KEY,
                        mqMsg);
            } catch (AmqpException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchCollect(Long id, Integer cancel) {
        //查看是否是新纪录
         Collect collect = communityMapper.checkCollect(id,ThreadLocalUserIdUtil.getCurrentId());
        //新纪录一定是点赞 cancel=1
         if (collect == null){
             communityMapper.addCollect(Collect.builder()
                     .userId(ThreadLocalUserIdUtil.getCurrentId())
                     .postId(id)
                     .createTime(LocalDateTime.now())
                     .isCancel(1).build());
             communityMapper.collectPost(id,cancel);
             //异步社区算法加权
             try {
                 RabbitMQDTO mqMsg = RabbitMQDTO.builder().
                         postId(id)
                         .weight(COLLECT)
                         .userId(ThreadLocalUserIdUtil.getCurrentId())
                         .build();
                 rabbitTemplate.convertAndSend(COMMUNITY_EXCHANGE,
                         COMMUNITY_ROUTING_KEY,
                         mqMsg);
             } catch (AmqpException e) {
                 throw new RuntimeException(e);
             }
         }else {//老记录则传cancel=1为点赞,-1为取消
             communityMapper.updateCollect(id, ThreadLocalUserIdUtil.getCurrentId(), cancel);
             communityMapper.collectPost(id,cancel);
             //异步社区算法加权：按本次动作方向发权重（cancel=1 收藏 +COLLECT，-1 取消 -COLLECT）
             try {
                 RabbitMQDTO mqMsg = RabbitMQDTO.builder().
                         postId(id)
                         .weight(cancel > 0 ? COLLECT : -COLLECT)
                         .userId(ThreadLocalUserIdUtil.getCurrentId())
                         .build();
                 rabbitTemplate.convertAndSend(COMMUNITY_EXCHANGE,
                         COMMUNITY_ROUTING_KEY,
                         mqMsg);
             } catch (AmqpException e) {
                 throw new RuntimeException(e);
             }
         }
    }

    @Override
    public List<MyCollectVO> getCollect() {
        try {
            List<Integer> postIdList = communityMapper.getCollect(ThreadLocalUserIdUtil.getCurrentId());
            //空收藏时直接返回空列表，避免 IN () 语法错误
            if (postIdList == null || postIdList.isEmpty()) {
                return Collections.emptyList();
            }
            return communityMapper.getPostListByPostId(postIdList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MaybeLikeDTO> maybeLike() {
        Long userId = ThreadLocalUserIdUtil.getCurrentId();
        String userKey = CommunityConsumer.USER_RECOMMEND_PREFIX + userId;

        // 1. 取兴趣权重最高的前 3 个帖子（member 即 postId）
        java.util.Set<Object> topPosts = redisTemplate.opsForZSet().reverseRange(userKey, 0, 2);
        if (topPosts == null || topPosts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> postIds = topPosts.stream()
                .map(member -> member == null ? null : String.valueOf(member))
                .filter(Objects::nonNull)
                // RedisTemplate 用 Jackson 序列化，ZSET member 可能带 JSON 引号（"7"），统一剥离
                .map(member -> member.replace("\"", "").trim())
                .filter(member -> !member.isEmpty())
                .map(member -> {
                    try {
                        return Long.valueOf(member);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        if (postIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 用这些帖子的关键词作为查询向量，做「相似帖子」召回
        String queryKeyword = postIds.stream()
                .map(keywordExtractor::keywordOf)
                .filter(kw -> kw != null && !kw.isBlank())
                .findFirst()
                .orElse(null);

        // 3. 合并「用户已表达兴趣的帖子」与「向量召回的相似帖子」
        LinkedHashSet<Long> candidateIds = new LinkedHashSet<>(postIds);
        if (queryKeyword != null) {
            recommendVectorStore.similaritySearch(queryKeyword).stream()
                    .map(document -> parsePostId(document.getId()))
                    .filter(Objects::nonNull)
                    .limit(3)
                    .forEach(candidateIds::add);
        }

        // 4. 按 postId 精确回表（向量库里的 doc id 就是 postId）
        return communityMapper.getMaybeLikeByPostIds(new ArrayList<>(candidateIds));
    }

    private Long parsePostId(String documentId) {
        try {
            return Long.valueOf(documentId);
        } catch (Exception e) {
            return null;
        }
    }


}
