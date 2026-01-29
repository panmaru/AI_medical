package com.medical.controller;

import com.medical.common.Result;
import com.medical.service.SparkAiService;
import com.medical.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 皮肤图片分析控制器
 *
 * @author AI Medical Team
 */
@Slf4j
@RestController
@RequestMapping("/skin-analysis")
@RequiredArgsConstructor
public class SkinAnalysisController {

    private final SparkAiService sparkAiService;
    private final FileUploadUtil fileUploadUtil;

    /**
     * 上传皮肤图片并进行分析
     */
    @PostMapping("/analyze")
    public Result<Map<String, Object>> analyzeSkinImages(
            @RequestParam("patientId") Long patientId,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "description", required = false) String description) {
        try {
            log.info("收到皮肤图片分析请求，患者ID: {}, 图片数量: {}", patientId, images.size());

            // 上传图片
            List<String> imageUrls = fileUploadUtil.uploadImages(images);
            log.info("图片上传成功，URLs: {}", imageUrls);

            // 调用AI分析
            Map<String, Object> analysisResult = sparkAiService.analyzeSkinImage(patientId, imageUrls, description);

            return Result.success("分析成功", analysisResult);

        } catch (Exception e) {
            log.error("皮肤图片分析失败", e);
            return Result.error("分析失败: " + e.getMessage());
        }
    }

    /**
     * 单独上传图片
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile image) {
        try {
            log.info("收到图片上传请求，文件名: {}, 大小: {}", image.getOriginalFilename(), image.getSize());

            String imageUrl = fileUploadUtil.uploadImage(image);

            Map<String, String> result = new HashMap<>();
            result.put("url", imageUrl);

            return Result.success("上传成功", result);

        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/upload/batch")
    public Result<Map<String, Object>> uploadImages(@RequestParam("images") List<MultipartFile> images) {
        try {
            log.info("收到批量图片上传请求，图片数量: {}", images.size());

            List<String> imageUrls = fileUploadUtil.uploadImages(images);

            Map<String, Object> result = new HashMap<>();
            result.put("urls", imageUrls);
            result.put("count", imageUrls.size());

            return Result.success("上传成功", result);

        } catch (Exception e) {
            log.error("批量图片上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
