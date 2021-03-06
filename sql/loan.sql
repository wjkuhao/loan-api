

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for report_order_loan
-- ----------------------------
DROP TABLE IF EXISTS `report_order_loan`;
CREATE TABLE `report_order_loan`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `day_key` date NULL DEFAULT NULL COMMENT '放款日期',
  `merchant` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户别名',
  `arrive_cnt` int(11) NULL DEFAULT NULL COMMENT '放款笔数',
  `arrive_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '放款金额',
  `first_cnt` int(11) NULL DEFAULT NULL COMMENT '首借人数',
  `first_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '首借金额',
  `second_cnt` int(11) NULL DEFAULT NULL COMMENT '次新人数',
  `second_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '次新金额',
  `old_cnt` int(11) NULL DEFAULT NULL COMMENT '续借人数',
  `old_amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '续借金额',
  `total_fee` decimal(20, 2) NULL DEFAULT NULL COMMENT '综合费用',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_day_merchant`(`day_key`, `merchant`) USING BTREE,
  INDEX `idx_merchant`(`merchant`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '放款统计报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for report_order_repay
-- ----------------------------
DROP TABLE IF EXISTS `report_order_repay`;
CREATE TABLE `report_order_repay`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `day_key` date NULL DEFAULT NULL COMMENT '应还日期',
  `merchant` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户别名',
  `should_repay_cnt` int(11) NULL DEFAULT 0 COMMENT '应还笔数',
  `early_repay_cnt` int(11) NULL DEFAULT 0 COMMENT '提前还笔数',
  `normal_repay_cnt` int(11) NULL DEFAULT 0 COMMENT '正常还笔数',
  `overdue_repay_cnt` int(11) NULL DEFAULT 0 COMMENT '逾期还笔数',
  `overdue_cnt` int(11) NULL DEFAULT 0 COMMENT '逾期中笔数',
  `bad_cnt` int(11) NULL DEFAULT 0 COMMENT '坏账笔数',
  `overdue1_repay_cnt` int(11) NULL DEFAULT 0 COMMENT '当天逾期还笔数',
  `overdue3_repay_cnt` int(11) NULL DEFAULT 0 COMMENT '3天内逾期还笔数',
  `overdue7_repay_cnt` int(11) NULL DEFAULT 0 COMMENT '7天内逾期还笔数',
  `overdue15_repay_cnt` int(11) NULL DEFAULT 0 COMMENT '15天内逾期还笔数',
  `repay_amount` decimal(20, 2) NULL DEFAULT 0.00 COMMENT '应还还款金额，不含利息',
  `real_repay_amount` decimal(20, 2) NULL DEFAULT 0.00 COMMENT '实际还款金额',
  `pay_amount` decimal(20, 2) NULL DEFAULT 0.00 COMMENT '实际放款金额，成本',
  `overdue_fee` decimal(20, 2) NULL DEFAULT 0.00 COMMENT '逾期费',
  `reduce_money` decimal(20, 2) NULL DEFAULT 0.00 COMMENT '减免金额',
  `overdue_repay_amount` decimal(20, 2) NULL DEFAULT 0.00 COMMENT '逾期待还款金额',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_day_merchant`(`day_key`, `merchant`) USING BTREE,
  INDEX `idx_merchant`(`merchant`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '还款统计报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for report_partner_effect
-- ----------------------------
DROP TABLE IF EXISTS `report_partner_effect`;
CREATE TABLE `report_partner_effect` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `day_key` date DEFAULT NULL COMMENT '注册日期',
  `merchant` varchar(50) DEFAULT NULL COMMENT '商户',
  `user_origin` varchar(16) DEFAULT NULL COMMENT '注册渠道，来源',
  `reg_cnt` int(11) DEFAULT NULL COMMENT '注册人数',
  `login_cnt` int(11) DEFAULT NULL COMMENT '注册的登录数量',
  `real_name_cnt` int(11) DEFAULT NULL COMMENT '实名人数',
  `submit_order_cnt` int(11) DEFAULT NULL COMMENT '提单人数',
  `first_submit_cnt` int(11) DEFAULT NULL COMMENT '首借人数',
  `first_submit_amount` decimal(20,2) DEFAULT NULL COMMENT '首借金额',
  `personal_info_certi_cnt` int(11) DEFAULT NULL COMMENT '个人信息认证数',
  `yys_cnt` int(11) DEFAULT NULL COMMENT '运营商认证数',
  `bank_cnt` int(11) DEFAULT NULL COMMENT '银行卡绑定数',
  `order_cnt` int(11) DEFAULT NULL COMMENT '申请订单数',
  `pass_risk_cnt` int(11) DEFAULT NULL COMMENT '风控通过数',
  `loan_success_cnt` int(11) DEFAULT NULL COMMENT '下款数',
  `real_name_certi_rate` varchar(11) DEFAULT NULL COMMENT '实名认证率',
  `personal_info_certi_rate` varchar(11) DEFAULT NULL COMMENT '个人信息认证率',
  `yys_certi_rate` varchar(11) DEFAULT NULL COMMENT '运营商认证率',
  `bank_bound_rate` varchar(11) DEFAULT NULL COMMENT '银行卡绑定率',
  `reg_apply_trans_rate` varchar(11) DEFAULT NULL COMMENT '申请转化率',
  `loan_rate` varchar(11) DEFAULT NULL COMMENT '下款率',
  `audit_pass_rate` varchar(11) DEFAULT NULL COMMENT '审核通过率',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_day_merchant_origin` (`day_key`,`merchant`,`user_origin`) USING BTREE,
  KEY `idx_merchant` (`merchant`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='渠道统计报表';



-- ----------------------------
-- Table structure for report_register_order
-- ----------------------------
DROP TABLE IF EXISTS `report_register_order`;
CREATE TABLE `report_register_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `day_key` date NULL DEFAULT NULL,
  `merchant` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `register_cnt` int(11) NULL DEFAULT NULL,
  `realname_cnt` int(11) NULL DEFAULT NULL,
  `zfb_cnt` int(11) NULL DEFAULT NULL,
  `mobile_cnt` int(11) NULL DEFAULT NULL,
  `order_cnt` int(11) NULL DEFAULT NULL,
  `first_cnt` int(11) NULL DEFAULT NULL,
  `second_cnt` int(11) NULL DEFAULT NULL,
  `old_cnt` int(11) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_day_merchant`(`day_key`, `merchant`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for sms_config
-- ----------------------------
DROP TABLE IF EXISTS `sms_config`;
CREATE TABLE `sms_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sign_key` varchar(128) DEFAULT NULL COMMENT '签名',
  `sign_id` varchar(128) DEFAULT NULL COMMENT '标记名id',
  `sign_name` varchar(128) DEFAULT NULL COMMENT '标记名',
  `access_key` varchar(1000) DEFAULT NULL COMMENT 'access_key',
  `secret` varchar(1000) DEFAULT NULL COMMENT 'secret',
  `template_key` varchar(128) DEFAULT NULL COMMENT '1001-注册,1002-忘记密码,2001-打款,2002-当天提醒,2003-提前提醒',
  `template_id` varchar(128) DEFAULT NULL COMMENT '第三方模板key',
  `message_platform` varchar(128) DEFAULT NULL COMMENT '短信平台',
  `template_content` varchar(1000) DEFAULT NULL COMMENT '短信模板信息',
  `sms_notice` varchar(128) DEFAULT NULL COMMENT '提醒信息',
  `create_time` datetime DEFAULT NULL COMMENT '插入时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for tb_app_config
-- ----------------------------
DROP TABLE IF EXISTS `tb_app_config`;
CREATE TABLE `tb_app_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `merchant` varchar(50) DEFAULT NULL COMMENT '商户名称',
  `channel_id` bigint(20) DEFAULT NULL COMMENT '渠道号',
  `h5_url` varchar(255) DEFAULT NULL COMMENT 'h5地址',
  `api_url` varchar(255) DEFAULT NULL COMMENT 'api地址',
  `youdun_callback_url` varchar(255) DEFAULT NULL COMMENT '有盾回调地址',
  `youdun_key` varchar(255) DEFAULT NULL COMMENT '有盾key',
  `tongdun_url` varchar(255) DEFAULT NULL COMMENT '同盾调转地址',
  `operators_url` varchar(255) DEFAULT NULL COMMENT '运营商跳转链接',
  `taobao_url` varchar(255) DEFAULT NULL COMMENT '淘宝的跳转链接',
  `service_url` varchar(255) DEFAULT NULL COMMENT '在线客服链接',
  `callback` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL COMMENT '插入时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_merchant` (`merchant`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;


-- ----------------------------
-- Table structure for tb_app_article
-- ----------------------------
DROP TABLE IF EXISTS `tb_app_article`;
CREATE TABLE `tb_app_article`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `article_title` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文章标题',
  `article_content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '内容',
  `article_tag` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标签',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '自定义文章' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_app_banner
-- ----------------------------
DROP TABLE IF EXISTS `tb_app_banner`;
CREATE TABLE `tb_app_banner`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `banner_imgurl` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片地址',
  `banner_url` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '连接地址',
  `banner_idx` int(4) NULL DEFAULT 0 COMMENT '排序，从大到小排列',
  `banner_remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `banner_status` tinyint(4) NULL DEFAULT 0 COMMENT '状态,0-停用，1-定时，2-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '定时开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '定时结束时间',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_app_entry
-- ----------------------------
DROP TABLE IF EXISTS `tb_app_entry`;
CREATE TABLE `tb_app_entry`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `entry_imgurl` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片url',
  `entry_url` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '点击url',
  `entry_idx` int(4) NULL DEFAULT 0 COMMENT '排序，从大到小排列',
  `entry_remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `entry_status` tinyint(4) NULL DEFAULT 0 COMMENT '状态,0-停用，1-定时，2-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '定时开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '定时结束时间',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '首页快捷入口' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_app_feedback
-- ----------------------------
DROP TABLE IF EXISTS `tb_app_feedback`;
CREATE TABLE `tb_app_feedback`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uid` bigint(20) NULL DEFAULT NULL,
  `question_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `question_desc` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `question_img` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '链接之间“|”隔开',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `merchant` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `uid`(`uid`) USING BTREE,
  INDEX `idx_merchant_create_time`(`merchant`, `create_time`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '意见反馈' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_app_home
-- ----------------------------
DROP TABLE IF EXISTS `tb_app_home`;
CREATE TABLE `tb_app_home`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `imgurl` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片url',
  `url` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '点击url',
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态,0-停用，1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '首页图片弹窗' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_app_notice
-- ----------------------------
DROP TABLE IF EXISTS `tb_app_notice`;
CREATE TABLE `tb_app_notice`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `notice_title` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标题',
  `notice_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '链接地址',
  `notice_idx` int(4) NULL DEFAULT 0 COMMENT '排序，从大到小排列',
  `notice_status` tinyint(4) NULL DEFAULT 0 COMMENT '状态,0-停用，1-定时，2-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '定时开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '定时结束时间',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户',
  `notice_tag` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标签',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '平台公告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_app_startup
-- ----------------------------
DROP TABLE IF EXISTS `tb_app_startup`;
CREATE TABLE `tb_app_startup`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ad_imgurl` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片地址',
  `ad_url` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片链接地址',
  `ad_idx` int(11) NULL DEFAULT NULL COMMENT '排序',
  `ad_status` tinyint(4) NULL DEFAULT 0 COMMENT '状态,0-停用，1-定时，2-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '定时开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '定时结束时间',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '启动页广告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_app_version
-- ----------------------------
DROP TABLE IF EXISTS `tb_app_version`;
CREATE TABLE `tb_app_version`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version_alias` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'app别名',
  `version_type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '类型，ios,android',
  `version_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '版本名称，1.0.0',
  `version_code` int(11) NULL DEFAULT NULL COMMENT '版本编号数字越大越新，1',
  `version_force` tinyint(4) NULL DEFAULT 0 COMMENT '强制更新，0-否，1-是',
  `version_status` tinyint(4) NULL DEFAULT 0 COMMENT '状态，0-停用，1-启用',
  `version_url` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '下载地址',
  `version_content` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '版本更新内容',
  `app_market` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '应用市场地址',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'app版本控制' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_bank
-- ----------------------------
DROP TABLE IF EXISTS `tb_bank`;
CREATE TABLE `tb_bank`  (
  `code` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `bank_name` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行名称',
  `bank_imgurl` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行图标',
  `bank_status` tinyint(4) NULL DEFAULT 0 COMMENT '0-关闭，1-启用',
  `money_unit_limit` decimal(8, 2) NULL DEFAULT NULL COMMENT '单笔交易限额',
  `money_day_limit` decimal(10, 2) NULL DEFAULT NULL COMMENT '单日限额',
  `idx` tinyint(4) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `code_helipay` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '合利宝银行代码code',
  `code_yeepay` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '易宝银行代码code',
  PRIMARY KEY (`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `tb_blacklist`;
CREATE TABLE `tb_blacklist`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uid` bigint(20) NULL DEFAULT NULL COMMENT '用户id',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `tel` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `cert_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '身份证',
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '姓名',
  `type` tinyint(4) NULL DEFAULT NULL COMMENT '类型 1:灰名单(失效时间动态化） 2:永久黑名单  0:正常',
  `tag` tinyint(4) NULL DEFAULT NULL COMMENT '标签，1-正常/2-老赖/3-代偿/4-特殊行业/5-学生/6-高负债/7-欺诈(欠款本人欺诈) /8-非本人(身份信息被冒用)/ 9-故意拖欠/10-疾病或死亡/11-其他',
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `invalid_time` datetime(0) NULL DEFAULT NULL COMMENT '失效时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uid`(`uid`) USING BTREE,
  INDEX `idx_tel`(`tel`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商户自建黑名单' ROW_FORMAT = Dynamic;



-- ----------------------------
-- Table structure for tb_whitelist
-- ----------------------------
DROP TABLE IF EXISTS `tb_whitelist`;
CREATE TABLE `tb_whitelist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tel` varchar(20) DEFAULT NULL COMMENT '手机号',
  `cert_no` varchar(20) DEFAULT NULL COMMENT '身份证',
  `name` varchar(20) DEFAULT NULL COMMENT '姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='客户白名单';



-- ----------------------------
-- Table structure for tb_manager
-- ----------------------------
DROP TABLE IF EXISTS `tb_manager`;
CREATE TABLE `tb_manager`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `login_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '登陆账号',
  `login_password` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '登录密码',
  `user_role_id` bigint(20) NULL DEFAULT NULL COMMENT '角色ID',
  `user_phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '登陆手机号',
  `user_name` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '人员姓名',
  `user_qq` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'QQ',
  `user_work_number` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工号',
  `company_id` bigint(20) NULL DEFAULT NULL COMMENT '所属公司',
  `department_id` bigint(20) NULL DEFAULT NULL COMMENT '所属部门',
  `last_login_time` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `account_status` int(2) NOT NULL DEFAULT 0 COMMENT '状态0-正常；1-已停用',
  `account_type` int(2) NOT NULL DEFAULT 0 COMMENT '类型0-公司员工；',
  `user_security` tinyint(1) NOT NULL DEFAULT 1 COMMENT '安全验证: 0-不安全 1-安全，默认1',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '后台登录帐号' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_manager_company
-- ----------------------------
DROP TABLE IF EXISTS `tb_manager_company`;
CREATE TABLE `tb_manager_company`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '公司主键',
  `company_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公司名称',
  `company_address` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公司地址',
  `company_type` int(2) NOT NULL COMMENT '类型0-总部；1-分公司',
  `company_status` int(2) NOT NULL COMMENT '状态0-正常；1-已停用',
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `company_phone` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '公司列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_manager_department
-- ----------------------------
DROP TABLE IF EXISTS `tb_manager_department`;
CREATE TABLE `tb_manager_department`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company_id` bigint(20) NOT NULL COMMENT '公司主键',
  `department_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '部门名称',
  `department_status` int(2) NOT NULL COMMENT '状态0-正常；1-已停用',
  `department_type` int(2) NOT NULL COMMENT '部门类型0-内部部门；',
  `remark` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '部门信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_manager_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_manager_resource`;
CREATE TABLE `tb_manager_resource`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pid` bigint(20) NOT NULL COMMENT '父主键',
  `resource_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '模块名称',
  `resource_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '链接地址',
  `resource_icon` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `resource_order` int(11) NOT NULL COMMENT '排列顺序',
  `resource_status` int(11) NOT NULL DEFAULT 0 COMMENT '状态0-正常；1-已停用 ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '模块资源' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_manager_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_manager_role`;
CREATE TABLE `tb_manager_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名称',
  `role_remark` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `role_status` int(11) NOT NULL DEFAULT 0 COMMENT '状态0-正常；1-已停用',
  `role_type` int(11) NOT NULL DEFAULT 0 COMMENT '类型0-普通用户；1-系统管理员',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_manager_role_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_manager_role_resource`;
CREATE TABLE `tb_manager_role_resource`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `resource_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_market_channel
-- ----------------------------
DROP TABLE IF EXISTS `tb_market_channel`;
CREATE TABLE `tb_market_channel`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channel_name` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称',
  `channel_idx` int(11) NULL DEFAULT 0 COMMENT '顺序,越大越靠前',
  `channel_status` tinyint(4) NULL DEFAULT 0 COMMENT '状态0-关闭，1-启用',
  `product_id` bigint(20) NULL DEFAULT NULL COMMENT '推荐产品',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '栏目' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_market_config
-- ----------------------------
DROP TABLE IF EXISTS `tb_market_config`;
CREATE TABLE `tb_market_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '渠道别名',
  `code` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '渠道code',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态0-关闭，1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_code`(`code`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_market_flow
-- ----------------------------
DROP TABLE IF EXISTS `tb_market_flow`;
CREATE TABLE `tb_market_flow`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NULL DEFAULT NULL,
  `flow_date` date NULL DEFAULT NULL COMMENT '日期',
  `flow_uv` bigint(20) NULL DEFAULT 0 COMMENT '独立访客',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '流量统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_market_module
-- ----------------------------
DROP TABLE IF EXISTS `tb_market_module`;
CREATE TABLE `tb_market_module`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channel_id` bigint(20) NULL DEFAULT NULL COMMENT '栏目id',
  `module_name` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称',
  `module_status` tinyint(4) NULL DEFAULT 0 COMMENT '状态0-关闭，1-启用',
  `module_idx` int(11) NULL DEFAULT 0 COMMENT '顺序,越大越靠前',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_market_product
-- ----------------------------
DROP TABLE IF EXISTS `tb_market_product`;
CREATE TABLE `tb_market_product`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `module_id` bigint(20) NULL DEFAULT NULL,
  `product_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称',
  `product_img` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片',
  `product_url` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '链接地址',
  `product_slogan` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标语',
  `product_type` tinyint(4) NULL DEFAULT 1 COMMENT '类型 1:内部 2:外部',
  `product_status` tinyint(4) NULL DEFAULT 0 COMMENT '状态，  0：下线  1：上线',
  `product_idx` int(11) NULL DEFAULT 0 COMMENT '排序',
  `loan_min` decimal(9, 2) NULL DEFAULT 0.00 COMMENT '额度下限',
  `loan_max` decimal(9, 2) NULL DEFAULT 0.00 COMMENT '额度上限',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `num` int(11) NULL DEFAULT 0 COMMENT '人数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant`;
CREATE TABLE `tb_merchant`  (
  `merchant_alias` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '商户别名与app别名一致',
  `merchant_app` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'app别名',
  `merchant_app_ios` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'ios别名',
  `merchant_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '商户名称',
  `merchant_zfb` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户支付宝',
  `merchant_status` tinyint(4) NOT NULL COMMENT '商户状态',
  `merchant_market` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '贷款超市',
  `merchant_channel` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户已开通的支付平台',
  `bind_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '绑卡类型：1合利宝,2富友,3汇聚,4易宝,5畅捷,6快钱,7汇潮',
  `hlb_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '合利宝商户编号',
  `hlb_rsa_private_key` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '合利宝rsa签名私钥',
  `hlb_merchant_sign` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '合利宝商户端签名sign',
  `hlb_entrusted_sign_key` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '合利宝委托代付签名signKey',
  `hlb_entrusted_private_key` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '合利宝委托代付私钥',
  `fuyou_merid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '富友商户id',
  `fuyou_secureid` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '富友代收付交易密钥',
  `fuyou_h5key` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '富友B2C/B2B网关支付密钥或手机银行App支付密钥',
  `huiju_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '汇聚商编',
  `huiju_md5_key` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '汇聚md5私钥',
  `yeepay_group_no` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '易宝商户编号',
  `yeepay_repay_appkey` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '易宝还款appkey',
  `yeepay_repay_private_key` varchar(3072) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '易宝还款私钥',
  `yeepay_loan_appkey` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '易宝放款appkey',
  `yeepay_loan_private_key` varchar(3072) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '易宝放款私钥',
  `kq_terminal_id` varchar(32) DEFAULT NULL COMMENT '快钱终端号',
  `kq_merchant_id` varchar(32) DEFAULT NULL COMMENT '快钱商户号',
  `kq_cert_pwd` varchar(32) DEFAULT NULL COMMENT '快钱证书密码',
  `kq_cert_path` varchar(64) DEFAULT NULL COMMENT '快钱证书路径',
  `kq_merchant_code` varchar(32) DEFAULT NULL COMMENT '快钱商户会员号',
  `cj_partnerId` varchar(32) DEFAULT NULL COMMENT '畅捷商户号',
  `cj_public_key` varchar(1024) DEFAULT NULL COMMENT '畅捷公钥',
  `cj_merchant_private_key` varchar(1024) DEFAULT NULL COMMENT '商户私钥',
  `huichao_merid` varchar(30) DEFAULT NULL COMMENT '汇潮的商户id',
  `huichao_public_key` varchar(500) DEFAULT NULL COMMENT '汇潮自己的公钥',
  `huichao_merchant_repay_private_key` varchar(1000) DEFAULT NULL COMMENT '汇潮商户的微信、支付宝、代扣的私钥',
  `huichao_merchant_pay_private_key` varchar(1000) DEFAULT NULL COMMENT '汇潮商户的代付的私钥',
  `jinyuntong_public_key` varchar(1024) DEFAULT NULL COMMENT '金运通公钥',
  `jinyuntong_merchant_id` varchar(32) DEFAULT NULL COMMENT '金运通商户号',
  `jinyuntong_merchant_private_key` varchar(2048) DEFAULT NULL COMMENT '金运通商户私钥',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`merchant_alias`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant_config
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_config`;
CREATE TABLE `tb_merchant_config`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `merchant` VARCHAR(50) DEFAULT NULL COMMENT '商户名称',
  `user_pay_confirm` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要放款，0：不需要，1：需要',
  `mx_risk_token` VARCHAR(1000) DEFAULT NULL COMMENT '风控默认token',
  `mx_risk_renew_token` VARCHAR(1000) DEFAULT NULL COMMENT '风控续借token',
  `h5_url` VARCHAR(255)  COMMENT 'h5地址',
  `overdue_blacklist_day` tinyint(2)   COMMENT '加入黑名单逾期天数',
  `reject_keyword` VARCHAR(128)   COMMENT '地址、公司拒绝关键字，逗号分格',
  `ident_invalid_day` tinyint(2)   COMMENT '认证失效天数',
  `auto_apply_order` tinyint(1) DEFAULT 1 COMMENT '自动提单:0-关闭 1-自动提单',
  `service_phone` varchar(20) DEFAULT NULl COMMENT '客服电话',
  `default_origin_status` tinyint(1) DEFAULT 1 COMMENT 'app渠道号注册:0-拒绝 1-允许',
  `max_overdue_fee_rate` tinyint(4) DEFAULT 100 COMMENT '最大逾期费费率率,数值类型,例:30表示逾期费率为百分之三十(借款金额*最大逾期费率=最大逾期费)',
  `promote_quota_type` tinyint(1) DEFAULT 1 COMMENT '0-关闭提额 1-新老客都提额 2-只新客提额 3-只老客提额',
  `old_customer_risk` tinyint(1) DEFAULT 0 COMMENT '老客是否过风控:0-不过风控 1-过风控',
  `multi_loan_merchant` VARCHAR(1024) DEFAULT null COMMENT '共债检查商户。未设置就是检查所有；设置了就是检查指定商户，多个商户逗号分割',
  `multi_loan_count` tinyint(1) DEFAULT 0 COMMENT '允许的共债系统个数。大于设置的共债数量则拒绝',
  `yys_operator_type` varchar(20) DEFAULT 'tongdun' COMMENT '运营商类型：tongdun.同盾，moxie.魔蝎',
  `old_customer_risk_renew_day` int(11) DEFAULT NULL COMMENT '可配置的老客过风控的静置天数',
  `create_time` CHAR(19) DEFAULT NULL COMMENT '插入时间',
  `update_time` CHAR(19) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_merchant`(`merchant`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant_login_record
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_login_record`;
CREATE TABLE `tb_merchant_login_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `manager_id` bigint(20) NULL DEFAULT NULL,
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所属商户',
  `login_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录名',
  `user_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `login_ip` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录ip',
  `user_ua` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `user_host` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_manager_id`(`manager_id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户登录记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant_manager
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_manager`;
CREATE TABLE `tb_merchant_manager`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所属商户',
  `role_id` bigint(20) NULL DEFAULT NULL COMMENT '角色ID',
  `login_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '登陆账号',
  `login_password` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '登录密码',
  `user_phone` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登陆手机号',
  `user_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '人员姓名',
  `last_login_time` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `account_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态0-正常；1-已停用',
  `account_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '类型0-公司员工；1-管理员，拥有所有权限',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `user_email` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `user_security` tinyint(4) UNSIGNED NOT NULL DEFAULT 1 COMMENT '0-不安全，1-安全。默认1',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_merchant_login`(`merchant`, `login_name`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商户后台人员信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant_manager_origin
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_manager_origin`;
CREATE TABLE `tb_merchant_manager_origin`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `origin_id` bigint(20) NULL DEFAULT NULL,
  `manager_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_origin_id`(`origin_id`) USING BTREE,
  INDEX `idx_manager_id`(`manager_id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant_origin
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_origin`;
CREATE TABLE `tb_merchant_origin`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所属商户',
  `origin_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '渠道别名',
  `mx_risk_token` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '风控token',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态0-正常；1-已停用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `deduction_rate` tinyint(3) NOT NULL DEFAULT 0 COMMENT '扣量比例',
  `check_repay` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否检查存在还款订单 0-不检查；1-检查',
  `check_overdue` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否检查存在逾期订单 0-不检查；1-检查',
  `check_blacklist` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否检查存在于黑名单 0-不检查；1-检查',
  `sms_merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户短信签名，用于马甲包',
  `weskit_alias` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '马甲包别名,订单总列表中展示的包名',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商户渠道' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant_rate
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_rate`;
CREATE TABLE `tb_merchant_rate`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '产品名称',
  `product_day` int(11) NOT NULL COMMENT '借款期限',
  `product_money` decimal(8, 2) NOT NULL COMMENT '借款金额',
  `product_level` int(11) NOT NULL COMMENT '优先级',
  `product_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：1:启用；0:禁用',
  `product_rate` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '年化利率',
  `total_rate` decimal(5, 2) NOT NULL COMMENT '综合费率',
  `overdue_rate` decimal(5, 2) NOT NULL COMMENT '逾期费率',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `merchant` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户别名',
  `product_type` tinyint(4) NULL DEFAULT 0 COMMENT '0-通用，1-新客，2-次新，3-续客',
  `borrow_type` int(4) NULL DEFAULT 99 COMMENT '借款次数,99-通用次数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '分期产品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_resource`;
CREATE TABLE `tb_merchant_resource`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pid` bigint(20) NOT NULL COMMENT '父主键',
  `resource_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '模块名称',
  `resource_url` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '链接地址',
  `resource_icon` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `resource_order` int(11) NULL DEFAULT 0 COMMENT '排列顺序',
  `resource_status` int(11) NOT NULL DEFAULT 0 COMMENT '状态0-正常；1-已停用 ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_role`;
CREATE TABLE `tb_merchant_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所属商户',
  `role_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名称',
  `role_remark` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `role_status` int(11) NOT NULL DEFAULT 0 COMMENT '状态0-正常；1-已停用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant_role_ref
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_role_ref`;
CREATE TABLE `tb_merchant_role_ref`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `resource_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_message_voice
-- ----------------------------
DROP TABLE IF EXISTS `tb_message_voice`;
CREATE TABLE `tb_message_voice`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uid` bigint(20) NULL DEFAULT NULL,
  `order_id` bigint(20) NULL DEFAULT NULL,
  `user_phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `call_id` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '唯一流水号',
  `merchant` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` tinyint(4) NULL DEFAULT NULL COMMENT '1：腾讯云或者轻码云；2：联信；3-轻码云',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0:初始化；1:受理成功；2：回调接通；3：回调未接通；4：受理失败',
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_call_id`(`call_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_moxie_mobile
-- ----------------------------
DROP TABLE IF EXISTS `tb_moxie_mobile`;
CREATE TABLE `tb_moxie_mobile`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uid` bigint(20) NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '认证手机号',
  `task_id` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '魔蝎唯一标识',
  `status` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'task.submit-任务创建通知,task-任务授权登录结果通知,task.fail-任务采集失败通知,bill-账单通知, report-用户报告通知',
  `message` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '加密请求报文，用来显示报告',
  `remark` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `tag` tinyint(4) NULL DEFAULT 0 COMMENT '0-未抓取数据，1-已抓取数据',
  `tag_magic` tinyint(4) NULL DEFAULT 0 COMMENT '0-未抓取魔杖数据，1-已抓取魔杖数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_task_id`(`task_id`) USING BTREE,
  INDEX `idx_uid`(`uid`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_moxie_zfb
-- ----------------------------
DROP TABLE IF EXISTS `tb_moxie_zfb`;
CREATE TABLE `tb_moxie_zfb`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uid` bigint(20) NULL DEFAULT NULL,
  `task_id` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '魔蝎唯一标识',
  `message` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '加密请求报文，用来显示报告',
  `status` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'task.submit-任务创建通知,task-任务授权登录结果通知,task.fail-任务采集失败通知,bill-账单通知, report-用户报告通知',
  `remark` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `tag` tinyint(4) NULL DEFAULT 0 COMMENT '0-未抓取数据，1-已抓取数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_task_id`(`task_id`) USING BTREE,
  INDEX `idx_uid`(`uid`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_moxie_zfb_fail
-- ----------------------------
DROP TABLE IF EXISTS `tb_moxie_zfb_fail`;
CREATE TABLE `tb_moxie_zfb_fail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '魔蝎唯一标识',
  `uid` bigint(20) NOT NULL,
  `status` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'task.submit-任务创建通知,task-任务授权登录结果通知,task.fail-任务采集失败通知,bill-账单通知, report-用户报告通知',
  `remark` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '错误描述',
  `create_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_task_id`(`task_id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `uid` bigint(20) NOT NULL COMMENT '用户Id',
  `borrow_day` tinyint(11) UNSIGNED NOT NULL COMMENT '借款期限',
  `borrow_money` decimal(7, 2) NOT NULL COMMENT '借款金额',
  `actual_money` decimal(7, 2) NOT NULL COMMENT '实际到账金额=借款金额-综合费',
  `total_rate` decimal(4, 2) NOT NULL DEFAULT 0.00 COMMENT '综合费率',
  `total_fee` decimal(7, 2) NOT NULL DEFAULT 0.00 COMMENT '综合费=借款金额*综合费率',
  `interest_rate` decimal(4, 2) NOT NULL DEFAULT 0.00 COMMENT '利率',
  `interest_fee` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '利息',
  `overdue_rate` decimal(4, 2) NOT NULL DEFAULT 0.00 COMMENT '逾期费率',
  `overdue_day` int(11) NOT NULL DEFAULT 0 COMMENT '逾期天数',
  `overdue_fee` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '逾期费用=借款金额*逾期费率*天数',
  `should_repay` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '应还金额=借款金额+利息+逾期费用-还款减免金额',
  `had_repay` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '已还金额',
  `reduce_money` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '还款减免金额',
  `status` tinyint(11) UNSIGNED NOT NULL DEFAULT 11 COMMENT '审核中10+：11-新建;12-等待复审;\r\n放款中20+；21-待放款;22-放款中(已受理);23-放款失败(可以重新放款);24-放款待确认(用户确认);\r\n还款中30+；31-已放款/还款中;32-还款确认中;33-逾期;34-坏账；35-展期；36-逾期后展期；37-展期后逾期；38-展期后坏账；\r\n已结清中40+；41-正常还款;42-逾期还款;43-展期还款; \r\n订单结束50+；51-自动审核失败 ;52-复审失败;53-取消',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `audit_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `arrive_time` datetime(0) NULL DEFAULT NULL COMMENT '到账时间',
  `repay_time` date NULL DEFAULT NULL COMMENT '应还日期=data(到账时间+借款期限-1)',
  `real_repay_time` datetime(0) NULL DEFAULT NULL COMMENT '实际全部结清时间',
  `order_version` int(11) NULL DEFAULT 0 COMMENT '版本号，防止并发操作',
  `merchant` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户别名',
  `follow_user_id` bigint(20) NULL DEFAULT 0 COMMENT '催收人id',
  `recycle_date` CHAR(10) DEFAULT NULL COMMENT '入催日期',
  `product_id` bigint(20) NOT NULL,
  `user_type` tinyint(4) NULL DEFAULT 1 COMMENT '1-新客，2-次新，3-续客',
  `recycle_type` tinyint(4) NULL DEFAULT 0 COMMENT '催收标签0-其他1-承诺还款2-谈判-高负债3-谈判-还款意愿低4-无人接听5-关机6-无法接通7-设置8-通话中9-停机10-跳票11-家人代偿12-线下已还款13-失联（本人通讯录无效）14-拒绝还款15-部分还款16-谈判中17-第三方转告18-停止催收',
  `group_id` bigint(20) NULL COMMENT 'tb_recycle_group表的主键,用于进行逾期S级分组',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uid`(`uid`) USING BTREE,
  INDEX `idx_merchant`(`merchant`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_update_time` (`update_time`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order_audit
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_audit`;
CREATE TABLE `tb_order_audit`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '订单id',
  `audit_id` bigint(20) NULL DEFAULT NULL COMMENT '审核人id',
  `audit_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审核人姓名',
  `fail_reason` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审核失败原因',
  `status` tinyint(4) NULL DEFAULT NULL COMMENT '0:审核通过；1:审核失败；2：人工复审',
  `crete_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `merchant` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_audit_id`(`audit_id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '审核记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order_pay
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_pay`;
CREATE TABLE `tb_order_pay`  (
  `pay_no` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '第三方放款流水号',
  `uid` bigint(20) NULL DEFAULT NULL,
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `pay_status` tinyint(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0-初始；1:受理成功；2:受理失败； 3:放款成功；4:放款失败',
  `pay_money` decimal(6, 2) NULL DEFAULT NULL COMMENT '支付金额',
  `bank` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '到帐银行',
  `bank_no` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行卡号',
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `pay_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1，合利宝；2，富友',
  PRIMARY KEY (`pay_no`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单放款记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order_phone
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_phone`;
CREATE TABLE `tb_order_phone`  (
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `phone_type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '手机类型（1，ios；2，android）',
  `param_value` varchar(400) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '估价参数值',
  `phone_model` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '型号',
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order_recycle_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_recycle_log`;
CREATE TABLE `tb_order_recycle_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `merchant` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '商户',
  `follow_user_id` bigint(20) NOT NULL COMMENT '催收人id',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_follow_user_id`(`follow_user_id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '逾期催收记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order_recycle_record
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_recycle_record`;
CREATE TABLE `tb_order_recycle_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `uid` bigint(20) NOT NULL,
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '商户',
  `follow_status` tinyint(4) NULL DEFAULT 0 COMMENT '0-未催收，1-催收中，2-停止催收，3-完成催收',
  `follow_user_id` bigint(20) NOT NULL COMMENT '催收人id',
  `remark` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `type` tinyint(4) NULL DEFAULT 0 COMMENT '催收标签0-其他1-承诺还款2-谈判-高负债3-谈判-还款意愿低4-无人接听5-关机6-无法接通7-设置8-通话中9-停机10-跳票11-家人代偿12-线下已还款13-失联（本人通讯录无效）14-拒绝还款',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_uid`(`uid`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '逾期催收记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order_repay
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_repay`;
CREATE TABLE `tb_order_repay`  (
  `repay_no` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '还款流水号',
  `uid` bigint(20) NULL DEFAULT NULL,
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `repay_type` tinyint(4) NULL DEFAULT 1 COMMENT '还款方式1-银行卡，2-支付宝，3-微信，4-线下转账',
  `repay_status` tinyint(11) NOT NULL DEFAULT 0 COMMENT '0-初始；1:受理成功；2:受理失败； 3:还款成功；4:还款失败;5:回调信息异常',
  `repay_money` decimal(6, 2) NULL DEFAULT NULL COMMENT '支付金额',
  `repay_cert` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '还款凭证',
  `bank` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '还款银行',
  `bank_no` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '还款卡号',
  `remark` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`repay_no`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单还款记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_question_ref
-- ----------------------------
DROP TABLE IF EXISTS `tb_question_ref`;
CREATE TABLE `tb_question_ref`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type_id` bigint(20) NULL DEFAULT NULL COMMENT '类型id',
  `article_id` bigint(20) NULL DEFAULT NULL COMMENT '文章id',
  `idx` int(11) NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '类型与问题详情中间表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_question_type
-- ----------------------------
DROP TABLE IF EXISTS `tb_question_type`;
CREATE TABLE `tb_question_type`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态（0，隐藏；1，显示。）',
  `idx` int(11) NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '帮助中心问题类型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_recycle_group
-- ----------------------------
DROP TABLE IF EXISTS `tb_recycle_group`;
CREATE TABLE `tb_recycle_group`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态0-正常；1-已停用',
  `group_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '组名',
  `create_time` datetime(0) NOT NULL,
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所属商户',
  `start_day` int(11) NULL COMMENT '逾期开始时间(大于等于)',
  `end_day` int(11) NULL COMMENT '逾期结束时间(小于)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_recycle_order_export
-- ----------------------------
DROP TABLE IF EXISTS `tb_recycle_order_export`;
CREATE TABLE `tb_recycle_order_export`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户',
  `manager_id` bigint(20) NULL DEFAULT NULL,
  `param` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求参数',
  `status` tinyint(4) NULL DEFAULT NULL COMMENT '状态（0，正在进行；1，已完成。）',
  `url` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件下载地址',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '催收订单导出任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_recycle_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_recycle_user`;
CREATE TABLE `tb_recycle_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL,
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态0-正常；1-已停用',
  `create_time` datetime(0) NOT NULL,
  `follow_user_id` bigint(20) NOT NULL COMMENT '催收人员id',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_pwd` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_origin` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '注册渠道，来源',
  `user_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `user_cert_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '身份证号',
  `img_face` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '人脸识别',
  `img_cert_front` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '身份证识别正面',
  `img_cert_back` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '身份证识别背面',
  `ia` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '签发机关',
  `indate` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '有效期',
  `address` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '住址',
  `nation` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '民族',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户',
  `user_nick` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `user_qq` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'qq号',
  `user_email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'email',
  `img_head` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `user_wechat` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '微信号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_phone_merchant`(`user_phone`, `merchant`) USING BTREE,
  INDEX `idx_user_origin`(`user_origin`) USING BTREE,
  INDEX `idx_create_time_merchant`(`create_time`, `merchant`) USING BTREE,
  INDEX `idx_user_cert_no`(`user_cert_no`) USING BTREE,
  INDEX `idx_update_time` (`update_time`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_user_address_list
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_address_list`;
CREATE TABLE `tb_user_address_list`  (
  `uid` bigint(20) NOT NULL,
  `address_list` mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '通讯录信息，json',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(0) NULL DEFAULT NULL,
  `task_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` tinyint(4) UNSIGNED NULL DEFAULT 0 COMMENT '0-初始化，1-已上传，2-待上传',
  PRIMARY KEY (`uid`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户通讯录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_user_bank
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_bank`;
CREATE TABLE `tb_user_bank`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uid` bigint(20) NULL DEFAULT NULL COMMENT '用户Id',
  `card_code` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行代码',
  `card_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行名称',
  `card_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '卡号',
  `card_phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '预留手机号',
  `card_status` tinyint(4) NULL DEFAULT 0 COMMENT '是否使用,0：禁用,1:使用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `foreign_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第三方标识',
  `remark` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `card_code_helipay` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '合利宝银行代码code',
  `bind_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '绑卡类型:1：合利宝；2：富友；3：汇聚; 4：易宝',
  `hlb_entrusted_cuid` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '合利宝委托代付(合利宝分配用户号)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uid`(`uid`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_user_device
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_device`;
CREATE TABLE `tb_user_device`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uid` bigint(20) NULL DEFAULT NULL,
  `deviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备id',
  `ip` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `location` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '经纬度',
  `city` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '城市',
  `net_type` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '网络类型',
  `phone_brand` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机品牌',
  `phone_model` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机型号',
  `phone_system` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机系统版本',
  `phone_resolution` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '分辨率',
  `phone_memory` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '内存',
  `isp` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '运营商',
  `client_alias` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '应用名称',
  `client_version` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '应用版本',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `black_box` varchar(4096) NULL COMMENT '设备指纹',
  `imei` varchar(64) NULL COMMENT '设备编号, ios取idfa',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uid`(`uid`) USING BTREE,
  INDEX `idx_deviceid`(`deviceid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_user_ident
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_ident`;
CREATE TABLE `tb_user_ident`  (
  `uid` bigint(20) NOT NULL COMMENT '用户id',
  `real_name` tinyint(4) NULL DEFAULT 0 COMMENT '是否实名认证,,0，未认证，1认证中,2认证成功 3认证失败,4已失效 ',
  `real_name_time` datetime(0) NULL DEFAULT NULL COMMENT '实名时间',
  `user_details` tinyint(4) NULL DEFAULT 0 COMMENT '是否个人信息认证（通讯录数据获取）,0，未认证，1认证中,2认证成功 3认证失败,4已失效 ',
  `user_details_time` datetime(0) NULL DEFAULT NULL COMMENT '个人信息认证时间',
  `bindbank` tinyint(4) NULL DEFAULT 0 COMMENT '绑卡：,0，未认证，1认证中,2认证成功 3认证失败,4已失效 ',
  `bindbank_time` datetime(0) NULL DEFAULT NULL COMMENT '绑卡时间',
  `mobile` tinyint(4) NULL DEFAULT 0 COMMENT '是否运营商手机认证,0，未认证，1认证中,2认证成功 3认证失败,4已失效 ',
  `mobile_time` datetime(0) NULL DEFAULT NULL COMMENT '手机认证时间',
  `liveness` tinyint(4) NULL DEFAULT 0 COMMENT '是否人脸识别认证,0，未认证，1认证中,2认证成功 3认证失败,4已失效 ',
  `liveness_time` datetime(0) NULL DEFAULT NULL COMMENT '人脸识别认证时间',
  `alipay` tinyint(4) NULL DEFAULT 0 COMMENT '是否支付宝认证,0，未认证，1认证中,2认证成功 3认证失败,4已失效 ',
  `alipay_time` datetime(0) NULL DEFAULT NULL COMMENT '支付宝认证时间',
  `taobao` tinyint(4) NULL DEFAULT 0 COMMENT '是否淘宝认证,0，未认证，1认证中,2认证成功 3认证失败,4已失效 ',
  `taobao_time` datetime(0) NULL DEFAULT NULL COMMENT '淘宝认证时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`uid`) USING BTREE,
  INDEX `idx_real_name_time`(`real_name_time`) USING BTREE,
  INDEX `idx_user_details_time`(`user_details_time`) USING BTREE,
  INDEX `idx_bindbank_time`(`bindbank_time`) USING BTREE,
  INDEX `idx_update_time` (`update_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户信息认证' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_user_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info`  (
  `uid` bigint(20) NOT NULL,
  `education` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学历',
  `live_province` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '居住省份',
  `live_city` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '居住城市',
  `live_district` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '居住区',
  `live_address` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '具体地址',
  `live_time` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '居住时长',
  `live_marry` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '婚姻状况',
  `work_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '职业',
  `work_company` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '公司',
  `work_address` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工作地址',
  `direct_contact` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '直系联系人关系。 父子',
  `direct_contact_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '直系联系人姓名。李三',
  `direct_contact_phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '直系联系人电话。1263637',
  `others_contact` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '其他联系人关系。 朋友',
  `others_contact_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '其他联系人姓名。李三',
  `others_contact_phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '其他联系人电话。1263637',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`uid`) USING BTREE,
  INDEX `index_direct`(`direct_contact_phone`) USING BTREE,
  INDEX `index_others`(`others_contact_phone`) USING BTREE,
  INDEX `index_update_time` (`update_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order_risk_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_risk_info`;
CREATE TABLE `tb_order_risk_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `risk_id` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '风控id',
  `risk_status` TINYINT(1) DEFAULT NULL COMMENT '风控状态, 0-未知 1-通过 2-拒绝 3-人审',
  `risk_result` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '风控结果',
  `user_phone`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户手机',
  `user_name`   varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT ' 用户姓名',
  `user_cert_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT ' 身份证号',
  `risk_model_score` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '天机、拍拍、天御模型分数JSON串',
  `create_time` char(19) DEFAULT NULL COMMENT '创建时间',
  `update_time` char(19) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_risk_id`(`risk_id`) USING BTREE,
  INDEX `idx_phone`(`user_phone`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_update_time` (`update_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户风控信息' ROW_FORMAT = Dynamic;


DROP TABLE IF EXISTS `report_partner_effect_deduction`;
CREATE TABLE `report_partner_effect_deduction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `day_key` date DEFAULT NULL COMMENT '注册日期',
  `merchant` varchar(50) DEFAULT NULL COMMENT '商户',
  `user_origin` varchar(16) DEFAULT NULL COMMENT '注册渠道，来源',
  `reg_cnt` int(11) DEFAULT NULL COMMENT '注册人数',
  `login_cnt` int(11) DEFAULT NULL COMMENT '注册的登录数量',
  `real_name_cnt` int(11) DEFAULT NULL COMMENT '实名人数',
  `submit_order_cnt` int(11) DEFAULT NULL COMMENT '提单人数',
  `first_submit_cnt` int(11) DEFAULT NULL COMMENT '首借人数',
  `first_submit_amount` decimal(20,2) DEFAULT NULL COMMENT '首借金额',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `personal_info_certi_cnt` int(11) DEFAULT NULL COMMENT '个人信息认证数',
  `yys_cnt` int(11) DEFAULT NULL COMMENT '运营商认证数',
  `bank_cnt` int(11) DEFAULT NULL COMMENT '银行卡绑定数',
  `order_cnt` int(11) DEFAULT NULL COMMENT '申请订单数',
  `pass_risk_cnt` int(11) DEFAULT NULL COMMENT '风控通过数',
  `loan_success_cnt` int(11) DEFAULT NULL COMMENT '下款数',
  `real_name_certi_rate` varchar(11) DEFAULT NULL COMMENT '实名认证率',
  `personal_info_certi_rate` varchar(11) DEFAULT NULL COMMENT '个人信息认证率',
  `yys_certi_rate` varchar(11) DEFAULT NULL COMMENT '运营商认证率',
  `bank_bound_rate` varchar(11) DEFAULT NULL COMMENT '银行卡绑定率',
  `reg_apply_trans_rate` varchar(11) DEFAULT NULL COMMENT '申请转化率',
  `loan_rate` varchar(11) DEFAULT NULL COMMENT '下款率',
  `audit_pass_rate` varchar(11) DEFAULT NULL COMMENT '审核通过率',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `report_partner_effect_deduction` (`day_key`,`merchant`,`user_origin`) USING BTREE,
  KEY `idx_merchant` (`merchant`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='渠道统计报表(扣量)';


DROP TABLE IF EXISTS `tb_user_deduction`;
CREATE TABLE `tb_user_deduction`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_origin` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '注册渠道，来源',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `merchant` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属商户',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_origin`(`user_origin`) USING BTREE,
  INDEX `idx_merchant`(`merchant`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '扣量用户' ROW_FORMAT = Dynamic;


DROP TABLE IF EXISTS `report_register_order_deduction`;
CREATE TABLE `report_register_order_deduction`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `day_key` date NULL DEFAULT NULL,
  `merchant` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `register_cnt` int(11) NULL DEFAULT NULL,
  `realname_cnt` int(11) NULL DEFAULT NULL,
  `zfb_cnt` int(11) NULL DEFAULT NULL,
  `mobile_cnt` int(11) NULL DEFAULT NULL,
  `order_cnt` int(11) NULL DEFAULT NULL,
  `first_cnt` int(11) NULL DEFAULT NULL,
  `second_cnt` int(11) NULL DEFAULT NULL,
  `old_cnt` int(11) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_day_merchant`(`day_key`, `merchant`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8402 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '注册量统计报表(扣量)' ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for tb_tongdun_taobao
-- ----------------------------
DROP TABLE IF EXISTS `tb_tongdun_taobao`;
CREATE TABLE `tb_tongdun_taobao`  (
  `uid` bigint(20) NOT NULL,
  `task_id` varchar(40) DEFAULT NULL COMMENT '同盾唯一标识',
  `message` varchar(64) DEFAULT NULL COMMENT '消息',
  `status` varchar(16) DEFAULT NULL COMMENT 'task.submit-任务创建通知,task-任务授权登录结果通知,task.fail-任务采集失败通知,bill-账单通知, report-用户报告通知',
  `remark` varchar(32) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `tag` tinyint(4) DEFAULT '0' COMMENT '0-未抓取数据，1-已抓取数据',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `idx_task_id` (`task_id`) USING BTREE,
  KEY `idx_uid` (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_tongdun_mobile
-- ----------------------------
DROP TABLE IF EXISTS `tb_tongdun_mobile`;
CREATE TABLE `tb_tongdun_mobile`  (
  `uid` bigint(20) NOT NULL,
  `task_id` varchar(40) DEFAULT NULL COMMENT '同盾唯一标识',
  `message` varchar(64) DEFAULT NULL COMMENT '消息',
  `status` varchar(16) DEFAULT NULL COMMENT 'task.submit-任务创建通知,task-任务授权登录结果通知,task.fail-任务采集失败通知,bill-账单通知, report-用户报告通知',
  `remark` varchar(32) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `tag` tinyint(4) DEFAULT '0' COMMENT '0-未抓取数据，1-已抓取数据',
  `mobile_upload_status` tinyint(4) DEFAULT '0' COMMENT '0-运营商数据未上传OSS，1-运营商数据已上传OSS',
  `mofang_upload_status` tinyint(4) DEFAULT '0' COMMENT '0-魔方报告未上传到OSS，1-魔方报告已上传到OSS',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `idx_task_id` (`task_id`) USING BTREE,
  KEY `idx_uid` (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_merchant_defer_config
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_defer_config`;
CREATE TABLE `tb_merchant_defer_config`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `merchant` VARCHAR(50) DEFAULT NULL COMMENT '商户名称',
  `status` TINYINT(1) DEFAULT 0 COMMENT '续期开关: 0-禁用 1-启用',
  `defer_day` TINYINT(2) DEFAULT 7 COMMENT '续期天数',
  `daily_defer_rate` DOUBLE(7, 2) DEFAULT NULL COMMENT '日续期费率:百分比',
  `daily_defer_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '日续期费',
  `daily_other_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '日额外费',
  `max_defer_times` TINYINT(2) DEFAULT NULL COMMENT '最大续期次数',
  `create_time` CHAR(19) DEFAULT NULL COMMENT '插入时间',
  `update_time` CHAR(19) DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order_defer
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_defer`;
CREATE TABLE `tb_order_defer`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单号',
  `uid` bigint(20) NULL DEFAULT NULL COMMENT '用户Id',
  `user_name` VARCHAR(30) DEFAULT NULL COMMENT '姓名',
  `user_phone` VARCHAR(20) DEFAULT NULL COMMENT '手机',
  `overdue_day` TINYINT(2) default NUll COMMENT '逾期天数',
  `overdue_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '逾期费',
  `reduce_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '减免金额',
  `defer_day` TINYINT(2) default NUll COMMENT '续期天数',
  `daily_defer_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '日续期费',
  `defer_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '续期费',
  `defer_times` TINYINT(2) DEFAULT NULL COMMENT '当前第几次续期',
  `defer_total_fee` DOUBLE(7, 2) DEFAULT NULL COMMENT '续期总额: 逾期费+续期费',
  `pay_type` CHAR(10) DEFAULT NULL COMMENT '支付方式:线上/线下',
  `pay_no` VARCHAR(64) DEFAULT NULL COMMENT '支付单号:线上支付',
  `pay_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0-初始；1:受理成功；2:受理失败； 3:还款成功；4:还款失败;5:回调信息异常',
  `pay_time` CHAR(19) DEFAULT NULL COMMENT '续期支付时间',
  `create_time` CHAR(19) DEFAULT NULL COMMENT '续期申请时间',
  `repay_date` CHAR(10) DEFAULT NULL COMMENT '原始到期日',
  `defer_repay_date` CHAR(10) DEFAULT NULL COMMENT '续期到期日',
  `merchant` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户别名',
  `remark` VARCHAR(255) DEFAULT null COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_pay_no`(`pay_no`) USING BTREE,
  INDEX `idx_uid`(`uid`) USING BTREE,
  INDEX `idx_pay_time`(`pay_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
DROP TABLE IF EXISTS `tb_user_register_code_stat`;
CREATE TABLE `tb_user_register_code_stat`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_phone` VARCHAR(20) DEFAULT NULL COMMENT '手机',
  `merchant` VARCHAR(50) DEFAULT NULL COMMENT '商户名称',
  `day_count` int(6) default NUll COMMENT '当日获取验证码次数',
  `total_count` int(6) default NUll COMMENT '获取总验证码次数',
  `register_date` CHAR(10) DEFAULT NULL COMMENT '注册日期',
  `create_time` CHAR(30) DEFAULT NULL COMMENT '插入时间',
  `update_time` CHAR(30) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_phone`(`user_phone`, merchant ) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- ----------------------------
DROP TABLE IF EXISTS `report_recycle_repay_stat`;
CREATE TABLE `report_recycle_repay_stat`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `recycled_id` bigint(20) DEFAULT NULL COMMENT '催收人ID',
  `recycled_name` VARCHAR(20) DEFAULT NULL COMMENT '催收人姓名',
  `merchant` VARCHAR(50) DEFAULT NULL COMMENT '商户名称',
  `recycle_cnt` int(6) NOT NULL default 0 COMMENT '入催订单数',
  `not_return_cnt` int(6) NOT NULL default 0 COMMENT '未还订单数',
  `overdue_day` int(6) NOT NULL default 0 COMMENT '逾期天数',
  `repay_1_rate` DOUBLE(3, 2) NOT NULL default 0.00 COMMENT '入催一天还款率',
  `repay_3_rate` DOUBLE(3, 2) NOT NULL default 0.00 COMMENT '入催三天还款率',
  `repay_7_rate` DOUBLE(3, 2) NOT NULL default 0.00 COMMENT '入催七天还款率',
  `repay_60_rate` DOUBLE(3, 2) NOT NULL default 0.00 COMMENT '入催六十天还款率',
  `recycle_date` CHAR(10) DEFAULT NULL COMMENT '入催日期',
  `create_time` CHAR(30) DEFAULT NULL COMMENT '插入时间',
  `update_time` CHAR(30) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY  `idx_date`(`recycle_date`, recycled_id ) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- uv/pv
DROP TABLE IF EXISTS `tb_loan_market_stat`;
CREATE TABLE `tb_loan_market_stat`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant` VARCHAR(50) DEFAULT NULL COMMENT '商户名称',
  `loan_market_url` VARCHAR(255) DEFAULT NULL COMMENT '贷超链接',
  `loan_market_pv` int(11) DEFAULT 0 COMMENT 'pv',
  `loan_market_uv` int(11) DEFAULT 0 COMMENT 'uv',
  `stat_date` char(10) default NULL COMMENT '统计日期: yyyy-MM-dd',
  `update_time` char(19) default null COMMENT '更新时: yyyy-MM-dd HH:mm:ss',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_stats_date`(`merchant`, stat_date) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- tb_merchant_quota_config
DROP TABLE IF EXISTS `tb_merchant_quota_config`;
CREATE TABLE `tb_merchant_quota_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `quota_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '额度名称',
  `quota_value` int(11) NOT NULL COMMENT '提升额度，可以为负数',
  `comparator` varchar(11) DEFAULT NULL COMMENT '字段比较符: eq/gt/lt/gte/lte/range/in/exists',
  `preset_value` varchar(128) DEFAULT NULL COMMENT '预设值',
  `merchant` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户别名',
  `quota_type` tinyint(4) NULL  COMMENT '1-天机分，2-展期次数',
  `borrow_type` int(4) NULL DEFAULT 0 COMMENT '借款次数',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '1-启用, 0-停用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_borrow_type`(`merchant`, borrow_type) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商户提额配置表' ROW_FORMAT = Dynamic;

-- sms_record
DROP TABLE IF EXISTS `sms_record`;
CREATE TABLE `sms_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant` varchar(30) NOT NULL COMMENT '商户别名',
  `channel` int(4) NOT NULL COMMENT '短信渠道 1 创蓝  2 飞鸽',
  `content` varchar(255) DEFAULT NULL COMMENT '短信内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信发送历史表';

-- tb_merchant_moxie_config
DROP TABLE IF EXISTS `tb_merchant_moxie_config`;
CREATE TABLE `tb_merchant_moxie_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant` varchar(16) NOT NULL COMMENT '商户名称',
  `moxie_token` varchar(2048) NOT NULL COMMENT '商户对应的魔蝎秘钥',
  `moxie_secret` varchar(2048) NOT NULL COMMENT '魔蝎的异步回调秘钥',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_merchant` (`merchant`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商户魔蝎配置表' ROW_FORMAT = Dynamic;


-- tb_merchant_fee
DROP TABLE IF EXISTS `tb_merchant_fee`;
CREATE TABLE `tb_merchant_fee` (
  `merchant_alias` varchar(20) CHARACTER SET utf8 NOT NULL COMMENT '商户号',
  `sms1_price` decimal(6,2) DEFAULT NULL COMMENT '短信1创蓝单价',
  `sms2_price` decimal(6,2) DEFAULT NULL COMMENT '短信2飞鸽单价',
  `youdun_price` decimal(6,2) DEFAULT NULL COMMENT '有盾单价',
  `operator_price` decimal(6,2) DEFAULT NULL COMMENT '运营商单价',
  `risk_price` decimal(6,2) DEFAULT NULL COMMENT '风控单价',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`merchant_alias`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商户费用项单价配置';


-- tb_merchant_fee_statistics
DROP TABLE IF EXISTS `tb_merchant_fee_statistics`;
CREATE TABLE `tb_merchant_fee_statistics` (
  `day` varchar(20) NOT NULL COMMENT '日期yyyy-MM-dd',
  `merchant` varchar(20) NOT NULL COMMENT '商户别名',
  `sms1_price` decimal(6,2) DEFAULT NULL COMMENT '短信1创蓝单价',
  `sms1_count` int(11) DEFAULT NULL COMMENT '短信1创蓝条数',
  `sms1_fee` decimal(12,2) DEFAULT NULL COMMENT '短信1创蓝总费用',
  `sms2_price` decimal(6,2) DEFAULT NULL COMMENT '飞鸽单价',
  `sms2_count` int(11) DEFAULT NULL COMMENT '飞鸽条数',
  `sms2_fee` decimal(12,2) DEFAULT NULL COMMENT '飞鸽总费用',
  `youdun_price` decimal(6,2) DEFAULT NULL COMMENT '有盾单价',
  `youdun_count` int(11) DEFAULT NULL COMMENT '有盾次数',
  `youdun_fee` decimal(12,2) DEFAULT NULL COMMENT '有盾总费用',
  `operator_price` decimal(6,2) DEFAULT NULL COMMENT '运营商单价',
  `operator_count` int(11) DEFAULT NULL COMMENT '运营商个数',
  `operator_fee` decimal(12,2) DEFAULT NULL COMMENT '运营商费用',
  `risk_price` decimal(6,2) DEFAULT NULL COMMENT '风控单价',
  `risk_count` int(11) DEFAULT NULL COMMENT '风控个数',
  `risk_fee` decimal(12,2) DEFAULT NULL COMMENT '风控费用',
  `refused_count` int(11) DEFAULT NULL COMMENT '风控拒绝量',
  PRIMARY KEY (`day`,`merchant`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商户费用统计表';

-- tb_third_call_history
DROP TABLE IF EXISTS `tb_third_call_history`;
CREATE TABLE `tb_third_call_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant` varchar(30) NOT NULL COMMENT '商户别名',
  `day` varchar(20) NOT NULL COMMENT '日期yyyy-MM-dd',
  `code` varchar(11) DEFAULT NULL COMMENT '区分不同第三方 1 有盾 2 运营商',
  `create_time` datetime DEFAULT NULL,
  `uid` bigint(20) NOT NULL COMMENT 'user表主键',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_code_day` (`merchant`,`day`,`code`) USING BTREE,
  KEY `idx_day` (`day`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='第三方调用历史表';
