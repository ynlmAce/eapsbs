package com.bs.eaps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

/**
 * Web配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.base.dir:D:/uploads}")
    private String fileBaseDir;

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    /**
     * 配置静态资源映射
     * 将本地文件目录映射为Web可访问的路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保目录以"/"结尾
        String dir = fileBaseDir.endsWith("/") ? fileBaseDir : fileBaseDir + "/";

        // 将/uploads/**映射到本地文件系统
        registry.addResourceHandler(uploadPath + "/**")
                .addResourceLocations("file:" + dir);

        // 输出日志
        System.out.println("静态资源映射: " + uploadPath + "/** -> " + "file:" + dir);

        // 检查目录是否存在，如果不存在则创建
        File uploadDir = new File(dir);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            System.out.println("创建上传目录: " + dir + (created ? " 成功" : " 失败"));
        }
    }
}