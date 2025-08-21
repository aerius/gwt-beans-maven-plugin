/*
 * Copyright the State of the Netherlands
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package nl.aerius.codegen.plugin;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import nl.aerius.codegen.ParserGenerator;
import nl.aerius.codegen.generator.ParserWriterUtils;
import nl.aerius.codegen.util.ClassFinder;
import nl.aerius.codegen.util.Logger;

@Mojo(name = "generate-parsers", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class ParserGeneratorMojo extends AbstractMojo {

  /**
   * List of root class names to generate parsers for.
   */
  @Parameter(required = true)
  private List<String> rootClassNames;

  /**
   * The package the parsers should be generated in.
   */
  @Parameter(required = true)
  private String parserPackage;

  /**
   * The base source directory the Parser classes will be generated in.
   */
  @Parameter(defaultValue = "${project.build.directory}/generated-sources/gwt-bean-parsers")
  private String outputDir;

  /**
   * Optional directory where custom parsers are located.
   */
  @Parameter
  private String customParserDir;


  /**
   * The Maven project instance for the executing project.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Parameter( defaultValue = "${mojoExecution}", readonly = true )
  private MojoExecution mojoExecution;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final MojoLogger logger = new MojoLogger();
    final String absoluteOutputDir = absoluteOrProjectRelative(outputDir);
    final String customParserDirectory = absoluteOrProjectRelative(customParserDir);

    getLog().info("Step 0: Start generating parsers");
    getLog().info("Output directory: " + outputDir);
    getLog().info("Parser package: " + parserPackage);
    getLog().info("Custom parser dir:" + customParserDirectory);

    try (final MoJoClassFinder finder = new MoJoClassFinder()) {
      ParserWriterUtils.initParsers(finder, logger);
      for (final String rootClassName : rootClassNames) {
        ParserGenerator.generateParsers(rootClassName, absoluteOutputDir, parserPackage,
            customParserDirectory, finder, logger);
      }
    } catch (ClassNotFoundException | IOException | DependencyResolutionRequiredException |  IllegalArgumentException | SecurityException e) {
      throw new MojoExecutionException(e);
    }
    addGeneratedSourcesAsResource(absoluteOutputDir);
  }

  private String absoluteOrProjectRelative(final String path) {
    if (path == null) {
      return null;
    }
    final File directory = new File(path);
    if (directory.exists()) {
      return directory.getAbsolutePath();
    } else {
      return Paths.get(project.getBasedir().getAbsolutePath(), "src/main/java/" + path).toFile().getAbsolutePath();
    }
  }

  private void addGeneratedSourcesAsResource(final String outputPath) {
    getLog().info("Add resource path" + outputPath);
    final Resource resource = new Resource();

    resource.setDirectory(outputPath);
    project.addResource(resource);
    project.addCompileSourceRoot(outputPath);
  }

  /**
   * Logger that logs using the Maven Mojo logger.
   */
  private class MojoLogger implements Logger {

    @Override
    public void info(final String message) {
      getLog().info(message);
    }

    @Override
    public void warn(final String message) {
      getLog().warn(message);
    }

    @Override
    public String pluginVersion() {
      return mojoExecution.getMojoDescriptor().getPluginDescriptor().getVersion();
    }

    @Override
    public String gitHash() {
      return getClass().getPackage().getImplementationVersion().split("_")[2];
    }
  }

  /**
   * ClassFinder implementation that finds classes based on Maven plugin project information.
   */
  private class MoJoClassFinder implements ClassFinder, Closeable {
    private final URLClassLoader classLoader;

    public MoJoClassFinder() throws MalformedURLException, DependencyResolutionRequiredException {
      final List<URL> urls = new ArrayList<>();

      for (final String element : project.getRuntimeClasspathElements()) {
        urls.add(new File(element).toURI().toURL());
      }

      classLoader = new URLClassLoader(
          urls.toArray(new URL[0]),
          Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> forName(final String clazz) throws ClassNotFoundException {
      return classLoader.loadClass(clazz);
    }

    @Override
    public void close() throws IOException {
      if (classLoader != null) {
        classLoader.close();
      }
    }
  }
}
