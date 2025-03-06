package nl.overheid.aerius.codegen.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

/**
 * Utility class for file system operations, providing centralized file searching, parsing,
 * and path management functionality across the codebase.
 */
public final class FileUtils {
    private FileUtils() {
        // Utility class
    }

    /**
     * Discovers all subclasses of a given class that are available on the classpath.
     *
     * @param <T> The type of the base class
     * @param baseClass The class to find subclasses for
     * @return A list of all subclasses found
     */
    public static <T> List<Class<?>> findSubclasses(final Class<T> baseClass) {
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages("nl.aerius", "nl.overheid.aerius")  // Scan all relevant packages
                .scan()) {
            return (List<Class<?>>) scanResult
                    .getSubclasses(baseClass.getName())
                    .loadClasses();
        }
    }

    /**
     * Analyzes a field's type and processes any discovered types using the provided consumer.
     * This is used by both TypeAnalyzer and ConfigurationValidator to discover and process types.
     *
     * @param field The field to analyze
     * @param typeProcessor Consumer that will process each discovered type
     * @throws TypeNotPresentException if a type is referenced but not found in the classpath
     */
    public static void analyzeFieldType(final Field field, final Consumer<Type> typeProcessor) {
        final Class<?> fieldType = field.getType();

        // Process the field type itself
        typeProcessor.accept(fieldType);

        // If it's a collection or map, process its generic parameters
        if (Collection.class.isAssignableFrom(fieldType)) {
            if (field.getGenericType() instanceof ParameterizedType) {
                final Type elementType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (elementType instanceof Class) {
                    typeProcessor.accept(elementType);
                } else if (elementType instanceof ParameterizedType) {
                    analyzeParameterizedType((ParameterizedType) elementType, typeProcessor);
                }
            }
        } else if (Map.class.isAssignableFrom(fieldType)) {
            if (field.getGenericType() instanceof ParameterizedType) {
                final ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                final Type[] typeArgs = paramType.getActualTypeArguments();
                // Process both key and value types
                for (Type typeArg : typeArgs) {
                    if (typeArg instanceof Class<?>) {
                        typeProcessor.accept(typeArg);
                    } else if (typeArg instanceof ParameterizedType) {
                        analyzeParameterizedType((ParameterizedType) typeArg, typeProcessor);
                    }
                }
            }
        } else if (fieldType.isArray()) {
            typeProcessor.accept(fieldType.getComponentType());
        }
    }

    private static void analyzeParameterizedType(final ParameterizedType type, final Consumer<Type> typeProcessor) {
        final Type rawType = type.getRawType();
        if (!(rawType instanceof Class<?>)) {
            return;
        }

        typeProcessor.accept(rawType);

        if (Map.class.isAssignableFrom((Class<?>) rawType)) {
            // Process both key and value types
            for (Type typeArg : type.getActualTypeArguments()) {
                if (typeArg instanceof Class<?>) {
                    typeProcessor.accept(typeArg);
                } else if (typeArg instanceof ParameterizedType) {
                    analyzeParameterizedType((ParameterizedType) typeArg, typeProcessor);
                }
            }
        } else if (Collection.class.isAssignableFrom((Class<?>) rawType)) {
            final Type elementType = type.getActualTypeArguments()[0];
            if (elementType instanceof Class) {
                typeProcessor.accept(elementType);
            } else if (elementType instanceof ParameterizedType) {
                analyzeParameterizedType((ParameterizedType) elementType, typeProcessor);
            }
        }
    }

    public static Optional<CompilationUnit> findAndParseFile(final String sourceRoot, final String fileName) {
        try {
            final File sourceDir = new File(sourceRoot);
            if (sourceDir.exists() && sourceDir.isDirectory()) {
                try (Stream<Path> paths = Files.walk(sourceDir.toPath())) {
                    final Optional<Path> file = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> p.getFileName().toString().equals(fileName))
                        .findFirst();

                    if (file.isPresent()) {
                        return Optional.of(StaticJavaParser.parse(file.get().toFile()));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error finding/parsing file " + fileName + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public static <T> Optional<String> findClassFile(final String sourceRoot, final Class<T> clazz) {
        final String relativePath = clazz.getName().replace('.', '/') + ".java";

        try {
            final File sourceDir = new File(sourceRoot);
            if (sourceDir.exists() && sourceDir.isDirectory()) {
                try (Stream<Path> paths = Files.walk(sourceDir.toPath())) {
                    return paths
                        .filter(Files::isRegularFile)
                        .map(Path::toString)
                        .filter(p -> p.endsWith(relativePath))
                        .findFirst();
                }
            }
        } catch (IOException e) {
            System.out.println("Error finding class file for " + clazz.getName() + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public static List<Path> findAllMatchingFiles(final String sourceRoot, final String fileName) {
        try {
            final File sourceDir = new File(sourceRoot);
            if (sourceDir.exists() && sourceDir.isDirectory()) {
                try (Stream<Path> paths = Files.walk(sourceDir.toPath())) {
                    return paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> p.getFileName().toString().equals(fileName))
                        .collect(Collectors.toList());
                }
            }
        } catch (IOException e) {
            System.out.println("Error finding files " + fileName + ": " + e.getMessage());
        }
        return List.of();
    }

    public static String getSourceRoot() {
        final String cwd = System.getProperty("user.dir");
        return new File(cwd).getParentFile().getParentFile().getParentFile().getAbsolutePath();
    }

    public static String getWuiClientOutputDir() {
        final String cwd = System.getProperty("user.dir");
        return new File(cwd).getParentFile().getAbsolutePath() + "/aerius-calculator-wui-client/src/main/java";
    }

    public static String getSimpleClassName(final String fullyQualifiedClassName) {
        return fullyQualifiedClassName.substring(fullyQualifiedClassName.lastIndexOf('.') + 1);
    }

    public static String getPackageName(final String fullyQualifiedClassName) {
        return fullyQualifiedClassName.substring(0, fullyQualifiedClassName.lastIndexOf('.'));
    }
}