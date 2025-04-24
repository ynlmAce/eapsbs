-- 创建数据库
CREATE DATABASE IF NOT EXISTS eaps DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE eaps;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password_hash` varchar(100) NOT NULL COMMENT '密码哈希',
  `role` varchar(20) NOT NULL COMMENT '角色：ROLE_STUDENT, ROLE_COMPANY, ROLE_COUNSELOR',
  `status` varchar(10) NOT NULL DEFAULT '正常' COMMENT '状态：正常、禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 学生资料表
CREATE TABLE IF NOT EXISTS `student_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生资料ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `contact` varchar(50) NOT NULL COMMENT '联系方式',
  `school` varchar(100) NOT NULL COMMENT '学校',
  `major` varchar(100) NOT NULL COMMENT '专业',
  `grade` varchar(20) NOT NULL COMMENT '年级',
  `self_introduction` text COMMENT '自我介绍',
  `job_preferences` text COMMENT '求职意向',
  `behavior_score` int NOT NULL DEFAULT '100' COMMENT '行为分',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_student_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生资料表';

-- 学生简历文件表
CREATE TABLE IF NOT EXISTS `student_resume_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '简历文件ID',
  `student_profile_id` bigint NOT NULL COMMENT '学生资料ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_path` varchar(255) NOT NULL COMMENT '文件路径',
  `uploaded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_profile_id` (`student_profile_id`),
  CONSTRAINT `fk_resume_file_student` FOREIGN KEY (`student_profile_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生简历文件表';

-- 企业表
CREATE TABLE IF NOT EXISTS `company` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '企业ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(100) NOT NULL COMMENT '企业名称',
  `unified_social_credit_code` varchar(50) NOT NULL COMMENT '统一社会信用代码',
  `industry` varchar(50) NOT NULL COMMENT '行业',
  `size` varchar(20) NOT NULL COMMENT '规模',
  `address` varchar(255) NOT NULL COMMENT '地址',
  `description` text COMMENT '企业简介',
  `hr_contact` varchar(100) NOT NULL COMMENT 'HR联系方式',
  `logo_path` varchar(255) DEFAULT NULL COMMENT '企业Logo路径',
  `certification_status` varchar(20) NOT NULL DEFAULT '未认证' COMMENT '认证状态：未认证、待认证、已认证、认证失败',
  `certification_expiry_date` datetime DEFAULT NULL COMMENT '认证有效期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_company_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业表';

-- 辅导员资料表
CREATE TABLE IF NOT EXISTS `counselor_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '辅导员资料ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `employee_id` varchar(50) NOT NULL COMMENT '工号',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `contact` varchar(50) NOT NULL COMMENT '联系方式',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_counselor_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员资料表';

-- 岗位表
CREATE TABLE IF NOT EXISTS `job_posting` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `title` varchar(100) NOT NULL COMMENT '岗位标题',
  `description` text NOT NULL COMMENT '岗位描述',
  `requirement` text NOT NULL COMMENT '岗位要求',
  `location` varchar(100) NOT NULL COMMENT '工作地点',
  `salary` varchar(50) NOT NULL COMMENT '薪资范围',
  `education` varchar(20) NOT NULL COMMENT '学历要求',
  `experience` varchar(20) NOT NULL COMMENT '经验要求',
  `job_type` varchar(20) NOT NULL COMMENT '工作类型',
  `headcount` int NOT NULL COMMENT '招聘人数',
  `work_time` varchar(100) NOT NULL COMMENT '工作时间',
  `valid_until` datetime NOT NULL COMMENT '有效期至',
  `contact_person` varchar(50) NOT NULL COMMENT '联系人',
  `contact_method` varchar(100) NOT NULL COMMENT '联系方式',
  `show_contact` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否显示联系方式',
  `status` varchar(20) NOT NULL DEFAULT '待审核' COMMENT '状态：待审核、招聘中、已驳回、已结束',
  `published_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  CONSTRAINT `fk_job_posting_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 岗位申请表
CREATE TABLE IF NOT EXISTS `application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `resume_id` bigint DEFAULT NULL COMMENT '简历ID',
  `cover_letter` text COMMENT '求职信',
  `applied_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `status` varchar(20) NOT NULL DEFAULT '已投递' COMMENT '状态：已投递、企业已查看、已邀请面试、已拒绝等',
  `company_notes` text COMMENT '企业备注',
  `last_updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_job` (`student_id`,`job_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_student_id` (`student_id`),
  CONSTRAINT `fk_application_job` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_application_student` FOREIGN KEY (`student_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位申请表';

-- 企业评价表
CREATE TABLE IF NOT EXISTS `rating` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `job_id` bigint DEFAULT NULL COMMENT '岗位ID',
  `overall_score` decimal(2,1) NOT NULL COMMENT '总体评分',
  `job_authenticity_score` decimal(2,1) NOT NULL COMMENT '岗位真实性评分',
  `interview_experience_score` decimal(2,1) NOT NULL COMMENT '面试体验评分',
  `work_environment_score` decimal(2,1) NOT NULL COMMENT '公司环境评分',
  `welfare_delivery_score` decimal(2,1) NOT NULL COMMENT '福利兑现评分',
  `comment` text NOT NULL COMMENT '评价内容',
  `is_anonymous` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否匿名',
  `status` varchar(10) NOT NULL DEFAULT 'Active' COMMENT '状态：Active/Deleted',
  `submitted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_student_id` (`student_id`),
  CONSTRAINT `fk_rating_company` FOREIGN KEY (`company_id`) REFERENCES `company` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rating_student` FOREIGN KEY (`student_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业评价表';

-- 聊天会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `type` varchar(2) NOT NULL COMMENT '会话类型：SE(学生-企业), SC(学生-辅导员), SS(学生群组)',
  `related_job_id` bigint DEFAULT NULL COMMENT '关联的岗位ID',
  `group_id` bigint DEFAULT NULL COMMENT '群组ID',
  `participant1_id` bigint NOT NULL COMMENT '参与者1ID',
  `participant2_id` bigint DEFAULT NULL COMMENT '参与者2ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_active_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  `status` varchar(10) NOT NULL DEFAULT 'active' COMMENT '状态：active, archived',
  `is_readonly` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否只读',
  PRIMARY KEY (`id`),
  KEY `idx_participant1` (`participant1_id`),
  KEY `idx_participant2` (`participant2_id`),
  KEY `idx_related_job` (`related_job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `content_type` varchar(10) NOT NULL DEFAULT 'text' COMMENT '内容类型：text, image, file',
  `file_path` varchar(255) DEFAULT NULL COMMENT '文件路径',
  `sent_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 辅导员任务表
CREATE TABLE IF NOT EXISTS `counselor_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `type` varchar(30) NOT NULL COMMENT '任务类型：companyCertification, jobAudit, reportHandling',
  `target_item_type` varchar(20) NOT NULL COMMENT '目标项类型：Company, Job, Rating, ChatMessage, ChatSession',
  `target_item_id` bigint NOT NULL COMMENT '目标项ID',
  `title` varchar(100) NOT NULL COMMENT '任务标题',
  `company_id` bigint DEFAULT NULL COMMENT '公司ID（如果相关）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending, processing, completed',
  `counselor_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `notes` text COMMENT '处理备注',
  `priority` varchar(10) NOT NULL DEFAULT 'normal' COMMENT '优先级：high, normal, low',
  PRIMARY KEY (`id`),
  KEY `idx_type_status` (`type`,`status`),
  KEY `idx_counselor_id` (`counselor_id`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员任务表';

-- 辅导员操作日志表
CREATE TABLE IF NOT EXISTS `counselor_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '操作ID',
  `counselor_id` bigint NOT NULL COMMENT '辅导员ID',
  `operation_type` varchar(30) NOT NULL COMMENT '操作类型：companyCertification, jobAudit, reportHandling',
  `target_item_type` varchar(20) NOT NULL COMMENT '目标项类型：Company, Job, Rating, ChatMessage, ChatSession',
  `target_item_id` bigint NOT NULL COMMENT '目标项ID',
  `result` varchar(20) NOT NULL COMMENT '操作结果：approve, reject, delete',
  `reason` varchar(255) NOT NULL COMMENT '操作原因',
  `notes` text COMMENT '操作备注',
  `timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_counselor_id` (`counselor_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员操作日志表';

-- 举报表
CREATE TABLE IF NOT EXISTS `report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporting_user_id` bigint NOT NULL COMMENT '举报人ID',
  `reported_item_type` varchar(20) NOT NULL COMMENT '举报项类型：rating, message, chat_session',
  `reported_item_id` bigint NOT NULL COMMENT '举报项ID',
  `reason` text NOT NULL COMMENT '举报原因',
  `reported_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `status` varchar(10) NOT NULL DEFAULT 'Pending' COMMENT '状态：Pending/Resolved',
  `counselor_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `resolution` varchar(255) DEFAULT NULL COMMENT '处理结果',
  PRIMARY KEY (`id`),
  KEY `idx_reported_item` (`reported_item_type`,`reported_item_id`),
  KEY `idx_reporting_user` (`reporting_user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 初始化管理员账号
INSERT INTO `user` (`username`, `password_hash`, `role`, `status`) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOSsz5R/6gKqmbxA.xaqbIKpjA4C2', 'ROLE_ADMIN', '正常'); -- 密码: admin123 