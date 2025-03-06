#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p test-logs

# Log file for this test
LOG_FILE="test-logs/step0-all-tests.log"

{
  echo "=== GWT Bean Parser Generator Test Suite ==="
  echo "Running all tests in sequence to validate the parser generator."
  echo "Full output will be written to: $LOG_FILE"
  echo ""
} | tee "$LOG_FILE"

# Function to run a test and report its status
run_test() {
  local test_script=$1
  local test_name=$2
  
  echo "Running $test_name..." | tee -a "$LOG_FILE"
  
  # Run the test script
  ./$test_script
  local status=$?
  
  if [ $status -eq 0 ]; then
    echo "✓ $test_name passed" | tee -a "$LOG_FILE"
    echo "" | tee -a "$LOG_FILE"
    return 0
  else
    echo "✗ $test_name failed" | tee -a "$LOG_FILE"
    echo "" | tee -a "$LOG_FILE"
    return 1
  fi
}

# Run all tests in sequence
run_test "test-step1-expected-roundtrip.sh" "Step 1: Expected Parser Roundtrip Test"
step1_status=$?

run_test "test-step2-generated-validation.sh" "Step 2: Generated Parser Validation Test"
step2_status=$?

run_test "test-step3-generated-roundtrip.sh" "Step 3: Generated Parser Roundtrip Test"
step3_status=$?

run_test "test-step4-verify-custom-parsers.sh" "Step 4: Custom Parser Verification Test"
step4_status=$?

run_test "test-unsupported-types.sh" "Step 5: Unsupported Types Test"
step5_status=$?

# Summarize results
echo "=== Test Summary ===" | tee -a "$LOG_FILE"
[ $step1_status -eq 0 ] && echo "✓ Step 1: Expected Parser Roundtrip Test - PASSED" | tee -a "$LOG_FILE" || echo "✗ Step 1: Expected Parser Roundtrip Test - FAILED" | tee -a "$LOG_FILE"
[ $step2_status -eq 0 ] && echo "✓ Step 2: Generated Parser Validation Test - PASSED" | tee -a "$LOG_FILE" || echo "✗ Step 2: Generated Parser Validation Test - FAILED" | tee -a "$LOG_FILE"
[ $step3_status -eq 0 ] && echo "✓ Step 3: Generated Parser Roundtrip Test - PASSED" | tee -a "$LOG_FILE" || echo "✗ Step 3: Generated Parser Roundtrip Test - FAILED" | tee -a "$LOG_FILE"
[ $step4_status -eq 0 ] && echo "✓ Step 4: Custom Parser Verification Test - PASSED" | tee -a "$LOG_FILE" || echo "✗ Step 4: Custom Parser Verification Test - FAILED" | tee -a "$LOG_FILE"
[ $step5_status -eq 0 ] && echo "✓ Step 5: Unsupported Types Test - PASSED" | tee -a "$LOG_FILE" || echo "✗ Step 5: Unsupported Types Test - FAILED" | tee -a "$LOG_FILE"

# Calculate overall status
if [ $step1_status -eq 0 ] && [ $step2_status -eq 0 ] && [ $step3_status -eq 0 ] && [ $step4_status -eq 0 ] && [ $step5_status -eq 0 ]; then
  echo "" | tee -a "$LOG_FILE"
  echo "✅ All tests passed successfully!" | tee -a "$LOG_FILE"
  exit 0
else
  echo "" | tee -a "$LOG_FILE"
  echo "❌ Some tests failed. See individual test logs for details." | tee -a "$LOG_FILE"
  exit 1
fi 