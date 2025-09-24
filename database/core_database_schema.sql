-- =====================================================
-- DialTestCenter 核心数据库结构
-- =====================================================
-- 创建时间: 2025-01-24
-- 描述: 拨测控制中心核心数据库结构，包含主要业务表
-- 数据库: PostgreSQL 16+
-- 字符集: UTF-8
-- =====================================================

-- 设置数据库参数
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- =====================================================
-- 1. 删除已存在的表（按依赖关系顺序删除）
-- =====================================================
DROP TABLE IF EXISTS test_case CASCADE;
DROP TABLE IF EXISTS app_type CASCADE;
DROP TABLE IF EXISTS test_case_set CASCADE;
DROP TABLE IF EXISTS software_package CASCADE;
DROP TABLE IF EXISTS operation_logs CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- =====================================================
-- 2. 创建序列
-- =====================================================

-- 应用类型序列
CREATE SEQUENCE IF NOT EXISTS public.app_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 角色序列
CREATE SEQUENCE IF NOT EXISTS public.roles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 软件包序列
CREATE SEQUENCE IF NOT EXISTS public.software_package_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 测试用例序列
CREATE SEQUENCE IF NOT EXISTS public.test_case_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 测试用例集序列
CREATE SEQUENCE IF NOT EXISTS public.test_case_set_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 用户角色序列
CREATE SEQUENCE IF NOT EXISTS public.user_roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 操作记录序列
CREATE SEQUENCE IF NOT EXISTS public.operation_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- =====================================================
-- 3. 创建核心表结构
-- =====================================================

-- 3.1 角色表
CREATE TABLE public.roles (
    id integer NOT NULL DEFAULT nextval('public.roles_id_seq'::regclass),
    code character varying(20) NOT NULL,
    name_zh character varying(50) NOT NULL,
    name_en character varying(50) NOT NULL,
    description_zh text,
    description_en text,
    CONSTRAINT roles_pkey PRIMARY KEY (id),
    CONSTRAINT roles_code_key UNIQUE (code)
);

-- 3.2 用户角色表
CREATE TABLE public.user_roles (
    id bigint NOT NULL DEFAULT nextval('public.user_roles_id_seq'::regclass),
    username character varying(50) NOT NULL,
    role character varying(20) NOT NULL,
    CONSTRAINT user_roles_pkey PRIMARY KEY (id),
    CONSTRAINT user_roles_username_role_key UNIQUE (username, role)
);

-- 3.3 测试用例集表
CREATE TABLE public.test_case_set (
    id bigint NOT NULL DEFAULT nextval('public.test_case_set_id_seq'::regclass),
    name character varying(200) NOT NULL,
    version character varying(50) NOT NULL,
    file_content bytea NOT NULL,
    file_size bigint NOT NULL,
    description character varying(1000),
    sha256 character varying(64),
    business_zh character varying(50) DEFAULT 'VPN阻断'::character varying,
    business_en character varying(50) DEFAULT 'VPN_BLOCK'::character varying,
    CONSTRAINT test_case_set_pkey PRIMARY KEY (id),
    CONSTRAINT uk_name_version UNIQUE (name, version)
);

-- 3.4 测试用例表
CREATE TABLE public.test_case (
    id bigint NOT NULL DEFAULT nextval('public.test_case_id_seq'::regclass),
    test_case_set_id bigint NOT NULL,
    case_name character varying(200) NOT NULL,
    case_number character varying(100) NOT NULL,
    test_steps text,
    expected_result text,
    business_category character varying(200),
    app_name character varying(200),
    dependencies_package text,
    dependencies_rule text,
    environment_config text,
    script_exists boolean DEFAULT false NOT NULL,
    CONSTRAINT test_case_pkey PRIMARY KEY (id),
    CONSTRAINT uk_test_case_set_case_number UNIQUE (test_case_set_id, case_number),
    CONSTRAINT fk_test_case_set_id FOREIGN KEY (test_case_set_id) REFERENCES public.test_case_set(id) ON DELETE CASCADE
);

-- 3.5 应用类型表
CREATE TABLE public.app_type (
    id bigint NOT NULL DEFAULT nextval('public.app_type_id_seq'::regclass),
    business_category character varying(200) NOT NULL,
    app_name character varying(200) NOT NULL,
    description character varying(500),
    CONSTRAINT app_type_pkey PRIMARY KEY (id),
    CONSTRAINT uk_business_app UNIQUE (business_category, app_name)
);

-- 3.6 软件包表
CREATE TABLE public.software_package (
    id bigint NOT NULL DEFAULT nextval('public.software_package_id_seq'::regclass),
    software_name character varying(255) NOT NULL,
    file_content bytea NOT NULL,
    file_format character varying(10) NOT NULL,
    sha512 character varying(128),
    platform character varying(20) NOT NULL,
    creator character varying(100) NOT NULL,
    file_size bigint NOT NULL,
    description character varying(1000),
    created_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT software_package_pkey PRIMARY KEY (id),
    CONSTRAINT uk_software_package_sha512 UNIQUE (sha512),
    CONSTRAINT uk_software_package_software_name UNIQUE (software_name)
);

-- 3.7 操作记录表
CREATE TABLE public.operation_logs (
    id bigint NOT NULL DEFAULT nextval('public.operation_logs_id_seq'::regclass),
    username character varying(50) NOT NULL,
    operation_type character varying(20) NOT NULL,
    operation_target character varying(50) NOT NULL,
    operation_description_zh text,
    operation_description_en text,
    operation_data jsonb,
    operation_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT operation_logs_pkey PRIMARY KEY (id),
    CONSTRAINT chk_description_not_empty CHECK (((operation_description_zh IS NOT NULL) OR (operation_description_en IS NOT NULL)))
);

-- =====================================================
-- 4. 创建索引
-- =====================================================

-- 测试用例集表索引
CREATE INDEX idx_test_case_set_name ON public.test_case_set USING btree (name);

-- 测试用例表索引
CREATE INDEX idx_case_number ON public.test_case USING btree (case_number);
CREATE INDEX idx_script_exists ON public.test_case USING btree (script_exists);
CREATE INDEX idx_test_case_set_id ON public.test_case USING btree (test_case_set_id);

-- 应用类型表索引
CREATE INDEX idx_app_type_app ON public.app_type USING btree (app_name);
CREATE INDEX idx_app_type_category ON public.app_type USING btree (business_category);

-- 软件包表索引
CREATE INDEX idx_software_package_created_time ON public.software_package USING btree (created_time);
CREATE INDEX idx_software_package_creator ON public.software_package USING btree (creator);
CREATE INDEX idx_software_package_file_format ON public.software_package USING btree (file_format);
CREATE INDEX idx_software_package_platform ON public.software_package USING btree (platform);
CREATE INDEX idx_software_package_sha512 ON public.software_package USING btree (sha512);
CREATE INDEX idx_software_package_software_name ON public.software_package USING btree (software_name);

-- 用户角色表索引
CREATE INDEX idx_user_roles_role ON public.user_roles USING btree (role);
CREATE INDEX idx_user_roles_username ON public.user_roles USING btree (username);

-- 操作记录表索引
CREATE INDEX idx_operation_logs_operation_target ON public.operation_logs USING btree (operation_target);
CREATE INDEX idx_operation_logs_operation_time ON public.operation_logs USING btree (operation_time);
CREATE INDEX idx_operation_logs_operation_type ON public.operation_logs USING btree (operation_type);
CREATE INDEX idx_operation_logs_type_time ON public.operation_logs USING btree (operation_type, operation_time);
CREATE INDEX idx_operation_logs_username ON public.operation_logs USING btree (username);
CREATE INDEX idx_operation_logs_username_time ON public.operation_logs USING btree (username, operation_time);

-- =====================================================
-- 5. 插入初始数据
-- =====================================================

-- 插入角色定义
INSERT INTO public.roles (code, name_zh, name_en, description_zh, description_en) VALUES
('ADMIN', '管理员', 'Administrator', '拥有所有权限', 'Has all permissions'),
('OPERATOR', '操作员', 'Operator', '可以执行所有拨测相关操作', 'Can execute all dial test related operations'),
('BROWSER', '浏览者', 'Browser', '只能查看', 'View only'),
('EXECUTOR', '执行者', 'Executor', '用于执行者注册', 'For executor registration')
ON CONFLICT (code) DO NOTHING;

-- 插入默认管理员用户
INSERT INTO public.user_roles (username, role) VALUES ('admin', 'ADMIN')
ON CONFLICT (username, role) DO NOTHING;

-- =====================================================
-- 6. 添加表注释
-- =====================================================

COMMENT ON TABLE public.roles IS 'Role enumeration table';
COMMENT ON TABLE public.user_roles IS 'User role relationship table';
COMMENT ON TABLE public.test_case_set IS 'Test case set table';
COMMENT ON TABLE public.test_case IS 'Test case table';
COMMENT ON TABLE public.app_type IS 'Application type table';
COMMENT ON TABLE public.software_package IS 'Software package management table';
COMMENT ON TABLE public.operation_logs IS 'Operation logs table with Chinese and English descriptions';

-- =====================================================
-- 7. 验证数据插入
-- =====================================================

-- 显示插入的数据统计
SELECT 'Roles inserted:' as status, count(*) as count FROM public.roles;
SELECT 'Admin user inserted:' as status, count(*) as count FROM public.user_roles WHERE username = 'admin' AND role = 'ADMIN';

-- =====================================================
-- 脚本执行完成
-- =====================================================
