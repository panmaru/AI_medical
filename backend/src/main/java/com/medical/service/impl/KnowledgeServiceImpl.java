package com.medical.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.entity.MedicalKnowledge;
import com.medical.exception.BusinessException;
import com.medical.mapper.MedicalKnowledgeMapper;
import com.medical.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 医疗知识库Service实现类
 *
 * @author AI Medical Team
 */
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl extends ServiceImpl<MedicalKnowledgeMapper, MedicalKnowledge> implements KnowledgeService {

    @Override
    public IPage<MedicalKnowledge> getKnowledgePage(Page<MedicalKnowledge> page, String diseaseName, String category, Integer auditStatus) {
        LambdaQueryWrapper<MedicalKnowledge> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(diseaseName)) {
            wrapper.like(MedicalKnowledge::getDiseaseName, diseaseName);
        }

        if (StringUtils.hasText(category)) {
            wrapper.eq(MedicalKnowledge::getCategory, category);
        }

        if (auditStatus != null) {
            wrapper.eq(MedicalKnowledge::getAuditStatus, auditStatus);
        }

        wrapper.orderByDesc(MedicalKnowledge::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditKnowledge(Long knowledgeId, Integer auditStatus, Long auditorId) {
        MedicalKnowledge knowledge = this.getById(knowledgeId);
        if (knowledge == null) {
            throw new BusinessException("知识不存在");
        }

        // 只有待审核的记录才能审核
        if (knowledge.getAuditStatus() != 0) {
            throw new BusinessException("该知识已审核，不能重复审核");
        }

        knowledge.setAuditStatus(auditStatus);
        knowledge.setAuditorId(auditorId);
        knowledge.setAuditTime(LocalDateTime.now());

        return this.updateById(knowledge);
    }

}
