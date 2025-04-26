-- 更新用户表，将哈希密码和密保答案改为明文
USE employment_support_system;

-- 先创建一个备份表，以防万一
CREATE TABLE user_backup_with_hash AS SELECT * FROM user;

-- 将所有用户的密码更改为简单明文密码
UPDATE user SET password_hash = '123456' WHERE 1=1;

-- 将所有用户的密保答案更改为明文
UPDATE user SET security_answer_hash = '北京' WHERE 1=1;

-- 如果需要还原，可以使用以下语句
-- INSERT INTO user SELECT * FROM user_backup_with_hash WHERE id NOT IN (SELECT id FROM user);
-- 或者
-- UPDATE user u JOIN user_backup_with_hash b ON u.id = b.id SET u.password_hash = b.password_hash, u.security_answer_hash = b.security_answer_hash; 