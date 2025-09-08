#!/bin/bash

# Code format validation and correction script
# Used to ensure all Java files comply with project rule requirements

set -e

echo "=== Code Format Validation and Correction Script ==="

# 1. Check Javadoc empty line format
echo "1. Checking Javadoc empty line format..."
PROBLEM_FILES=$(find backend/src/main/java -name "*.java" -exec grep -l "^ \* $" {} \; 2>/dev/null || true)

if [ -n "$PROBLEM_FILES" ]; then
    echo "Found Javadoc empty line format issues, fixing..."
    find backend/src/main/java -name "*.java" -exec sed -i '' 's/^ \* $/ */' {} \;
    echo "✅ Javadoc empty line format corrected"
else
    echo "✅ Javadoc empty line format is correct"
fi

# 2. Check copyright header
echo ""
echo "2. Checking copyright header..."
MISSING_COPYRIGHT=$(find backend/src/main/java -name "*.java" -exec sh -c 'if ! head -3 "$1" | grep -q "Copyright"; then echo "$1"; fi' _ {} \; 2>/dev/null || true)

if [ -n "$MISSING_COPYRIGHT" ]; then
    echo "Warning: The following files are missing copyright header:"
    echo "$MISSING_COPYRIGHT"
else
    echo "✅ All files have copyright header"
fi

# 3. Check import order
echo ""
echo "3. Checking import order..."
echo "Note: Import order needs manual checking, ensure following order:"
echo "  1. com.huawei.* (Huawei company packages)"
echo "  2. org.* (Open source organization packages)"
echo "  3. java.* (Java standard packages)"
echo "  4. javax.* (Java standard packages)"

# 4. Check class definition format
echo ""
echo "4. Checking class definition format..."
echo "Note: No empty lines between class definition and variable declaration"

# 5. Check file ending newline
echo ""
echo "5. Checking file ending newline..."
MISSING_NEWLINE=$(find backend/src/main/java -name "*.java" -exec sh -c 'if [ "$(tail -c1 "$1")" != "" ]; then echo "$1"; fi' _ {} \; 2>/dev/null || true)

if [ -n "$MISSING_NEWLINE" ]; then
    echo "The following files are missing newline at the end:"
    echo "$MISSING_NEWLINE"
    echo "Fixing..."
    find backend/src/main/java -name "*.java" -exec sh -c 'echo "" >> "$1"' _ {} \;
    echo "✅ File ending newline corrected"
else
    echo "✅ All files have newline at the end"
fi

# 6. Check log message language
echo ""
echo "6. Checking log message language..."
CHINESE_LOGS=$(find backend/src/main/java -name "*.java" -exec grep -l "logger.*[\u4e00-\u9fff]" {} \; 2>/dev/null || true)

if [ -n "$CHINESE_LOGS" ]; then
    echo "Warning: The following files contain Chinese log messages:"
    echo "$CHINESE_LOGS"
else
    echo "✅ All log messages use English"
fi

# 7. Check exception handling
echo ""
echo "7. Checking exception handling..."
BASE_EXCEPTIONS=$(find backend/src/main/java -name "*.java" -exec grep -l "catch.*Exception[^a-zA-Z]" {} \; 2>/dev/null || true)

if [ -n "$BASE_EXCEPTIONS" ]; then
    echo "Warning: The following files may use base exception classes:"
    echo "$BASE_EXCEPTIONS"
else
    echo "✅ Exception handling complies with standards"
fi

# 8. Check conditional statements
echo ""
echo "8. Checking conditional statements..."
echo "Note: All if-else if chains must have a final else branch"
echo "Manual review recommended for if-else if statements"

echo ""
echo "=== Validation Complete ==="
echo "Recommendation: Run this script regularly to ensure code format compliance"
