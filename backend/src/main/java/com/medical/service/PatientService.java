package com.medical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.entity.Patient;

/**
 * 患者Service接口
 *
 * @author AI Medical Team
 */
public interface PatientService extends IService<Patient> {

    /**
     * 根据用户ID查询患者信息
     *
     * @param userId 用户ID
     * @return 患者信息
     */
    Patient getByUserId(Long userId);

}
