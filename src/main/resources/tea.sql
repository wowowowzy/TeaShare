-- phpMyAdmin SQL Dump
-- version 5.0.4
-- https://www.phpmyadmin.net/
--
-- 主机： localhost
-- 生成日期： 2026-10-13 15:42:47
-- 服务器版本： 8.0.45
-- PHP 版本： 8.0.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `tea`
--

CREATE DATABASE IF NOT EXISTS `tea` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `tea`;

-- --------------------------------------------------------

--
-- 表的结构 `address`
--

CREATE TABLE `address` (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID，自增',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '关联用户ID，外键关联用户表(users.id)',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人姓名',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人手机号（11位）',
  `province` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区县/区',
  `detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址（街道、小区、门牌号等）',
  `postal_code` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '邮政编码（选填）',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认地址：0-非默认，1-默认',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户地址表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `cart`
--

CREATE TABLE `cart` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `goods_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `collect_record`
--

CREATE TABLE `collect_record` (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID，自增',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID，关联用户表主键',
  `post_id` bigint UNSIGNED NOT NULL COMMENT '内容ID（帖子/文章ID），关联内容表主键',
  `is_cancel` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否取消收藏：1-未取消（正常收藏），-1-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `coupon`
--

CREATE TABLE `coupon` (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联用户表）',
  `intro` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '优惠券描述（如"满100减20，全场通用"）',
  `reduce_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '满减金额（如20=减20元）',
  `min_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '使用门槛（满X元可用，无门槛则为0）',
  `start_time` datetime NOT NULL COMMENT '生效时间',
  `end_time` datetime NOT NULL COMMENT '失效时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-未使用 2-已使用 3-已过期 4-已作废',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间（状态为2时必填）',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID（使用后绑定）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满减优惠券表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `goods`
--

CREATE TABLE `goods` (
  `goods_id` bigint NOT NULL COMMENT '商品ID（主键）',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名字',
  `goods_intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '商品介绍（支持长文本描述）',
  `goods_price` decimal(10,2) NOT NULL COMMENT '商品价格（精确到分，避免浮点数误差）',
  `category_id` bigint NOT NULL COMMENT '商品分类ID（关联分类表/自定义分类编码）',
  `stock_num` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（自动填充）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动更新）',
  `goods_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品核心信息表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `goods_review`
--

CREATE TABLE `goods_review` (
  `id` bigint NOT NULL COMMENT '评价ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评价内容',
  `image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '评价图片',
  `level` tinyint NOT NULL COMMENT '星级评分（1-5星）',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品评价表';

-- --------------------------------------------------------

--
-- 表的结构 `goods_seckill`
--

CREATE TABLE `goods_seckill` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `goods_intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '商品介绍',
  `goods_price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `stock_num` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `goods_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品图片'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀商品表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `like_record`
--

CREATE TABLE `like_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `target_id` bigint NOT NULL COMMENT '被点赞对象ID',
  `target_type` tinyint NOT NULL COMMENT '1=文章，2=评论',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  `is_cancel` tinyint DEFAULT '0' COMMENT '1=有效，-1=取消'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞记录表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `order`
--

CREATE TABLE `order` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `goods_id` int NOT NULL,
  `order_id` bigint NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` int NOT NULL COMMENT '0未审批 1审批完成'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `order_detail`
--

CREATE TABLE `order_detail` (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `order_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号（外部业务主键）',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `coupon_id` bigint UNSIGNED DEFAULT NULL COMMENT '优惠券ID（可为空）',
  `total_price` decimal(10,2) NOT NULL COMMENT '订单总价（单位：元）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单详情表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `sys_history`
--

CREATE TABLE `sys_history` (
  `id` bigint NOT NULL COMMENT '序列号',
  `datetime` datetime DEFAULT NULL COMMENT '聊天时间',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '聊天内容',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'user' COMMENT '角色',
  `session_id` bigint DEFAULT NULL COMMENT '会话Id',
  `user_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记录' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `sys_oper_log`
--

CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL COMMENT '日志ID',
  `oper_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作者ID（管理员ID）',
  `oper_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作类型(新增/修改/删除)',
  `oper_module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作模块(用户/角色/订单)',
  `request_method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请求方法全路径',
  `request_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '请求参数',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `status` int DEFAULT '1' COMMENT '执行状态(1成功 0失败)',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '错误信息'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `sys_user`
--

CREATE TABLE `sys_user` (
  `id` bigint UNSIGNED NOT NULL COMMENT '用户ID（主键）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名（登录账户）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密后存储，不存明文）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号（唯一，用于登录/验证码）',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '用户状态：1-正常，0-禁用（防止恶意账号）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（注册时间）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注（如管理员备注）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城用户表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `tea_choice_option`
--

CREATE TABLE `tea_choice_option` (
  `option_id` int NOT NULL COMMENT '选项ID（主键）',
  `question_id` int NOT NULL COMMENT '关联的问题ID',
  `option_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项内容（如：A、95℃）',
  `option_label` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项标签（如：A/B/C/D）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='茶叶选择题选项表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `tea_choice_question`
--

CREATE TABLE `tea_choice_question` (
  `question_id` int NOT NULL COMMENT '问题ID（主键）',
  `question_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选择题问题内容',
  `correct_option_id` int DEFAULT NULL COMMENT '正确选项ID（关联选项表）',
  `tea_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '茶叶品类（如：铁观音、普洱）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='茶叶选择题问题表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `t_comment`
--

CREATE TABLE `t_comment` (
  `id` bigint NOT NULL COMMENT '评论ID（主键）',
  `post_id` bigint NOT NULL COMMENT '关联帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父评论ID：0-直接评论帖子，>0-回复某条评论',
  `to_user_id` bigint NOT NULL DEFAULT '0' COMMENT '回复的目标用户ID（仅回复时非0）',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '评论点赞数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-正常，0-删除，2-封禁',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区评论/回复表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `t_post`
--

CREATE TABLE `t_post` (
  `id` bigint NOT NULL COMMENT '帖子ID（主键）',
  `user_id` bigint NOT NULL COMMENT '发布用户ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子内容',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '帖子图片URL',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `collect_count` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-正常，0-删除，2-封禁',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区帖子表' ROW_FORMAT=DYNAMIC;

--
-- 转储表的索引
--

--
-- 表的索引 `address`
--
ALTER TABLE `address`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引，快速查询用户所有地址',
  ADD KEY `idx_user_default` (`user_id`,`is_default`) USING BTREE COMMENT '用户+默认地址索引，快速获取用户默认地址';

--
-- 表的索引 `cart`
--
ALTER TABLE `cart`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_user_goods` (`user_id`,`goods_id`) USING BTREE;

--
-- 表的索引 `collect_record`
--
ALTER TABLE `collect_record`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_user_post` (`user_id`,`post_id`) USING BTREE COMMENT '唯一索引：一个用户只能收藏一个内容一次',
  ADD KEY `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引，用于按时间查询收藏记录',
  ADD KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引，用于查询用户的所有收藏';

--
-- 表的索引 `coupon`
--
ALTER TABLE `coupon`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引（查询用户优惠券）',
  ADD KEY `idx_status_time` (`status`,`end_time`) USING BTREE COMMENT '状态+有效期索引（筛选可用优惠券）',
  ADD KEY `idx_order_id` (`order_id`) USING BTREE COMMENT '订单ID索引（追溯订单使用的优惠券）';

--
-- 表的索引 `goods`
--
ALTER TABLE `goods`
  ADD PRIMARY KEY (`goods_id`) USING BTREE,
  ADD KEY `idx_category_id` (`category_id`) USING BTREE,
  ADD KEY `idx_goods_name` (`goods_name`) USING BTREE;

--
-- 表的索引 `goods_review`
--
ALTER TABLE `goods_review`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `goods_seckill`
--
ALTER TABLE `goods_seckill`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_goods_id` (`goods_id`) USING BTREE;

--
-- 表的索引 `like_record`
--
ALTER TABLE `like_record`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_user_target` (`user_id`,`target_id`,`target_type`) USING BTREE,
  ADD KEY `idx_target` (`target_id`,`target_type`) USING BTREE;

--
-- 表的索引 `order`
--
ALTER TABLE `order`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- 表的索引 `order_detail`
--
ALTER TABLE `order_detail`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_order_id` (`order_id`) USING BTREE,
  ADD KEY `idx_user_id` (`user_id`) USING BTREE,
  ADD KEY `idx_create_time` (`create_time`) USING BTREE;

--
-- 表的索引 `sys_history`
--
ALTER TABLE `sys_history`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- 表的索引 `sys_oper_log`
--
ALTER TABLE `sys_oper_log`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- 表的索引 `sys_user`
--
ALTER TABLE `sys_user`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `idx_username` (`username`) USING BTREE,
  ADD UNIQUE KEY `idx_phone` (`phone`) USING BTREE,
  ADD KEY `idx_status` (`status`) USING BTREE;

--
-- 表的索引 `tea_choice_option`
--
ALTER TABLE `tea_choice_option`
  ADD PRIMARY KEY (`option_id`) USING BTREE,
  ADD KEY `idx_question_id` (`question_id`) USING BTREE;

--
-- 表的索引 `tea_choice_question`
--
ALTER TABLE `tea_choice_question`
  ADD PRIMARY KEY (`question_id`) USING BTREE;

--
-- 表的索引 `t_comment`
--
ALTER TABLE `t_comment`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_post_id` (`post_id`) USING BTREE,
  ADD KEY `idx_parent_id` (`parent_id`) USING BTREE,
  ADD KEY `idx_user_id` (`user_id`) USING BTREE;

--
-- 表的索引 `t_post`
--
ALTER TABLE `t_post`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_user_id` (`user_id`) USING BTREE,
  ADD KEY `idx_create_time` (`create_time`) USING BTREE,
  ADD KEY `idx_status_create_time` (`status`,`create_time`) USING BTREE;

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `address`
--
ALTER TABLE `address`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增';

--
-- 使用表AUTO_INCREMENT `cart`
--
ALTER TABLE `cart`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `collect_record`
--
ALTER TABLE `collect_record`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增';

--
-- 使用表AUTO_INCREMENT `coupon`
--
ALTER TABLE `coupon`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID';

--
-- 使用表AUTO_INCREMENT `goods`
--
ALTER TABLE `goods`
  MODIFY `goods_id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID（主键）';

--
-- 使用表AUTO_INCREMENT `goods_review`
--
ALTER TABLE `goods_review`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID';

--
-- 使用表AUTO_INCREMENT `goods_seckill`
--
ALTER TABLE `goods_seckill`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';

--
-- 使用表AUTO_INCREMENT `like_record`
--
ALTER TABLE `like_record`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';

--
-- 使用表AUTO_INCREMENT `order`
--
ALTER TABLE `order`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `order_detail`
--
ALTER TABLE `order_detail`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID';

--
-- 使用表AUTO_INCREMENT `sys_history`
--
ALTER TABLE `sys_history`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '序列号';

--
-- 使用表AUTO_INCREMENT `sys_oper_log`
--
ALTER TABLE `sys_oper_log`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID';

--
-- 使用表AUTO_INCREMENT `sys_user`
--
ALTER TABLE `sys_user`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）';

--
-- 使用表AUTO_INCREMENT `tea_choice_option`
--
ALTER TABLE `tea_choice_option`
  MODIFY `option_id` int NOT NULL AUTO_INCREMENT COMMENT '选项ID（主键）';

--
-- 使用表AUTO_INCREMENT `tea_choice_question`
--
ALTER TABLE `tea_choice_question`
  MODIFY `question_id` int NOT NULL AUTO_INCREMENT COMMENT '问题ID（主键）';

--
-- 使用表AUTO_INCREMENT `t_comment`
--
ALTER TABLE `t_comment`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID（主键）';

--
-- 使用表AUTO_INCREMENT `t_post`
--
ALTER TABLE `t_post`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID（主键）';

--
-- 限制导出的表
--

--
-- 限制表 `address`
--
ALTER TABLE `address`
  ADD CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- 限制表 `tea_choice_option`
--
ALTER TABLE `tea_choice_option`
  ADD CONSTRAINT `tea_choice_option_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `tea_choice_question` (`question_id`) ON DELETE CASCADE ON UPDATE RESTRICT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

-- --------------------------------------------------------

--
-- RAG 知识库切片（BM25/关键词召回的 MySQL 全文侧，ngram 支持中文分词）
--

CREATE TABLE `rag_knowledge` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '切片ID（内容哈希）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识切片内容',
  `create_time` datetime DEFAULT NULL COMMENT '创建/更新时间',
  PRIMARY KEY (`id`),
  FULLTEXT KEY `ft_rag_knowledge_content` (`content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG知识库切片(全文检索)';
