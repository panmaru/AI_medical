package com.medical.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * AI诊断DTO
 *
 * @author AI Medical Team
 */
@Data
public class AiDiagnosisDTO {

    /**
     * 患者ID
     */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /**
     * 主诉
     */
    @NotBlank(message = "主诉不能为空")
    private String chiefComplaint;

    /**
     * 症状列表
     */
    private List<String> symptoms;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 既往史
     */
    private String pastHistory;

    /**
     * 过敏史
     */
    private String allergyHistory;

}
