/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.testng.annotations.AfterSuite;

/**
 * Utility class to load the Spring testing config once, speeding up the tests.
 */
public final class DbMasterTestUtils {

  private static ConfigurableApplicationContext s_context = null;

  /**
   * Gets the Spring context, sharing where possible.
   * 
   * @param databaseType  ignored
   * @return the context, not null
   */
  public static synchronized ConfigurableApplicationContext getContext(String databaseType) {
    if (s_context == null) {
      s_context = new FileSystemXmlApplicationContext("config/test-master-context.xml");
    }
    return s_context;
  }

  /**
   * Closes the Spring contexts.
   */
  @AfterSuite
  public static synchronized void closeAfterSuite() {
    if (s_context != null) {
      s_context.close();
    }
  }

}
