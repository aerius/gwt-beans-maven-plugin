# Maven Plugin

This project is a Maven Plugin.
By adding the plugin in your project file it can generate Parsers for the configured root classes.

## Usage

Add the plugin to the build plugin section in your pom.xml:

```xml
  <build>
    <plugins>
      <plugin>
        <groupId>nl.aerius</groupId>
        <artifactId>gwt-beans-codegen-maven-plugin</artifactId>
        <version>1.0.0</version>
        <configuration>
            <rootClassNames>
              <rootClassName>some.package.SomeClass</rootClassName>
            </rootClassNames>
            <parserPackage>some.package.parsers.generated</parserPackage>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>generate-parsers</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

## Configuration

The following configuration properties are present:

### rootClassNames

A list of root class names for which parsers should be generated.
This parameter is required.

### parserPackage

The package under which the parsers will be generated.
This parameter is required.

### outputDir

The source directory the generated Parsers will be stored in.
To generate the parsers in the src directory use `<outputDir>${project.basedir}/src/main/java</outputDir>`.
By default this directory will be `target/generated-sources/gwt-bean-parsers`.

### customParserDir

The directory where custom parsers are located.
This parameters is optional.
