/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Ignore;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Utility class to load the Spring testing config once, speeding up the tests.
 */
@Ignore
public final class DbMasterTestUtils {

  private static Map<String, ConfigurableApplicationContext> s_context = new ConcurrentHashMap<String, ConfigurableApplicationContext>();

  /**
   * Gets the Spring context, sharing where possible.
   * @return the context, not null
   */
  public static ConfigurableApplicationContext getContext(String databaseType) {
    ConfigurableApplicationContext context = s_context.get(databaseType);
    if (context == null) {
      context = new FileSystemXmlApplicationContext("config/test-master-context.xml");
      s_context.put(databaseType, context);
    }
    return context;
  }

}
