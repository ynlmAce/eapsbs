package com.bs.eaps.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.File;

/**
 * 文件上传配置
 */
@Slf4j
@Configuration
public class FileUploadConfig {

    @Value("${file.upload.path:/tmp/uploads}")
    private String fileUploadPath;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    @PostConstruct
    public void init() {
        try {
            log.info("初始化文件上传配置，上传路径：{}，最大文件大小：{}", fileUploadPath, maxFileSize);

            // 创建基础上传目录
            File uploadDir = new File(fileUploadPath);
            if (!uploadDir.exists()) {
                if (!uploadDir.mkdirs()) {
                    log.error("创建基础上传目录失败: {}", uploadDir.getAbsolutePath());
                    throw new RuntimeException("无法创建上传目录: " + uploadDir.getAbsolutePath());
                } else {
                    log.info("已创建基础上传目录: {}", uploadDir.getAbsolutePath());
                }
            } else {
                log.info("基础上传目录已存在: {}", uploadDir.getAbsolutePath());
            }

            // 预创建常用目录结构
            createSubDir(fileUploadPath, "resumes"); // 简历目录
            createSubDir(fileUploadPath, "company_logos"); // 企业logo目录
            createSubDir(fileUploadPath, "company_licenses"); // 企业证书目录
            createSubDir(fileUploadPath, "chat_files"); // 聊天文件目录

            // 确保目录有正确的读写权限
            ensureDirectoryPermissions(uploadDir);

            log.info("文件上传目录初始化完成");
        } catch (Exception e) {
            log.error("初始化文件上传目录失败", e);
            // 不要抛出运行时异常，只记录错误，让应用继续启动
            // 这样即使目录创建失败，也可以在实际使用时再次尝试创建
        }
    }

    /**
     * 创建子目录
     * 
     * @param basePath   基础路径
     * @param subDirName 子目录名
     */
    private void createSubDir(String basePath, String subDirName) {
        File subDir = new File(basePath, subDirName);
        if (!subDir.exists()) {
            if (subDir.mkdirs()) {
                log.info("已创建子目录: {}", subDir.getAbsolutePath());
            } else {
                log.error("创建子目录失败: {}", subDir.getAbsolutePath());
            }
        } else {
            log.info("子目录已存在: {}", subDir.getAbsolutePath());
        }
    }

    /**
     * 确保目录有正确的读写权限
     * 
     * @param dir 目录
     */
    private void ensureDirectoryPermissions(File dir) {
        try {
            if (!dir.canWrite() || !dir.canRead()) {
                boolean setWritable = dir.setWritable(true, false);
                boolean setReadable = dir.setReadable(true, false);
                boolean setExecutable = dir.setExecutable(true, false); // 需要执行权限才能列出目录内容

                log.info("设置目录权限结果 - 读:{}, 写:{}, 执行:{} - 路径: {}",
                        setReadable, setWritable, setExecutable, dir.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("设置目录权限时出错: {}", dir.getAbsolutePath(), e);
        }
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}