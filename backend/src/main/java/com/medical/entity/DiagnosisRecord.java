package com.medical.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 诊断记录实体类
 *
 * @author AI Medical Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("diagnosis_record")
public class DiagnosisRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 诊断编号
     */
    private String recordNo;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 症状描述(JSON格式)
     */
    private String symptoms;

    /**
     * 皮肤图片URL(JSON格式，存储多个图片URL)
     */
    private String imageUrls;

    /**
     * AI诊断结果(JSON格式)
     */
    private String aiDiagnosis;

    /**
     * AI建议
     */
    private String aiSuggestion;

    /**
     * 医生确认诊断
     */
    private String doctorDiagnosis;

    /**
     * 治疗方案
     */
    private String treatmentPlan;

    /**
     * 处方信息(JSON格式)
     */
    private String prescription;

    /**
     * 诊断类型: 0-AI辅助, 1-人工诊断
     */
    private Integer diagnosisType;

    /**
     * 匹配度
     */
    private BigDecimal matchRate;

    /**
     * 状态: 0-待确认, 1-已确认, 2-已完成
     */
    private Integer status;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
