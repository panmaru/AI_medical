package com.medical.service;

import com.medical.entity.DiagnosisRecord;

import java.util.Map;

/**
 * 讯飞星火AI服务接口
 *
 * @author AI Medical Team
 */
public interface SparkAiService {

    /**
     * AI对话问诊
     *
     * @param message 用户消息
     * @param sessionId 会话ID
     * @return AI回复
     */
    String chat(String message, String sessionId);

    /**
     * 保存诊断记录
     *
     * @param record 诊断记录
     * @return 保存的记录
     */
    DiagnosisRecord saveDiagnosisRecord(DiagnosisRecord record);

    /**
     * AI皮肤图片分析
     *
     * @param patientId 患者ID
     * @param imageUrls 图片URL列表
     * @param description 患者描述（可选）
     * @return 分析结果
     */
    Map<String, Object> analyzeSkinImage(Long patientId, java.util.List<String> imageUrls, String description);

}
