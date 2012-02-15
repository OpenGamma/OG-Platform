/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import com.opengamma.examples.marketdata.SimulatedHistoricalDataGenerator;
import com.opengamma.examples.tool.AbstractTool;

/**
 * Example tool to initialize historical data.
 */
public class SimulatedHistoricalDataGeneratorTool extends AbstractTool {

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    if (init()) {
      new SimulatedHistoricalDataGeneratorTool().run();
    }
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    new SimulatedHistoricalDataGenerator(getToolContext().getDbHistoricalTimeSeriesMaster()).run();
  }

}
