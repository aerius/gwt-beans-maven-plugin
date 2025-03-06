# GWT Bean Parser Generator - Temporary Script

A temporary script to generate JSON parsers for Java beans while the Maven plugin is under development.

## Usage

```bash
./generate-parsers.sh \
  --project-dir /path/to/your/maven/project \
  --root-class com.example.YourRootClass \
  --output-dir src/generated/java \
  --parser-package com.example.parser \
  [--custom-parser-dir src/main/java/custom/parsers]
```

### Required Arguments

- `--project-dir`: The Maven project containing your beans
- `--root-class`: Fully qualified name of the root class to generate parsers for
- `--output-dir`: Where to write the generated parsers
- `--parser-package`: Package name for the generated parsers

### Optional Arguments

- `--custom-parser-dir`: Directory containing custom parser implementations

## Requirements

- Java 11+
- Maven 3.6+
- Bash shell
