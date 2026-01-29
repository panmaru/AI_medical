package com.medical.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.medical.entity.DiagnosisRecord;
import com.medical.entity.Patient;
import com.medical.mapper.DiagnosisRecordMapper;
import com.medical.mapper.PatientMapper;
import com.medical.service.SparkAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;

/**
 * 智谱AI服务实现类 - 使用GLM-4
 *
 * @author AI Medical Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SparkAiServiceImpl implements SparkAiService {

    @Value("${zhipu.api.key}")
    private String zhipuApiKey;

    @Value("${zhipu.api.model}")
    private String model;

    @Value("${zhipu.api.url}")
    private String apiUrl;

    private final DiagnosisRecordMapper diagnosisRecordMapper;
    private final PatientMapper patientMapper;

    @Override
    public String chat(String message, String sessionId) {
        try {
            // 构建对话提示词
            String prompt = buildChatPrompt(message);

            // 调用智谱AI API
            return callZhipuApi(prompt);

        } catch (Exception e) {
            log.error("AI对话失败", e);
            return "抱歉，我暂时无法回答您的问题。错误：" + e.getMessage();
        }
    }

    @Override
    public DiagnosisRecord saveDiagnosisRecord(DiagnosisRecord record) {
        diagnosisRecordMapper.insert(record);
        return record;
    }

    /**
     * 构建对话提示词
     */
    private String buildChatPrompt(String message) {
        return "你是一位专业医生助手，请回答患者的医疗咨询。回答要专业、准确、易懂。\n\n" +
                "患者问题：" + message + "\n\n" +
                "请提供专业的医疗建议，如果问题涉及紧急情况，请建议患者立即就医。";
    }

    /**
     * 调用智谱AI API
     */
    private String callZhipuApi(String prompt) {
        try {
            log.info("调用智谱AI API，问题：{}", prompt);

            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", prompt);
            messages.add(msg);

            params.put("messages", messages);

            log.info("智谱AI请求参数：{}", JSONUtil.toJsonStr(params));

            // 发送HTTP POST请求
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + zhipuApiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(params))
                    .timeout(60000)  // 60秒超时
                    .execute();

            String responseBody = response.body();
            log.info("智谱AI响应：{}", responseBody);

            // 解析响应
            return parseZhipuResponse(responseBody);

        } catch (Exception e) {
            log.error("调用智谱AI失败", e);
            throw new RuntimeException("API调用失败: " + e.getMessage());
        }
    }

    /**
     * 解析智谱AI响应
     */
    private String parseZhipuResponse(String responseBody) {
        try {
            JSONObject response = JSONUtil.parseObj(responseBody);

            // 检查是否有错误
            if (response.containsKey("error")) {
                JSONObject error = response.getJSONObject("error");
                String message = error.getStr("message");
                log.error("智谱AI返回错误: {}", message);
                throw new RuntimeException("智谱AI错误: " + message);
            }

            // 获取响应内容 - 使用Hutool的工具方法
            if (response.containsKey("choices")) {
                cn.hutool.json.JSONArray choices = response.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    if (choice != null && choice.containsKey("message")) {
                        JSONObject msg = choice.getJSONObject("message");
                        if (msg != null && msg.containsKey("content")) {
                            String content = msg.getStr("content");
                            if (content != null && !content.isEmpty()) {
                                return content;
                            }
                        }
                    }
                }
            }

            log.warn("无法解析智谱AI响应，原始响应：{}", responseBody);
            return "抱歉，AI回复解析失败。";

        } catch (Exception e) {
            log.error("解析智谱AI响应失败", e);
            throw new RuntimeException("响应解析失败: " + e.getMessage());
        }
    }

    /**
     * 生成诊断编号
     */
    private String generateRecordNo() {
        return "DR" + System.currentTimeMillis() + IdUtil.randomUUID().substring(0, 4).toUpperCase();
    }

    @Override
    public Map<String, Object> analyzeSkinImage(Long patientId, java.util.List<String> imageUrls, String description) {
        try {
            // 查询患者信息
            Patient patient = patientMapper.selectById(patientId);
            if (patient == null) {
                throw new RuntimeException("患者不存在");
            }

            // 构建AI皮肤分析提示词
            String prompt = buildSkinImageAnalysisPrompt(patient, description);

            // 调用GLM-4V API进行图片分析
            String aiResponse = callZhipuVisionApi(prompt, imageUrls);

            // 解析AI响应
            Map<String, Object> analysisResult = parseSkinAnalysisResponse(aiResponse);

            // 保存诊断记录
            DiagnosisRecord record = new DiagnosisRecord();
            record.setRecordNo(generateRecordNo());
            record.setPatientId(patient.getId());
            record.setPatientName(patient.getName());
            record.setChiefComplaint("皮肤图片分析");
            record.setPresentIllness(description);
            record.setImageUrls(JSONUtil.toJsonStr(imageUrls));
            record.setAiDiagnosis(JSONUtil.toJsonStr(analysisResult));
            record.setAiSuggestion((String) analysisResult.get("suggestion"));
            record.setDiagnosisType(0);
            record.setStatus(0);

            diagnosisRecordMapper.insert(record);

            analysisResult.put("recordId", record.getId());
            analysisResult.put("recordNo", record.getRecordNo());
            analysisResult.put("imageUrls", imageUrls);

            return analysisResult;

        } catch (Exception e) {
            log.error("AI皮肤图片分析失败", e);
            throw new RuntimeException("AI皮肤图片分析失败: " + e.getMessage());
        }
    }

    /**
     * 构建皮肤图片分析提示词
     */
    private String buildSkinImageAnalysisPrompt(Patient patient, String description) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位经验丰富的皮肤科医生，请仔细分析上传的皮肤病变图片。\n\n");

        prompt.append("【患者基本信息】\n");
        prompt.append("姓名：").append(patient.getName()).append("\n");
        prompt.append("性别：").append(patient.getGender() == 1 ? "男" : "女").append("\n");
        prompt.append("年龄：").append(patient.getAge()).append("岁\n\n");

        if (StringUtils.hasText(description)) {
            prompt.append("【患者描述】\n").append(description).append("\n\n");
        }

        prompt.append("请提供以下信息（以JSON格式返回）：\n");
        prompt.append("{\n");
        prompt.append("  \"possibleDiseases\": [\n");
        prompt.append("    {\"name\": \"疾病名称\", \"confidence\": 置信度(0-1之间的小数)}\n");
        prompt.append("  ],\n");
        prompt.append("  \"features\": \"观察到的皮肤特征描述\",\n");
        prompt.append("  \"severity\": \"严重程度(轻微/中度/严重)\",\n");
        prompt.append("  \"suggestion\": \"治疗建议和注意事项\",\n");
        prompt.append("  \"needDoctor\": \"是否需要就医(true/false)\",\n");
        prompt.append("  \"urgency\": \"紧急程度(不需要/一般/紧急/非常紧急)\"\n");
        prompt.append("}\n\n");

        prompt.append("请仔细观察图片中的：\n");
        prompt.append("1. 皮损的形态（红斑、丘疹、水疱、鳞屑等）\n");
        prompt.append("2. 皮损的分布和范围\n");
        prompt.append("3. 颜色变化\n");
        prompt.append("4. 有无渗出、结痂等\n");
        prompt.append("5. 皮肤纹理变化\n\n");

        return prompt.toString();
    }

    /**
     * 调用智谱AI视觉API (GLM-4V)
     */
    private String callZhipuVisionApi(String prompt, List<String> imageUrls) {
        try {
            log.info("调用智谱AI视觉API，图片数量：{}", imageUrls.size());

            // 构建多模态消息
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", "user");

            // 构建content数组，包含文本和图片
            List<Map<String, Object>> content = new ArrayList<>();

            // 添加文本
            Map<String, Object> textItem = new HashMap<>();
            textItem.put("type", "text");
            textItem.put("text", prompt);
            content.add(textItem);

            // 添加图片（转换为Base64）
            for (String imageUrl : imageUrls) {
                Map<String, Object> imageItem = new HashMap<>();
                imageItem.put("type", "image_url");

                // 将本地文件转换为Base64
                String base64Image = convertImageToBase64(imageUrl);

                Map<String, String> imageUrlObj = new HashMap<>();
                imageUrlObj.put("url", base64Image);
                imageItem.put("image_url", imageUrlObj);
                content.add(imageItem);
            }

            msg.put("content", content);
            messages.add(msg);

            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("model", "glm-4v"); // 使用GLM-4V模型
            params.put("messages", messages);

            log.info("智谱AI视觉请求参数：{}", JSONUtil.toJsonStr(params));

            // 发送HTTP POST请求
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + zhipuApiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(params))
                    .timeout(60000)  // 60秒超时
                    .execute();

            String responseBody = response.body();
            log.info("智谱AI视觉响应：{}", responseBody);

            // 解析响应
            return parseZhipuResponse(responseBody);

        } catch (Exception e) {
            log.error("调用智谱AI视觉失败", e);
            throw new RuntimeException("AI视觉API调用失败: " + e.getMessage());
        }
    }

    /**
     * 将图片文件转换为Base64编码
     */
    private String convertImageToBase64(String imageUrl) {
        try {
            // 从URL中提取相对路径
            String relativePath = imageUrl.replace("/uploads", "");

            // 构建实际文件路径
            String userDir = System.getProperty("user.dir");
            String uploadPath = userDir + File.separator + "uploads";
            String fullPath = uploadPath + relativePath.replace("/", File.separator);

            // 读取文件
            File file = new File(fullPath);
            if (!file.exists()) {
                log.error("图片文件不存在: {}", fullPath);
                throw new RuntimeException("图片文件不存在: " + imageUrl);
            }

            // 转换为Base64
            byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
            String base64 = java.util.Base64.getEncoder().encodeToString(fileContent);

            // 获取文件扩展名
            String extension = getFileExtension(file.getName());
            String mimeType = "image/" + extension;

            // 返回Base64格式的URL
            return "data:" + mimeType + ";base64," + base64;

        } catch (Exception e) {
            log.error("图片转Base64失败: {}", imageUrl, e);
            throw new RuntimeException("图片处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpeg";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "jpeg";
        }
        String ext = filename.substring(lastDotIndex + 1).toLowerCase();
        // 处理特殊格式
        if (ext.equals("jpg")) {
            return "jpeg";
        }
        return ext;
    }

    /**
     * 解析皮肤分析响应
     */
    private Map<String, Object> parseSkinAnalysisResponse(String aiResponse) {
        try {
            // 尝试解析为JSON
            if (aiResponse.contains("{") && aiResponse.contains("}")) {
                // 提取JSON部分
                int start = aiResponse.indexOf("{");
                int end = aiResponse.lastIndexOf("}") + 1;
                String jsonStr = aiResponse.substring(start, end);

                Map<String, Object> json = JSONUtil.parseObj(jsonStr);

                // 确保必要字段存在且有值
                if (!json.containsKey("possibleDiseases") || json.get("possibleDiseases") == null) {
                    json.put("possibleDiseases", new ArrayList<>());
                }
                if (!json.containsKey("features") || json.get("features") == null) {
                    json.put("features", "根据图片特征进行分析");
                }
                if (!json.containsKey("severity") || json.get("severity") == null) {
                    json.put("severity", "待进一步评估");
                }
                if (!json.containsKey("needDoctor")) {
                    json.put("needDoctor", true);
                }
                if (!json.containsKey("urgency") || json.get("urgency") == null) {
                    json.put("urgency", "建议就医咨询");
                }
                if (!json.containsKey("suggestion") || json.get("suggestion") == null) {
                    json.put("suggestion", aiResponse);
                }

                return json;
            }

            // 如果不是JSON，从文本中提取信息
            Map<String, Object> result = new HashMap<>();
            result.put("suggestion", aiResponse);
            result.put("possibleDiseases", extractPossibleDiseases(aiResponse));
            result.put("features", extractFeatures(aiResponse));
            result.put("severity", extractSeverity(aiResponse));
            result.put("needDoctor", true);
            result.put("urgency", extractUrgency(aiResponse));
            return result;

        } catch (Exception e) {
            log.error("解析皮肤分析响应失败", e);
            log.info("原始AI响应: {}", aiResponse);

            // 解析失败，从文本中提取信息
            Map<String, Object> result = new HashMap<>();
            result.put("suggestion", aiResponse);
            result.put("possibleDiseases", extractPossibleDiseases(aiResponse));
            result.put("features", extractFeatures(aiResponse));
            result.put("severity", extractSeverity(aiResponse));
            result.put("needDoctor", true);
            result.put("urgency", extractUrgency(aiResponse));
            return result;
        }
    }

    /**
     * 从文本中提取可能的疾病
     */
    private List<Map<String, Object>> extractPossibleDiseases(String text) {
        List<Map<String, Object>> diseases = new ArrayList<>();

        // 常见皮肤疾病关键词
        String[] diseaseKeywords = {
            "湿疹", "皮炎", "荨麻疹", "痤疮", "银屑病", "带状疱疹",
            "白癜风", "真菌感染", "过敏", "皮炎", "感染"
        };

        for (String disease : diseaseKeywords) {
            if (text.contains(disease)) {
                Map<String, Object> diseaseInfo = new HashMap<>();
                diseaseInfo.put("name", disease);
                diseaseInfo.put("confidence", 0.6);
                diseases.add(diseaseInfo);
                break; // 只添加第一个匹配的
            }
        }

        // 如果没有找到特定疾病，添加一个通用的
        if (diseases.isEmpty()) {
            Map<String, Object> diseaseInfo = new HashMap<>();
            diseaseInfo.put("name", "皮肤病变");
            diseaseInfo.put("confidence", 0.5);
            diseases.add(diseaseInfo);
        }

        return diseases;
    }

    /**
     * 从文本中提取皮肤特征
     */
    private String extractFeatures(String text) {
        // 提取前200个字符作为特征描述
        if (text.length() > 200) {
            return text.substring(0, 200) + "...";
        }
        return text;
    }

    /**
     * 从文本中提取严重程度
     */
    private String extractSeverity(String text) {
        if (text.contains("轻微") || text.contains("轻度")) {
            return "轻微";
        } else if (text.contains("中度")) {
            return "中度";
        } else if (text.contains("严重") || text.contains("重度")) {
            return "严重";
        } else if (text.contains("感染") || text.contains("炎症")) {
            return "需要关注";
        }
        return "待进一步评估";
    }

    /**
     * 从文本中提取紧急程度
     */
    private String extractUrgency(String text) {
        if (text.contains("紧急") || text.contains("立即") || text.contains("尽快")) {
            return "紧急";
        } else if (text.contains("建议就医") || text.contains("需要就医")) {
            return "建议就医";
        } else if (text.contains("观察") || text.contains("注意")) {
            return "需要观察";
        }
        return "建议咨询医生";
    }
}
