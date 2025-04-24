-- 大学生就业帮扶系统测试数据

-- 插入测试用户
INSERT INTO `user` (`username`, `password_hash`, `role`, `status`, `created_at`, `updated_at`) VALUES 
-- 学生用户
('student1', '$2a$10$xPPCeSJ9mA9wDA1xkiFdKeeKXWKqUGGZWQSy3AZiRbW6UJ.rXdQZa', 'student', 'active', NOW(), NOW()),
('student2', '$2a$10$xPPCeSJ9mA9wDA1xkiFdKeeKXWKqUGGZWQSy3AZiRbW6UJ.rXdQZa', 'student', 'active', NOW(), NOW()),
-- 企业用户
('company1', '$2a$10$xPPCeSJ9mA9wDA1xkiFdKeeKXWKqUGGZWQSy3AZiRbW6UJ.rXdQZa', 'company', 'active', NOW(), NOW()),
('company2', '$2a$10$xPPCeSJ9mA9wDA1xkiFdKeeKXWKqUGGZWQSy3AZiRbW6UJ.rXdQZa', 'company', 'active', NOW(), NOW()),
-- 辅导员用户
('counselor1', '$2a$10$xPPCeSJ9mA9wDA1xkiFdKeeKXWKqUGGZWQSy3AZiRbW6UJ.rXdQZa', 'counselor', 'active', NOW(), NOW());

-- 插入学生档案
INSERT INTO `student_profile` (`user_id`, `name`, `contact`, `school`, `major`, `grade`, `self_introduction`, `job_preferences`, `behavior_score`, `created_at`, `updated_at`) VALUES 
(1, '张三', '13800138001', '示范大学', '计算机科学与技术', '大四', '我是一名热爱编程的学生，曾参与多个项目开发...', '期望从事后端开发或全栈开发工作', 100, NOW(), NOW()),
(2, '李四', '13800138002', '示范大学', '软件工程', '大四', '专注于移动应用开发，具有良好的团队协作能力...', '期望从事移动端开发工作', 100, NOW(), NOW());

-- 添加学生技能标签关联
INSERT INTO `student_skill` (`student_profile_id`, `skill_tag_id`, `created_at`) VALUES 
(1, 1, NOW()), (1, 3, NOW()), (1, 18, NOW()), -- 张三: Java, JavaScript, Spring
(2, 4, NOW()), (2, 14, NOW()), (2, 17, NOW()); -- 李四: TypeScript, Vue, NodeJS

-- 插入企业档案
INSERT INTO `company_profile` (`user_id`, `name`, `unified_social_credit_code`, `industry`, `size`, `address`, `description`, `hr_contact`, `certification_status`, `certification_expiry_date`, `created_at`, `updated_at`) VALUES 
(3, '科技有限公司', '91110000100000001X', '互联网/软件开发', '100-499人', '北京市海淀区', '我们是一家专注于企业级软件开发的科技公司，致力于为企业提供全方位的技术解决方案...', 'hr@tech.com', 'certified', '2024-12-31', NOW(), NOW()),
(4, '创新科技有限公司', '91110000100000002X', '互联网/人工智能', '50-99人', '上海市浦东新区', '创新科技是一家人工智能领域的创新企业，专注于计算机视觉和自然语言处理技术的研发...', 'hr@innovation.com', 'certified', '2024-12-31', NOW(), NOW());

-- 插入辅导员档案
INSERT INTO `counselor_profile` (`user_id`, `employee_id`, `name`, `contact`, `created_at`, `updated_at`) VALUES 
(5, 'C001', '王辅导', '13900139000', NOW(), NOW());

-- 插入岗位信息
INSERT INTO `job_posting` (`company_id`, `title`, `description`, `requirement`, `location`, `salary`, `education`, `experience`, `job_type`, `headcount`, `work_time`, `contact_person`, `contact_method`, `show_contact`, `valid_until`, `status`, `published_at`, `created_at`, `updated_at`) VALUES 
(1, '后端开发工程师', '负责公司核心业务系统的后端开发与维护...', '1. 熟悉Java/Spring框架;\n2. 熟悉MySQL数据库;\n3. 良好的团队协作能力', '北京', '15k-25k', '本科', '1-3年', '全职', 2, '9:00-18:00, 周末双休', '张先生', 'hr@tech.com', 1, '2024-06-30', 'active', NOW(), NOW(), NOW()),
(1, '前端开发工程师', '负责公司产品的前端开发与维护...', '1. 熟悉HTML/CSS/JavaScript;\n2. 熟悉Vue或React框架;\n3. 良好的团队协作能力', '北京', '15k-20k', '本科', '1-3年', '全职', 1, '9:00-18:00, 周末双休', '张先生', 'hr@tech.com', 1, '2024-06-30', 'active', NOW(), NOW(), NOW()),
(2, 'AI算法工程师', '负责公司AI算法的研发与优化...', '1. 熟悉机器学习和深度学习算法;\n2. 熟悉Python/TensorFlow/PyTorch;\n3. 具有实际项目经验', '上海', '25k-35k', '硕士', '3-5年', '全职', 2, '9:30-18:30, 弹性工作制', '李女士', 'hr@innovation.com', 1, '2024-06-30', 'active', NOW(), NOW(), NOW());

-- 添加岗位标签关联
INSERT INTO `job_tag_relation` (`job_id`, `job_tag_id`, `created_at`) VALUES 
(1, 2, NOW()), (1, 3, NOW()), -- 后端开发, 全栈开发
(2, 1, NOW()), (2, 3, NOW()), -- 前端开发, 全栈开发
(3, 6, NOW()), (3, 7, NOW()); -- 人工智能, 机器学习

-- 添加岗位福利关联
INSERT INTO `job_welfare` (`job_id`, `welfare_tag_id`, `created_at`) VALUES 
(1, 1, NOW()), (1, 2, NOW()), (1, 3, NOW()), -- 五险一金, 带薪年假, 定期团建
(2, 1, NOW()), (2, 2, NOW()), (2, 3, NOW()),
(3, 1, NOW()), (3, 2, NOW()), (3, 6, NOW()), (3, 7, NOW()); -- 五险一金, 带薪年假, 弹性工作制, 专业培训

-- 插入应聘申请
INSERT INTO `application` (`student_id`, `job_id`, `status`, `applied_at`, `updated_at`) VALUES 
(1, 2, 'pending', NOW(), NOW()),
(2, 1, 'viewed', NOW(), NOW());

-- 插入企业评价
INSERT INTO `rating` (`student_id`, `company_id`, `job_id`, `overall_score`, `job_authenticity_score`, `interview_experience_score`, `work_environment_score`, `welfare_delivery_score`, `comment`, `is_anonymous`, `status`, `submitted_at`) VALUES 
(1, 1, 2, 4.5, 5, 4, 4.5, 4, '面试流程专业，工作环境良好，团队氛围友好...', 1, 'active', NOW()),
(2, 2, 3, 4.0, 4, 4, 4, 4, '面试体验不错，算法题目有一定挑战性，但面试官很友善...', 0, 'active', NOW());

-- 创建聊天会话
INSERT INTO `chat_session` (`type`, `related_job_id`, `participant1_id`, `participant2_id`, `created_at`, `last_active_at`, `status`, `is_readonly`) VALUES 
('SE', 2, 1, 3, NOW(), NOW(), 'active', 0), -- 张三与科技有限公司关于前端开发岗位的会话
('SC', NULL, 2, 5, NOW(), NOW(), 'active', 0); -- 李四与王辅导的会话

-- 添加聊天消息
INSERT INTO `chat_message` (`session_id`, `sender_id`, `content`, `content_type`, `sent_at`, `is_read`) VALUES 
(1, 1, '您好，我对贵公司的前端开发岗位很感兴趣，想了解一下工作内容和技术栈。', 'text', NOW(), 1),
(1, 3, '您好，我们的前端开发主要使用Vue3框架，负责公司产品的界面开发和优化。欢迎您投递简历！', 'text', NOW(), 0),
(2, 2, '王老师您好，我想咨询一下关于实习证明的问题。', 'text', NOW(), 1),
(2, 5, '你好，实习证明需要企业盖章，然后提交到就业指导中心。', 'text', NOW(), 0);

-- 添加未读消息计数
INSERT INTO `unread_message_count` (`session_id`, `user_id`, `count`, `updated_at`) VALUES 
(1, 1, 1, NOW()),
(2, 2, 1, NOW());

-- 添加辅导员任务
INSERT INTO `counselor_task` (`type`, `target_item_type`, `target_item_id`, `created_at`, `status`, `priority`) VALUES 
('jobAudit', 'Job', 3, NOW(), 'pending', 'normal'); 