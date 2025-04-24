package com.bs.eaps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.File;

/**
 * 文件上传配置类
 */
@Configuration
public class FileUploadConfig {

    @Value("${file.upload.path:/tmp/uploads}")
    private String fileUploadPath;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    /**
     * 初始化上传目录
     */
    @Bean
    public void initUploadDirectory() {
        File uploadDir = new File(fileUploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    /**
     * 配置文件上传解析器
     */
    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");

        // 解析maxFileSize（例如："10MB"转为字节数）
        String size = maxFileSize.toUpperCase();
        long bytes = 10 * 1024 * 1024; // 默认10MB

        if (size.endsWith("KB")) {
            bytes = Long.parseLong(size.substring(0, size.length() - 2)) * 1024;
        } else if (size.endsWith("MB")) {
            bytes = Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024;
        } else if (size.endsWith("GB")) {
            bytes = Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024;
        }

        resolver.setMaxUploadSize(bytes);
        resolver.setMaxUploadSizePerFile(bytes);
        resolver.setMaxInMemorySize(4096);

        return resolver;
    }
}