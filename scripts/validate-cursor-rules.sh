#!/bin/bash

# Cursor Rules Validation Script
# This script validates all Cursor rules in the .cursor/rules directory

set -e

echo "🔍 Validating Cursor Rules..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TOTAL_RULES=0
VALID_RULES=0
INVALID_RULES=0

# Function to validate a single rule file
validate_rule() {
    local rule_file="$1"
    local rule_name=$(basename "$rule_file" .mdc)
    
    echo -e "${BLUE}📋 Validating rule: $rule_name${NC}"
    
    # Check if file exists
    if [[ ! -f "$rule_file" ]]; then
        echo -e "${RED}❌ Rule file not found: $rule_file${NC}"
        return 1
    fi
    
    # Check if file is not empty
    if [[ ! -s "$rule_file" ]]; then
        echo -e "${RED}❌ Rule file is empty: $rule_file${NC}"
        return 1
    fi
    
    # Check for required frontmatter
    if ! grep -q "^---$" "$rule_file"; then
        echo -e "${RED}❌ Missing frontmatter delimiter (---) in: $rule_file${NC}"
        return 1
    fi
    
    # Check for frontmatter content
    local frontmatter_lines=$(awk '/^---$/{flag=1;next}/^---$/{flag=0}flag' "$rule_file" | wc -l)
    if [[ $frontmatter_lines -eq 0 ]]; then
        echo -e "${RED}❌ Empty frontmatter in: $rule_file${NC}"
        return 1
    fi
    
    # Check for required metadata
    local has_globs=$(grep -q "globs:" "$rule_file" && echo "true" || echo "false")
    local has_always_apply=$(grep -q "alwaysApply:" "$rule_file" && echo "true" || echo "false")
    local has_description=$(grep -q "description:" "$rule_file" && echo "true" || echo "false")
    
    if [[ "$has_globs" == "false" && "$has_always_apply" == "false" ]]; then
        echo -e "${RED}❌ Missing required metadata (globs or alwaysApply) in: $rule_file${NC}"
        return 1
    fi
    
    if [[ "$has_description" == "false" ]]; then
        echo -e "${RED}❌ Missing description in: $rule_file${NC}"
        return 1
    fi
    
    # Check for markdown content after frontmatter (simplified check)
    local total_lines=$(wc -l < "$rule_file")
    local frontmatter_end=$(grep -n "^---$" "$rule_file" | tail -1 | cut -d: -f1)
    if [[ $frontmatter_end -lt $total_lines ]]; then
        local content_lines=$((total_lines - frontmatter_end))
        if [[ $content_lines -le 1 ]]; then
            echo -e "${RED}❌ No content after frontmatter in: $rule_file${NC}"
            return 1
        fi
    else
        echo -e "${RED}❌ No content after frontmatter in: $rule_file${NC}"
        return 1
    fi
    
    # Check for proper markdown structure
    if ! grep -q "^#" "$rule_file"; then
        echo -e "${RED}❌ Missing main heading (#) in: $rule_file${NC}"
        return 1
    fi
    
    # Check for examples (should have both correct and incorrect examples)
    local has_correct_examples=$(grep -q "✅" "$rule_file" && echo "true" || echo "false")
    local has_incorrect_examples=$(grep -q "❌" "$rule_file" && echo "true" || echo "false")
    
    if [[ "$has_correct_examples" == "false" ]]; then
        echo -e "${YELLOW}⚠️  Missing correct examples (✅) in: $rule_file${NC}"
    fi
    
    if [[ "$has_incorrect_examples" == "false" ]]; then
        echo -e "${YELLOW}⚠️  Missing incorrect examples (❌) in: $rule_file${NC}"
    fi
    
    echo -e "${GREEN}✅ Rule validation passed: $rule_name${NC}"
    return 0
}

# Function to check rule consistency
check_rule_consistency() {
    echo -e "${BLUE}🔗 Checking rule consistency...${NC}"
    
    # Check for duplicate rules
    local duplicate_rules=$(find .cursor/rules -name "*.mdc" -exec basename {} \; | sort | uniq -d)
    if [[ -n "$duplicate_rules" ]]; then
        echo -e "${RED}❌ Duplicate rule files found:${NC}"
        echo "$duplicate_rules"
        return 1
    fi
    
    # Check for cross-references
    local rules_with_references=$(grep -l "mdc:" .cursor/rules/*.mdc | wc -l)
    echo -e "${GREEN}✅ Found $rules_with_references rules with cross-references${NC}"
    
    return 0
}

# Function to generate rule summary
generate_summary() {
    echo -e "${BLUE}📊 Rule Summary:${NC}"
    echo -e "Total rules: $TOTAL_RULES"
    echo -e "Valid rules: $VALID_RULES"
    echo -e "Invalid rules: $INVALID_RULES"
    
    if [[ $INVALID_RULES -eq 0 ]]; then
        echo -e "${GREEN}🎉 All rules are valid!${NC}"
        return 0
    else
        echo -e "${RED}❌ Some rules need attention${NC}"
        return 1
    fi
}

# Main validation logic
main() {
    # Check if .cursor/rules directory exists
    if [[ ! -d ".cursor/rules" ]]; then
        echo -e "${RED}❌ .cursor/rules directory not found${NC}"
        exit 1
    fi
    
    # Validate each rule file
    for rule_file in .cursor/rules/*.mdc; do
        if [[ -f "$rule_file" ]]; then
            TOTAL_RULES=$((TOTAL_RULES + 1))
            if validate_rule "$rule_file"; then
                VALID_RULES=$((VALID_RULES + 1))
            else
                INVALID_RULES=$((INVALID_RULES + 1))
            fi
            echo ""
        fi
    done
    
    # Check rule consistency
    if ! check_rule_consistency; then
        INVALID_RULES=$((INVALID_RULES + 1))
    fi
    
    echo ""
    generate_summary
    
    # Exit with appropriate code
    if [[ $INVALID_RULES -eq 0 ]]; then
        exit 0
    else
        exit 1
    fi
}

# Run main function
main "$@"
