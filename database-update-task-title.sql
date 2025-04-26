-- 添加title字段到counselor_task表
ALTER TABLE `counselor_task` 
ADD COLUMN `title` VARCHAR(255) NULL COMMENT '任务标题' AFTER `target_item_id`;

-- 更新现有记录的title字段
-- 为companyCertification类型的任务设置标题
UPDATE `counselor_task` 
SET `title` = CONCAT('企业认证 - ', 
                     (SELECT `name` FROM `company_profile` WHERE `id` = `counselor_task`.`target_item_id`)) 
WHERE `type` = 'companyCertification';

-- 为jobAudit类型的任务设置标题
UPDATE `counselor_task` 
SET `title` = CONCAT('岗位审核 - ', 
                     (SELECT `title` FROM `job_posting` WHERE `id` = `counselor_task`.`target_item_id`)) 
WHERE `type` = 'jobAudit';

-- 为reportHandling类型的任务设置标题
UPDATE `counselor_task` 
SET `title` = CONCAT('举报处理 - ', 
                     CASE `target_item_type` 
                         WHEN 'Rating' THEN '评价' 
                         WHEN 'ChatMessage' THEN '消息' 
                         ELSE `target_item_type` 
                     END) 
WHERE `type` = 'reportHandling'; 