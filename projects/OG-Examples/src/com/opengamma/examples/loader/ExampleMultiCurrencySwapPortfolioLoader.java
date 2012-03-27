/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import com.opengamma.examples.generator.PortfolioGeneratorTool;
import com.opengamma.examples.tool.AbstractExampleTool;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;

/**
 * Example code to load a very simple multicurrency swap portfolio.
 * <p>
 * This code is kept deliberately as simple as possible.
 * There are no checks for the securities or portfolios already existing, so if you run it
 * more than once you will get multiple copies portfolios and securities with the same names.
 * It is designed to run against the HSQLDB example database.
 */
public class ExampleMultiCurrencySwapPortfolioLoader extends AbstractExampleTool {

  /**
   * The name of the portfolio.
   */
  public static final String PORTFOLIO_NAME = "Example MultiCurrency Swap Portfolio";
  
  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleTimeSeriesRatingLoader().initAndRun(args);
    new ExampleMultiCurrencySwapPortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    YieldCurveConfigPopulator.populateCurveConfigMaster(getToolContext().getConfigMaster());
    (new PortfolioGeneratorTool()).run(getToolContext(), "Example MultiCurrency Swap Portfolio", "Swap", false);
  }

}
