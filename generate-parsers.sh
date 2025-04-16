#!/bin/bash

# Exit on error
set -e

# Function to print usage
print_usage() {
    echo "Usage: $0 --project-dir <maven-project-dir> --root-class <root-class> --output-dir <output-dir> --parser-package <parser-package> [--custom-parser-dir <custom-parser-dir>]"
    echo ""
    echo "Arguments:"
    echo "  --project-dir        The directory containing the Maven project for which to generate parsers"
    echo "  --root-class         The fully qualified name of the root class to generate parsers for"
    echo "  --output-dir         Directory where generated parsers will be written"
    echo "  --parser-package     Package name for the generated parsers"
    echo "  --custom-parser-dir  (Optional) Directory containing custom parser implementations"
    exit 1
}

echo "Debug: Script started"

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --project-dir)
            PROJECT_DIR="$2"
            shift 2
            ;;
        --root-class)
            ROOT_CLASS="$2"
            shift 2
            ;;
        --output-dir)
            OUTPUT_DIR="$2"
            shift 2
            ;;
        --parser-package)
            PARSER_PACKAGE="$2"
            shift 2
            ;;
        --custom-parser-dir)
            CUSTOM_PARSER_DIR="$2"
            shift 2
            ;;
        *)
            echo "Unknown argument: $1"
            print_usage
            ;;
    esac
done

# Validate required arguments
if [[ -z "$PROJECT_DIR" ]] || [[ -z "$ROOT_CLASS" ]] || [[ -z "$OUTPUT_DIR" ]] || [[ -z "$PARSER_PACKAGE" ]]; then
    echo "Error: Missing required arguments"
    print_usage
fi

# Validate project directory exists and contains pom.xml
if [[ ! -d "$PROJECT_DIR" ]] || [[ ! -f "$PROJECT_DIR/pom.xml" ]]; then
    echo "Error: Project directory does not exist or does not contain pom.xml: $PROJECT_DIR"
    exit 1
fi

# Get the absolute path of the project directory
PROJECT_DIR=$(cd "$PROJECT_DIR" && pwd)

# Get the directory where this script is located
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
GENERATOR_CORE_DIR="$SCRIPT_DIR/gwt-beans-codegen-core" # Define generator dir

echo "Debug: Script Directory: $SCRIPT_DIR"
echo "Debug: Generator Core Directory: $GENERATOR_CORE_DIR"
echo "Debug: Checking contents of Generator Core Directory:"
ls -la "$GENERATOR_CORE_DIR" || echo "Debug: Failed to list contents of $GENERATOR_CORE_DIR"

echo "Debug: Building in directory: $GENERATOR_CORE_DIR"
echo "Debug: Checking if directory exists..."
if [[ ! -d "$GENERATOR_CORE_DIR" ]]; then
    echo "Error: Build directory does not exist: $GENERATOR_CORE_DIR"
    exit 1
fi

echo "Debug: Checking for pom.xml..."
if [[ ! -f "$GENERATOR_CORE_DIR/pom.xml" ]]; then
    echo "Error: pom.xml not found in build directory"
    exit 1
fi

echo "Building GWT Bean Parser Generator..."
(cd "$GENERATOR_CORE_DIR" && mvn clean -q package -DskipTests)

# --- Added Debugging ---
echo "Debug: Attempting to get generator version..."
# Use N/A as default to distinguish from empty string
# Add -B for batch mode to further suppress logs
GENERATOR_VERSION=$(cd "$GENERATOR_CORE_DIR" && mvn -B help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null || echo "N/A")
MVN_EXIT_CODE=$?
echo "Debug: mvn help:evaluate exit code: $MVN_EXIT_CODE"
# Trim potential whitespace/newlines from Maven output
GENERATOR_VERSION=$(echo "$GENERATOR_VERSION" | xargs)
# Set to unknown-version if mvn failed or returned N/A or empty
if [[ $MVN_EXIT_CODE -ne 0 || "$GENERATOR_VERSION" == "N/A" || -z "$GENERATOR_VERSION" ]]; then
    echo "Debug: Failed to get version from Maven, using default."
    GENERATOR_VERSION="unknown-version"
fi
echo "Debug: Captured Generator Version: '$GENERATOR_VERSION'"

echo "Debug: Attempting to get git hash..."
# Use N/A as default
GENERATOR_GIT_HASH=$(cd "$GENERATOR_CORE_DIR" && git rev-parse --short HEAD 2>/dev/null || echo "N/A")
GIT_EXIT_CODE=$?
echo "Debug: git rev-parse exit code: $GIT_EXIT_CODE"
# Trim potential whitespace/newlines
GENERATOR_GIT_HASH=$(echo "$GENERATOR_GIT_HASH" | xargs)
# Set to unknown-git if git failed or returned N/A or empty
if [[ $GIT_EXIT_CODE -ne 0 || "$GENERATOR_GIT_HASH" == "N/A" || -z "$GENERATOR_GIT_HASH" ]]; then
    echo "Debug: Failed to get git hash, using default."
    GENERATOR_GIT_HASH="unknown-git"
fi
echo "Debug: Captured Generator Git Hash: '$GENERATOR_GIT_HASH'"
# --- End Debugging additions ---

echo "Debug: Checking if JAR was built..."
GENERATOR_JAR="$GENERATOR_CORE_DIR/target/gwt-beans-codegen-shaded.jar"
if [[ ! -f "$GENERATOR_JAR" ]]; then
    echo "Error: Build failed - JAR file not found at $GENERATOR_JAR"
    exit 1
fi
echo "Debug: Generator JAR found: $GENERATOR_JAR"

echo "Building target project to ensure classes are available..."
(cd "$PROJECT_DIR" && mvn clean -q compile)

echo "Getting dependency classpath from target project..."
# Get both the project's classes and its dependencies
TARGET_CP="$PROJECT_DIR/target/classes"
DEP_CP=$(cd "$PROJECT_DIR" && mvn dependency:build-classpath -U -q -DincludeScope=compile -Dmdep.outputFile=/dev/stdout)
FULL_CP="$TARGET_CP:$DEP_CP"

echo "Debug: Using classpath: $FULL_CP"
echo "Debug: Using JAR: $GENERATOR_JAR"

# Prepare generator arguments
GENERATOR_ARGS=(
    "--root-class" "$ROOT_CLASS"
    "--output-dir" "$OUTPUT_DIR"
    "--parser-package" "$PARSER_PACKAGE"
)

if [[ -n "$CUSTOM_PARSER_DIR" ]]; then
    GENERATOR_ARGS+=("--custom-parser-dir" "$CUSTOM_PARSER_DIR")
fi

# --- Modified java command (ensure variables are passed, remove line continuations) ---
echo "Running GWT Bean Parser Generator..."
echo "Debug: Running with arguments: ${GENERATOR_ARGS[@]}"
echo "Debug: Passing system properties: -Dgenerator.version='$GENERATOR_VERSION' -Dgenerator.githash='$GENERATOR_GIT_HASH'"
java -Dgenerator.version="$GENERATOR_VERSION" -Dgenerator.githash="$GENERATOR_GIT_HASH" -cp "$FULL_CP:$GENERATOR_JAR" nl.aerius.codegen.ParserGenerator "${GENERATOR_ARGS[@]}"
# --- End Modified java command ---

echo "Parser generation completed successfully!"
