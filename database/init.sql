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
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
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
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待付款 1-已付款 2-已发货 3-已完成 4-已取消',
    `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
    `receiver_address` VARCHAR(500) DEFAULT NULL COMMENT '收货地址',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
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
-- 初始数据：管理员账号
-- 密码为 admin123 的BCrypt加密值
-- ========================================
INSERT INTO `users` (`username`, `password`, `nickname`, `role`, `email`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ADMIN', 'admin@shopping.com');

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
('iPhone 15 Pro Max 256GB', '苹果最新旗舰手机，A17 Pro芯片，钛金属设计，4800万像素主摄', 9999.00, 10999.00, 500, 1234, 'https://picsum.photos/seed/iphone15/400/400', 1, 1),
('MacBook Pro 14英寸 M3 Pro', 'Apple M3 Pro芯片，18GB统一内存，512GB固态硬盘，Liquid Retina XDR显示屏', 14999.00, 16499.00, 200, 856, 'https://picsum.photos/seed/macbook/400/400', 2, 1),
('华为 Mate 60 Pro', '麒麟9000S芯片，超可靠玄武架构，卫星通话，XMAGE影像', 6999.00, 7999.00, 300, 2341, 'https://picsum.photos/seed/huawei/400/400', 1, 1),
('Sony WH-1000XM5 头戴式耳机', '行业领先降噪，30小时续航，多点连接，高解析度音频', 2499.00, 2999.00, 150, 678, 'https://picsum.photos/seed/sony/400/400', 1, 1),
('戴尔 U2723QE 4K显示器', '27英寸IPS Black技术，USB-C 90W供电，HDR400，99% sRGB', 3999.00, 4599.00, 100, 432, 'https://picsum.photos/seed/dell/400/400', 2, 1),
('海尔冰箱 BCD-510WDPF', '510升对开门，风冷无霜，变频压缩机，智能控温', 3299.00, 3999.00, 80, 567, 'https://picsum.photos/seed/haier/400/400', 3, 1),
('Nike Air Jordan 1 Retro High', '经典复刻篮球鞋，优质皮革鞋面，Air-Sole缓震', 1299.00, 1499.00, 200, 890, 'https://picsum.photos/seed/nike/400/400', 4, 1),
('兰蔻小黑瓶精华液 50ml', '第二代小黑瓶，微生态科技，修护肌肤屏障', 799.00, 980.00, 300, 1567, 'https://picsum.photos/seed/lancome/400/400', 5, 1),
('三只松鼠坚果大礼包', '每日坚果混合装，14袋/盒，健康零食礼盒', 89.90, 129.90, 1000, 5678, 'https://picsum.photos/seed/nuts/400/400', 6, 1),
('小米14 Ultra', '骁龙8 Gen3，徕卡光学Summilux镜头，5000mAh电池', 5999.00, 6499.00, 250, 1890, 'https://picsum.photos/seed/xiaomi/400/400', 1, 1),
('联想 ThinkPad X1 Carbon', '14英寸2.8K OLED屏，酷睿Ultra处理器，1.08kg轻薄商务本', 11999.00, 13999.00, 120, 345, 'https://picsum.photos/seed/thinkpad/400/400', 2, 1),
('格力空调 云佳 1.5匹', '新一级能效，变频冷暖，WiFi智控，自清洁', 2899.00, 3499.00, 150, 789, 'https://picsum.photos/seed/gree/400/400', 3, 1);
