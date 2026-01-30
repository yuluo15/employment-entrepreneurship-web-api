package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;

@Data
@TableName(value = "sys_dict_data")
public class DictDataEntity {
    private String id;
    private Integer dictSort;
    private String dictLabel;
    private String dictValue;
    private String dictType;
    private Integer status;
    private String remark;
    private Timestamp createTime;
    private Integer isDeleted;

    public static <T> DictDataEntity ofReq(T req){
        DictDataEntity dictDataEntity = new DictDataEntity();
        BeanUtils.copyProperties(req,dictDataEntity);
        return dictDataEntity;
    }
}
