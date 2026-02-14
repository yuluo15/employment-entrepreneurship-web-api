package com.gxcj.handle;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用于处理 PostgreSQL vector 类型与 Java float[] 的转换
 * 用于AI向量功能，将float数组映射到PostgreSQL的vector类型
 */
@MappedTypes(float[].class)
@MappedJdbcTypes(JdbcType.OTHER) // vector 在 JDBC 中通常被识别为 OTHER
public class ListToVectorTypeHandler extends BaseTypeHandler<float[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
        // 将 float[] 转换为 PostgreSQL vector 格式的字符串: "[1.1,2.2,3.3]"
        if (parameter == null || parameter.length == 0) {
            ps.setObject(i, null);
            return;
        }
        
        // 构建向量字符串
        StringBuilder sb = new StringBuilder("[");
        for (int j = 0; j < parameter.length; j++) {
            if (j > 0) {
                sb.append(",");
            }
            sb.append(parameter[j]);
        }
        sb.append("]");
        
        // 创建 PGobject 并指定类型为 vector
        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue(sb.toString());

        ps.setObject(i, pgObject);
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseVector(rs.getString(columnName));
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseVector(rs.getString(columnIndex));
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseVector(cs.getString(columnIndex));
    }

    /**
     * 辅助方法：将数据库取出的字符串 "[1.1,2.2,3.3]" 解析回 float[]
     */
    private float[] parseVector(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        
        // 去掉方括号和空格
        String content = value.replace("[", "").replace("]", "").trim();
        if (!StringUtils.hasText(content)) {
            return null;
        }

        // 分割字符串并转换为float数组
        String[] parts = content.split(",");
        float[] result = new float[parts.length];
        
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        
        return result;
    }
}
