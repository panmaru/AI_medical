package com.medical.controller;

import com.medical.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 *
 * @author AI Medical Team
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    @Value("${file.upload-url-prefix:/uploads}")
    private String uploadUrlPrefix;

    /**
     * 上传头像
     */
    @PostMapping("/upload/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            return Result.error("只能上传图片文件");
        }

        // 验证文件大小（5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Result.error("图片大小不能超过5MB");
        }

        try {
            // 创建上传目录
            File uploadDir = new File(uploadPath + "/avatars");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(uploadDir.getAbsolutePath(), filename);
            Files.write(filePath, file.getBytes());

            // 返回文件访问URL
            String fileUrl = uploadUrlPrefix + "/avatars/" + filename;

            Map<String, String> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("filename", filename);

            return Result.success("上传成功", data);

        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
