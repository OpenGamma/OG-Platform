/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Helper to load the testing properties file.
 */
public class TestProperties {

  /** Default file name. */
  private static final String DEFAULT_PROPS_FILE_NAME = "tests.properties";
  /** Relative file location, deprecated (just use classpath). */
  private static final String DEFAULT_PROPS_DIR1 = "../../../Integration-Tests/src/test/resources/";
  /** Relative file location, deprecated (just use classpath). */
  private static final String DEFAULT_PROPS_DIR2 = "../Integration-Tests/src/test/resources/";
  /** Relative file location, deprecated (just use classpath). */
  private static final String DEFAULT_PROPS_DIR3 = "../../common/"; // OG-Platform/common/
  /** The properties. */
  private static volatile Properties s_props;

  /**
   * Gets the testing properties.
   * 
   * @return the properties, not null
   */
  public static synchronized Properties getTestProperties() {
    if (s_props == null) {
      s_props = new Properties();
      
      // file name
      String overridePropsFileName = System.getProperty("test.properties"); // from command line
      String propsFileName = selectFileName(overridePropsFileName);
      
      // load properties
      ClassPathResource res = new ClassPathResource(propsFileName, ClassUtils.getDefaultClassLoader());
      if (res.exists()) {
        loadClasspath(res);
      } else {
        loadFile(propsFileName);
      }
    }
    return s_props;
  }

  //-------------------------------------------------------------------------
  private static String selectFileName(String overridePropsFileName) {
    String propsFileName = DEFAULT_PROPS_FILE_NAME;
    if (overridePropsFileName != null) {
      propsFileName = overridePropsFileName;
      System.out.println("Using test.properties from system property: " + propsFileName);
    } else {
      System.out.println("Using default test.properties file name: " + propsFileName);
    }
    return propsFileName;
  }

  private static void loadClasspath(ClassPathResource res) {
    URL url = null;
    try {
      url = res.getURL();
    } catch (IOException ex) {
      System.out.println("Unable to get test properties URL: " + res.getDescription());
      throw new OpenGammaRuntimeException("Unable to get test properties URL: " + res.getDescription(), ex);
    }
    System.out.println("Loading test properties from classpath: " + url);
    try (InputStream fis = res.getInputStream()) {
      s_props.load(fis);
    } catch (IOException ex) {
      System.out.println("Unable to read test properties: " + url);
      throw new OpenGammaRuntimeException("Unable to read test properties: " + url, ex);
    }
  }

  private static void loadFile(String propsFileName) {
    String overridePropsDir = System.getProperty("test.properties.dir"); // from command line
    if (overridePropsDir != null) {
      System.out.println("Using test.properties.dir from system property: " + overridePropsDir);
      loadFile(new File(overridePropsDir, propsFileName));
      
    } else {
      File file = new File(DEFAULT_PROPS_DIR1, propsFileName);
      if (file.exists()) {
        loadFile(file);
      } else {
        file = new File(DEFAULT_PROPS_DIR2, propsFileName);
        if (file.exists()) {
          loadFile(file);
        } else {
          file = new File(DEFAULT_PROPS_DIR3, propsFileName);
          if (file.exists()) {
            loadFile(file);
          } else {
            System.out.println("Unable to find test properties in known locations");
            throw new OpenGammaRuntimeException("Unable to find test properties in known locations");
          }
        }
      }
    }
  }

  private static void loadFile(File file) {
    try {
      System.out.println("Loading test properties from file: " + file.getCanonicalPath());
    } catch (IOException ex) {
      System.out.println("Unable to get canonical path: " + file);
      throw new OpenGammaRuntimeException("Unable to get canonical path: " + file, ex);
    }
    try (InputStream fis = new FileInputStream(file)) {
      s_props.load(fis);
    } catch (IOException ex) {
      System.out.println("Unable to read test properties: " + file);
      throw new OpenGammaRuntimeException("Unable to read test properties: " + file, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a bean suitable for use in a Spring XML file to reference the test properties.
   * 
   * @return the Spring XML bean, not null
   */
  public static PropertyPlaceholderConfigurer springProperties() {
    return new PropertyPlaceholderConfigurer() {
      @Override
      protected void loadProperties(Properties props) throws IOException {
        Properties testProperties = getTestProperties();
        props.putAll(testProperties);
      }
    };
  }

}
