-- ============================================
-- 系统字典补充数据 SQL
-- 生成日期: 2026-02-14
-- 说明: 补充项目中缺失的字典类型和字典数据
-- ============================================

-- ============================================
-- 第一部分: 补充字典类型 (sys_dict_type)
-- ============================================

INSERT INTO public.sys_dict_type (id,dict_name,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('dict_type_001','通知类型','sys_notice_type',1,'通知公告的类型分类','2026-02-14 17:30:00',0),
	 ('dict_type_002','面试方式','sys_interview_type',1,'面试的方式类型','2026-02-14 17:30:00',0),
	 ('dict_type_003','面试状态','sys_interview_status',1,'面试的当前状态','2026-02-14 17:30:00',0),
	 ('dict_type_004','投递状态','sys_delivery_status',1,'简历投递的状态','2026-02-14 17:30:00',0),
	 ('dict_type_005','面试结果','sys_interview_result',1,'面试的最终结果','2026-02-14 17:30:00',0),
	 ('dict_type_006','薪资范围','sys_salary_range',1,'职位薪资范围','2026-02-14 17:30:00',0),
	 ('dict_type_007','工作经验','sys_work_experience',1,'工作经验要求','2026-02-14 17:30:00',0),
	 ('dict_type_008','公司规模','sys_company_scale',1,'企业规模分类','2026-02-14 17:30:00',0),
	 ('dict_type_009','职位类型','sys_job_type',1,'职位类型分类','2026-02-14 17:30:00',0),
	 ('dict_type_010','消息类型','sys_message_type',1,'系统消息类型','2026-02-14 17:30:00',0),
	 ('dict_type_011','审核状态','sys_audit_status',1,'审核状态','2026-02-14 17:30:00',0),
	 ('dict_type_012','发布者类型','sys_publisher_type',1,'通知发布者类型','2026-02-14 17:30:00',0),
	 ('dict_type_013','目标受众','sys_target_audience',1,'通知目标受众','2026-02-14 17:30:00',0),
	 ('dict_type_014','项目申请状态','sys_application_status',1,'项目申请状态','2026-02-14 17:30:00',0),
	 ('dict_type_015','学历要求','sys_education_requirement',1,'职位学历要求','2026-02-14 17:30:00',0);

-- ============================================
-- 第二部分: 补充字典数据 (sys_dict_data)
-- ============================================

-- 1. 通知类型 (sys_notice_type)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('notice_type_001',1,'通知','1','sys_notice_type',1,'一般性通知','2026-02-14 17:30:00',0),
	 ('notice_type_002',2,'公告','2','sys_notice_type',1,'重要公告','2026-02-14 17:30:00',0),
	 ('notice_type_003',3,'政策','3','sys_notice_type',1,'政策文件','2026-02-14 17:30:00',0),
	 ('notice_type_004',4,'新闻','4','sys_notice_type',1,'新闻资讯','2026-02-14 17:30:00',0);

-- 2. 面试方式 (sys_interview_type)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('interview_type_001',1,'现场面试','1','sys_interview_type',1,'ONSITE','2026-02-14 17:30:00',0),
	 ('interview_type_002',2,'视频面试','2','sys_interview_type',1,'VIDEO','2026-02-14 17:30:00',0),
	 ('interview_type_003',3,'电话面试','3','sys_interview_type',1,'PHONE','2026-02-14 17:30:00',0);

-- 3. 面试状态 (sys_interview_status)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('interview_status_001',1,'已安排','SCHEDULED','sys_interview_status',1,'面试已安排','2026-02-14 17:30:00',0),
	 ('interview_status_002',2,'已完成','COMPLETED','sys_interview_status',1,'面试已完成','2026-02-14 17:30:00',0),
	 ('interview_status_003',3,'已取消','CANCELLED','sys_interview_status',1,'面试已取消','2026-02-14 17:30:00',0);

-- 4. 投递状态 (sys_delivery_status)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('delivery_status_001',1,'已投递','DELIVERED','sys_delivery_status',1,'简历已投递','2026-02-14 17:30:00',0),
	 ('delivery_status_002',2,'已查看','VIEWED','sys_delivery_status',1,'简历已被查看','2026-02-14 17:30:00',0),
	 ('delivery_status_003',3,'面试中','INTERVIEW','sys_delivery_status',1,'正在面试','2026-02-14 17:30:00',0),
	 ('delivery_status_004',4,'已录用','OFFER','sys_delivery_status',1,'已发放Offer','2026-02-14 17:30:00',0),
	 ('delivery_status_005',5,'已拒绝','REJECTED','sys_delivery_status',1,'简历被拒绝','2026-02-14 17:30:00',0);

-- 5. 面试结果 (sys_interview_result)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('interview_result_001',1,'通过','PASS','sys_interview_result',1,'面试通过','2026-02-14 17:30:00',0),
	 ('interview_result_002',2,'不通过','FAIL','sys_interview_result',1,'面试未通过','2026-02-14 17:30:00',0);

-- 6. 薪资范围 (sys_salary_range)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('salary_range_001',1,'3K以下','3K以下','sys_salary_range',1,NULL,'2026-02-14 17:30:00',0),
	 ('salary_range_002',2,'3-5K','3-5K','sys_salary_range',1,NULL,'2026-02-14 17:30:00',0),
	 ('salary_range_003',3,'5-8K','5-8K','sys_salary_range',1,NULL,'2026-02-14 17:30:00',0),
	 ('salary_range_004',4,'8-12K','8-12K','sys_salary_range',1,NULL,'2026-02-14 17:30:00',0),
	 ('salary_range_005',5,'12-15K','12-15K','sys_salary_range',1,NULL,'2026-02-14 17:30:00',0),
	 ('salary_range_006',6,'15-20K','15-20K','sys_salary_range',1,NULL,'2026-02-14 17:30:00',0),
	 ('salary_range_007',7,'20-30K','20-30K','sys_salary_range',1,NULL,'2026-02-14 17:30:00',0),
	 ('salary_range_008',8,'30-50K','30-50K','sys_salary_range',1,NULL,'2026-02-14 17:30:00',0),
	 ('salary_range_009',9,'50K以上','50K以上','sys_salary_range',1,NULL,'2026-02-14 17:30:00',0);

-- 7. 工作经验 (sys_work_experience)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('work_exp_001',1,'不限','UNLIMITED','sys_work_experience',1,'无经验要求','2026-02-14 17:30:00',0),
	 ('work_exp_002',2,'应届生','FRESH','sys_work_experience',1,'应届毕业生','2026-02-14 17:30:00',0),
	 ('work_exp_003',3,'1年以下','LESS_1','sys_work_experience',1,'1年以下经验','2026-02-14 17:30:00',0),
	 ('work_exp_004',4,'1-3年','1_3','sys_work_experience',1,'1-3年经验','2026-02-14 17:30:00',0),
	 ('work_exp_005',5,'3-5年','3_5','sys_work_experience',1,'3-5年经验','2026-02-14 17:30:00',0),
	 ('work_exp_006',6,'5-10年','5_10','sys_work_experience',1,'5-10年经验','2026-02-14 17:30:00',0),
	 ('work_exp_007',7,'10年以上','MORE_10','sys_work_experience',1,'10年以上经验','2026-02-14 17:30:00',0);

-- 8. 公司规模 (sys_company_scale)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('company_scale_001',1,'0-20人','0_20','sys_company_scale',1,'微型企业','2026-02-14 17:30:00',0),
	 ('company_scale_002',2,'20-99人','20_99','sys_company_scale',1,'小型企业','2026-02-14 17:30:00',0),
	 ('company_scale_003',3,'100-499人','100_499','sys_company_scale',1,'中型企业','2026-02-14 17:30:00',0),
	 ('company_scale_004',4,'500-999人','500_999','sys_company_scale',1,'中大型企业','2026-02-14 17:30:00',0),
	 ('company_scale_005',5,'1000-9999人','1000_9999','sys_company_scale',1,'大型企业','2026-02-14 17:30:00',0),
	 ('company_scale_006',6,'10000人以上','10000_PLUS','sys_company_scale',1,'超大型企业','2026-02-14 17:30:00',0);

-- 9. 职位类型 (sys_job_type)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('job_type_001',1,'全职','1','sys_job_type',1,'全职工作','2026-02-14 17:30:00',0),
	 ('job_type_002',2,'兼职','2','sys_job_type',1,'兼职工作','2026-02-14 17:30:00',0),
	 ('job_type_003',3,'实习','3','sys_job_type',1,'实习岗位','2026-02-14 17:30:00',0),
	 ('job_type_004',4,'远程','4','sys_job_type',1,'远程工作','2026-02-14 17:30:00',0);

-- 10. 消息类型 (sys_message_type)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('message_type_001',1,'系统消息','1','sys_message_type',1,'系统通知','2026-02-14 17:30:00',0),
	 ('message_type_002',2,'面试通知','2','sys_message_type',1,'面试相关通知','2026-02-14 17:30:00',0),
	 ('message_type_003',3,'投递通知','3','sys_message_type',1,'简历投递通知','2026-02-14 17:30:00',0),
	 ('message_type_004',4,'Offer通知','4','sys_message_type',1,'录用通知','2026-02-14 17:30:00',0),
	 ('message_type_005',5,'项目审核','5','sys_message_type',1,'项目审核通知','2026-02-14 17:30:00',0);

-- 11. 审核状态 (sys_audit_status)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('audit_status_001',1,'待审核','0','sys_audit_status',1,'等待审核','2026-02-14 17:30:00',0),
	 ('audit_status_002',2,'审核通过','1','sys_audit_status',1,'审核已通过','2026-02-14 17:30:00',0),
	 ('audit_status_003',3,'审核驳回','2','sys_audit_status',1,'审核未通过','2026-02-14 17:30:00',0);

-- 12. 发布者类型 (sys_publisher_type)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('publisher_type_001',1,'管理员','admin','sys_publisher_type',1,'系统管理员发布','2026-02-14 17:30:00',0),
	 ('publisher_type_002',2,'学校','school','sys_publisher_type',1,'学校发布','2026-02-14 17:30:00',0);

-- 13. 目标受众 (sys_target_audience)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('target_audience_001',1,'所有人','all','sys_target_audience',1,'面向所有用户','2026-02-14 17:30:00',0),
	 ('target_audience_002',2,'学校教职工','school','sys_target_audience',1,'面向学校教职工','2026-02-14 17:30:00',0),
	 ('target_audience_003',3,'学生','student','sys_target_audience',1,'面向学生','2026-02-14 17:30:00',0),
	 ('target_audience_004',4,'企业','company','sys_target_audience',1,'面向企业','2026-02-14 17:30:00',0);

-- 14. 项目申请状态 (sys_application_status)
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('application_status_001',1,'待审核','PENDING','sys_application_status',1,'申请待审核','2026-02-14 17:30:00',0),
	 ('application_status_002',2,'已通过','APPROVED','sys_application_status',1,'申请已通过','2026-02-14 17:30:00',0),
	 ('application_status_003',3,'已拒绝','REJECTED','sys_application_status',1,'申请被拒绝','2026-02-14 17:30:00',0),
	 ('application_status_004',4,'已取消','CANCELLED','sys_application_status',1,'申请已取消','2026-02-14 17:30:00',0);

-- 15. 学历要求 (sys_education_requirement) - 补充更多学历选项
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('education_req_001',1,'不限','UNLIMITED','sys_education_requirement',1,'无学历要求','2026-02-14 17:30:00',0),
	 ('education_req_002',2,'高中','HIGH_SCHOOL','sys_education_requirement',1,'高中及以上','2026-02-14 17:30:00',0),
	 ('education_req_003',3,'大专','JUNIOR','sys_education_requirement',1,'大专及以上','2026-02-14 17:30:00',0),
	 ('education_req_004',4,'本科','BACHELOR','sys_education_requirement',1,'本科及以上','2026-02-14 17:30:00',0),
	 ('education_req_005',5,'硕士','MASTER','sys_education_requirement',1,'硕士及以上','2026-02-14 17:30:00',0),
	 ('education_req_006',6,'博士','DOCTOR','sys_education_requirement',1,'博士学历','2026-02-14 17:30:00',0);

-- ============================================
-- 第三部分: 补充现有字典类型的数据
-- ============================================

-- 补充学历层次 (sys_education) - 添加博士学历
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('education_004',4,'博士','DOCTOR','sys_education',1,NULL,'2026-02-14 17:30:00',0),
	 ('education_005',5,'高中','HIGH_SCHOOL','sys_education',1,NULL,'2026-02-14 17:30:00',0);

-- 补充就业状态 (sys_emp_status) - 添加更多状态
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('emp_status_004',4,'自主创业','ENTREPRENEURSHIP','sys_emp_status',1,'自主创业','2026-02-14 17:30:00',0),
	 ('emp_status_005',5,'灵活就业','FLEXIBLE','sys_emp_status',1,'灵活就业','2026-02-14 17:30:00',0),
	 ('emp_status_006',6,'暂不就业','NOT_EMPLOYED','sys_emp_status',1,'暂不就业','2026-02-14 17:30:00',0);

-- 补充项目领域 (sys_project_domain) - 添加更多领域
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('project_domain_005',5,'生物医药','BIO_MEDICINE','sys_project_domain',1,NULL,'2026-02-14 17:30:00',0),
	 ('project_domain_006',6,'新能源','NEW_ENERGY','sys_project_domain',1,NULL,'2026-02-14 17:30:00',0),
	 ('project_domain_007',7,'人工智能','AI','sys_project_domain',1,NULL,'2026-02-14 17:30:00',0),
	 ('project_domain_008',8,'大数据','BIG_DATA','sys_project_domain',1,NULL,'2026-02-14 17:30:00',0),
	 ('project_domain_009',9,'物联网','IOT','sys_project_domain',1,NULL,'2026-02-14 17:30:00',0),
	 ('project_domain_010',10,'区块链','BLOCKCHAIN','sys_project_domain',1,NULL,'2026-02-14 17:30:00',0);

-- 补充所属行业 (sys_industry) - 添加更多行业
INSERT INTO public.sys_dict_data (id,dict_sort,dict_label,dict_value,dict_type,status,remark,create_time,is_deleted) VALUES
	 ('industry_009',9,'医疗健康','HEALTHCARE','sys_industry',1,NULL,'2026-02-14 17:30:00',0),
	 ('industry_010',10,'汽车交通','AUTOMOBILE','sys_industry',1,NULL,'2026-02-14 17:30:00',0),
	 ('industry_011',11,'旅游酒店','TOURISM','sys_industry',1,NULL,'2026-02-14 17:30:00',0),
	 ('industry_012',12,'物流运输','LOGISTICS','sys_industry',1,NULL,'2026-02-14 17:30:00',0),
	 ('industry_013',13,'农林牧渔','AGRICULTURE','sys_industry',1,NULL,'2026-02-14 17:30:00',0),
	 ('industry_014',14,'能源化工','ENERGY','sys_industry',1,NULL,'2026-02-14 17:30:00',0),
	 ('industry_015',15,'政府机构','GOVERNMENT','sys_industry',1,NULL,'2026-02-14 17:30:00',0);

-- ============================================
-- 执行说明
-- ============================================
-- 1. 请在执行前备份数据库
-- 2. 建议在测试环境先执行验证
-- 3. 如果某些ID已存在，请根据实际情况调整
-- 4. 执行完成后，建议重启应用以刷新缓存
-- ============================================
