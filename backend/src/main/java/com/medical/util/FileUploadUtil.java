package com.medical.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传工具类
 *
 * @author AI Medical Team
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    @Value("${file.upload-url-prefix:/uploads}")
    private String uploadUrlPrefix;

    @Value("${file.max-size:5MB}")
    private String maxSize;

    private String absoluteUploadPath;

    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");

    /**
     * 初始化上传目录
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // 如果是相对路径，转换为绝对路径
        File path = new File(uploadPath);
        if (!path.isAbsolute()) {
            // 使用用户目录作为根目录
            String userDir = System.getProperty("user.dir");
            absoluteUploadPath = userDir + File.separator + uploadPath;
        } else {
            absoluteUploadPath = uploadPath;
        }

        // 确保上传目录存在
        File uploadDir = new File(absoluteUploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        log.info("文件上传目录初始化完成: {}", absoluteUploadPath);
    }

    /**
     * 上传单个图片文件
     *
     * @param file 文件
     * @return 文件访问URL
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // 验证文件
        validateImageFile(file);

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;

        // 创建目录路径：uploads/2025/01/29/
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = "images/" + datePath;
        String fullUploadPath = absoluteUploadPath + File.separator + relativePath;
        String fullPath = fullUploadPath + File.separator + newFilename;

        // 确保目录存在
        Path path = Paths.get(fullUploadPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        // 保存文件
        file.transferTo(new File(fullPath));

        // 返回访问URL
        String fileUrl = uploadUrlPrefix + "/" + relativePath + "/" + newFilename;
        log.info("文件上传成功: {}", fileUrl);

        return fileUrl;
    }

    /**
     * 上传多个图片文件
     *
     * @param files 文件列表
     * @return 文件访问URL列表
     */
    public List<String> uploadImages(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadImage(file);
            urls.add(url);
        }
        return urls;
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("只支持以下图片格式：JPG、JPEG、PNG、GIF、WEBP");
        }

        // 检查文件大小
        long maxSizeBytes = parseSize(maxSize);
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("文件大小不能超过" + maxSize);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "jpg";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 解析文件大小
     */
    private long parseSize(String size) {
        if (size == null || size.isEmpty()) {
            return 5 * 1024 * 1024; // 默认5MB
        }

        size = size.toUpperCase().trim();
        long multiplier = 1;

        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        }

        try {
            return Long.parseLong(size) * multiplier;
        } catch (NumberFormatException e) {
            return 5 * 1024 * 1024; // 默认5MB
        }
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return false;
            }

            // 从URL提取文件路径
            String relativePath = fileUrl.replace(uploadUrlPrefix, "");
            String fullPath = absoluteUploadPath + relativePath.replace("/", File.separator);

            File file = new File(fullPath);
            if (file.exists() && file.delete()) {
                log.info("文件删除成功: {}", fullPath);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("删除文件失败: {}", fileUrl, e);
            return false;
        }
    }
}
