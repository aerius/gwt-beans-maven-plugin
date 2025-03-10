# JSON Utilities

A lightweight JSON handling library built on Jackson.

## Importing

Add the following dependency to your Maven project:

```xml
<dependency>
    <groupId>nl.aerius</groupId>
    <artifactId>json-utils</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Usage

Import the JSON handling classes:

```java
import nl.aerius.json.JSONObjectHandle;
import nl.aerius.json.JSONArrayHandle;
import nl.aerius.json.JSONValue;
```

### Example

```java
// Parse JSON string
JSONObjectHandle json = JSONObjectHandle.fromText("{\"name\":\"John\",\"age\":30}");

// Get values
String name = json.getString("name");
int age = json.getInteger("age");

// Check if a field exists
if (json.has("address")) {
    JSONObjectHandle address = json.getObject("address");
    // ...
}

// Handle arrays
JSONArrayHandle array = json.getArray("items");
array.forEachString(item -> {
    // Process each string item
});
```

## Requirements

- Java 17 or higher
- Jackson Databind
