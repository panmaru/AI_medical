package com.medical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.entity.Patient;
import com.medical.mapper.PatientMapper;
import com.medical.service.PatientService;
import org.springframework.stereotype.Service;

/**
 * 患者Service实现类
 *
 * @author AI Medical Team
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

}
