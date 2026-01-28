package com.medical.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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
    public Map<String, Object> aiDiagnosis(AiDiagnosisDTO dto) {
        try {
            // 查询患者信息
            Patient patient = patientMapper.selectById(dto.getPatientId());
            if (patient == null) {
                throw new RuntimeException("患者不存在");
            }

            // 构建AI诊断提示词
            String prompt = buildDiagnosisPrompt(patient, dto);

            // 调用智谱AI API
            String aiResponse = callZhipuApi(prompt);

            // 解析AI响应
            Map<String, Object> diagnosisResult = parseAiResponse(aiResponse);

            // 保存诊断记录
            DiagnosisRecord record = new DiagnosisRecord();
            record.setRecordNo(generateRecordNo());
            record.setPatientId(patient.getId());
            record.setPatientName(patient.getName());
            record.setChiefComplaint(dto.getChiefComplaint());
            record.setPresentIllness(dto.getPresentIllness());
            record.setSymptoms(JSONUtil.toJsonStr(dto.getSymptoms()));
            record.setAiDiagnosis(JSONUtil.toJsonStr(diagnosisResult));
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
     * 解析AI响应
     */
    private Map<String, Object> parseAiResponse(String aiResponse) {
        try {
            // 尝试解析为JSON
            if (aiResponse.startsWith("{")) {
                Map<String, Object> json = JSONUtil.parseObj(aiResponse);
                return json;
            }

            // 如果不是JSON，包装成文本格式
            Map<String, Object> result = new HashMap<>();
            result.put("suggestion", aiResponse);
            result.put("diagnosis", new ArrayList<>());
            result.put("needDoctor", true);
            result.put("urgency", "一般");
            return result;

        } catch (Exception e) {
            // 解析失败，返回文本格式
            Map<String, Object> result = new HashMap<>();
            result.put("suggestion", aiResponse);
            result.put("diagnosis", new ArrayList<>());
            result.put("needDoctor", true);
            result.put("urgency", "一般");
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
