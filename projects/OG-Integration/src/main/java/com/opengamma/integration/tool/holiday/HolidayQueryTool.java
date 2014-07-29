/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.holiday;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;

/**
 * Tool to return whether or not today is a holiday in a given ccy.
 */
@Scriptable
public class HolidayQueryTool extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new HolidayQueryTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    ToolContext toolContext = getToolContext();
    CommandLine commandLine = getCommandLine();
    boolean verbose = commandLine.hasOption("verbose");
    if ((commandLine.hasOption("today") && commandLine.hasOption("yesterday")) || 
        (commandLine.hasOption("date") && commandLine.hasOption("today")) ||
        (commandLine.hasOption("date") && commandLine.hasOption("yesterday"))) {
      System.err.println("Can only return today OR yesterday OR date!");
      System.exit(2);
    }
    String ccyStr = commandLine.getOptionValue("ccy");
    try {
      Currency ccy = Currency.of(ccyStr);
      LocalDate date = null;
      if (commandLine.hasOption("yesterday")) {
        date = LocalDate.now().minusDays(1);
      } else if (commandLine.hasOption("today")) {
        date = LocalDate.now();
      } else if (commandLine.hasOption("date")) {
        try {
          date = (LocalDate) DateTimeFormatter.BASIC_ISO_DATE.parse(commandLine.getOptionValue("date"));
        } catch (Exception e) {
          System.err.println("Could not parse date, should be YYYYMMDD format");
          System.exit(2);
        }
      } else {
        System.err.println("Must specify either today or yesterday option");
        System.exit(2);
      }
      boolean isHoliday = toolContext.getHolidaySource().isHoliday(date, ccy);
      if (isHoliday) {
        if (verbose) {
          System.out.println("Day was a holiday");
        }
        System.exit(0);
      } else {
        if (verbose) {
          System.out.println("Day was not a holiday");
        }
        System.exit(1);
      }
    } catch (IllegalArgumentException iae) {
      System.err.println("Invalid currency code");
      System.exit(2);
    }
  }
  

  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    Options options = super.createOptions(mandatoryConfig);
    options.addOption(createCurrencyOption());
    options.addOption(createTodayOption());
    options.addOption(createYesterdayOption());
    options.addOption(createDateOption());
    options.addOption(createVerboseOption());
    return options;
  }

  @SuppressWarnings("static-access")
  private Option createCurrencyOption() {
    return OptionBuilder.isRequired(true)
                        .hasArg()
                        .withArgName("currency code")
                        .withDescription("The currency you want to look up")
                        .withLongOpt("currency")
                        .create("ccy");
  }
  
  @SuppressWarnings("static-access")
  private Option createTodayOption() {
    return OptionBuilder.isRequired(false)
                        .withDescription("Return if today is a holiday")
                        .withLongOpt("today")
                        .create("t");
  }

  @SuppressWarnings("static-access")
  private Option createYesterdayOption() {
    return OptionBuilder.isRequired(false)
                        .hasArg(false)
                        .withDescription("Return if yesterday is a holiday")
                        .withLongOpt("yesterday")
                        .create("y");
  }
  
  @SuppressWarnings("static-access")
  private Option createDateOption() {
    return OptionBuilder.isRequired(false)
                        .hasArg(true)
                        .withArgName("date")
                        .withDescription("Return if date (YYYYMMDD) is a holiday")
                        .withLongOpt("yesterday")
                        .create("y");
  }
  
  @SuppressWarnings("static-access")
  private Option createVerboseOption() {
    return OptionBuilder.isRequired(false)
                        .hasArg(false)
                        .withDescription("Verbose output")
                        .withLongOpt("verbose")
                        .create("v");
  }
  
  protected Class<?> getEntryPointClass() {
    return getClass();
  }

  @Override
  protected void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("config-import-export-tool.sh [file...]", options, true);
  }
  
}
