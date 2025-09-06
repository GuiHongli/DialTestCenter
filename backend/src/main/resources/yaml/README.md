# API Documentation YAML Files

This directory contains OpenAPI/Swagger 3.0 specification files for all API endpoints.

## File Naming Convention
- Use descriptive names: `{module}-api.yaml`
- Examples: `user-role-api.yaml`, `test-case-set-api.yaml`

## Documentation Requirements
- **MANDATORY**: Generate YAML documentation for ALL new API endpoints
- **Format**: OpenAPI/Swagger 3.0 specification
- **Language**: English
- **File Size**: Each YAML file ≤ 200 lines
- **Content**: Include endpoints, schemas, examples, error responses

## File Structure
```
yaml/
├── README.md (this file)
├── user-role-api.yaml
├── test-case-set-api.yaml
└── {module}-api.yaml
```

## Content Requirements
- All HTTP methods (GET, POST, PUT, DELETE)
- Request/response schemas
- Example requests and responses
- Error response documentation
- File upload specifications (multipart/form-data)
- Supported file formats (.zip, .tar.gz)
- File size limits (100MB)

## Generation Rules
- Create YAML files simultaneously with controller implementation
- Update existing YAML files when modifying endpoints
- Maintain consistency across all API documentation
- Use proper OpenAPI 3.0 syntax and structure
