/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.examples.portfolioloader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Command line harness for portfolio import functionality
 */
public class CommandLineTool {

  private static final Logger s_logger = LoggerFactory.getLogger(CommandLineTool.class);

  /** Tool name */
  private static final String TOOL_NAME = "OpenGamma Portfolio Importer";
  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Portfolio name option flag*/
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** Run mode option flag */
  private static final String RUN_MODE_OPT = "r";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Command-line options */
  private static final Options OPTIONS = getOptions();
  /** Standard rate formatter */

  /**
   * ENTRY POINT FOR COMMAND LINE TOOL
   * @param args
   */
  public static void main(String[] args) { //CSIGNORE
    
    System.out.println(TOOL_NAME + " is initialising.");
    System.out.println("cwd " + System.getProperty("user.dir"));
    
    // Parse command line arguments
    CommandLine cmdLine;
    try {
      cmdLine = new PosixParser().parse(OPTIONS, args);
    } catch (ParseException e) {
      s_logger.warn(e.getMessage());
      (new HelpFormatter()).printHelp(" ", OPTIONS);
      return;
    }
    String filename = cmdLine.getOptionValue(FILE_NAME_OPT);
    String portfolioName = cmdLine.getOptionValue(PORTFOLIO_NAME_OPT);
    String runMode = cmdLine.getOptionValue(RUN_MODE_OPT);
    
    // Open input file for reading
    FileInputStream fileInputStream;
    try {
      fileInputStream = new FileInputStream(filename);
    } catch (FileNotFoundException ex) {
      s_logger.warn(ex.getMessage());
      System.out.println(TOOL_NAME + " could not open file " + filename + " for reading, exiting immediately.");
      return;
    }

    // Create a sheet loader for the input file format
    SheetReader sheet = new CsvSheet(fileInputStream);

    // Create importer(s) for the security type(s) in the input file(s)
    PortfolioLoader portfolioLoader = new EquityFuturePortfolioLoader(sheet);
    
    // Configure the OG platform
    PlatformConfigUtils.configureSystemProperties(runMode);
    
    // Get an OG loader context, which will provide access to any required masters/sources
    AbstractApplicationContext applicationContext = 
        new ClassPathXmlApplicationContext("com/opengamma/examples/portfolioloader/loaderContext.xml");
    applicationContext.start();
    LoaderContext loaderContext = (LoaderContext) applicationContext.getBean("loaderContext");

    // Create a portfolio buffer to store and persist imported positions and securities to the OG masters
    PortfolioWriter portfolioWriter = new PortfolioWriter(portfolioName, loaderContext);
    
    // Import from the input file format into the reader object (as many times as needed)
    // PortfolioWriter could lazy-load in future to reduce memory requirements
    portfolioWriter.load(portfolioLoader);
    // portfolioLoader.writeTo(portfolioWriter);
    
    // Ensure that all imported data is persisted into the OG masters
    //portfolioWriter.flush();
    portfolioWriter.prettyPrint();
    
    System.out.println(TOOL_NAME + " is finished.");
    
    // Shut down active context
    applicationContext.close();
  }
  
  /**
   * Builds the command line option list
   * @return the command line options
   */
  private static Options getOptions() {
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

}
