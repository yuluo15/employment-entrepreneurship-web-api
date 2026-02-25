package com.gxcj.service;

import com.gxcj.entity.dto.UpdateTeacherInfoDto;
import com.gxcj.entity.vo.teacher.TeacherProfileVo;

public interface TeacherProfileService {
    TeacherProfileVo getTeacherInfo();
    
    void updateTeacherInfo(UpdateTeacherInfoDto dto);
}
