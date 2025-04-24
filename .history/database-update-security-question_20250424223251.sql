-- 给用户表添加密保问题和密保答案字段

USE employment_support_system;

-- 添加密保问题和密保答案字段
ALTER TABLE `user` 
ADD COLUMN `security_question` varchar(200) DEFAULT NULL COMMENT '密保问题' AFTER `password_hash`,
ADD COLUMN `security_answer_hash` varchar(100) DEFAULT NULL COMMENT '密保答案的哈希值' AFTER `security_question`;

-- 更新现有账户的密保问题（为测试方便）
UPDATE `user` SET 
`security_question` = '您的出生地是？',
`security_answer_hash` = '$2a$10$qMJHd1wYIDvySEyrvGZNQO3whFpj5VLECplbLAkqTLIN0.wRVRVwe' -- 答案是"北京"加密后的哈希值
WHERE `username` IN ('student123', '91110108MA01GYT44K', 'counselor456'); 