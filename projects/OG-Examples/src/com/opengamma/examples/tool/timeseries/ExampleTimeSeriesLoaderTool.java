/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool.timeseries;

import com.opengamma.examples.tool.AbstractTool;
import com.opengamma.financial.loader.LoaderContext;
import com.opengamma.financial.loader.PortfolioLoaderTool;
import com.opengamma.financial.loader.TimeSeriesLoaderTool;

/**
 * Wrapper to expose the Examples classpath to the standard portfolio loader tool
 */
public class ExampleTimeSeriesLoaderTool extends AbstractTool {

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
      new ExampleTimeSeriesLoaderTool().run();
    }
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  @Override 
  protected void doRun() {
    LoaderContext context = new LoaderContext();
    context.setConfigMaster(getToolContext().getConfigMaster());
    context.setConventionBundleSource(getToolContext().getConventionBundleSource());
    context.setExchangeSource(getToolContext().getExchangeSource());
    context.setHistoricalTimeSeriesMaster(getToolContext().getHistoricalTimeSeriesMaster());
    context.setHistoricalTimeSeriesSource(getToolContext().getHistoricalTimeSeriesSource());
    context.setHolidaySource(getToolContext().getHolidaySource());
    context.setPortfolioMaster(getToolContext().getPortfolioMaster());
    context.setPositionMaster(getToolContext().getPositionMaster());
    context.setSecurityLoader(getToolContext().getSecurityLoader());
    context.setSecurityMaster(getToolContext().getSecurityMaster());
    context.setSecuritySource(getToolContext().getSecuritySource());
    new TimeSeriesLoaderTool().run(s_args, context);
  }

}
