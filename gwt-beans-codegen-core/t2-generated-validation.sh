#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p test-logs

# Log file for this test
LOG_FILE="test-logs/step2-generated-validation.log"

{
  echo "=== Step 2: Generated Parser Validation Test ==="
  echo "Testing if our parser generator produces exactly the same"
  echo "code as our reference parsers. This validates that the"
  echo "generator creates parsers matching our expected format."
  echo ""
  echo "Running GeneratedParserValidationTest..."
  echo "Full output will be written to: $LOG_FILE"
  echo ""
} | tee "$LOG_FILE"

# Run the test and capture output
mvn test -q -Dtest=GeneratedParserValidationTest >> "$LOG_FILE" 2>&1
status=$?

# Show just the summary from the log
if [ $status -eq 0 ]; then
    echo "✓ Step 2: Generated parser validation passed"
    echo "See $LOG_FILE for full output"
    exit 0
else
    echo "✗ Step 2: Generated parser validation failed"
    echo "See $LOG_FILE for full output"
    echo ""
    echo "=== Parser Comparison Details ==="
    # Extract the relevant section from the log file
    awk '/Generated parser content doesn.t match expected/,/junit/' "$LOG_FILE" | \
        grep -v "junit" | \
        grep -v "^[[:space:]]*$" | \
        sed 's/^[[:space:]]*//'
    exit 1
fi 