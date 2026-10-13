package com.example.tea.config;

import com.example.tea.ai.community.CommunityKeywordExtractor;
import com.example.tea.entity.dto.Community.PostIdWithTitleDTO;
import com.example.tea.entity.vo.Goods.GoodsVO;
import com.example.tea.entity.vo.Goods.GoodsWithQuantityVO;
import com.example.tea.mapper.CommunityMapper;
import com.example.tea.mapper.GoodsMapper;
import com.example.tea.mapper.KnowledgeMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
public class InitConfig {

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
    @org.springframework.beans.factory.annotation.Qualifier("ragVectorStore")
    private VectorStore ragVectorStore;
    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("recommendVectorStore")
    private VectorStore recommendVectorStore;
    private final String GOODSID = "goodsId:";

    private static final String STOCK_PREFIX = "seckill:stock:";

    /**
     * 初始化RAG知识库（Redis 向量 + MySQL 全文 双写，切片ID用内容哈希保证幂等）。
     */
    @PostConstruct
    public void initRagKnowledge() {
        log.info("=== 开始初始化知识库 ===");
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
        log.info("=== 知识库初始化完成，切片数={}（Redis + MySQL 均已写入）===", knowledgeDocuments.size());
    }

    /**
     * 初始化商品Redis缓存
     */
    @PostConstruct
    public void initAllGoods() {
        log.info("开始初始化所有商品和热点商品");
        List<GoodsWithQuantityVO> list = goodsMapper.getAllGoodsDetail();
        list.forEach(dto -> {
            redisTemplate.opsForValue().set(GOODSID + dto.getGoodsId(), dto);
        });
        List<GoodsVO> goodsVOS = goodsMapper.getAllSeckillGoods();
        goodsVOS.forEach(goodsVO -> {
            String stockKey = STOCK_PREFIX + goodsVO.getGoodsId();
            redisTemplate.opsForValue().set(stockKey, goodsVO.getStockNum());
        });
    }

    /**
     * 初始化帖子关键词
     */
    @PostConstruct
    public void initPostWord() {
        List<PostIdWithTitleDTO> postList = communityMapper.getAllPost();
        postList.forEach(dto -> {
            try {
                log.info("初始化" + dto.getPostId() + "号帖子");
                String keyword = keywordExtractor.extract(dto.getTitle());
                if (keyword != null) {
                    redisTemplate.opsForValue().set(
                            CommunityKeywordExtractor.POST_WORD_PREFIX + dto.getPostId(), keyword);
                }
            } catch (Exception e) {
                log.info("初始化帖子失败 postId=" + dto.getPostId() + "，错误：" + e.getMessage());
            }
        });
    }

    /**
     * 初始化所有帖子向量
     */
    @PostConstruct
    public void initAllPostRecommend() {
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
        log.info("✅ 推荐向量库初始化完成，总帖子数：" + list.size());
    }
}