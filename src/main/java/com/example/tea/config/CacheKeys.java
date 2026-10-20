package com.example.tea.config;

import java.time.Duration;

/**
 * 缓存 key 与过期时间的统一定义。
 * <p>
 * 初始化预热、读缓存、失效三处必须使用同一份定义，否则很容易出现
 * 「写进去的 key 和删掉的 key 不是同一个」这类边界不一致问题。
 */
public final class CacheKeys {

    /**
     * 商品详情二级缓存（Redis）key 前缀，最终 key 形如 {@code hot_data:goodsId:1001}。
     * <p>
     * 带 {@code hot_data:} 项目前缀，避免与同库其他业务 key 冲突；
     * 旧版本遗留的无 TTL 的 {@code goodsId:*} key 不再读取，可在上线后手工清理。
     */
    public static final String GOODS_DETAIL_PREFIX = "hot_data:goodsId:";

    /** 商品详情二级缓存（Redis）过期时间，限制脏数据最长存活时间。 */
    public static final Duration GOODS_DETAIL_TTL = Duration.ofMinutes(30);

    /** 注销 token 黑名单 key 前缀，最终 key 为 {@code logout:token:{token 的 MD5}}。 */
    public static final String LOGOUT_TOKEN_PREFIX = "logout:token:";

    private CacheKeys() {
    }
}
