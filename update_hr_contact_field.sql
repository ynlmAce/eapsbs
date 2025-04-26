-- 使用数据库
USE employment_support_system;

-- 更新company_profile表中的hrContact字段类型为JSON类型
ALTER TABLE company_profile 
MODIFY COLUMN hr_contact JSON COMMENT 'HR联系信息，JSON格式';

-- 处理现有数据，将字符串值转换为JSON格式
UPDATE company_profile
SET hr_contact = JSON_OBJECT('name', hr_contact, 'phone', '', 'email', '', 'workTime', '')
WHERE hr_contact IS NOT NULL AND JSON_VALID(hr_contact) = 0;

-- 对于已经是有效JSON的数据，不需要转换 