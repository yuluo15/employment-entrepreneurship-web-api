# AI简历筛选功能 - 后端实施完成

## ✅ 已完成内容

### 1. VO类 ✅
**文件**: `src/main/java/com/gxcj/entity/vo/ResumeMatchVo.java`

包含字段:
- 简历基本信息（resumeId, studentId, studentName）
- 学生信息（school, major, education, graduationYear）
- 求职意向（expectedPosition, expectedSalary, targetCity）
- 技能信息（skills）
- 匹配信息（matchScore, matchReason）
- 联系方式（phone, email）- 已脱敏

### 2. Service接口扩展 ✅
**文件**: `src/main/java/com/gxcj/service/AIRecommendationService.java`

新增方法:
```java
List<ResumeMatchVo> screenResumes(String jobId, int limit);
```

### 3. Service实现 ✅
**文件**: `src/main/java/com/gxcj/service/impl/AIRecommendationServiceImpl.java`

实现功能:
- `screenResumes()` - 为职位筛选匹配的简历
- `buildResumeMatch()` - 构建简历匹配结果
- `generateMatchReason()` - 生成匹配理由
- `maskPhone()` - 手机号脱敏（138****1234）
- `maskEmail()` - 邮箱脱敏（zha***@example.com）

核心逻辑:
1. 获取职位信息，如果没有向量则先生成
2. 使用PostgreSQL向量相似度搜索（`<=>` 操作符）
3. 只筛选公开的简历（is_public = 1）
4. 按相似度排序，返回指定数量的结果
5. 计算匹配度分数（0-100）
6. 生成匹配理由
7. 脱敏处理联系方式

### 4. Controller层 ✅
**文件**: `src/main/java/com/gxcj/controller/company/AIRecruitmentController.java`

接口信息:
- **路径**: `GET /api/company/ai/screen/resumes`
- **权限**: `@PreAuthorize("hasRole('ROLE_COMPANY')")`
- **参数**: 
  - jobId (必填) - 职位ID
  - limit (可选，默认20) - 返回数量
- **返回**: `Result<List<ResumeMatchVo>>`

### 5. 测试SQL ✅
**文件**: `docs/AI简历筛选功能-测试SQL.sql`

包含测试查询:
- 检查职位向量
- 检查简历向量
- 测试向量相似度查询
- 查看向量生成统计

---

## 📝 接口文档

### AI筛选简历接口

**接口地址**: `GET /api/company/ai/screen/resumes`

**权限**: 企业端（ROLE_COMPANY）

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| jobId | String | 是 | 职位ID |
| limit | Integer | 否 | 返回数量，默认20 |

**请求示例**:
```
GET /api/company/ai/screen/resumes?jobId=xxx&limit=20
Authorization: Bearer {company_token}
```

**返回示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "resumeId": "resume123",
      "studentId": "student123",
      "studentName": "张三",
      "school": "计算机学院",
      "major": "软件工程",
      "education": "本科",
      "graduationYear": 2026,
      "expectedPosition": "Java开发工程师",
      "expectedSalary": "7000",
      "targetCity": "北京，上海",
      "skills": "java，springboot，mybatis，redis",
      "matchScore": 92,
      "matchReason": "候选人的技能和经验与职位要求高度匹配",
      "phone": "138****1234",
      "email": "zha***@example.com"
    }
  ]
}
```

---

## 🔧 核心算法

### 1. 向量相似度搜索
```sql
SELECT 
    r.resume_id,
    r.student_id,
    r.expected_position,
    r.expected_salary,
    r.target_city,
    r.skills,
    1 - (r.embedding <=> CAST(? AS vector)) as similarity
FROM biz_student_resume r
WHERE r.is_public = 1
  AND r.embedding IS NOT NULL
ORDER BY r.embedding <=> CAST(? AS vector)
LIMIT ?
```

说明:
- 使用 `<=>` 操作符计算余弦距离
- `1 - distance` 得到相似度（0-1之间）
- 按相似度排序（距离越小，相似度越高）

### 2. 匹配度计算
```java
int matchScore = (int) (similarity * 100);
```

将相似度（0-1）转换为百分比（0-100）

### 3. 匹配理由生成
```java
if (matchScore >= 90) {
    return "候选人的技能和经验与职位要求高度匹配";
} else if (matchScore >= 80) {
    return "候选人的专业背景与职位要求相符";
} else if (matchScore >= 70) {
    return "候选人具备职位所需的基本能力";
} else {
    return "候选人可能适合该职位";
}
```

### 4. 数据脱敏
```java
// 手机号: 138****1234
phone.substring(0, 3) + "****" + phone.substring(7)

// 邮箱: zha***@example.com
name.substring(0, 3) + "***@" + parts[1]
```

---

## 🧪 测试步骤

### 1. 确保数据准备
```sql
-- 检查职位向量
SELECT COUNT(*) FROM biz_job 
WHERE status = 1 AND audit = 1 AND embedding IS NOT NULL;

-- 检查简历向量
SELECT COUNT(*) FROM biz_student_resume 
WHERE is_public = 1 AND embedding IS NOT NULL;
```

### 2. 使用Postman测试接口
```
GET http://localhost:8080/api/company/ai/screen/resumes?jobId=xxx&limit=20
Headers: 
  Authorization: Bearer {company_token}
```

### 3. 验证返回结果
- ✅ 返回的简历按匹配度从高到低排序
- ✅ 匹配度分数在70-100之间
- ✅ 匹配理由准确
- ✅ 简历信息完整
- ✅ 手机号和邮箱已脱敏

---

## ⚠️ 注意事项

### 1. 权限控制
- 只有企业端（ROLE_COMPANY）可以访问
- 使用 `@PreAuthorize` 注解控制权限

### 2. 数据安全
- 手机号脱敏: 138****1234
- 邮箱脱敏: zha***@example.com
- 只返回公开的简历（is_public = 1）

### 3. 性能优化
- 使用向量索引加速查询（ivfflat）
- 限制返回数量（默认20）
- 可以添加Redis缓存

### 4. 异常处理
- 职位不存在 → 抛出 BusinessException
- 职位没有向量 → 自动生成向量
- 简历没有向量 → 不参与匹配

---

## ✅ 实施检查清单

- [x] 创建 `ResumeMatchVo` 类
- [x] 扩展 `AIRecommendationService` 接口
- [x] 实现 `screenResumes` 方法
- [x] 实现 `buildResumeMatch` 方法
- [x] 实现 `generateMatchReason` 方法
- [x] 实现数据脱敏方法（maskPhone, maskEmail）
- [x] 创建 `AIRecruitmentController` 控制器
- [x] 添加权限控制注解
- [x] 创建测试SQL文件

---

## 🎯 功能特点

### 1. 智能匹配
- 基于语义相似度，而非简单关键词匹配
- 考虑技能、经验、教育背景等多维度
- 匹配度分数直观可见（0-100）

### 2. 自动化
- 职位没有向量时自动生成
- 自动计算匹配度
- 自动生成匹配理由

### 3. 安全性
- 权限控制（只有企业端可访问）
- 数据脱敏（手机号、邮箱）
- 只筛选公开的简历

### 4. 易用性
- 接口简单（只需传入职位ID）
- 返回结果按匹配度排序
- 包含完整的候选人信息

---

## 📊 预期效果

### 效率提升
- **传统方式**: HR需要逐个查看简历，耗时2-3小时
- **AI筛选**: 1分钟内完成筛选，直接看到最匹配的候选人

### 准确性提升
- 基于语义相似度，而非简单关键词匹配
- 考虑技能、经验、教育背景等多维度
- 匹配度分数直观可见

### 用户满意度
- HR工作效率大幅提升
- 候选人匹配度更高
- 招聘成功率提升

---

## 📞 后续工作

### 前端集成
前端已完成（根据文档）:
- ✅ 创建 `src/api/company/ai.ts`
- ✅ 修改人才库页面
- ✅ 添加AI筛选按钮
- ✅ 实现筛选结果对话框

### 测试验证
- [ ] 使用Postman测试接口
- [ ] 验证匹配度计算准确性
- [ ] 验证数据脱敏功能
- [ ] 性能测试

### 优化方向
- [ ] 添加Redis缓存
- [ ] 批量筛选多个职位
- [ ] 导出筛选结果
- [ ] 筛选历史记录

---

**实施日期**: 2026年2月14日  
**实施人员**: 后端开发  
**状态**: 后端已完成 ✅ | 待测试 ⏳
