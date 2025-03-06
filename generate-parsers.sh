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
echo "Debug: Script directory is $SCRIPT_DIR"

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

echo "Debug: Building in directory: $SCRIPT_DIR/gwt-beans-codegen-core"
echo "Debug: Checking if directory exists..."
if [[ ! -d "$SCRIPT_DIR/gwt-beans-codegen-core" ]]; then
    echo "Error: Build directory does not exist: $SCRIPT_DIR/gwt-beans-codegen-core"
    exit 1
fi

echo "Debug: Checking for pom.xml..."
if [[ ! -f "$SCRIPT_DIR/gwt-beans-codegen-core/pom.xml" ]]; then
    echo "Error: pom.xml not found in build directory"
    exit 1
fi

echo "Building GWT Bean Parser Generator..."
(cd "$SCRIPT_DIR/gwt-beans-codegen-core" && mvn clean -q package -DskipTests)

echo "Debug: Checking if JAR was built..."
if [[ ! -f "$SCRIPT_DIR/gwt-beans-codegen-core/target/gwt-beans-codegen-shaded.jar" ]]; then
    echo "Error: Build failed - JAR file not found"
    exit 1
fi

echo "Building target project to ensure classes are available..."
(cd "$PROJECT_DIR" && mvn clean -q compile)

echo "Getting dependency classpath from target project..."
# Get both the project's classes and its dependencies
TARGET_CP="$PROJECT_DIR/target/classes"
DEP_CP=$(cd "$PROJECT_DIR" && mvn dependency:build-classpath -q -DincludeScope=compile -Dmdep.outputFile=/dev/stdout)
FULL_CP="$TARGET_CP:$DEP_CP"

echo "Debug: Using classpath: $FULL_CP"
echo "Debug: Using JAR: $SCRIPT_DIR/gwt-beans-codegen-core/target/gwt-beans-codegen-shaded.jar"

# Prepare generator arguments
GENERATOR_ARGS=(
    "--root-class" "$ROOT_CLASS"
    "--output-dir" "$OUTPUT_DIR"
    "--parser-package" "$PARSER_PACKAGE"
)

if [[ -n "$CUSTOM_PARSER_DIR" ]]; then
    GENERATOR_ARGS+=("--custom-parser-dir" "$CUSTOM_PARSER_DIR")
fi

echo "Running GWT Bean Parser Generator..."
echo "Debug: Running with arguments: ${GENERATOR_ARGS[@]}"
java -cp "$FULL_CP:$SCRIPT_DIR/gwt-beans-codegen-core/target/gwt-beans-codegen-shaded.jar" nl.overheid.aerius.codegen.ParserGenerator "${GENERATOR_ARGS[@]}"

echo "Parser generation completed successfully!" 