package com.bs.eaps.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

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
        return new StandardServletMultipartResolver();
    }

    /**
     * 配置Multipart参数
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // 设置最大文件大小
        DataSize maxSize;
        if (maxFileSize.toUpperCase().endsWith("KB")) {
            maxSize = DataSize.ofKilobytes(
                    Long.parseLong(maxFileSize.substring(0, maxFileSize.length() - 2)));
        } else if (maxFileSize.toUpperCase().endsWith("MB")) {
            maxSize = DataSize.ofMegabytes(
                    Long.parseLong(maxFileSize.substring(0, maxFileSize.length() - 2)));
        } else if (maxFileSize.toUpperCase().endsWith("GB")) {
            maxSize = DataSize.ofGigabytes(
                    Long.parseLong(maxFileSize.substring(0, maxFileSize.length() - 2)));
        } else {
            // 默认10MB
            maxSize = DataSize.ofMegabytes(10);
        }

        factory.setMaxFileSize(maxSize);
        // 设置最大请求大小，为文件大小的2倍
        factory.setMaxRequestSize(DataSize.ofBytes(maxSize.toBytes() * 2));

        return factory.createMultipartConfig();
    }
}