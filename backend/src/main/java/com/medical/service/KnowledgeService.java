package com.medical.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.entity.MedicalKnowledge;

/**
 * 医疗知识库Service接口
 *
 * @author AI Medical Team
 */
public interface KnowledgeService extends IService<MedicalKnowledge> {

    /**
     * 分页查询医疗知识库
     *
     * @param page 分页参数
     * @param diseaseName 疾病名称(可选)
     * @param category 分类(可选)
     * @param auditStatus 审核状态(可选)
     * @return 知识库列表
     */
    IPage<MedicalKnowledge> getKnowledgePage(Page<MedicalKnowledge> page, String diseaseName, String category, Integer auditStatus);

    /**
     * 审核知识
     *
     * @param knowledgeId 知识ID
     * @param auditStatus 审核状态: 0-待审核, 1-已审核
     * @param auditorId 审核人ID
     * @return 是否成功
     */
    boolean auditKnowledge(Long knowledgeId, Integer auditStatus, Long auditorId);

}
