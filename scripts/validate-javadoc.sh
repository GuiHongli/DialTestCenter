#!/bin/bash

# Javadoc empty line format validation and correction script
# Used to ensure all Java files have correct Javadoc empty line format

set -e

echo "=== Javadoc Empty Line Format Validation and Correction Script ==="

# Check for Javadoc empty lines containing spaces
echo "1. Checking for Javadoc empty lines containing spaces..."
PROBLEM_FILES=$(find backend/src/main/java -name "*.java" -exec grep -l "^ \* $" {} \; 2>/dev/null || true)

if [ -n "$PROBLEM_FILES" ]; then
    echo "Found the following files with incorrectly formatted Javadoc empty lines:"
    echo "$PROBLEM_FILES"
    echo ""
    
    echo "2. Fixing Javadoc empty line format..."
    find backend/src/main/java -name "*.java" -exec sed -i '' 's/^ \* $/ */' {} \;
    echo "Fix completed!"
    echo ""
    
    echo "3. Verifying fix results..."
    REMAINING_PROBLEMS=$(find backend/src/main/java -name "*.java" -exec grep -l "^ \* $" {} \; 2>/dev/null || true)
    
    if [ -n "$REMAINING_PROBLEMS" ]; then
        echo "Warning: Still have files with incorrectly formatted Javadoc empty lines:"
        echo "$REMAINING_PROBLEMS"
        exit 1
    else
        echo "✅ All Javadoc empty line formats have been corrected!"
    fi
else
    echo "✅ All Javadoc empty line formats are correct!"
fi

echo ""
echo "=== Validation Complete ==="
