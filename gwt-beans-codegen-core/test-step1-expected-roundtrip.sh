#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p test-logs

# Log file for this test
LOG_FILE="test-logs/step1-expected-roundtrip.log"

{
  echo "=== Step 1: Expected Parser Roundtrip Test ==="
  echo "Testing if our handwritten reference parsers in"
  echo "src/test/resources/parsers/expected/ can correctly"
  echo "serialize and deserialize objects. This validates"
  echo "that our reference implementation is correct."
  echo ""
  echo "Running ExpectedParserRoundTripTest..."
  echo "Full output will be written to: $LOG_FILE"
  echo ""
} | tee "$LOG_FILE"

# Run the test and capture output
mvn test -q -Dtest=ExpectedParserRoundTripTest >> "$LOG_FILE" 2>&1
status=$?

# Show just the summary from the log
if [ $status -eq 0 ]; then
    echo "✓ Step 1: Expected parser roundtrip validation passed"
    echo "See $LOG_FILE for full output"
    exit 0
else
    echo "✗ Step 1: Expected parser roundtrip validation failed"
    echo "See $LOG_FILE for full output"
    echo ""
    echo "=== Roundtrip Test Details ==="
    # Extract the relevant section from the log file
    awk '/Roundtrip test failed/,/junit/' "$LOG_FILE" | \
        grep -v "junit" | \
        grep -v "^[[:space:]]*$" | \
        sed 's/^[[:space:]]*//'
    exit 1
fi 