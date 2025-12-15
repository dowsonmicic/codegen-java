/*
 Navicat Premium Data Transfer

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80038 (8.0.38)
 Source Host           : localhost:3306
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 80038 (8.0.38)
 File Encoding         : 65001

 Date: 15/12/2025 22:47:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for author
-- ----------------------------
DROP TABLE IF EXISTS `author`;
CREATE TABLE `author`  (
  `author_id` bigint NOT NULL AUTO_INCREMENT COMMENT '作者ID',
  `author_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作者姓名',
  `gender` tinyint(1) NULL DEFAULT NULL COMMENT '性别（1=男，2=女，0=未知）',
  `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '作者简介',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者头像URL',
  PRIMARY KEY (`author_id`) USING BTREE,
  INDEX `idx_author_name`(`author_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for book
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book`  (
  `book_id` bigint NOT NULL AUTO_INCREMENT COMMENT '书籍ID',
  `book_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '书籍名称',
  `category_id` int NOT NULL COMMENT '分类ID',
  `author_id` bigint NOT NULL COMMENT '作者ID',
  `isbn` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ISBN编号（唯一）',
  `price` decimal(10, 2) NOT NULL COMMENT '售价（元）',
  `stock` int NULL DEFAULT 0 COMMENT '库存数量',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '书籍封面URL',
  `publish_date` date NULL DEFAULT NULL COMMENT '出版日期',
  `sales` int NULL DEFAULT 0 COMMENT '销量',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '书籍简介',
  PRIMARY KEY (`book_id`) USING BTREE,
  UNIQUE INDEX `isbn`(`isbn` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_book_name`(`book_name` ASC) USING BTREE,
  CONSTRAINT `fk_book_author` FOREIGN KEY (`author_id`) REFERENCES `author` (`author_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_book_category` FOREIGN KEY (`category_id`) REFERENCES `book_category` (`category_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '书籍表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for book_category
-- ----------------------------
DROP TABLE IF EXISTS `book_category`;
CREATE TABLE `book_category`  (
  `category_id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称（如小说、科技、教辅）',
  `parent_id` int NULL DEFAULT 0 COMMENT '父分类ID（0=一级分类）',
  `sort` int NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类描述',
  PRIMARY KEY (`category_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '书籍分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bookshop_user
-- ----------------------------
DROP TABLE IF EXISTS `bookshop_user`;
CREATE TABLE `bookshop_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号（唯一）',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（建议加密存储，示例为MD5加密）',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货地址',
  `register_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_info
-- ----------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info`  (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `order_status` tinyint(1) NULL DEFAULT 0 COMMENT '订单状态（0=待支付，1=已支付，2=已发货，3=已完成，4=已取消）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_status`(`order_status` ASC) USING BTREE,
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `bookshop_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `book_id` bigint NOT NULL COMMENT '书籍ID',
  `buy_num` int NOT NULL COMMENT '购买数量',
  `price` decimal(10, 2) NOT NULL COMMENT '购买单价（下单时的价格）',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  CONSTRAINT `fk_item_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `order_info` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单项表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
