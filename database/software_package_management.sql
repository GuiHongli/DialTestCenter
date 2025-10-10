-- Software Package Management Module Database Schema
-- Database table structure for software package management module

-- Drop existing software_package table and related sequences
DROP TABLE IF EXISTS public.software_package CASCADE;
DROP SEQUENCE IF EXISTS public.software_package_id_seq;

-- Recreate software_package table according to software package management module design requirements
CREATE TABLE public.software_package (
    id BIGSERIAL PRIMARY KEY,
    software_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    file_content BYTEA NOT NULL,
    file_sha256 VARCHAR(64) NOT NULL,
    file_size BIGINT NOT NULL
);

-- Create indexes
CREATE INDEX idx_software_package_name ON public.software_package(software_name);

-- Add table and column comments
COMMENT ON TABLE public.software_package IS 'Software package management table';
COMMENT ON COLUMN public.software_package.id IS 'Primary key ID';
COMMENT ON COLUMN public.software_package.software_name IS 'Software name (complete filename with extension)';
COMMENT ON COLUMN public.software_package.description IS 'Description information';
COMMENT ON COLUMN public.software_package.file_content IS 'File content (binary data)';
COMMENT ON COLUMN public.software_package.file_sha256 IS 'SHA256 hash value of file content';
COMMENT ON COLUMN public.software_package.file_size IS 'File size in bytes';