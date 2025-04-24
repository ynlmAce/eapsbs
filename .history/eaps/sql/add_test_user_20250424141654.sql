-- 添加测试用户202106
INSERT INTO `user` (`username`, `password_hash`, `role`, `status`, `created_at`, `updated_at`) 
VALUES ('202106', '$2a$10$xPPCeSJ9mA9wDA1xkiFdKeeKXWKqUGGZWQSy3AZiRbW6UJ.rXdQZa', 'student', 'active', NOW(), NOW());

-- 添加学生档案
INSERT INTO `student_profile` (`user_id`, `name`, `contact`, `school`, `major`, `grade`, `self_introduction`, `job_preferences`, `behavior_score`, `created_at`, `updated_at`) 
VALUES ((SELECT id FROM `user` WHERE username = '202106'), '测试学生', '13800138001', '测试大学', '计算机科学', '大四', '自我介绍', '求职意向', 100, NOW(), NOW()); 