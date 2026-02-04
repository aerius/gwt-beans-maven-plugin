# GWT Beans Codegen Test JSON Stubs

JVM-based test stubs for GWT JSON classes (`nl.aerius.wui.service.json.JSONObjectHandle` etc.), backed by Jackson. This module enables unit testing of generated parsers without requiring a GWT runtime by swapping GWT JSON imports for these JVM-compatible implementations.

## Importing

Add the following test dependency to your Maven project:

```xml
<dependency>
    <groupId>nl.aerius</groupId>
    <artifactId>gwt-beans-codegen-test-json</artifactId>
    <version>1.1.0</version>
    <scope>test</scope>
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
