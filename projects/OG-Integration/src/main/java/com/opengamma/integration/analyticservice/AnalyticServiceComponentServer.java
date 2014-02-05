/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;

import com.opengamma.component.OpenGammaComponentServer;
import com.opengamma.util.ShutdownUtils;

/**
 * Main entry point for OpenGamma Analytic service component-based servers.
 * <p>
 * This class exists to easily capture the classpath.
 */
public class AnalyticServiceComponentServer extends OpenGammaComponentServer {

  /**
   * Main method to start an OpenGamma JVM process.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) { // CSIGNORE
    if (!new AnalyticServiceComponentServer().run(args)) {
      ShutdownUtils.exit(-1);
    }
  }

}
