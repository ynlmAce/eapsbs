package com.bs.eaps.common;

/**
 * 系统常量类
 */
public class Constants {

    /**
     * 角色常量
     */
    public static class Role {
        /**
         * 学生角色
         */
        public static final String STUDENT = "STUDENT";

        /**
         * 企业角色
         */
        public static final String COMPANY = "COMPANY";

        /**
         * 辅导员角色
         */
        public static final String COUNSELOR = "COUNSELOR";
    }

    /**
     * 认证状态常量
     */
    public static class CertificationStatus {
        /**
         * 待认证
         */
        public static final String PENDING = "待认证";

        /**
         * 已认证
         */
        public static final String CERTIFIED = "已认证";

        /**
         * 认证失败
         */
        public static final String FAILED = "认证失败";

        /**
         * 已过期
         */
        public static final String EXPIRED = "已过期";
    }

    /**
     * 岗位状态常量
     */
    public static class JobStatus {
        /**
         * 草稿
         */
        public static final String DRAFT = "draft";

        /**
         * 待审核
         */
        public static final String PENDING = "pending";

        /**
         * 招聘中
         */
        public static final String RECRUITING = "active";

        /**
         * 已驳回
         */
        public static final String REJECTED = "rejected";

        /**
         * 已结束
         */
        public static final String ENDED = "closed";
    }

    /**
     * 申请状态常量
     */
    public static class ApplicationStatus {
        /**
         * 已投递
         */
        public static final String APPLIED = "已投递";

        /**
         * 企业已查看
         */
        public static final String VIEWED = "企业已查看";

        /**
         * 已邀请面试
         */
        public static final String INVITED = "已邀请面试";

        /**
         * 已拒绝
         */
        public static final String REJECTED = "已拒绝";
    }

    /**
     * 评价状态常量
     */
    public static class RatingStatus {
        /**
         * 有效
         */
        public static final String ACTIVE = "Active";

        /**
         * 已删除
         */
        public static final String DELETED = "Deleted";
    }

    /**
     * 会话类型常量
     */
    public static class ChatSessionType {
        /**
         * 学生-企业
         */
        public static final String STUDENT_COMPANY = "SE";

        /**
         * 学生-辅导员
         */
        public static final String STUDENT_COUNSELOR = "SC";

        /**
         * 学生群组
         */
        public static final String STUDENT_GROUP = "SS";
    }

    /**
     * 会话状态常量
     */
    public static class ChatSessionStatus {
        /**
         * 活跃
         */
        public static final String ACTIVE = "活跃";

        /**
         * 归档
         */
        public static final String ARCHIVED = "归档";
    }

    /**
     * 消息类型常量
     */
    public static class MessageType {
        /**
         * 文本
         */
        public static final String TEXT = "text";

        /**
         * 图片
         */
        public static final String IMAGE = "image";

        /**
         * 文件
         */
        public static final String FILE = "file";
    }

    /**
     * 任务类型常量
     */
    public static class TaskType {
        /**
         * 企业认证
         */
        public static final String COMPANY_CERTIFICATION = "companyCertification";

        /**
         * 岗位审核
         */
        public static final String JOB_AUDIT = "jobAudit";

        /**
         * 举报处理
         */
        public static final String REPORT_HANDLING = "reportHandling";
    }

    /**
     * 任务优先级常量
     */
    public static class TaskPriority {
        /**
         * 高
         */
        public static final String HIGH = "high";

        /**
         * 普通
         */
        public static final String NORMAL = "normal";

        /**
         * 低
         */
        public static final String LOW = "low";
    }

    /**
     * 用户状态
     */
    public static class UserStatus {
        /**
         * 活跃
         */
        public static final String ACTIVE = "active";

        /**
         * 非活跃
         */
        public static final String INACTIVE = "inactive";

        /**
         * 锁定
         */
        public static final String LOCKED = "locked";
    }

    /**
     * 文件类型
     */
    public static class FileType {
        /**
         * 简历
         */
        public static final String RESUME = "resume";

        /**
         * 营业执照
         */
        public static final String COMPANY_LICENSE = "company_license";

        /**
         * 企业logo
         */
        public static final String COMPANY_LOGO = "company_logo";

        /**
         * 聊天文件
         */
        public static final String CHAT_FILE = "chat_file";
    }
}