/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool.timeseries;

import com.opengamma.examples.tool.AbstractExampleTool;
import com.opengamma.financial.loader.TimeSeriesLoaderTool;

/**
 * Wrapper to expose the Examples classpath to the standard portfolio loader tool
 */
public class ExampleTimeSeriesLoaderTool extends AbstractExampleTool {

  private static String[] s_args;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    if (init(TOOL_LOGBACK_XML)) {
      s_args = args;
      new ExampleTimeSeriesLoaderTool().run(TOOLCONTEXT_EXAMPLE_PROPERTIES);
    }
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  @Override 
  protected void doRun() {
    new TimeSeriesLoaderTool().run(s_args, getToolContext());
  }

}
