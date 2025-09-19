-- Operation Log Module Database Script
-- Created: 2024-01-15

-- 1. Operation Types Enum Table
CREATE TABLE IF NOT EXISTS operation_types (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name_zh VARCHAR(50) NOT NULL,
    name_en VARCHAR(50) NOT NULL,
    description_zh TEXT,
    description_en TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Operation Targets Enum Table
CREATE TABLE IF NOT EXISTS operation_targets (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name_zh VARCHAR(50) NOT NULL,
    name_en VARCHAR(50) NOT NULL,
    description_zh TEXT,
    description_en TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Operation Logs Table
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    operation_target VARCHAR(50) NOT NULL,
    operation_description_zh TEXT,
    operation_description_en TEXT,
    operation_data JSONB,
    language VARCHAR(10) NOT NULL DEFAULT 'zh',
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Insert Basic Operation Types
INSERT INTO operation_types (code, name_zh, name_en, description_zh, description_en) VALUES
('CREATE', 'Create', 'Create', 'Create new record', 'Create new record'),
('UPDATE', 'Update', 'Update', 'Update existing record', 'Update existing record'),
('DELETE', 'Delete', 'Delete', 'Delete record', 'Delete record'),
('VIEW', 'View', 'View', 'View record', 'View record'),
('LOGIN', 'Login', 'Login', 'User login', 'User login'),
('LOGOUT', 'Logout', 'Logout', 'User logout', 'User logout'),
('EXPORT', 'Export', 'Export', 'Export data', 'Export data'),
('IMPORT', 'Import', 'Import', 'Import data', 'Import data'),
('UPLOAD', 'Upload', 'Upload', 'Upload file', 'Upload file'),
('DOWNLOAD', 'Download', 'Download', 'Download file', 'Download file')
ON CONFLICT (code) DO NOTHING;

-- 5. Insert Basic Operation Targets
INSERT INTO operation_targets (code, name_zh, name_en, description_zh, description_en) VALUES
('USER', 'User Management', 'User Management', 'User related operations', 'User related operations'),
('USER_ROLE', 'Role Management', 'Role Management', 'User role related operations', 'User role related operations'),
('TEST_CASE_SET', 'Test Case Set', 'Test Case Set', 'Test case set related operations', 'Test case set related operations'),
('SOFTWARE_PACKAGE', 'Software Package Management', 'Software Package Management', 'Software package related operations', 'Software package related operations'),
('OPERATION_LOG', 'Operation Log', 'Operation Log', 'Operation log related operations', 'Operation log related operations'),
('SYSTEM', 'System', 'System', 'System related operations', 'System related operations')
ON CONFLICT (code) DO NOTHING;

-- 6. Create Indexes
CREATE INDEX IF NOT EXISTS idx_operation_logs_username ON operation_logs(username);
CREATE INDEX IF NOT EXISTS idx_operation_logs_operation_type ON operation_logs(operation_type);
CREATE INDEX IF NOT EXISTS idx_operation_logs_operation_target ON operation_logs(operation_target);
CREATE INDEX IF NOT EXISTS idx_operation_logs_language ON operation_logs(language);
CREATE INDEX IF NOT EXISTS idx_operation_logs_operation_time ON operation_logs(operation_time);

-- 7. Create Composite Indexes
CREATE INDEX IF NOT EXISTS idx_operation_logs_username_time ON operation_logs(username, operation_time);
CREATE INDEX IF NOT EXISTS idx_operation_logs_type_time ON operation_logs(operation_type, operation_time);
CREATE INDEX IF NOT EXISTS idx_operation_logs_language_time ON operation_logs(language, operation_time);

-- 8. Insert Test Data
INSERT INTO operation_logs (
    username, operation_type, operation_target, 
    operation_description_zh, operation_description_en, 
    operation_data, language, operation_time
) VALUES 
(
    'admin', 'CREATE', 'USER', 
    'Create user: testuser1', 
    'Create user: testuser1',
    '{"username": "testuser1", "role": "USER"}', 
    'zh', '2024-01-15 14:30:25'
),
(
    'admin', 'CREATE', 'USER', 
    'Create user: testuser2', 
    'Create user: testuser2',
    '{"username": "testuser2", "role": "USER"}', 
    'zh', '2024-01-15 14:31:10'
),
(
    'admin', 'UPDATE', 'USER', 
    'Update user: testuser1', 
    'Update user: testuser1',
    '{"old": {"role": "USER"}, "new": {"role": "ADMIN"}}', 
    'en', '2024-01-15 14:35:00'
)
ON CONFLICT DO NOTHING;
