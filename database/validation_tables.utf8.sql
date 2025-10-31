SET client_encoding = 'UTF8';
-- =====================================================
-- 鐢ㄤ緥闆嗘牎楠屽姛鑳芥暟鎹簱琛ㄨ剼鏈?
-- =====================================================
-- 鍒涘缓鏃堕棿: 2025-10-30
-- 鎻忚堪: 鐢ㄤ緥闆嗘牎楠屼换鍔″拰缁撴灉瀛樺偍琛?
-- 鏁版嵁搴? PostgreSQL 16+
-- =====================================================

-- 鍒犻櫎宸插瓨鍦ㄧ殑琛紙濡傛灉瀛樺湪锛?
DROP TABLE IF EXISTS test_case_set_validation_result CASCADE;
DROP TABLE IF EXISTS test_case_set_validation_task CASCADE;

-- =====================================================
-- 1. 鏍￠獙浠诲姟琛?
-- =====================================================
CREATE TABLE test_case_set_validation_task (
    id BIGSERIAL PRIMARY KEY,
    test_case_set_id BIGINT NOT NULL,
    task_id VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INTEGER DEFAULT 0,
    started_time TIMESTAMP,
    completed_time TIMESTAMP,
    error_message TEXT,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_test_case_set_validation_task FOREIGN KEY (test_case_set_id) 
        REFERENCES test_case_set(id) ON DELETE CASCADE,
    CONSTRAINT chk_task_status CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

-- 鍒涘缓绱㈠紩
CREATE INDEX idx_validation_task_test_case_set_id ON test_case_set_validation_task(test_case_set_id);
CREATE INDEX idx_validation_task_task_id ON test_case_set_validation_task(task_id);
CREATE INDEX idx_validation_task_status ON test_case_set_validation_task(status);
CREATE INDEX idx_validation_task_created_time ON test_case_set_validation_task(created_time);

-- 娣诲姞琛ㄦ敞閲?
COMMENT ON TABLE test_case_set_validation_task IS '鐢ㄤ緥闆嗘牎楠屼换鍔¤〃';
COMMENT ON COLUMN test_case_set_validation_task.id IS '涓婚敭ID';
COMMENT ON COLUMN test_case_set_validation_task.test_case_set_id IS '鐢ㄤ緥闆咺D';
COMMENT ON COLUMN test_case_set_validation_task.task_id IS '浠诲姟ID锛圲UID锛?;
COMMENT ON COLUMN test_case_set_validation_task.status IS '浠诲姟鐘舵€侊細PENDING/RUNNING/COMPLETED/FAILED/CANCELLED';
COMMENT ON COLUMN test_case_set_validation_task.progress IS '浠诲姟杩涘害锛?-100锛?;
COMMENT ON COLUMN test_case_set_validation_task.started_time IS '浠诲姟寮€濮嬫墽琛屾椂闂?;
COMMENT ON COLUMN test_case_set_validation_task.completed_time IS '浠诲姟瀹屾垚鏃堕棿';
COMMENT ON COLUMN test_case_set_validation_task.error_message IS '閿欒淇℃伅锛堜换鍔″け璐ユ椂锛?;
COMMENT ON COLUMN test_case_set_validation_task.created_time IS '浠诲姟鍒涘缓鏃堕棿';

-- =====================================================
-- 2. 鏍￠獙缁撴灉琛紙瀛樺偍瀹屾暣鐨勬牎楠岀粨鏋淛SON锛?
-- =====================================================
CREATE TABLE test_case_set_validation_result (
    id BIGSERIAL PRIMARY KEY,
    test_case_set_id BIGINT NOT NULL UNIQUE,
    task_id VARCHAR(64) NOT NULL,
    validation_result JSONB NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_test_case_set_validation_result FOREIGN KEY (test_case_set_id) 
        REFERENCES test_case_set(id) ON DELETE CASCADE
);

-- 鍒涘缓绱㈠紩
CREATE INDEX idx_validation_result_test_case_set_id ON test_case_set_validation_result(test_case_set_id);
CREATE INDEX idx_validation_result_task_id ON test_case_set_validation_result(task_id);
CREATE INDEX idx_validation_result_created_time ON test_case_set_validation_result(created_time);

-- 娣诲姞琛ㄦ敞閲?
COMMENT ON TABLE test_case_set_validation_result IS '鐢ㄤ緥闆嗘牎楠岀粨鏋滆〃锛圝SON鏍煎紡瀛樺偍锛?;
COMMENT ON COLUMN test_case_set_validation_result.id IS '涓婚敭ID';
COMMENT ON COLUMN test_case_set_validation_result.test_case_set_id IS '鐢ㄤ緥闆咺D锛堝敮涓€锛?;
COMMENT ON COLUMN test_case_set_validation_result.task_id IS '鍏宠仈鐨勪换鍔D';
COMMENT ON COLUMN test_case_set_validation_result.validation_result IS '鏍￠獙缁撴灉JSON鏁版嵁';
COMMENT ON COLUMN test_case_set_validation_result.created_time IS '鍒涘缓鏃堕棿';
COMMENT ON COLUMN test_case_set_validation_result.updated_time IS '鏇存柊鏃堕棿';

-- =====================================================
-- 3. 鍒涘缓鏇存柊updated_time鐨勮Е鍙戝櫒鍑芥暟锛堝鏋滀笉瀛樺湪锛?
-- =====================================================
CREATE OR REPLACE FUNCTION update_validation_result_updated_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 鍒涘缓瑙﹀彂鍣?
DROP TRIGGER IF EXISTS trigger_update_validation_result_updated_time ON test_case_set_validation_result;
CREATE TRIGGER trigger_update_validation_result_updated_time
    BEFORE UPDATE ON test_case_set_validation_result
    FOR EACH ROW
    EXECUTE FUNCTION update_validation_result_updated_time();

-- =====================================================
-- 4. 鍒濆鍖栨搷浣滅被鍨嬫灇涓撅紙濡傛灉涓嶅瓨鍦級
-- =====================================================
-- 妫€鏌ュ苟鎻掑叆VALIDATE鎿嶄綔绫诲瀷
INSERT INTO operation_types (code, name_zh, name_en, description_zh, description_en, is_active) 
SELECT 'VALIDATE', '鏍￠獙', 'Validate', '鎵ц鏍￠獙鎿嶄綔', 'Perform validation operation', true
WHERE NOT EXISTS (
    SELECT 1 FROM operation_types WHERE code = 'VALIDATE'
);


