package com.gxcj.entity.dto;

import lombok.Data;

@Data
public class UpdateTeacherInfoDto {
    private String avatar;
    private String gender;
    private String phone;
    private String email;
    private String expertise;
    private String intro;
}
