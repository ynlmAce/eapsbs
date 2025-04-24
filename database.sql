-- 大学生就业帮扶系统数据库结构

-- 创建数据库
CREATE DATABASE IF NOT EXISTS employment_support_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE employment_support_system;

-- 用户表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名/学号/工号',
  `password_hash` varchar(100) NOT NULL COMMENT '密码哈希值',
  `role` enum('student','company','counselor') NOT NULL COMMENT '用户角色',
  `status` enum('active','inactive','locked') NOT NULL DEFAULT 'active' COMMENT '账号状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 学生档案表
CREATE TABLE `student_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '档案ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `contact` varchar(50) NOT NULL COMMENT '联系方式',
  `school` varchar(100) NOT NULL COMMENT '学校',
  `major` varchar(100) NOT NULL COMMENT '专业',
  `grade` varchar(20) NOT NULL COMMENT '年级',
  `self_introduction` text COMMENT '自我介绍',
  `job_preferences` text COMMENT '求职意向',
  `behavior_score` int NOT NULL DEFAULT 100 COMMENT '行为分',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_student_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生档案表';

-- 学生结构化简历表
CREATE TABLE `student_structured_resume` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `student_profile_id` bigint NOT NULL COMMENT '学生档案ID',
  `section_type` enum('education','project','experience','skill') NOT NULL COMMENT '简历部分类型',
  `content` json NOT NULL COMMENT '内容（JSON格式）',
  `order_num` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_profile_id` (`student_profile_id`),
  CONSTRAINT `fk_structured_resume_profile` FOREIGN KEY (`student_profile_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生结构化简历表';

-- 学生简历文件表
CREATE TABLE `student_resume_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `student_profile_id` bigint NOT NULL COMMENT '学生档案ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_path` varchar(255) NOT NULL COMMENT '文件路径',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(50) NOT NULL COMMENT '文件类型',
  `uploaded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_profile_id` (`student_profile_id`),
  CONSTRAINT `fk_resume_file_profile` FOREIGN KEY (`student_profile_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生简历文件表';

-- 技能标签表
CREATE TABLE `skill_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能标签表';

-- 学生技能关联表
CREATE TABLE `student_skill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `student_profile_id` bigint NOT NULL COMMENT '学生档案ID',
  `skill_tag_id` bigint NOT NULL COMMENT '技能标签ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_student_skill` (`student_profile_id`,`skill_tag_id`),
  KEY `idx_skill_tag_id` (`skill_tag_id`),
  CONSTRAINT `fk_student_skill_profile` FOREIGN KEY (`student_profile_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_student_skill_tag` FOREIGN KEY (`skill_tag_id`) REFERENCES `skill_tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生技能关联表';

-- 企业档案表
CREATE TABLE `company_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '企业ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(100) NOT NULL COMMENT '企业名称',
  `unified_social_credit_code` varchar(50) NOT NULL COMMENT '统一社会信用代码',
  `industry` varchar(50) NOT NULL COMMENT '行业',
  `size` varchar(50) NOT NULL COMMENT '规模',
  `address` varchar(255) NOT NULL COMMENT '地址',
  `description` text COMMENT '公司简介',
  `hr_contact` varchar(100) NOT NULL COMMENT 'HR联系方式',
  `logo_path` varchar(255) DEFAULT NULL COMMENT 'Logo路径',
  `certification_status` enum('pending','certified','rejected','expired') NOT NULL DEFAULT 'pending' COMMENT '认证状态',
  `certification_expiry_date` date DEFAULT NULL COMMENT '认证有效期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `idx_credit_code` (`unified_social_credit_code`),
  CONSTRAINT `fk_company_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业档案表';

-- 企业营业执照文件表
CREATE TABLE `company_license_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `company_profile_id` bigint NOT NULL COMMENT '企业档案ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_path` varchar(255) NOT NULL COMMENT '文件路径',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(50) NOT NULL COMMENT '文件类型',
  `uploaded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_profile_id` (`company_profile_id`),
  CONSTRAINT `fk_license_file_profile` FOREIGN KEY (`company_profile_id`) REFERENCES `company_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业营业执照文件表';

-- 辅导员档案表
CREATE TABLE `counselor_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '辅导员ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `employee_id` varchar(50) NOT NULL COMMENT '工号',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `contact` varchar(50) NOT NULL COMMENT '联系方式',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `idx_employee_id` (`employee_id`),
  CONSTRAINT `fk_counselor_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员档案表';

-- 福利标签表
CREATE TABLE `welfare_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `category` varchar(50) DEFAULT NULL COMMENT '标签分类',
  `status` enum('active','pending','rejected') NOT NULL DEFAULT 'active' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='福利标签表';

-- 岗位标签表
CREATE TABLE `job_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位标签表';

-- 岗位表
CREATE TABLE `job_posting` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `title` varchar(100) NOT NULL COMMENT '岗位名称',
  `description` text NOT NULL COMMENT '岗位描述',
  `requirement` text NOT NULL COMMENT '岗位要求',
  `location` varchar(100) NOT NULL COMMENT '工作地点',
  `salary` varchar(50) NOT NULL COMMENT '薪资范围',
  `education` varchar(50) NOT NULL COMMENT '学历要求',
  `experience` varchar(50) NOT NULL COMMENT '经验要求',
  `job_type` varchar(50) NOT NULL COMMENT '工作类型',
  `headcount` int NOT NULL COMMENT '招聘人数',
  `work_time` varchar(100) NOT NULL COMMENT '工作时间',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_method` varchar(100) DEFAULT NULL COMMENT '联系方式',
  `show_contact` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否显示联系方式',
  `valid_until` date NOT NULL COMMENT '有效期',
  `status` enum('draft','pending','active','rejected','closed') NOT NULL DEFAULT 'draft' COMMENT '状态',
  `published_at` datetime DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`location`),
  KEY `idx_job_type` (`job_type`),
  KEY `idx_education` (`education`),
  KEY `idx_experience` (`experience`),
  CONSTRAINT `fk_job_posting_company` FOREIGN KEY (`company_id`) REFERENCES `company_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 岗位福利关联表
CREATE TABLE `job_welfare` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `welfare_tag_id` bigint NOT NULL COMMENT '福利标签ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_job_welfare` (`job_id`,`welfare_tag_id`),
  KEY `idx_welfare_tag_id` (`welfare_tag_id`),
  CONSTRAINT `fk_job_welfare_job` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_job_welfare_tag` FOREIGN KEY (`welfare_tag_id`) REFERENCES `welfare_tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位福利关联表';

-- 岗位标签关联表
CREATE TABLE `job_tag_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `job_tag_id` bigint NOT NULL COMMENT '岗位标签ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_job_tag` (`job_id`,`job_tag_id`),
  KEY `idx_job_tag_id` (`job_tag_id`),
  CONSTRAINT `fk_job_tag_relation_job` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_job_tag_relation_tag` FOREIGN KEY (`job_tag_id`) REFERENCES `job_tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位标签关联表';

-- 应聘申请表
CREATE TABLE `application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `resume_id` bigint DEFAULT NULL COMMENT '简历文件ID',
  `cover_letter` text DEFAULT NULL COMMENT '求职信',
  `status` enum('pending','viewed','contacted','interviewed','accepted','rejected') NOT NULL DEFAULT 'pending' COMMENT '状态',
  `company_notes` text DEFAULT NULL COMMENT '企业备注',
  `applied_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_resume_id` (`resume_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_application_student` FOREIGN KEY (`student_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_application_job` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_application_resume` FOREIGN KEY (`resume_id`) REFERENCES `student_resume_file` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应聘申请表';

-- 企业收藏学生表
CREATE TABLE `student_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_company_student` (`company_id`,`student_id`),
  KEY `idx_student_id` (`student_id`),
  CONSTRAINT `fk_student_favorite_company` FOREIGN KEY (`company_id`) REFERENCES `company_profile` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_student_favorite_student` FOREIGN KEY (`student_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业收藏学生表';

-- 企业拉黑学生表
CREATE TABLE `student_blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `reason` varchar(255) DEFAULT NULL COMMENT '拉黑原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_company_student` (`company_id`,`student_id`),
  KEY `idx_student_id` (`student_id`),
  CONSTRAINT `fk_student_blacklist_company` FOREIGN KEY (`company_id`) REFERENCES `company_profile` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_student_blacklist_student` FOREIGN KEY (`student_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业拉黑学生表';

-- 岗位审核日志表
CREATE TABLE `job_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `counselor_id` bigint NOT NULL COMMENT '辅导员ID',
  `action` enum('approve','reject') NOT NULL COMMENT '操作',
  `reason` varchar(255) DEFAULT NULL COMMENT '原因',
  `timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
  PRIMARY KEY (`id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_counselor_id` (`counselor_id`),
  CONSTRAINT `fk_job_audit_job` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_job_audit_counselor` FOREIGN KEY (`counselor_id`) REFERENCES `counselor_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位审核日志表';

-- 企业认证审核日志表
CREATE TABLE `company_cert_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `counselor_id` bigint NOT NULL COMMENT '辅导员ID',
  `action` enum('approve','reject') NOT NULL COMMENT '操作',
  `reason` varchar(255) DEFAULT NULL COMMENT '原因',
  `timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_counselor_id` (`counselor_id`),
  CONSTRAINT `fk_company_audit_company` FOREIGN KEY (`company_id`) REFERENCES `company_profile` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_company_audit_counselor` FOREIGN KEY (`counselor_id`) REFERENCES `counselor_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业认证审核日志表';

-- 企业评价表
CREATE TABLE `rating` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `job_id` bigint DEFAULT NULL COMMENT '岗位ID',
  `overall_score` decimal(3,1) NOT NULL COMMENT '总体评分',
  `job_authenticity_score` decimal(3,1) NOT NULL COMMENT '岗位真实性评分',
  `interview_experience_score` decimal(3,1) NOT NULL COMMENT '面试体验评分',
  `work_environment_score` decimal(3,1) NOT NULL COMMENT '工作环境评分',
  `welfare_delivery_score` decimal(3,1) NOT NULL COMMENT '福利兑现评分',
  `comment` text NOT NULL COMMENT '评价内容',
  `is_anonymous` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否匿名',
  `status` enum('active','deleted') NOT NULL DEFAULT 'active' COMMENT '状态',
  `submitted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_rating_student` FOREIGN KEY (`student_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rating_company` FOREIGN KEY (`company_id`) REFERENCES `company_profile` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rating_job` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业评价表';

-- 举报表
CREATE TABLE `report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporting_user_id` bigint NOT NULL COMMENT '举报用户ID',
  `reported_item_type` enum('rating','message','chat_session') NOT NULL COMMENT '举报对象类型',
  `reported_item_id` bigint NOT NULL COMMENT '举报对象ID',
  `reason` text NOT NULL COMMENT '举报原因',
  `status` enum('pending','resolved') NOT NULL DEFAULT 'pending' COMMENT '状态',
  `counselor_id` bigint DEFAULT NULL COMMENT '处理辅导员ID',
  `resolution` varchar(255) DEFAULT NULL COMMENT '处理结果',
  `reported_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `resolved_at` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_reporting_user_id` (`reporting_user_id`),
  KEY `idx_reported_item` (`reported_item_type`,`reported_item_id`),
  KEY `idx_counselor_id` (`counselor_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_report_user` FOREIGN KEY (`reporting_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_report_counselor` FOREIGN KEY (`counselor_id`) REFERENCES `counselor_profile` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 聊天群组表
CREATE TABLE `chat_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '群组ID',
  `name` varchar(100) NOT NULL COMMENT '群组名称',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_created_by` (`created_by`),
  CONSTRAINT `fk_chat_group_user` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天群组表';

-- 聊天群组成员表
CREATE TABLE `chat_group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_group_student` (`group_id`,`student_id`),
  KEY `idx_student_id` (`student_id`),
  CONSTRAINT `fk_group_member_group` FOREIGN KEY (`group_id`) REFERENCES `chat_group` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_group_member_student` FOREIGN KEY (`student_id`) REFERENCES `student_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天群组成员表';

-- 聊天会话表
CREATE TABLE `chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `type` enum('SE','SC','SS') NOT NULL COMMENT '会话类型',
  `related_job_id` bigint DEFAULT NULL COMMENT '关联岗位ID',
  `group_id` bigint DEFAULT NULL COMMENT '群组ID',
  `participant1_id` bigint DEFAULT NULL COMMENT '参与者1ID',
  `participant2_id` bigint DEFAULT NULL COMMENT '参与者2ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_active_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  `status` enum('active','archived') NOT NULL DEFAULT 'active' COMMENT '状态',
  `is_readonly` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否只读',
  PRIMARY KEY (`id`),
  KEY `idx_related_job_id` (`related_job_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_participant1_id` (`participant1_id`),
  KEY `idx_participant2_id` (`participant2_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_chat_session_job` FOREIGN KEY (`related_job_id`) REFERENCES `job_posting` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_chat_session_group` FOREIGN KEY (`group_id`) REFERENCES `chat_group` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_chat_session_participant1` FOREIGN KEY (`participant1_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chat_session_participant2` FOREIGN KEY (`participant2_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `content_type` enum('text','image','file') NOT NULL DEFAULT 'text' COMMENT '内容类型',
  `file_path` varchar(255) DEFAULT NULL COMMENT '文件路径',
  `sent_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_sent_at` (`sent_at`),
  CONSTRAINT `fk_chat_message_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chat_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 未读消息计数表
CREATE TABLE `unread_message_count` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `count` int NOT NULL DEFAULT 0 COMMENT '未读数量',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_session_user` (`session_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_unread_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_unread_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='未读消息计数表';

-- 辅导员任务表
CREATE TABLE `counselor_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `type` enum('companyCertification','jobAudit','reportHandling') NOT NULL COMMENT '任务类型',
  `target_item_type` enum('Company','Job','Rating','ChatMessage','ChatSession') NOT NULL COMMENT '目标对象类型',
  `target_item_id` bigint NOT NULL COMMENT '目标对象ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` enum('pending','processing','completed') NOT NULL DEFAULT 'pending' COMMENT '状态',
  `counselor_id` bigint DEFAULT NULL COMMENT '处理辅导员ID',
  `notes` text DEFAULT NULL COMMENT '备注',
  `priority` enum('high','normal','low') NOT NULL DEFAULT 'normal' COMMENT '优先级',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_target_item` (`target_item_type`,`target_item_id`),
  KEY `idx_counselor_id` (`counselor_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`),
  KEY `idx_priority` (`priority`),
  CONSTRAINT `fk_task_counselor` FOREIGN KEY (`counselor_id`) REFERENCES `counselor_profile` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员任务表';

-- 辅导员操作日志表
CREATE TABLE `counselor_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `counselor_id` bigint NOT NULL COMMENT '辅导员ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `target_item_type` varchar(50) NOT NULL COMMENT '目标对象类型',
  `target_item_id` bigint NOT NULL COMMENT '目标对象ID',
  `result` varchar(50) NOT NULL COMMENT '操作结果',
  `reason` varchar(255) DEFAULT NULL COMMENT '原因',
  `timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
  PRIMARY KEY (`id`),
  KEY `idx_counselor_id` (`counselor_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_target_item` (`target_item_type`,`target_item_id`),
  KEY `idx_timestamp` (`timestamp`),
  CONSTRAINT `fk_operation_log_counselor` FOREIGN KEY (`counselor_id`) REFERENCES `counselor_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员操作日志表';

-- 初始数据
-- 插入默认的福利标签
INSERT INTO `welfare_tag` (`name`, `category`, `status`) VALUES 
('五险一金', '社保福利', 'active'),
('带薪年假', '休假福利', 'active'),
('定期团建', '企业文化', 'active'),
('免费工作餐', '生活福利', 'active'),
('节日福利', '生活福利', 'active'),
('弹性工作制', '工作制度', 'active'),
('专业培训', '成长福利', 'active'),
('绩效奖金', '薪酬福利', 'active'),
('股票期权', '薪酬福利', 'active'),
('住房补贴', '生活福利', 'active');

-- 插入常用岗位标签
INSERT INTO `job_tag` (`name`) VALUES 
('前端开发'),
('后端开发'),
('全栈开发'),
('移动开发'),
('数据分析'),
('人工智能'),
('机器学习'),
('云计算'),
('DevOps'),
('产品经理'),
('UI设计'),
('UX设计'),
('测试工程师'),
('运维工程师'),
('网络安全'),
('项目管理'),
('技术支持'),
('销售'),
('市场营销'),
('人力资源');

-- 插入常用技能标签
INSERT INTO `skill_tag` (`name`) VALUES 
('Java'),
('Python'),
('JavaScript'),
('TypeScript'),
('C++'),
('C#'),
('Go'),
('Rust'),
('PHP'),
('SQL'),
('HTML/CSS'),
('React'),
('Vue'),
('Angular'),
('Node.js'),
('Spring'),
('Django'),
('Flask'),
('Express'),
('Laravel'),
('MySQL'),
('PostgreSQL'),
('MongoDB'),
('Redis'),
('Docker'),
('Kubernetes'),
('Git'),
('Linux'),
('AWS'),
('Azure'),
('Google Cloud'),
('TensorFlow'),
('PyTorch'),
('Hadoop'),
('Spark'),
('Tableau'),
('PowerBI'),
('Excel'),
('Word'),
('PhotoShop'),
('Illustrator'),
('Figma'),
('Sketch'); 