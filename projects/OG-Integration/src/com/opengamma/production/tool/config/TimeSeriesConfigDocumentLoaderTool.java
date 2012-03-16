/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.production.tool.config;

import com.opengamma.production.tool.AbstractProductionTool;
import com.opengamma.production.tool.portfolio.DemoBondFuturePortfolioLoader;

/**
 * Puts an entry in the TSS configuration for default MetaData lookup.
 */
public class TimeSeriesConfigDocumentLoaderTool extends AbstractProductionTool {

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new DemoBondFuturePortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the tool.
   */
  @Override 
  protected void doRun() {
    new TimeSeriesConfigDocumentLoader(getToolContext().getConfigMaster()).run();
  }

}
