/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.newloader;

import com.opengamma.financial.loader.PortfolioLoaderTool;

/**
 * Wrapper to expose the Examples classpath to the standard portfolio loader tool
 */
public class ExamplePortfolioLoaderTool {

  /**
   * Main method
   * @param args the args
   */
  public static void main(String[] args) { //CSIGNORE
    new PortfolioLoaderTool().run(args);
  }
}
