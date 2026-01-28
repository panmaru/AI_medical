package com.medical.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.common.Result;
import com.medical.entity.MedicalKnowledge;
import com.medical.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 医疗知识库控制器
 *
 * @author AI Medical Team
 */
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 分页查询医疗知识库
     */
    @GetMapping("/page")
    public Result<IPage<MedicalKnowledge>> getKnowledgePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String diseaseName,
            @RequestParam(required = false) String category) {
        Page<MedicalKnowledge> page = new Page<>(current, size);
        IPage<MedicalKnowledge> result = knowledgeService.getKnowledgePage(page, diseaseName, category, null);
        return Result.success(result);
    }

    /**
     * 获取知识详情
     */
    @GetMapping("/{id}")
    public Result<MedicalKnowledge> getKnowledgeById(@PathVariable Long id) {
        MedicalKnowledge knowledge = knowledgeService.getById(id);
        if (knowledge == null) {
            return Result.error("知识不存在");
        }
        return Result.success(knowledge);
    }

    /**
     * 新增知识
     */
    @SaCheckPermission("knowledge:create")
    @PostMapping
    public Result<String> createKnowledge(@RequestBody MedicalKnowledge knowledge) {
        Long userId = StpUtil.getLoginIdAsLong();
        knowledge.setCreateBy(userId);
        knowledge.setSource(1); // 手动录入
        knowledge.setAuditStatus(1); // 直接设置为已审核，去掉审核流程

        boolean success = knowledgeService.save(knowledge);
        if (success) {
            return Result.success("新增成功");
        } else {
            return Result.error("新增失败");
        }
    }

    /**
     * 更新知识
     */
    @SaCheckPermission("knowledge:update")
    @PutMapping
    public Result<String> updateKnowledge(@RequestBody MedicalKnowledge knowledge) {
        boolean success = knowledgeService.updateById(knowledge);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 删除知识
     */
    @SaCheckPermission("knowledge:delete")
    @DeleteMapping("/{id}")
    public Result<String> deleteKnowledge(@PathVariable Long id) {
        boolean success = knowledgeService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

}
