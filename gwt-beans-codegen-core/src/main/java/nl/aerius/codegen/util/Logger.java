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
package nl.aerius.codegen.util;

/**
 * Interface to abstract logging.
 * This is needed because running from the command line uses a different way as in the Maven Mojo.
 */
public interface Logger {

  /**
   * Logs the message at info level.
   *
   * @param message message to log
   */
  default void info(final String message) {
    System.out.println(message);
  }

  /**
   * Logs the message at warn level.
   *
   * @param message message to log
   */
  default void warn(final String message) {
    System.out.println(message);
  }

  /**
   * @return Returns the plugin version.
   */
  default String pluginVersion() {
    return System.getProperty("generator.version", "unknown-version");
  }

  /**
   * @return Returns the git hash the plugin was generated with.
   */
  default String gitHash() {
    return System.getProperty("generator.githash", "unknown-hash");
  }

}
