/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.net.URL;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * Utility methods for working with logging.
 */
public final class LogUtils {

  /**
   * Hidden constructor.
   */
  private LogUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Configures logging from a logback resource.
   * 
   * @param logbackResource  the logback resource, not null
   * @return true if logging was configured successfully, false otherwise
   */
  public static boolean configureLogger(String logbackResource) {
    try {
      ArgumentChecker.notNull(logbackResource, "logbackResource");
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset();
      URL logbackResourceUrl = LogUtils.class.getClassLoader().getResource(logbackResource);
      if (logbackResourceUrl == null) {
        throw new IllegalArgumentException("Logback file not found: " + logbackResource);
      }
      configurator.doConfigure(logbackResourceUrl);
      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

}
