package com.medical.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.medical.common.Result;
import com.medical.dto.AiDiagnosisDTO;
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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 讯飞星火AI服务实现类
 *
 * @author AI Medical Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SparkAiServiceImpl implements SparkAiService {

    @Value("${spark.api.app-id}")
    private String appId;

    @Value("${spark.api.api-key}")
    private String apiKey;

    @Value("${spark.api.api-secret}")
    private String apiSecret;

    @Value("${spark.api.url}")
    private String apiUrl;

    private final DiagnosisRecordMapper diagnosisRecordMapper;
    private final PatientMapper patientMapper;

    private final WebClient.Builder webClientBuilder = WebClient.builder();

    @Override
    public Map<String, Object> aiDiagnosis(AiDiagnosisDTO dto) {
        try {
            // 查询患者信息
            Patient patient = patientMapper.selectById(dto.getPatientId());
            if (patient == null) {
                throw new RuntimeException("患者不存在");
            }

            // 构建AI诊断提示词
            String prompt = buildDiagnosisPrompt(patient, dto);

            // 调用讯飞星火API
            String aiResponse = callSparkApi(prompt);

            // 解析AI响应
            Map<String, Object> diagnosisResult = parseAiResponse(aiResponse);

            // 保存诊断记录
            DiagnosisRecord record = new DiagnosisRecord();
            record.setRecordNo(generateRecordNo());
            record.setPatientId(patient.getId());
            record.setPatientName(patient.getName());
            record.setChiefComplaint(dto.getChiefComplaint());
            record.setPresentIllness(dto.getPresentIllness());
            record.setSymptoms(JSON.toJSONString(dto.getSymptoms()));
            record.setAiDiagnosis(JSON.toJSONString(diagnosisResult));
            record.setAiSuggestion((String) diagnosisResult.get("suggestion"));
            record.setDiagnosisType(0);
            record.setStatus(0);

            diagnosisRecordMapper.insert(record);

            diagnosisResult.put("recordId", record.getId());
            diagnosisResult.put("recordNo", record.getRecordNo());

            return diagnosisResult;

        } catch (Exception e) {
            log.error("AI诊断失败", e);
            throw new RuntimeException("AI诊断失败: " + e.getMessage());
        }
    }

    @Override
    public String chat(String message, String sessionId) {
        try {
            // 构建对话提示词
            String prompt = buildChatPrompt(message);

            // 调用讯飞星火API
            return callSparkApi(prompt);

        } catch (Exception e) {
            log.error("AI对话失败", e);
            return "抱歉，我暂时无法回答您的问题，请稍后再试。";
        }
    }

    @Override
    public DiagnosisRecord saveDiagnosisRecord(DiagnosisRecord record) {
        diagnosisRecordMapper.insert(record);
        return record;
    }

    /**
     * 构建诊断提示词
     */
    private String buildDiagnosisPrompt(Patient patient, AiDiagnosisDTO dto) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位经验丰富的医生，请根据以下患者信息进行诊断分析：\n\n");

        prompt.append("【患者基本信息】\n");
        prompt.append("姓名：").append(patient.getName()).append("\n");
        prompt.append("性别：").append(patient.getGender() == 1 ? "男" : "女").append("\n");
        prompt.append("年龄：").append(patient.getAge()).append("岁\n\n");

        prompt.append("【主诉】\n").append(dto.getChiefComplaint()).append("\n\n");

        if (dto.getSymptoms() != null && !dto.getSymptoms().isEmpty()) {
            prompt.append("【症状】\n");
            dto.getSymptoms().forEach(symptom -> prompt.append("- ").append(symptom).append("\n"));
            prompt.append("\n");
        }

        if (StringUtils.hasText(dto.getPresentIllness())) {
            prompt.append("【现病史】\n").append(dto.getPresentIllness()).append("\n\n");
        }

        if (StringUtils.hasText(patient.getAllergyHistory())) {
            prompt.append("【过敏史】\n").append(patient.getAllergyHistory()).append("\n\n");
        }

        if (StringUtils.hasText(patient.getPastHistory())) {
            prompt.append("【既往病史】\n").append(patient.getPastHistory()).append("\n\n");
        }

        prompt.append("请提供：\n");
        prompt.append("1. 可能的疾病诊断（按概率排序）\n");
        prompt.append("2. 诊断依据\n");
        prompt.append("3. 建议的检查项目\n");
        prompt.append("4. 治疗建议\n");
        prompt.append("5. 注意事项\n");
        prompt.append("6. 是否需要就医（是/否，如果需要，说明紧急程度）\n\n");
        prompt.append("请以JSON格式回复，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"diagnosis\": [\"疾病1\", \"疾病2\"],\n");
        prompt.append("  \"basis\": \"诊断依据\",\n");
        prompt.append("  \"examinations\": [\"检查1\", \"检查2\"],\n");
        prompt.append("  \"treatment\": \"治疗建议\",\n");
        prompt.append("  \"precautions\": \"注意事项\",\n");
        prompt.append("  \"needDoctor\": true,\n");
        prompt.append("  \"urgency\": \"紧急程度\",\n");
        prompt.append("  \"suggestion\": \"综合建议\"\n");
        prompt.append("}");

        return prompt.toString();
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
     * 调用讯飞星火API
     */
    private String callSparkApi(String prompt) throws Exception {
        // 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("app_id", appId);
        params.put("domain", "general");

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);
        params.put("messages", messages);

        // 计算鉴权URL
        String authUrl = getAuthUrl(apiUrl);
        log.info("请求讯飞星火API: {}", authUrl);

        // 发送请求
        WebClient webClient = webClientBuilder.build();
        String response = webClient.post()
                .uri(authUrl)
                .bodyValue(JSON.toJSONString(params))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("讯飞星火API响应: {}", response);
        return response;
    }

    /**
     * 计算鉴权URL
     */
    private String getAuthUrl(String url) throws Exception {
        // 解析原始URL
        java.net.URL urlObj = new java.net.URL(url);
        String host = urlObj.getHost();
        String path = urlObj.getPath();

        // 生成日期和签名
        String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).format(new Date());
        String signatureOrigin = "host: " + host + "\ndate: " + date + "\nGET " + path + " HTTP/1.1";

        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signatureBytes);

        // 构建 authorization
        String authorizationOrigin = "api_key=\"" + apiKey + "\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"" + signature + "\"";
        String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

        // 构建最终URL
        return url + "?authorization=" + URLEncoder.encode(authorization, "UTF-8") +
                "&date=" + URLEncoder.encode(date, "UTF-8") +
                "&host=" + URLEncoder.encode(host, "UTF-8");
    }

    /**
     * 解析AI响应
     */
    private Map<String, Object> parseAiResponse(String aiResponse) {
        try {
            // 这里需要根据实际讯飞星火API的返回格式进行解析
            // 以下是示例代码，实际使用时需要根据API文档调整
            Map<String, Object> response = JSON.parseObject(aiResponse, Map.class);

            // 如果返回的是嵌套结构，需要提取实际内容
            Object content = response.get("content");
            if (content != null) {
                String contentStr = content.toString();
                // 尝试解析JSON格式的响应
                if (contentStr.startsWith("{")) {
                    return JSON.parseObject(contentStr, Map.class);
                }
            }

            // 如果无法解析为JSON，返回文本格式
            Map<String, Object> result = new HashMap<>();
            result.put("suggestion", content != null ? content.toString() : aiResponse);
            result.put("diagnosis", Collections.emptyList());
            result.put("needDoctor", true);

            return result;

        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("suggestion", aiResponse);
            result.put("diagnosis", Collections.emptyList());
            result.put("needDoctor", true);
            return result;
        }
    }

    /**
     * 生成诊断编号
     */
    private String generateRecordNo() {
        return "DR" + System.currentTimeMillis() + IdUtil.randomUUID().substring(0, 4).toUpperCase();
    }

}
