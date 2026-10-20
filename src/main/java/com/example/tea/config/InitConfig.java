package com.example.tea.config;

import com.example.tea.ai.community.CommunityKeywordExtractor;
import com.example.tea.entity.dto.Community.PostIdWithTitleDTO;
import com.example.tea.entity.vo.Goods.GoodsVO;
import com.example.tea.entity.vo.Goods.GoodsWithQuantityVO;
import com.example.tea.mapper.CommunityMapper;
import com.example.tea.mapper.GoodsMapper;
import com.example.tea.mapper.KnowledgeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 启动期数据预热。
 * <p>
 * 这里做的事情依赖外部系统（Redis Stack 向量库、大模型、MySQL），不适合放在
 * {@code @PostConstruct}：一旦外部依赖不可用，异常会冒泡到 Bean 初始化阶段，
 * 直接把整个应用启动搞挂。
 * <p>
 * 改为 {@link ApplicationRunner} 后：
 * <ul>
 *   <li>在容器刷新完成后才执行，失败只会被记录，不影响应用启动与对外服务；</li>
 *   <li>每个子任务独立 try/catch，互不牵连；</li>
 *   <li>由 {@code tea.init.enabled} 控制开关，{@code tea.init.async} 控制是否后台执行。</li>
 * </ul>
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "tea.init", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InitConfig implements ApplicationRunner {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private KnowledgeMapper knowledgeMapper;
    @Autowired
    private CommunityKeywordExtractor keywordExtractor;
    @Autowired
    @Qualifier("ragVectorStore")
    private VectorStore ragVectorStore;
    @Autowired
    @Qualifier("recommendVectorStore")
    private VectorStore recommendVectorStore;
    @Autowired
    @Qualifier("initExecutor")
    private TaskExecutor initExecutor;

    /** 是否放到后台线程执行，默认开启：embedding / 逐帖调用大模型都很慢，不应该阻塞启动。 */
    @Value("${tea.init.async:true}")
    private boolean async;

    private static final String STOCK_PREFIX = "seckill:stock:";

    @Override
    public void run(ApplicationArguments args) {
        if (async) {
            log.info("初始化任务已提交后台线程执行（tea.init.async=true），不阻塞应用启动");
            initExecutor.execute(this::initAllSafely);
        } else {
            initAllSafely();
        }
    }

    /** 串行执行各初始化任务，单个任务失败不影响其余任务，也不影响应用启动。 */
    private void initAllSafely() {
        long start = System.currentTimeMillis();
        runTask("RAG 知识库", this::initRagKnowledge);
        runTask("商品与秒杀库存缓存", this::initAllGoods);
        runTask("帖子关键词", this::initPostWord);
        runTask("帖子推荐向量", this::initAllPostRecommend);
        log.info("=== 初始化任务全部结束，耗时 {} ms ===", System.currentTimeMillis() - start);
    }

    private void runTask(String name, Runnable task) {
        try {
            log.info("=== 开始初始化：{} ===", name);
            task.run();
            log.info("=== 初始化完成：{} ===", name);
        } catch (Exception e) {
            log.error("=== 初始化失败：{}（已跳过，不影响应用启动）===", name, e);
        }
    }

    /**
     * 初始化RAG知识库（Redis 向量 + MySQL 全文 双写，切片ID用内容哈希保证幂等）。
     */
    private void initRagKnowledge() {
        Resource resource = new ClassPathResource("RAGKnowledge.txt");
        List<Document> documents = new TextReader(resource).get();

        // 文本切分
        List<Document> splitDocuments = new TokenTextSplitter().split(documents);

        List<Document> knowledgeDocuments = new ArrayList<>(splitDocuments.size());
        for (Document document : splitDocuments) {
            String text = document.getText();
            if (text == null || text.isBlank()) {
                continue;
            }
            String id = DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
            knowledgeDocuments.add(Document.builder().id(id).text(text).build());
            // MySQL 全文检索侧（BM25 召回用），同 ID 覆盖更新
            knowledgeMapper.upsert(id, text);
        }
        // Redis 向量侧，同 ID 覆盖，重启不会产生重复切片
        ragVectorStore.add(knowledgeDocuments);
        log.info("知识库写入完成，切片数={}（Redis + MySQL）", knowledgeDocuments.size());
    }

    /**
     * 初始化商品Redis缓存
     */
    private void initAllGoods() {
        List<GoodsWithQuantityVO> list = goodsMapper.getAllGoodsDetail();
        list.forEach(dto -> redisTemplate.opsForValue().set(
                CacheKeys.GOODS_DETAIL_PREFIX + dto.getGoodsId(), dto, CacheKeys.GOODS_DETAIL_TTL));
        List<GoodsVO> goodsVOS = goodsMapper.getAllSeckillGoods();
        goodsVOS.forEach(goodsVO -> {
            String stockKey = STOCK_PREFIX + goodsVO.getGoodsId();
            redisTemplate.opsForValue().set(stockKey, goodsVO.getStockNum());
        });
        log.info("商品缓存初始化完成，商品数={}，秒杀商品数={}", list.size(), goodsVOS.size());
    }

    /**
     * 初始化帖子关键词
     */
    private void initPostWord() {
        List<PostIdWithTitleDTO> postList = communityMapper.getAllPost();
        postList.forEach(dto -> {
            try {
                log.info("初始化{}号帖子", dto.getPostId());
                String keyword = keywordExtractor.extract(dto.getTitle());
                if (keyword != null) {
                    redisTemplate.opsForValue().set(
                            CommunityKeywordExtractor.POST_WORD_PREFIX + dto.getPostId(), keyword);
                }
            } catch (Exception e) {
                log.info("初始化帖子失败 postId={}，错误：{}", dto.getPostId(), e.getMessage());
            }
        });
        log.info("帖子关键词初始化完成，帖子数={}", postList.size());
    }

    /**
     * 初始化所有帖子向量
     */
    private void initAllPostRecommend() {
        List<PostIdWithTitleDTO> list = communityMapper.getAllPost();
        // 每 10 条分一批（满足大模型限制）
        final int BATCH_SIZE = 10;

        for (int i = 0; i < list.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, list.size());
            List<PostIdWithTitleDTO> batchList = list.subList(i, end);

            // 构建当前批次的文档
            List<Document> documentList = new ArrayList<>();
            for (PostIdWithTitleDTO dto : batchList) {
                Document document = Document.builder()
                        .id(dto.getPostId().toString()) // ✅ 直接用 postId 最标准
                        .text(dto.getTitle())           // ✅ 标题原文
                        .build();
                documentList.add(document);
            }

            // 分批存入 → 不报错、不超限、稳定
            recommendVectorStore.add(documentList);
        }
        log.info("✅ 推荐向量库初始化完成，总帖子数：{}", list.size());
    }
}
