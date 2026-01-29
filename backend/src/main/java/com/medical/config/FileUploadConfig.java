package com.medical.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 文件上传配置
 *
 * @author AI Medical Team
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    @Value("${file.upload-url-prefix:/uploads}")
    private String uploadUrlPrefix;

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取绝对路径
        String absolutePath;
        File path = new File(uploadPath);
        if (!path.isAbsolute()) {
            // 使用用户目录作为根目录
            String userDir = System.getProperty("user.dir");
            absolutePath = userDir + File.separator + uploadPath;
        } else {
            absolutePath = uploadPath;
        }

        // 确保上传目录存在
        File uploadDir = new File(absolutePath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 映射上传文件访问路径
        registry.addResourceHandler(uploadUrlPrefix + "/**")
                .addResourceLocations("file:" + absolutePath + "/");

        System.out.println("文件上传静态资源映射: " + uploadUrlPrefix + "/** -> file:" + absolutePath + "/");
    }
}
