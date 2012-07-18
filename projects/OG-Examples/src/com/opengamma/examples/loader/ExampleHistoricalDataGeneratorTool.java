/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import com.opengamma.examples.historical.SimulatedHistoricalDataGenerator;
import com.opengamma.examples.tool.AbstractExampleTool;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * Example tool to initialize historical data.
 */
@Scriptable
public class ExampleHistoricalDataGeneratorTool extends AbstractExampleTool {

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleHistoricalDataGeneratorTool().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    new SimulatedHistoricalDataGenerator(getToolContext().getHistoricalTimeSeriesMaster()).run();
  }

}
