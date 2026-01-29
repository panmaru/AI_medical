package com.medical.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.common.Result;
import com.medical.entity.Patient;
import com.medical.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 患者管理控制器
 *
 * @author AI Medical Team
 */
@RestController
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * 分页查询患者列表
     */
    @GetMapping("/page")
    @SaCheckPermission("patient:list")
    public Result<Page<Patient>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone) {

        Page<Patient> page = new Page<>(current, size);
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(Patient::getName, name);
        }

        if (StringUtils.hasText(phone)) {
            wrapper.like(Patient::getPhone, phone);
        }

        wrapper.orderByDesc(Patient::getCreateTime);

        Page<Patient> result = patientService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 根据ID查询患者
     */
    @GetMapping("/{id}")
    @SaCheckPermission("patient:detail")
    public Result<Patient> getById(@PathVariable Long id) {
        Patient patient = patientService.getById(id);
        if (patient == null) {
            return Result.error("患者不存在");
        }
        return Result.success(patient);
    }

    /**
     * 新增患者
     */
    @PostMapping
    @SaCheckPermission("patient:create")
    public Result<Void> add(@RequestBody Patient patient) {
        try {
            patientService.save(patient);
            return Result.success("添加成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新患者信息
     */
    @PutMapping
    @SaCheckPermission("patient:update")
    public Result<Void> update(@RequestBody Patient patient) {
        try {
            patientService.updateById(patient);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除患者
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("patient:delete-api")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            patientService.removeById(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的患者信息
     * 用于患者查看和编辑自己的信息
     */
    @GetMapping("/my-info")
    public Result<Patient> getMyInfo() {
        try {
            // 获取当前登录用户的ID
            Long userId = StpUtil.getLoginIdAsLong();
            Patient patient = patientService.getByUserId(userId);
            if (patient == null) {
                return Result.error("未找到您的患者信息，请联系管理员创建");
            }
            return Result.success(patient);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新当前登录用户的患者信息
     * 用于患者编辑自己的信息
     */
    @PutMapping("/my-info")
    public Result<Void> updateMyInfo(@RequestBody Patient patient) {
        try {
            // 获取当前登录用户的ID
            Long userId = StpUtil.getLoginIdAsLong();
            // 验证患者是否在编辑自己的信息
            Patient existingPatient = patientService.getByUserId(userId);
            if (existingPatient == null) {
                return Result.error("未找到您的患者信息");
            }
            // 确保只能修改自己的信息
            if (!existingPatient.getId().equals(patient.getId())) {
                return Result.error("无权修改其他患者的信息");
            }
            // 更新信息，保留不可修改的字段
            patient.setUserId(userId);
            patient.setPatientNo(existingPatient.getPatientNo());
            patient.setCreateBy(existingPatient.getCreateBy());
            patientService.updateById(patient);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
