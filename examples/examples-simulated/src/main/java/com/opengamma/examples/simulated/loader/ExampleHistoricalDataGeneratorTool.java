/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.examples.simulated.historical.SimulatedHistoricalDataGenerator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;

/**
 * Example tool to initialize historical data.
 */
@Scriptable
public class ExampleHistoricalDataGeneratorTool extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleHistoricalDataGeneratorTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    new SimulatedHistoricalDataGenerator(getToolContext().getHistoricalTimeSeriesMaster()).run();
  }

}
