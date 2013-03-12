/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Helper to load the testing properties file.
 */
public class TestProperties {

  private static final String DEFAULT_PROPS_FILE_NAME = "tests.properties";
  private static final String DEFAULT_PROPS_DIR = "../../common/"; // OG-Platform/common/

  private static Properties _props = null;

  /**
   * Gets the testing properties.
   * 
   * @return the properties, not null
   */
  public static synchronized Properties getTestProperties() {
    if (_props == null) {
      _props = new Properties();
      
      String propsFileName = DEFAULT_PROPS_FILE_NAME;
      String overridePropsFileName = System.getProperty("test.properties"); // passed in by Ant
      if (overridePropsFileName != null) {
        propsFileName = overridePropsFileName;
        System.err.println("Using test.properties from system property: " + propsFileName);
      } else {
        System.err.println("Using default test.properties file name: " + propsFileName);
      }
      String testPropsDir = DEFAULT_PROPS_DIR;
      String overridePropsDir = System.getProperty("test.properties.dir"); // passed in by Ant
      if (overridePropsDir != null) {
        testPropsDir = overridePropsDir;
        System.err.println("Using test.properties.dir from system property: " + testPropsDir);
      } else {
        System.err.println("Using default test.properties.dir: " + testPropsDir);
      }
      
      File file = new File(testPropsDir, propsFileName);
      try {
        System.err.println("Reading test properties from " + file.getCanonicalPath());
      } catch (IOException e) {
        throw new OpenGammaRuntimeException("Couldn't get canonical path of file " + file, e);
      }
      try (FileInputStream fis = new FileInputStream(file)) {
        _props.load(fis);
      } catch (IOException ex) {
        throw new OpenGammaRuntimeException("Could not read " + propsFileName, ex);
      }
    }
    return _props;
  }

}
