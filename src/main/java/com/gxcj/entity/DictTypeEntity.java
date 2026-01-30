package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;

@Data
@TableName(value = "sys_dict_type")
public class DictTypeEntity {
    private String id;
    private String dictName;
    private String dictType;
    private Integer status;
    private String remark;
    private Timestamp createTime;
    private Integer isDeleted;

    public static <T> DictTypeEntity ofReq(T req){
        DictTypeEntity dictTypeEntity = new DictTypeEntity();
        BeanUtils.copyProperties(req,dictTypeEntity);
        return dictTypeEntity;
    }
}
