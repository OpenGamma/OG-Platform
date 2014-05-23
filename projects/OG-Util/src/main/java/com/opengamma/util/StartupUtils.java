/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * Utility method to be run at system startup.
 */
public final class StartupUtils {

  /**
   * Hidden constructor.
   */
  private StartupUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes an OpenGamma system called from a main method.
   */
  public static void init() {
    try {
      // avoid EHCache/Quartz calling the internet
      if (System.getProperties().containsKey("net.sf.ehcache.skipUpdateCheck") == false) {
        System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
      }
      if (System.getProperties().containsKey("org.terracotta.quartz.skipUpdateCheck") == false) {
        System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
      }
      
    } catch (SecurityException ex) {
      // ignore silently
    } catch (RuntimeException ex) {
      ex.printStackTrace();
    }
  }

}
