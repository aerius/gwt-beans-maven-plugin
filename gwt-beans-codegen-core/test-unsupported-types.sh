#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p test-logs

# Log file for this test
LOG_FILE="test-logs/step5-unsupported-types.log"

{
  echo "=== Step 5: Unsupported Types Test ==="
  echo "Testing if the parser generator correctly fails when"
  echo "encountering unsupported types like LocalDate and Date."
  echo "This test verifies that the generator throws an appropriate"
  echo "exception rather than silently failing or generating incorrect code."
  echo ""
  echo "Running UnsupportedTypeTest..."
  echo "Full output will be written to: $LOG_FILE"
  echo ""
} | tee "$LOG_FILE"

# Run the test and capture output
mvn test -q -Dtest=UnsupportedTypeTest >> "$LOG_FILE" 2>&1
status=$?

# Show just the summary from the log
if [ $status -eq 0 ]; then
    echo "✓ Step 5: Unsupported types test passed"
    echo "See $LOG_FILE for full output"
    exit 0
else
    echo "✗ Step 5: Unsupported types test failed"
    echo "See $LOG_FILE for full output"
    echo ""
    echo "=== Unsupported Types Test Details ==="
    # Extract the relevant section from the log file
    awk '/Unsupported type .* found in field/,/junit/' "$LOG_FILE" | \
        grep -v "junit" | \
        grep -v "^[[:space:]]*$" | \
        sed 's/^[[:space:]]*//'
    exit 1
fi 