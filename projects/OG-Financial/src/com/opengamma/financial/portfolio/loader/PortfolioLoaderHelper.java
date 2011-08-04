/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.portfolio.loader;

import java.text.DecimalFormat;
import java.util.Map;

import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * 
 */
public class PortfolioLoaderHelper {
  /** File name option flag */
  public static final String FILE_NAME_OPT = "f";
  /** Portfolio name option flag*/
  public static final String PORTFOLIO_NAME_OPT = "n";
  /** Run mode option flag */
  public static final String RUN_MODE_OPT = "r";
  /** Write option flag */
  public static final String WRITE_OPT = "w";
  /** Standard date-time formatter for the input */
  public static final DateTimeFormatter CSV_DATE_FORMATTER;
  /** Standard date-time formatter for the output */
  public static final DateTimeFormatter OUTPUT_DATE_FORMATTER;
  /** Command-line options */
  public static final Options OPTIONS;
  /** Standard rate formatter */
  public static final DecimalFormat RATE_FORMATTER = new DecimalFormat("0.###%");
  /** Standard notional formatter */
  public static final DecimalFormat NOTIONAL_FORMATTER = new DecimalFormat("0,000");
  
  static {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern("dd/MM/yyyy");
    CSV_DATE_FORMATTER = builder.toFormatter();
    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyy-MM-dd");
    OUTPUT_DATE_FORMATTER = builder.toFormatter();
    OPTIONS = PortfolioLoaderHelper.getOptions();
  }

  public static Options getOptions() {
    Options options = new Options();
    Option filenameOption = new Option(FILE_NAME_OPT, "filename", true, "The path to the CSV file of cash details");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
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
  
  public static void normaliseHeaders(String[] headers) {
    for (int i = 0; i < headers.length; i++) {
      headers[i] = headers[i].toLowerCase();
    }
  }

  public static String getWithException(Map<String, String> fieldValueMap, String fieldName) {
    String result = fieldValueMap.get(fieldName);
    if (result == null) {
      System.err.println(fieldValueMap);
      throw new IllegalArgumentException("Could not find field '" + fieldName + "'");
    }
    return result;
  }

  public static AbstractApplicationContext getApplicationContext() {
    return new ClassPathXmlApplicationContext("com/opengamma/financial/portfolio/loader/loaderContext.xml");
  }

  public static LoaderContext getLoaderContext(AbstractApplicationContext context) {
    return (LoaderContext) context.getBean("loaderContext");
  }
}
