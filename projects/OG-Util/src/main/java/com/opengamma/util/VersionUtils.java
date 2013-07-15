/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods to work with the current OpenGamma build version.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class VersionUtils {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(VersionUtils.class);
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

}
