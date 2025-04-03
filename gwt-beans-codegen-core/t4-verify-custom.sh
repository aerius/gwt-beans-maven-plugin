#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p test-logs

# Log file for this test
LOG_FILE="test-logs/step4-custom-parsers.log"

{
  echo "=== Step 4: Custom Parser Verification Test ==="
  echo "Testing if our parser generator correctly identifies and uses"
  echo "custom parsers instead of generating new ones. This validates"
  echo "that developers can provide their own parser implementations"
  echo "for specific types when needed."
  echo ""
  echo "Running CustomParserTest..."
  echo "Full output will be written to: $LOG_FILE"
  echo ""
} | tee "$LOG_FILE"

# Run the test and capture output
mvn test -q -Dtest=CustomParserTest >> "$LOG_FILE" 2>&1
status=$?

# Show just the summary from the log
if [ $status -eq 0 ]; then
    echo "✓ Step 4: Custom parser verification passed"
    echo "See $LOG_FILE for full output"
    exit 0
else
    echo "✗ Step 4: Custom parser verification failed"
    echo "See $LOG_FILE for full output"
    echo ""
    echo "=== Custom Parser Verification Details ==="
    # Extract the relevant section from the log file
    awk '/Custom parser verification failed/,/junit/' "$LOG_FILE" | \
        grep -v "junit" | \
        grep -v "^[[:space:]]*$" | \
        sed 's/^[[:space:]]*//'
    exit 1
fi 