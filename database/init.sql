-- ========================================
-- 电商购物平台数据库初始化脚本
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS shopping_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shopping_db;

-- ========================================
-- 1. 用户表
-- ========================================
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/MERCHANT/ADMIN',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 2. 商品分类表
-- ========================================
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父分类ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ========================================
-- 3. 商品表
-- ========================================
CREATE TABLE IF NOT EXISTS `products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `description` TEXT COMMENT '商品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `sales` INT NOT NULL DEFAULT 0 COMMENT '销量',
    `main_image` VARCHAR(500) DEFAULT NULL COMMENT '主图URL',
    `images` TEXT COMMENT '商品图片列表（JSON数组）',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ========================================
-- 4. 购物车表
-- ========================================
CREATE TABLE IF NOT EXISTS `cart_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `selected` TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中：0-未选中 1-选中',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ========================================
-- 5. 订单表
-- ========================================
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待付款 1-待发货 2-待收货 3-待评价 4-已完成 5-已取消 6-已退款 7-已过期',
    `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
    `receiver_address` VARCHAR(500) DEFAULT NULL COMMENT '收货地址',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
    `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式：ALIPAY/WECHAT/BANK_CARD/COD',
    `payment_no` VARCHAR(50) DEFAULT NULL COMMENT '支付单号',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ========================================
-- 6. 订单明细表
-- ========================================
CREATE TABLE IF NOT EXISTS `order_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称（快照）',
    `product_image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片（快照）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品单价（快照）',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ========================================
-- 7. 支付记录表
-- ========================================
CREATE TABLE IF NOT EXISTS `payments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `payment_no` VARCHAR(50) NOT NULL COMMENT '支付单号',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式：ALIPAY/WECHAT/BANK_CARD/COD',
    `payment_amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `payment_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '支付状态：PENDING/SUCCESS/FAILED/EXPIRED',
    `idempotency_key` VARCHAR(64) NOT NULL COMMENT '幂等键',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status_expire` (`payment_status`, `expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ========================================
-- 初始数据：管理员账号
-- 密码为 admin123 的BCrypt加密值
-- ========================================
INSERT INTO `users` (`username`, `password`, `nickname`, `role`, `email`) VALUES
('admin', '$2a$10$tK7HmQv7cL6fVg8s9eR0TuOaPbCdEfGhIjKlMnOpQrStUvWxYzAbC', '系统管理员', 'ADMIN', 'admin@shopping.com');

-- ========================================
-- 初始数据：商品分类
-- ========================================
INSERT INTO `categories` (`name`, `icon`, `sort_order`) VALUES
('手机数码', 'Smartphone', 1),
('电脑办公', 'Computer', 2),
('家用电器', 'HomeAppliance', 3),
('服饰鞋包', 'Clothing', 4),
('美妆护肤', 'Beauty', 5),
('食品生鲜', 'Food', 6),
('图书音像', 'Book', 7),
('运动户外', 'Sports', 8);

-- ========================================
-- 初始数据：示例商品
-- ========================================
INSERT INTO `products` (`name`, `description`, `price`, `original_price`, `stock`, `sales`, `main_image`, `category_id`, `status`) VALUES
('iPhone 15 Pro Max 256GB', '苹果最新旗舰手机，A17 Pro芯片，钛金属设计，4800万像素主摄，超视网膜XDR显示屏，USB-C接口', 9999.00, 10999.00, 500, 1234, 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=400&h=400&fit=crop', 1, 1),
('MacBook Pro 14英寸 M3 Pro', 'Apple M3 Pro芯片，18GB统一内存，512GB固态硬盘，Liquid Retina XDR显示屏，长达17小时续航', 14999.00, 16499.00, 200, 856, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=400&fit=crop', 2, 1),
('华为 Mate 60 Pro', '麒麟9000S芯片，超可靠玄武架构，天通卫星通话，XMAGE影像系统，双向北斗卫星消息', 6999.00, 7999.00, 300, 2341, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop', 1, 1),
('Sony WH-1000XM5 头戴式耳机', '行业领先降噪技术，30小时超长续航，多点蓝牙连接，高解析度音频，可折叠设计', 2499.00, 2999.00, 150, 678, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop', 1, 1),
('戴尔 U2723QE 4K显示器', '27英寸IPS Black技术面板，USB-C 90W供电，HDR400认证，99% sRGB广色域', 3999.00, 4599.00, 100, 432, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400&h=400&fit=crop', 2, 1),
('海尔冰箱 BCD-510WDPF', '510升大容量对开门，风冷无霜，一级能效变频压缩机，智能控温，干湿分储', 3299.00, 3999.00, 80, 567, 'https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5?w=400&h=400&fit=crop', 3, 1),
('Nike Air Jordan 1 Retro High', '经典复刻篮球鞋，优质全粒面皮革鞋面，Air-Sole气垫缓震，橡胶外底抓地力强', 1299.00, 1499.00, 200, 890, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop', 4, 1),
('兰蔻小黑瓶精华液 50ml', '第二代小黑瓶肌底精华，微生态科技，修护肌肤屏障，改善肤质，提升光泽', 799.00, 980.00, 300, 1567, 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=400&h=400&fit=crop', 5, 1),
('三只松鼠坚果大礼包 1458g', '每日坚果混合装14袋/盒，含核桃仁、腰果、夏威夷果，健康零食年货礼盒', 89.90, 129.90, 1000, 5678, 'https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=400&h=400&fit=crop', 6, 1),
('小米14 Ultra 512GB', '骁龙8 Gen3旗舰处理器，徕卡Summilux光学镜头，5000mAh电池，120W快充', 5999.00, 6499.00, 250, 1890, 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=400&h=400&fit=crop', 1, 1),
('联想 ThinkPad X1 Carbon', '14英寸2.8K OLED屏，酷睿Ultra处理器，1.08kg极致轻薄商务本，军标品质', 11999.00, 13999.00, 120, 345, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop', 2, 1),
('格力空调 云佳 1.5匹', '新一级能效，变频冷暖，56℃净菌自清洁，WiFi智能控制，七档风速', 2899.00, 3499.00, 150, 789, 'https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=400&h=400&fit=crop', 3, 1);

-- ========================================
-- 8. 收货地址表
-- ========================================
CREATE TABLE IF NOT EXISTS `addresses` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
    `district` VARCHAR(50) DEFAULT NULL COMMENT '区/县',
    `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认地址',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- ========================================
-- 9. 商品评价表
-- ========================================
CREATE TABLE IF NOT EXISTS `reviews` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `order_item_id` BIGINT NOT NULL COMMENT '订单项ID',
    `rating` TINYINT NOT NULL COMMENT '评分1-5',
    `content` TEXT COMMENT '评价内容',
    `images` TEXT COMMENT '评价图片(JSON数组)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态:PENDING/APPROVED/REJECTED',
    `admin_reply` TEXT COMMENT '管理员回复',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_item_id` (`order_item_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- ========================================
-- 10. 商品收藏表
-- ========================================
CREATE TABLE IF NOT EXISTS `favorites` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_created` (`user_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';

-- ========================================
-- 11. 浏览历史表
-- ========================================
CREATE TABLE IF NOT EXISTS `browsing_histories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `browsed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_browsed` (`user_id`, `browsed_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览历史表';

-- ========================================
-- 12. 商品对比表
-- ========================================
CREATE TABLE IF NOT EXISTS `compare_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品对比表';

-- ========================================
-- 13. 促销活动表
-- ========================================
CREATE TABLE IF NOT EXISTS `promotions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
    `type` VARCHAR(20) NOT NULL COMMENT '活动类型：FLASH_SALE/FULL_REDUCTION',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '活动描述',
    `discount_value` DECIMAL(10,2) NOT NULL COMMENT '优惠值（折扣率或减免金额）',
    `min_order_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最低订单金额',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    `product_ids` TEXT COMMENT '适用商品ID（逗号分隔，NULL表示全部）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促销活动表';

-- ========================================
-- 商家入驻申请表
-- ========================================
CREATE TABLE IF NOT EXISTS `merchant_applications` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `business_license` VARCHAR(255) DEFAULT NULL COMMENT '营业执照编号',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `description` TEXT COMMENT '店铺描述',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    `reviewed_by` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家入驻申请表';

-- 商品表新增商家ID字段
ALTER TABLE `products` ADD COLUMN `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID' AFTER `category_id`;
