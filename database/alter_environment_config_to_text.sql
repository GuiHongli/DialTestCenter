-- Modify environment_config field type from JSONB to TEXT
-- Execution time: 2025-09-23
-- Reason: Simplify data storage and avoid JSON type conversion issues

-- Modify environment_config field type in test_case table
ALTER TABLE test_case ALTER COLUMN environment_config TYPE TEXT;

-- Add comment
COMMENT ON COLUMN test_case.environment_config IS 'Environment configuration (text format, supports JSON string)';
