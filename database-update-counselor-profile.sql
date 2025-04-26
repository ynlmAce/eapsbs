-- 扩展辅导员档案表,增加更多个人信息字段

USE employment_support_system;

-- 增加辅导员档案表的额外字段
ALTER TABLE `counselor_profile` 
ADD COLUMN `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱' AFTER `contact`,
ADD COLUMN `college` varchar(100) DEFAULT NULL COMMENT '所属学院' AFTER `email`,
ADD COLUMN `title` varchar(50) DEFAULT NULL COMMENT '职称' AFTER `college`,
ADD COLUMN `specialization` varchar(100) DEFAULT NULL COMMENT '专业方向' AFTER `title`,
ADD COLUMN `experience` int DEFAULT 0 COMMENT '带队经验(年)' AFTER `specialization`,
ADD COLUMN `office_location` varchar(100) DEFAULT NULL COMMENT '办公室地点' AFTER `experience`,
ADD COLUMN `office_hours` varchar(100) DEFAULT NULL COMMENT '办公时间' AFTER `office_location`,
ADD COLUMN `introduction` text DEFAULT NULL COMMENT '个人简介' AFTER `office_hours`; 