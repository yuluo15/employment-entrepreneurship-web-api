CREATE TABLE public.sys_user (
	id varchar(255) NOT NULL,
	login_identity varchar(255) NOT NULL,
	email varchar(100) NULL,
	phone varchar(20) NULL,
	"password" varchar(255) NOT NULL,
	nickname varchar(255) NULL,
	real_name varchar(255) NULL,
	avatar varchar(500) NULL,
	gender int2 NULL DEFAULT 0,
	role_key varchar(255) NOT NULL,
	owner_id varchar(255) NULL,
	status int2 NULL DEFAULT 1,
	is_deleted int2 NULL DEFAULT 0,
	login_ip varchar(50) NULL,
	login_date timestamp NULL,
	create_by varchar(255) NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT sys_user_pk PRIMARY KEY (id),
	CONSTRAINT uk_email UNIQUE (email),
	CONSTRAINT uk_phone UNIQUE (phone),
	CONSTRAINT uk_system_code UNIQUE (login_identity)
);


CREATE TABLE public.sys_school (
	id varchar NOT NULL,
	"name" varchar NULL,
	code varchar NOT NULL,
	contact_phone varchar NULL,
	address varchar NULL,
	status int4 NULL DEFAULT 1,
	logo varchar NULL,
	create_by varchar NULL,
	update_time timestamp NULL,
	create_time timestamp NULL,
	is_deleted int4 NOT NULL DEFAULT 0,
	default_account_id varchar NOT NULL,
	CONSTRAINT sys_school_pk PRIMARY KEY (id),
	CONSTRAINT sys_school_un UNIQUE (code)
);


CREATE TABLE public.sys_role (
	id varchar NOT NULL,
	role_name varchar NOT NULL,
	CONSTRAINT role_pk PRIMARY KEY (id)
);


CREATE TABLE public.sys_notice (
	notice_id varchar(64) NOT NULL,
	notice_title varchar(200) NOT NULL,
	notice_type varchar(10) NOT NULL,
	notice_content text NULL,
	status int2 NULL DEFAULT 0,
	is_top int2 NULL DEFAULT 0,
	view_count int4 NULL DEFAULT 0,
	attachments text NULL,
	create_by varchar(64) NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	publish_time timestamp NULL,
	CONSTRAINT sys_notice_pkey PRIMARY KEY (notice_id)
);
CREATE INDEX idx_notice_publish_time ON public.sys_notice USING btree (publish_time);
CREATE INDEX idx_notice_status ON public.sys_notice USING btree (status);
CREATE INDEX idx_notice_type ON public.sys_notice USING btree (notice_type);


CREATE TABLE public.sys_message (
	id varchar(255) NOT NULL,
	receiver_id varchar(255) NOT NULL,
	title varchar(255) NULL,
	"content" text NULL,
	"type" int2 NULL DEFAULT 1,
	is_read int2 NULL DEFAULT 0,
	ref_id varchar(255) NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT sys_message_pkey PRIMARY KEY (id)
);


CREATE TABLE public.sys_dict_type (
	id varchar(255) NOT NULL,
	dict_name varchar(255) NOT NULL,
	dict_type varchar(255) NOT NULL,
	status int2 NOT NULL DEFAULT 1,
	remark varchar(500) NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	is_deleted int2 NOT NULL DEFAULT 0,
	CONSTRAINT pk_sys_dict_type PRIMARY KEY (id),
	CONSTRAINT uk_dict_type UNIQUE (dict_type)
);


CREATE TABLE public.sys_dict_data (
	id varchar(255) NOT NULL,
	dict_sort int4 NOT NULL DEFAULT 0,
	dict_label varchar(255) NULL,
	dict_value varchar(255) NULL,
	dict_type varchar(255) NOT NULL,
	status int2 NOT NULL DEFAULT 1,
	remark varchar(500) NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	is_deleted int2 NOT NULL DEFAULT 0,
	CONSTRAINT pk_sys_dict_data PRIMARY KEY (id)
);
CREATE INDEX idx_dict_type_sort ON public.sys_dict_data USING btree (dict_type, dict_sort);
CREATE INDEX idx_dict_type_status ON public.sys_dict_data USING btree (dict_type, status);


CREATE TABLE public.sys_company (
	id varchar NOT NULL,
	name varchar NULL,
	code varchar NULL,
	industry varchar NULL,
	"scale" varchar NULL,
	logo varchar NULL,
	description varchar NULL,
	address varchar NULL,
	contact_person varchar NULL,
	contact_phone varchar NULL,
	email varchar NULL,
	license_url varchar NULL,
	status int4 NULL DEFAULT 0,
	audit_reason varchar NULL,
	audit_time timestamp NULL,
	auditor_id varchar NULL,
	create_time timestamp NULL,
	update_time timestamp NULL,
	is_deleted int4 NOT NULL DEFAULT 0,
	default_account_id varchar NOT NULL,
	funding_stage varchar NULL,
	tags varchar NULL,
	website varchar NULL,
	images varchar NULL,
	CONSTRAINT sys_company_pk PRIMARY KEY (id),
	CONSTRAINT sys_company_un UNIQUE (code)
);


CREATE TABLE public.biz_teacher (
	teacher_id varchar(255) NOT NULL,
	user_id varchar(255) NOT NULL,
	school_id varchar(255) NOT NULL,
	"name" varchar(255) NOT NULL,
	employee_no varchar(255) NOT NULL,
	gender varchar(255) NULL,
	college_name varchar(255) NULL,
	title varchar(255) NULL,
	expertise varchar(255) NULL,
	intro varchar(500) NULL,
	guidance_count int4 NULL DEFAULT 0,
	rating_score numeric(2, 1) NULL DEFAULT 5.0,
	phone varchar(255) NULL,
	email varchar(255) NULL,
	create_time timestamptz NULL DEFAULT now(),
	update_time timestamptz NULL DEFAULT now(),
	CONSTRAINT biz_teacher_pkey PRIMARY KEY (teacher_id),
	CONSTRAINT biz_teacher_user_id_key UNIQUE (user_id)
);
CREATE INDEX idx_biz_teacher_school_id ON public.biz_teacher USING btree (school_id);


CREATE TABLE public.biz_student_resume (
	resume_id varchar(255) NOT NULL,
	student_id varchar(255) NOT NULL,
	expected_position varchar(255) NULL,
	expected_salary varchar(255) NULL,
	target_city varchar(255) NULL,
	job_type int2 NULL DEFAULT 1,
	arrival_time varchar(255) NULL,
	personal_summary text NULL,
	skills text NULL,
	education_history jsonb NULL DEFAULT '[]'::jsonb,
	internship_exp jsonb NULL DEFAULT '[]'::jsonb,
	project_exp jsonb NULL DEFAULT '[]'::jsonb,
	certificates jsonb NULL DEFAULT '[]'::jsonb,
	is_public int2 NULL DEFAULT 1,
	resume_score int2 NULL DEFAULT 0,
	view_count int4 NULL DEFAULT 0,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT biz_student_resume_pkey PRIMARY KEY (resume_id),
	CONSTRAINT uk_student_id UNIQUE (student_id)
);
CREATE INDEX idx_resume_internship ON public.biz_student_resume USING gin (internship_exp);
CREATE INDEX idx_resume_project ON public.biz_student_resume USING gin (project_exp);


CREATE TABLE public.biz_student (
	student_id varchar(255) NOT NULL,
	user_id varchar(255) NOT NULL,
	school_id varchar(255) NOT NULL,
	student_name varchar(255) NULL,
	student_no varchar(255) NOT NULL,
	gender varchar NULL,
	college_name varchar(255) NULL,
	major_name varchar(255) NULL,
	class_name varchar(255) NULL,
	education varchar(255) NULL,
	enrollment_year int2 NULL,
	graduation_year int2 NULL,
	phone varchar(255) NULL,
	email varchar(255) NULL,
	employment_status varchar(255) NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT pk_biz_student PRIMARY KEY (student_id),
	CONSTRAINT uk_user_id UNIQUE (user_id)
);
CREATE INDEX idx_school_id ON public.biz_student USING btree (school_id);


CREATE TABLE public.biz_project_comment (
	id varchar(255) NOT NULL,
	project_id varchar(255) NOT NULL,
	teacher_id varchar(255) NOT NULL,
	"content" text NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	project_name varchar(255) NULL,
	teacher_name varchar(255) NULL,
	CONSTRAINT biz_project_comment_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_comment_project ON public.biz_project_comment USING btree (project_id);
CREATE INDEX idx_comment_teacher ON public.biz_project_comment USING btree (teacher_id);


CREATE TABLE public.biz_project (
	project_id varchar(255) NOT NULL,
	user_id varchar(255) NOT NULL,
	school_id varchar(255) NOT NULL,
	project_name varchar(255) NOT NULL,
	logo varchar(500) NULL DEFAULT ''::character varying,
	"domain" varchar(255) NOT NULL,
	description text NULL,
	team_size int4 NULL DEFAULT 1,
	jobs_created int4 NULL DEFAULT 0,
	mentor_id varchar NULL,
	mentor_name varchar(255) NULL,
	mentor_comment varchar(500) NULL,
	status varchar(255) NULL DEFAULT '0'::character varying,
	audit_reason varchar(255) NULL,
	audit_time timestamp NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	needs varchar NULL,
	slogan varchar NULL,
	CONSTRAINT biz_project_pkey PRIMARY KEY (project_id)
);
CREATE INDEX idx_biz_project_domain ON public.biz_project USING btree (domain);
CREATE INDEX idx_biz_project_project_name ON public.biz_project USING btree (project_name);
CREATE INDEX idx_biz_project_school_id ON public.biz_project USING btree (school_id);
CREATE INDEX idx_biz_project_user_id ON public.biz_project USING btree (user_id);


CREATE TABLE public.biz_job_delivery (
	id varchar(255) NOT NULL,
	student_id varchar(255) NOT NULL,
	job_id varchar(255) NOT NULL,
	company_id varchar(255) NOT NULL,
	resume_id varchar(255) NOT NULL,
	status varchar(255) NULL DEFAULT 'DELIVERED'::character varying,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	handle_reply varchar NULL,
	CONSTRAINT biz_job_delivery_pkey PRIMARY KEY (id),
	CONSTRAINT uk_biz_job_delivery_stu_job UNIQUE (student_id, job_id)
);
CREATE INDEX idx_biz_job_delivery_job ON public.biz_job_delivery USING btree (job_id);
CREATE INDEX idx_biz_job_delivery_status ON public.biz_job_delivery USING btree (status);
CREATE INDEX idx_biz_job_delivery_student ON public.biz_job_delivery USING btree (student_id);


CREATE TABLE public.biz_job (
	job_id varchar(255) NOT NULL,
	company_id varchar(255) NOT NULL,
	job_name varchar(255) NOT NULL,
	salary_range varchar(255) NOT NULL,
	city varchar(255) NOT NULL,
	education varchar(255) NULL,
	tags varchar(255) NULL,
	description text NULL,
	requirement text NULL,
	contact_phone varchar(255) NULL,
	status int4 NULL DEFAULT 1,
	view_count int4 NULL DEFAULT 0,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	hr_id varchar(255) NOT NULL,
	experience varchar NULL,
	audit int4 NULL,
	reason varchar NULL,
	job_type int4 NULL,
	CONSTRAINT biz_job_pkey PRIMARY KEY (job_id)
);
CREATE INDEX idx_biz_job_city ON public.biz_job USING btree (city);
CREATE INDEX idx_biz_job_company ON public.biz_job USING btree (company_id);


CREATE TABLE public.biz_interview (
	id varchar(255) NOT NULL,
	delivery_id varchar(255) NOT NULL,
	company_id varchar(255) NOT NULL,
	student_id varchar(255) NOT NULL,
	job_id varchar(255) NOT NULL,
	hr_id varchar(255) NOT NULL,
	interview_time timestamp NOT NULL,
	duration int4 NULL DEFAULT 30,
	"type" int2 NULL DEFAULT 1,
	"location" varchar(500) NULL,
	notes text NULL,
	status int2 NULL DEFAULT 0,
	interview_score int2 NULL,
	interview_comment text NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT pk_biz_interview PRIMARY KEY (id)
);
CREATE INDEX idx_interview_company ON public.biz_interview USING btree (company_id);
CREATE INDEX idx_interview_delivery ON public.biz_interview USING btree (delivery_id);
CREATE INDEX idx_interview_time ON public.biz_interview USING btree (interview_time);


CREATE TABLE public.biz_hr (
	hr_id varchar(255) NOT NULL,
	user_id varchar(255) NOT NULL,
	company_id varchar(255) NOT NULL,
	"name" varchar(255) NOT NULL,
	avatar varchar(500) NULL DEFAULT ''::character varying,
	"position" varchar(255) NULL DEFAULT '招聘专员'::character varying,
	work_phone varchar(255) NULL,
	work_email varchar(255) NULL,
	wechat_code varchar(255) NULL,
	status int4 NULL DEFAULT 1,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT biz_hr_pkey PRIMARY KEY (hr_id),
	CONSTRAINT biz_hr_user_id_key UNIQUE (user_id)
);
CREATE INDEX idx_biz_hr_company_id ON public.biz_hr USING btree (company_id);


CREATE TABLE public.biz_collection (
	id varchar(255) NOT NULL,
	user_id varchar(255) NOT NULL,
	target_id varchar(255) NOT NULL,
	"type" varchar(255) NOT NULL,
	title varchar(255) NULL,
	image varchar(500) NULL,
	sub_title varchar(255) NULL,
	create_time timestamptz NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT biz_collection_pkey PRIMARY KEY (id),
	CONSTRAINT biz_collection_type_check CHECK (((type)::text = ANY ((ARRAY['JOB'::character varying, 'PROJECT'::character varying])::text[]))),
	CONSTRAINT uk_biz_collection_user_target_type UNIQUE (user_id, target_id, type)
);
CREATE INDEX idx_biz_collection_user_id ON public.biz_collection USING btree (user_id);