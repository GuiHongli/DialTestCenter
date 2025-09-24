--
-- PostgreSQL database dump
--

\restrict Ls0IPwKkK4bgcvPZcqJlzJY27bm0ZNbbOhvhiAXVlfFG7zOAJ9U1ViPSncGdfIn

-- Dumped from database version 16.10
-- Dumped by pg_dump version 16.10

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: update_updated_time_column(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.update_updated_time_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: app_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_type (
    id bigint NOT NULL,
    business_category character varying(200) NOT NULL,
    app_name character varying(200) NOT NULL,
    description character varying(500)
);


--
-- Name: TABLE app_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.app_type IS 'Application type table';


--
-- Name: COLUMN app_type.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.app_type.id IS 'Primary key ID, auto increment';


--
-- Name: COLUMN app_type.business_category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.app_type.business_category IS 'Business category';


--
-- Name: COLUMN app_type.app_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.app_type.app_name IS 'Application name';


--
-- Name: COLUMN app_type.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.app_type.description IS 'Description';


--
-- Name: app_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: app_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_type_id_seq OWNED BY public.app_type.id;


--
-- Name: dial_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dial_user (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    last_login_time timestamp without time zone,
    created_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_time timestamp without time zone
);


--
-- Name: TABLE dial_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.dial_user IS '用户表，存储用户基本信息';


--
-- Name: COLUMN dial_user.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.dial_user.id IS '用户ID，主键';


--
-- Name: COLUMN dial_user.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.dial_user.username IS '用户名，唯一标识';


--
-- Name: COLUMN dial_user.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.dial_user.password IS '用户密码，加密存储';


--
-- Name: COLUMN dial_user.last_login_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.dial_user.last_login_time IS '最后登录时间';


--
-- Name: COLUMN dial_user.created_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.dial_user.created_time IS '创建时间';


--
-- Name: COLUMN dial_user.updated_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.dial_user.updated_time IS '更新时间';


--
-- Name: dial_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dial_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: dial_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.dial_user_id_seq OWNED BY public.dial_user.id;


--
-- Name: dial_users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dial_users (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    last_login_time timestamp without time zone
);


--
-- Name: dial_users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dial_users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: dial_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.dial_users_id_seq OWNED BY public.dial_users.id;


--
-- Name: operation_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.operation_log (
    id bigint NOT NULL,
    username character varying(100) NOT NULL,
    operation_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    operation_type character varying(50) NOT NULL,
    target character varying(200) NOT NULL,
    description text
);


--
-- Name: TABLE operation_log; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.operation_log IS '操作记录表，存储用户所有操作记录';


--
-- Name: COLUMN operation_log.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.operation_log.id IS '操作记录ID，主键';


--
-- Name: COLUMN operation_log.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.operation_log.username IS '操作用户名';


--
-- Name: COLUMN operation_log.operation_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.operation_log.operation_time IS '操作时间';


--
-- Name: COLUMN operation_log.operation_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.operation_log.operation_type IS '操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT等';


--
-- Name: COLUMN operation_log.target; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.operation_log.target IS '操作对象：如用户、角色、测试用例集等';


--
-- Name: COLUMN operation_log.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.operation_log.description IS '操作描述：详细的操作说明';


--
-- Name: operation_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.operation_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: operation_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.operation_log_id_seq OWNED BY public.operation_log.id;


--
-- Name: operation_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.operation_logs (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    operation_type character varying(20) NOT NULL,
    operation_target character varying(50) NOT NULL,
    operation_description_zh text,
    operation_description_en text,
    operation_data jsonb,
    operation_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_description_not_empty CHECK (((operation_description_zh IS NOT NULL) OR (operation_description_en IS NOT NULL)))
);


--
-- Name: operation_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.operation_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: operation_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.operation_logs_id_seq OWNED BY public.operation_logs.id;


--
-- Name: operation_targets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.operation_targets (
    id integer NOT NULL,
    code character varying(50) NOT NULL,
    name_zh character varying(50) NOT NULL,
    name_en character varying(50) NOT NULL,
    description_zh text,
    description_en text,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: operation_targets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.operation_targets_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: operation_targets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.operation_targets_id_seq OWNED BY public.operation_targets.id;


--
-- Name: operation_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.operation_types (
    id integer NOT NULL,
    code character varying(20) NOT NULL,
    name_zh character varying(50) NOT NULL,
    name_en character varying(50) NOT NULL,
    description_zh text,
    description_en text,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: operation_types_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.operation_types_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: operation_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.operation_types_id_seq OWNED BY public.operation_types.id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    code character varying(20) NOT NULL,
    name_zh character varying(50) NOT NULL,
    name_en character varying(50) NOT NULL,
    description_zh text,
    description_en text
);


--
-- Name: TABLE roles; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.roles IS 'Role enumeration table';


--
-- Name: COLUMN roles.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.roles.id IS 'Primary key ID, auto increment';


--
-- Name: COLUMN roles.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.roles.code IS 'Role code';


--
-- Name: COLUMN roles.name_zh; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.roles.name_zh IS 'Chinese name';


--
-- Name: COLUMN roles.name_en; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.roles.name_en IS 'English name';


--
-- Name: COLUMN roles.description_zh; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.roles.description_zh IS 'Chinese description';


--
-- Name: COLUMN roles.description_en; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.roles.description_en IS 'English description';


--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.roles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: software_package; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.software_package (
    id bigint NOT NULL,
    software_name character varying(255) NOT NULL,
    file_content bytea NOT NULL,
    file_format character varying(10) NOT NULL,
    sha512 character varying(128),
    platform character varying(20) NOT NULL,
    creator character varying(100) NOT NULL,
    file_size bigint NOT NULL,
    description character varying(1000),
    created_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: TABLE software_package; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.software_package IS '软件包管理表';


--
-- Name: COLUMN software_package.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.id IS '主键ID';


--
-- Name: COLUMN software_package.software_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.software_name IS '软件名称（完整文件名，带后缀）';


--
-- Name: COLUMN software_package.file_content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.file_content IS '文件内容（二进制数据）';


--
-- Name: COLUMN software_package.file_format; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.file_format IS '文件格式：apk 或 ipa';


--
-- Name: COLUMN software_package.sha512; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.sha512 IS '文件内容的SHA512哈希值';


--
-- Name: COLUMN software_package.platform; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.platform IS '平台：android 或 ios';


--
-- Name: COLUMN software_package.creator; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.creator IS '创建者';


--
-- Name: COLUMN software_package.file_size; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.file_size IS '文件大小（字节）';


--
-- Name: COLUMN software_package.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.description IS '描述信息';


--
-- Name: COLUMN software_package.created_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.created_time IS '创建时间';


--
-- Name: COLUMN software_package.updated_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.software_package.updated_time IS '更新时间';


--
-- Name: software_package_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.software_package_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: software_package_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.software_package_id_seq OWNED BY public.software_package.id;


--
-- Name: test_case; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.test_case (
    id bigint NOT NULL,
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
    script_exists boolean DEFAULT false NOT NULL
);


--
-- Name: TABLE test_case; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.test_case IS 'Test case table';


--
-- Name: COLUMN test_case.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.id IS 'Primary key ID, auto increment';


--
-- Name: COLUMN test_case.test_case_set_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.test_case_set_id IS 'Associated test case set ID';


--
-- Name: COLUMN test_case.case_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.case_name IS 'Test case name';


--
-- Name: COLUMN test_case.case_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.case_number IS 'Test case number';


--
-- Name: COLUMN test_case.test_steps; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.test_steps IS 'Test steps';


--
-- Name: COLUMN test_case.expected_result; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.expected_result IS 'Expected result';


--
-- Name: COLUMN test_case.business_category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.business_category IS 'Business category';


--
-- Name: COLUMN test_case.app_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.app_name IS 'Application name';


--
-- Name: COLUMN test_case.dependencies_package; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.dependencies_package IS 'Dependencies package';


--
-- Name: COLUMN test_case.dependencies_rule; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.dependencies_rule IS 'Dependencies rule';


--
-- Name: COLUMN test_case.environment_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.environment_config IS 'Environment configuration (text format, supports JSON string)';


--
-- Name: COLUMN test_case.script_exists; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.script_exists IS 'Script exists flag';


--
-- Name: test_case_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.test_case_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: test_case_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.test_case_id_seq OWNED BY public.test_case.id;


--
-- Name: test_case_set; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.test_case_set (
    id bigint NOT NULL,
    name character varying(200) NOT NULL,
    version character varying(50) NOT NULL,
    file_content bytea NOT NULL,
    file_size bigint NOT NULL,
    description character varying(1000),
    sha256 character varying(64),
    business_zh character varying(50) DEFAULT 'VPN闃绘柇'::character varying,
    business_en character varying(50) DEFAULT 'VPN_BLOCK'::character varying
);


--
-- Name: TABLE test_case_set; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.test_case_set IS 'Test case set table';


--
-- Name: COLUMN test_case_set.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.id IS 'Primary key ID, auto increment';


--
-- Name: COLUMN test_case_set.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.name IS 'Test case set name';


--
-- Name: COLUMN test_case_set.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.version IS 'Test case set version';


--
-- Name: COLUMN test_case_set.file_content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.file_content IS 'File content (binary)';


--
-- Name: COLUMN test_case_set.file_size; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.file_size IS 'File size (bytes)';


--
-- Name: COLUMN test_case_set.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.description IS 'Description';


--
-- Name: COLUMN test_case_set.sha256; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.sha256 IS 'File SHA256 hash value';


--
-- Name: COLUMN test_case_set.business_zh; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.business_zh IS '涓氬姟绫诲瀷锛堜腑鏂囷級';


--
-- Name: COLUMN test_case_set.business_en; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.business_en IS 'Business type (English)';


--
-- Name: test_case_set_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.test_case_set_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: test_case_set_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.test_case_set_id_seq OWNED BY public.test_case_set.id;


--
-- Name: user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role (
    id bigint NOT NULL,
    username character varying(100) NOT NULL,
    role character varying(50) NOT NULL,
    created_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: TABLE user_role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.user_role IS '用户角色关系表';


--
-- Name: COLUMN user_role.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_role.id IS '主键ID';


--
-- Name: COLUMN user_role.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_role.username IS '用户名';


--
-- Name: COLUMN user_role.role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_role.role IS '角色：ADMIN/OPERATOR/BROWSER/EXECUTOR';


--
-- Name: COLUMN user_role.created_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_role.created_time IS '创建时间';


--
-- Name: COLUMN user_role.updated_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_role.updated_time IS '更新时间';


--
-- Name: user_role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_role_id_seq OWNED BY public.user_role.id;


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_roles (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    role character varying(20) NOT NULL
);


--
-- Name: TABLE user_roles; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.user_roles IS 'User role relationship table';


--
-- Name: COLUMN user_roles.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_roles.id IS 'Primary key ID, auto increment';


--
-- Name: COLUMN user_roles.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_roles.username IS 'Username';


--
-- Name: COLUMN user_roles.role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_roles.role IS 'Role code';


--
-- Name: user_roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_roles_id_seq OWNED BY public.user_roles.id;


--
-- Name: app_type id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_type ALTER COLUMN id SET DEFAULT nextval('public.app_type_id_seq'::regclass);


--
-- Name: dial_user id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dial_user ALTER COLUMN id SET DEFAULT nextval('public.dial_user_id_seq'::regclass);


--
-- Name: dial_users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dial_users ALTER COLUMN id SET DEFAULT nextval('public.dial_users_id_seq'::regclass);


--
-- Name: operation_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_log ALTER COLUMN id SET DEFAULT nextval('public.operation_log_id_seq'::regclass);


--
-- Name: operation_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_logs ALTER COLUMN id SET DEFAULT nextval('public.operation_logs_id_seq'::regclass);


--
-- Name: operation_targets id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_targets ALTER COLUMN id SET DEFAULT nextval('public.operation_targets_id_seq'::regclass);


--
-- Name: operation_types id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_types ALTER COLUMN id SET DEFAULT nextval('public.operation_types_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: software_package id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.software_package ALTER COLUMN id SET DEFAULT nextval('public.software_package_id_seq'::regclass);


--
-- Name: test_case id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_case ALTER COLUMN id SET DEFAULT nextval('public.test_case_id_seq'::regclass);


--
-- Name: test_case_set id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_case_set ALTER COLUMN id SET DEFAULT nextval('public.test_case_set_id_seq'::regclass);


--
-- Name: user_role id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role ALTER COLUMN id SET DEFAULT nextval('public.user_role_id_seq'::regclass);


--
-- Name: user_roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles ALTER COLUMN id SET DEFAULT nextval('public.user_roles_id_seq'::regclass);


--
-- Name: app_type app_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_type
    ADD CONSTRAINT app_type_pkey PRIMARY KEY (id);


--
-- Name: dial_user dial_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dial_user
    ADD CONSTRAINT dial_user_pkey PRIMARY KEY (id);


--
-- Name: dial_user dial_user_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dial_user
    ADD CONSTRAINT dial_user_username_key UNIQUE (username);


--
-- Name: dial_users dial_users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dial_users
    ADD CONSTRAINT dial_users_pkey PRIMARY KEY (id);


--
-- Name: dial_users dial_users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dial_users
    ADD CONSTRAINT dial_users_username_key UNIQUE (username);


--
-- Name: operation_log operation_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_log
    ADD CONSTRAINT operation_log_pkey PRIMARY KEY (id);


--
-- Name: operation_logs operation_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_logs
    ADD CONSTRAINT operation_logs_pkey PRIMARY KEY (id);


--
-- Name: operation_targets operation_targets_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_targets
    ADD CONSTRAINT operation_targets_code_key UNIQUE (code);


--
-- Name: operation_targets operation_targets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_targets
    ADD CONSTRAINT operation_targets_pkey PRIMARY KEY (id);


--
-- Name: operation_types operation_types_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_types
    ADD CONSTRAINT operation_types_code_key UNIQUE (code);


--
-- Name: operation_types operation_types_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_types
    ADD CONSTRAINT operation_types_pkey PRIMARY KEY (id);


--
-- Name: roles roles_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_code_key UNIQUE (code);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: software_package software_package_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.software_package
    ADD CONSTRAINT software_package_pkey PRIMARY KEY (id);


--
-- Name: test_case test_case_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_case
    ADD CONSTRAINT test_case_pkey PRIMARY KEY (id);


--
-- Name: test_case_set test_case_set_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_case_set
    ADD CONSTRAINT test_case_set_pkey PRIMARY KEY (id);


--
-- Name: app_type uk_business_app; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_type
    ADD CONSTRAINT uk_business_app UNIQUE (business_category, app_name);


--
-- Name: test_case_set uk_name_version; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_case_set
    ADD CONSTRAINT uk_name_version UNIQUE (name, version);


--
-- Name: software_package uk_software_package_sha512; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.software_package
    ADD CONSTRAINT uk_software_package_sha512 UNIQUE (sha512);


--
-- Name: software_package uk_software_package_software_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.software_package
    ADD CONSTRAINT uk_software_package_software_name UNIQUE (software_name);


--
-- Name: test_case uk_test_case_set_case_number; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_case
    ADD CONSTRAINT uk_test_case_set_case_number UNIQUE (test_case_set_id, case_number);


--
-- Name: user_role uk_username_role; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role
    ADD CONSTRAINT uk_username_role UNIQUE (username, role);


--
-- Name: user_role user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role
    ADD CONSTRAINT user_role_pkey PRIMARY KEY (id);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (id);


--
-- Name: user_roles user_roles_username_role_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_username_role_key UNIQUE (username, role);


--
-- Name: idx_app_type_app; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_type_app ON public.app_type USING btree (app_name);


--
-- Name: idx_app_type_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_type_category ON public.app_type USING btree (business_category);


--
-- Name: idx_case_number; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_case_number ON public.test_case USING btree (case_number);


--
-- Name: idx_dial_user_created_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dial_user_created_time ON public.dial_user USING btree (created_time);


--
-- Name: idx_dial_user_last_login_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dial_user_last_login_time ON public.dial_user USING btree (last_login_time);


--
-- Name: idx_dial_user_username; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dial_user_username ON public.dial_user USING btree (username);


--
-- Name: idx_dial_users_last_login_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dial_users_last_login_time ON public.dial_users USING btree (last_login_time);


--
-- Name: idx_dial_users_username; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dial_users_username ON public.dial_users USING btree (username);


--
-- Name: idx_operation_log_operation_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operation_log_operation_time ON public.operation_log USING btree (operation_time);


--
-- Name: idx_operation_log_operation_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operation_log_operation_type ON public.operation_log USING btree (operation_type);


--
-- Name: idx_operation_log_target; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operation_log_target ON public.operation_log USING btree (target);


--
-- Name: idx_operation_log_username; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operation_log_username ON public.operation_log USING btree (username);


--
-- Name: idx_operation_logs_operation_target; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operation_logs_operation_target ON public.operation_logs USING btree (operation_target);


--
-- Name: idx_operation_logs_operation_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operation_logs_operation_time ON public.operation_logs USING btree (operation_time);


--
-- Name: idx_operation_logs_operation_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operation_logs_operation_type ON public.operation_logs USING btree (operation_type);


--
-- Name: idx_operation_logs_type_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operation_logs_type_time ON public.operation_logs USING btree (operation_type, operation_time);


--
-- Name: idx_operation_logs_username; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operation_logs_username ON public.operation_logs USING btree (username);


--
-- Name: idx_operation_logs_username_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operation_logs_username_time ON public.operation_logs USING btree (username, operation_time);


--
-- Name: idx_role; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_role ON public.user_role USING btree (role);


--
-- Name: idx_script_exists; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_script_exists ON public.test_case USING btree (script_exists);


--
-- Name: idx_software_package_created_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_software_package_created_time ON public.software_package USING btree (created_time);


--
-- Name: idx_software_package_creator; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_software_package_creator ON public.software_package USING btree (creator);


--
-- Name: idx_software_package_file_format; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_software_package_file_format ON public.software_package USING btree (file_format);


--
-- Name: idx_software_package_platform; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_software_package_platform ON public.software_package USING btree (platform);


--
-- Name: idx_software_package_sha512; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_software_package_sha512 ON public.software_package USING btree (sha512);


--
-- Name: idx_software_package_software_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_software_package_software_name ON public.software_package USING btree (software_name);


--
-- Name: idx_test_case_set_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_set_id ON public.test_case USING btree (test_case_set_id);


--
-- Name: idx_test_case_set_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_set_name ON public.test_case_set USING btree (name);


--
-- Name: idx_user_roles_role; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_roles_role ON public.user_roles USING btree (role);


--
-- Name: idx_user_roles_username; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_roles_username ON public.user_roles USING btree (username);


--
-- Name: idx_username; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_username ON public.user_role USING btree (username);


--
-- Name: software_package update_software_package_updated_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_software_package_updated_time BEFORE UPDATE ON public.software_package FOR EACH ROW EXECUTE FUNCTION public.update_updated_time_column();


--
-- Name: user_role update_user_role_updated_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_user_role_updated_time BEFORE UPDATE ON public.user_role FOR EACH ROW EXECUTE FUNCTION public.update_updated_time_column();


--
-- Name: test_case fk_test_case_set_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_case
    ADD CONSTRAINT fk_test_case_set_id FOREIGN KEY (test_case_set_id) REFERENCES public.test_case_set(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict Ls0IPwKkK4bgcvPZcqJlzJY27bm0ZNbbOhvhiAXVlfFG7zOAJ9U1ViPSncGdfIn

