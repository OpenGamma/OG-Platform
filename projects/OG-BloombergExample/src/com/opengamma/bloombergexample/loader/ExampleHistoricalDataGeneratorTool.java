/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.loader;

import com.opengamma.bloombergexample.marketdata.SimulatedHistoricalDataGenerator;
import com.opengamma.bloombergexample.tool.AbstractExampleTool;

/**
 * Example tool to initialize historical data.
 */
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
