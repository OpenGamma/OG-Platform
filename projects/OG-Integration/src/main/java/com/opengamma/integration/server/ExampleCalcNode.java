/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.server;

import com.opengamma.engine.calcnode.CalculationNodeProcess;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Starts a calculation node and joins a job dispatcher.
 */
public final class ExampleCalcNode {

  private ExampleCalcNode() {
  }

  /**
   * Starts a calculation node
   * @param args  the arguments, not used
   */
  public static void main(String[] args) { // CSIGNORE
    // Logging
    System.setProperty("logback.configurationFile", "com/opengamma/util/warn-logback.xml");
    // Run mode
    PlatformConfigUtils.configureSystemProperties();
    // Configuration URL (should get from command line)
    final String url = "http://" + System.getProperty("opengamma.viewprocessor.host", "localhost") + ":" + System.getProperty("opengamma.viewprocessor.port", "8080")
        + "/calcNode/example.xml";
    // And run
    CalculationNodeProcess.main(url);
  }

}
