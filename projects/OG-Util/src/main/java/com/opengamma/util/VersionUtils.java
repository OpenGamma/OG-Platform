/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ClasspathUtils.DependencyInfo;

/**
 * Utility methods to work with the current OpenGamma build version.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class VersionUtils {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(VersionUtils.class);
  /** Build name (git hash). */
  private static final Attributes.Name IMPLEMENTATION_BUILD = new Attributes.Name("Implementation-Build");
  /** Build ID (CI server ID). */
  private static final Attributes.Name IMPLEMENTATION_BUILD_ID = new Attributes.Name("Implementation-Build-Id");
  /**
   * The local build version number.
   */
  private static String s_localBuildVersion;

  /**
   * Restricted constructor.
   */
  private VersionUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the current OpenGamma build version.
   * <p>
   * The version is read from a property file in the classpath with name
   * <code>"/" + projectName + ".properties"</code>.
   * This file is created by Ant during a Bamboo build. If no such file is found,
   * the method assumes you are running a local build, and it will 
   * return <code>"local-" + System.currentTimeMillis()</code> 
   * where <code>System.currentTimeMillis()</code> becomes fixed on the first
   * call within this VM.
   * 
   * @param projectName  the name of the OpenGamma project, for example og-financial, not null
   * @return the current version of the specified project, not null
   */
  public static String getVersion(String projectName) {
    String fileName = "/" + projectName + ".properties";
    
    Properties properties = new Properties();
    try (InputStream stream = VersionUtils.class.getResourceAsStream(fileName)) {
      if (stream == null) {
        return getLocalBuildVersion();
      }
      properties.load(stream);
    } catch (IOException e) {
      s_logger.error("Failed to read properties", e);
      return getLocalBuildVersion();
    }
    
    String version = properties.getProperty("version");
    if (version == null) {
      return getLocalBuildVersion();
    }
    return version;
  }

  private static synchronized String getLocalBuildVersion() {
    if (s_localBuildVersion != null) {
      return s_localBuildVersion;
    }
    s_localBuildVersion = "local-" + System.currentTimeMillis();
    return s_localBuildVersion;
  }

  //-------------------------------------------------------------------------
  /**
   * Derives the OpenGamma version from the classpath and dependencies.
   * 
   * @return the version, null if not known
   */
  public static String deriveVersion() {
    URL url = ClasspathHelper.forClass(VersionUtils.class, VersionUtils.class.getClassLoader());
    if (url != null && url.toString().contains(".jar")) {
      try {
        final String part = ClasspathHelper.cleanPath(url);
        try (JarFile myJar = new JarFile(part)) {
          final Manifest manifest = myJar.getManifest();
          if (manifest != null) {
            Attributes attributes = manifest.getMainAttributes();
            if (attributes != null) {
              if (attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION) != null) {
                return attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
              }
              if (attributes.getValue(Attributes.Name.SPECIFICATION_VERSION) != null) {
                return attributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
              }
            }
          }
        }
      } catch (Exception ex) {
        s_logger.warn(ex.getMessage(), ex);
      }
    } else {
      List<DependencyInfo> dependencies = ClasspathUtils.getDependencies();
      for (DependencyInfo dependency : dependencies) {
        if ("og-util".equals(dependency.getArtifactId())) {
          return dependency.getVersion();
        }
      }
    }
    return null;
  }

  /**
   * Derives the OpenGamma build from the classpath and dependencies.
   * 
   * @return the build, null if not known
   */
  public static String deriveBuild() {
    URL url = ClasspathHelper.forClass(VersionUtils.class, VersionUtils.class.getClassLoader());
    if (url != null && url.toString().contains(".jar")) {
      try {
        final String part = ClasspathHelper.cleanPath(url);
        try (JarFile myJar = new JarFile(part)) {
          final Manifest manifest = myJar.getManifest();
          if (manifest != null) {
            Attributes attributes = manifest.getMainAttributes();
            if (attributes != null) {
              if (attributes.getValue(IMPLEMENTATION_BUILD) != null) {
                return attributes.getValue(IMPLEMENTATION_BUILD);
              }
            }
          }
        }
      } catch (Exception ex) {
        s_logger.warn(ex.getMessage(), ex);
      }
    }
    return null;
  }

  /**
   * Derives the OpenGamma build ID from the classpath and dependencies.
   * <p>
   * This ID is derived from the CI server.
   * 
   * @return the build ID, null if not known
   */
  public static String deriveBuildId() {
    URL url = ClasspathHelper.forClass(VersionUtils.class, VersionUtils.class.getClassLoader());
    if (url != null && url.toString().contains(".jar")) {
      try {
        final String part = ClasspathHelper.cleanPath(url);
        try (JarFile myJar = new JarFile(part)) {
          final Manifest manifest = myJar.getManifest();
          if (manifest != null) {
            Attributes attributes = manifest.getMainAttributes();
            if (attributes != null) {
              if (attributes.getValue(IMPLEMENTATION_BUILD_ID) != null) {
                return attributes.getValue(IMPLEMENTATION_BUILD_ID);
              }
            }
          }
        }
      } catch (Exception ex) {
        s_logger.warn(ex.getMessage(), ex);
      }
    }
    return null;
  }

}
