/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool.portfolio;

import com.opengamma.examples.tool.AbstractTool;
import com.opengamma.financial.loader.PortfolioLoaderTool;

/**
 * Wrapper to expose the Examples classpath to the standard portfolio loader tool
 */
public class ExamplePortfolioLoaderTool extends AbstractTool {

  private static String[] s_args;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    if (init()) {
      s_args = args;
      new ExamplePortfolioLoaderTool().run();
    }
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  @Override 
  protected void doRun() {
    new PortfolioLoaderTool().run(s_args, getToolContext());
  }

}
