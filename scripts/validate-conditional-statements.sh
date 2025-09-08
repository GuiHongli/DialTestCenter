#!/bin/bash

# Conditional statement validation script
# Used to check if all if-else if chains have final else branches

set -e

echo "=== Conditional Statement Validation Script ==="

# Find all if-else if chains
echo "1. Checking for if-else if chains..."
IF_ELSE_IF_FILES=$(find backend/src/main/java -name "*.java" -exec grep -l "} else if" {} \; 2>/dev/null || true)

if [ -n "$IF_ELSE_IF_FILES" ]; then
    echo "Found if-else if chains in the following files:"
    echo "$IF_ELSE_IF_FILES"
    echo ""
    
    echo "2. Analyzing if-else if chains..."
    for file in $IF_ELSE_IF_FILES; do
        echo "Checking file: $file"
        
        # Extract if-else if blocks and check for else branches
        awk '
        /} else if/ {
            in_else_if = 1
            else_if_count++
            print "  Found else if at line " NR ": " $0
        }
        /} else {/ && in_else_if {
            print "  ✅ Found else branch at line " NR ": " $0
            in_else_if = 0
        }
        /^[[:space:]]*}[[:space:]]*$/ && in_else_if {
            # Check if this is the end of an if-else if chain without else
            print "  ❌ Potential missing else branch at line " NR
            in_else_if = 0
        }
        ' "$file"
        echo ""
    done
    
    echo "3. Manual review recommended for the above files"
    echo "Ensure all if-else if chains have final else branches"
else
    echo "✅ No if-else if chains found"
fi

echo ""
echo "=== Validation Complete ==="
echo "Note: This script provides guidance. Manual code review is still required."
