/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
 * Gets the current OpenGamma build version. 
 */
public class VersionUtil {
  
  private static final Logger s_logger = LoggerFactory.getLogger(VersionUtil.class);
  
  /**
   * Gets the current OpenGamma build version.
   * <p>
   * The version is read from a property file in the classpath with name
   * <code>"/" + projectName + ".properties"</code>.
   * This file is created by Ant during a Bamboo build. If no such file is found,
   * the method assumes you are running a local build, and it will 
   * return <code>"local-" + System.currentTimeMillis()</code>.
   * 
   * @param projectName name of the OpenGamma project, for example og-financial, not null
   * @return current version of the specified project, not null
   */
  public static String getVersion(String projectName) {
    String fileName = "/" + projectName + ".properties";
    
    InputStream stream = VersionUtil.class.getResourceAsStream(fileName);
    if (stream == null) {
      return getLocalBuildVersion();
    }
    
    Properties properties = new Properties();
    
    try {
      properties.load(stream);
      stream.close();
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
  
  private static String getLocalBuildVersion() {
    return "local-" + System.currentTimeMillis();
  }

}
