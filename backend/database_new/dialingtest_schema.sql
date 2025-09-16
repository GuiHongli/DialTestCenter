--
-- PostgreSQL database dump
--

\restrict M9ZpamNbTlBdZmB7QElakj0aJuuvp5GIfh8dCSeXoxrdJORWZJq3DXtX819v5gQ

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
    network_topology character varying(500),
    business_category character varying(200),
    app_name character varying(200),
    test_steps text,
    expected_result text,
    script_exists boolean DEFAULT false NOT NULL,
    created_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: TABLE test_case; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.test_case IS '测试用例表';


--
-- Name: COLUMN test_case.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.id IS '主键ID';


--
-- Name: COLUMN test_case.test_case_set_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.test_case_set_id IS '关联的用例集ID';


--
-- Name: COLUMN test_case.case_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.case_name IS '用例名称';


--
-- Name: COLUMN test_case.case_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.case_number IS '用例编号';


--
-- Name: COLUMN test_case.network_topology; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.network_topology IS '逻辑组网';


--
-- Name: COLUMN test_case.business_category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.business_category IS '业务大类';


--
-- Name: COLUMN test_case.app_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.app_name IS '用例App';


--
-- Name: COLUMN test_case.test_steps; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.test_steps IS '测试步骤';


--
-- Name: COLUMN test_case.expected_result; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.expected_result IS '预期结果';


--
-- Name: COLUMN test_case.script_exists; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.script_exists IS '脚本是否存在';


--
-- Name: COLUMN test_case.created_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.created_time IS '创建时间';


--
-- Name: COLUMN test_case.updated_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.updated_time IS '更新时间';


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
    creator character varying(100) NOT NULL,
    file_size bigint NOT NULL,
    description character varying(1000),
    created_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    business character varying(50) DEFAULT 'VPN阻断业务'::character varying NOT NULL,
    file_format character varying(10) DEFAULT 'zip'::character varying NOT NULL,
    sha512 character varying(128)
);


--
-- Name: TABLE test_case_set; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.test_case_set IS '用例集表';


--
-- Name: COLUMN test_case_set.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.id IS '主键ID';


--
-- Name: COLUMN test_case_set.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.name IS '用例集名称';


--
-- Name: COLUMN test_case_set.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.version IS '用例集版本';


--
-- Name: COLUMN test_case_set.file_content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.file_content IS '文件内容（二进制数据）';


--
-- Name: COLUMN test_case_set.creator; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.creator IS '创建人';


--
-- Name: COLUMN test_case_set.file_size; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.file_size IS '文件大小（字节）';


--
-- Name: COLUMN test_case_set.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.description IS '描述';


--
-- Name: COLUMN test_case_set.created_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.created_time IS '创建时间';


--
-- Name: COLUMN test_case_set.updated_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.updated_time IS '更新时间';


--
-- Name: COLUMN test_case_set.business; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.business IS '业务类型，如：VPN阻断业务';


--
-- Name: COLUMN test_case_set.file_format; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.file_format IS '文件格式：zip 或 tar.gz';


--
-- Name: COLUMN test_case_set.sha512; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case_set.sha512 IS '文件内容的SHA512哈希值';


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
-- Name: dial_user id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dial_user ALTER COLUMN id SET DEFAULT nextval('public.dial_user_id_seq'::regclass);


--
-- Name: operation_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_log ALTER COLUMN id SET DEFAULT nextval('public.operation_log_id_seq'::regclass);


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
-- Name: operation_log operation_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operation_log
    ADD CONSTRAINT operation_log_pkey PRIMARY KEY (id);


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
-- Name: idx_case_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_case_name ON public.test_case USING btree (case_name);


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
-- Name: idx_test_case_created_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_created_time ON public.test_case USING btree (created_time);


--
-- Name: idx_test_case_set_business; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_set_business ON public.test_case_set USING btree (business);


--
-- Name: idx_test_case_set_created_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_set_created_time ON public.test_case_set USING btree (created_time);


--
-- Name: idx_test_case_set_creator; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_set_creator ON public.test_case_set USING btree (creator);


--
-- Name: idx_test_case_set_file_format; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_set_file_format ON public.test_case_set USING btree (file_format);


--
-- Name: idx_test_case_set_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_set_id ON public.test_case USING btree (test_case_set_id);


--
-- Name: idx_test_case_set_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_set_name ON public.test_case_set USING btree (name);


--
-- Name: idx_test_case_set_sha512; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_set_sha512 ON public.test_case_set USING btree (sha512);


--
-- Name: idx_username; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_username ON public.user_role USING btree (username);


--
-- Name: software_package update_software_package_updated_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_software_package_updated_time BEFORE UPDATE ON public.software_package FOR EACH ROW EXECUTE FUNCTION public.update_updated_time_column();


--
-- Name: test_case_set update_test_case_set_updated_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_test_case_set_updated_time BEFORE UPDATE ON public.test_case_set FOR EACH ROW EXECUTE FUNCTION public.update_updated_time_column();


--
-- Name: test_case update_test_case_updated_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_test_case_updated_time BEFORE UPDATE ON public.test_case FOR EACH ROW EXECUTE FUNCTION public.update_updated_time_column();


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

\unrestrict M9ZpamNbTlBdZmB7QElakj0aJuuvp5GIfh8dCSeXoxrdJORWZJq3DXtX819v5gQ

