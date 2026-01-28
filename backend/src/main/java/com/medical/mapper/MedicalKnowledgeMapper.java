package com.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.entity.MedicalKnowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医疗知识库Mapper接口
 *
 * @author AI Medical Team
 */
@Mapper
public interface MedicalKnowledgeMapper extends BaseMapper<MedicalKnowledge> {

}
