package com.employmentsupport.eaps.common;

/**
 * 系统常量
 */
public class Constants {

    /**
     * 用户角色
     */
    public static class Role {
        public static final String STUDENT = "student";
        public static final String COMPANY = "company";
        public static final String COUNSELOR = "counselor";
    }

    /**
     * 用户状态
     */
    public static class UserStatus {
        public static final String ACTIVE = "active";
        public static final String INACTIVE = "inactive";
        public static final String LOCKED = "locked";
    }

    /**
     * 文件类型
     */
    public static class FileType {
        public static final String RESUME = "resume";
        public static final String COMPANY_LICENSE = "company_license";
        public static final String COMPANY_LOGO = "company_logo";
        public static final String CHAT_FILE = "chat_file";
    }

    /**
     * 聊天会话类型
     */
    public static class ChatSessionType {
        public static final String STUDENT_COMPANY = "SE";
        public static final String STUDENT_COUNSELOR = "SC";
        public static final String STUDENT_GROUP = "SS";
    }

    /**
     * 申请状态
     */
    public static class ApplicationStatus {
        public static final String SUBMITTED = "submitted";
        public static final String VIEWED = "viewed";
        public static final String INVITED = "invited";
        public static final String REJECTED = "rejected";
    }

    /**
     * 岗位状态
     */
    public static class JobStatus {
        public static final String DRAFT = "draft";
        public static final String PENDING = "pending";
        public static final String ACTIVE = "active";
        public static final String REJECTED = "rejected";
        public static final String CLOSED = "closed";
    }

    /**
     * 认证状态
     */
    public static class CertificationStatus {
        public static final String PENDING = "pending";
        public static final String CERTIFIED = "certified";
        public static final String REJECTED = "rejected";
        public static final String EXPIRED = "expired";
    }
}