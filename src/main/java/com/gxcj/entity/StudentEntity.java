package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;

@Data
@TableName("biz_student")
public class StudentEntity {
    @TableId
    private String studentId;
    private String userId;
    private String schoolId;
    private String studentName;
    private String studentNo;
    private String gender;
    private String collegeName;
    private String majorName;
    private String className;
    private String education;
    private Integer enrollmentYear;
    private Integer graduationYear;
    private String phone;
    private String email;
    private String employmentStatus;
    private Timestamp createTime;
    private Timestamp updateTime;

    public static <T> StudentEntity ofReq(T req){
        StudentEntity studentEntity = new StudentEntity();
        BeanUtils.copyProperties(req,studentEntity);
        return studentEntity;
    }
}
