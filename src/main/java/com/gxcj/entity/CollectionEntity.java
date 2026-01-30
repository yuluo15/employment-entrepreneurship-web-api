package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.sql.Timestamp;

@Data
@TableName("biz_collection")
public class CollectionEntity {
    private String id;
    private String userId;
    private String targetId;
    private String type;
    private String title;
    private String image;
    private String subTitle;
    private Timestamp createTime;
}
