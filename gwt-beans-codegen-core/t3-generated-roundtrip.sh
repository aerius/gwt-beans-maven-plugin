#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p test-logs

# Log file for this test
LOG_FILE="test-logs/step3-generated-roundtrip.log"

{
  echo "=== Step 3: Generated Parser Roundtrip Test ==="
  echo "Testing if the automatically generated parsers can correctly"
  echo "serialize and deserialize objects. This validates that the"
  echo "generated parsers work functionally, regardless of their"
  echo "exact implementation."
  echo ""
  echo "Running GeneratedParserRoundTripTest..."
  echo "Full output will be written to: $LOG_FILE"
  echo ""
} | tee "$LOG_FILE"

# Run the test and capture output
mvn test -q -Dtest=GeneratedParserRoundTripTest >> "$LOG_FILE" 2>&1
status=$?

# Show just the summary from the log
if [ $status -eq 0 ]; then
    echo "✓ Step 3: Generated parser roundtrip validation passed"
    echo "See $LOG_FILE for full output"
    exit 0
else
    echo "✗ Step 3: Generated parser roundtrip validation failed"
    echo "See $LOG_FILE for full output"
    echo ""
    echo "=== Generated Parser Test Details ==="
    # Extract the relevant section from the log file
    awk '/Generated parser roundtrip test failed/,/junit/' "$LOG_FILE" | \
        grep -v "junit" | \
        grep -v "^[[:space:]]*$" | \
        sed 's/^[[:space:]]*//'
    exit 1
fi
