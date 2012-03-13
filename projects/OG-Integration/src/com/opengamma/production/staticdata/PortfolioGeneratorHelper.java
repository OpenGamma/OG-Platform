/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.production.staticdata;

import java.text.DecimalFormat;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Helper for loading portfolios.
 */
public class PortfolioGeneratorHelper {

  /** Portfolio name option flag*/
  public static final String PORTFOLIO_NAME_OPT = "n";
  /** Run mode option flag */
  public static final String RUN_MODE_OPT = "r";
  /** Write option flag */
  public static final String WRITE_OPT = "w";
  /** Command-line options */
  public static final Options OPTIONS;
  
  /** Standard rate formatter */
  public static final DecimalFormat RATE_FORMATTER = new DecimalFormat("###%");
  /** Standard notional formatter */
  public static final DecimalFormat NOTIONAL_FORMATTER = new DecimalFormat("0MM");
  
  static {
    OPTIONS = PortfolioGeneratorHelper.getOptions();
  }

  public static Options getOptions() {
    Options options = new Options();
  
    Option portfolioNameOption = new Option(PORTFOLIO_NAME_OPT, "name", true, "The name of the portfolio");
    portfolioNameOption.setRequired(true);
    options.addOption(portfolioNameOption);
    
    Option runModeOption = new Option(RUN_MODE_OPT, "runmode", true, "The run mode: shareddev, standalone");
    runModeOption.setRequired(true);
    options.addOption(runModeOption);
    
    Option writeOption = new Option(WRITE_OPT, "write", false, "Actually persists the portfolio to the database");
    options.addOption(writeOption);
    
    return options;
  }
  
  public static void usage(String loaderName) {
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.setWidth(100);
    helpFormatter.printHelp(loaderName, OPTIONS);
  }

}
