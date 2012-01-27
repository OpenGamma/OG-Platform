/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.examples.portfolioloader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;

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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Command line harness for portfolio import functionality
 */
public class CommandLineTool {

  private static final Logger s_logger = LoggerFactory.getLogger(CommandLineTool.class);

  private static final String CLASS_PREFIX = "com.opengamma.examples.portfolioloader.loaders.";
  private static final String CLASS_POSTFIX = "PortfolioLoader";

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
  /** Asset class flag */
  private static final String ASSET_CLASS_OPT = "a";
  /** Command-line options */
  private static final Options OPTIONS = getOptions();

  
  /**
   * ENTRY POINT FOR COMMAND LINE TOOL
   * @param args
   */
  public static void main(String[] args) { //CSIGNORE
    
    PortfolioLoader portfolioLoader;
    PortfolioWriter portfolioWriter;
    AbstractApplicationContext applicationContext = null;

    s_logger.info(TOOL_NAME + " is initialising...");
    s_logger.info("Current working directory is " + System.getProperty("user.dir"));

    /*
     * Parse command line arguments
     */
    
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
    
    
    /*
     * Set up writing side
     */
    
    if (cmdLine.hasOption(WRITE_OPT)) {  
      // Check that the portfolio name was specified on the command line
      if (portfolioName == null) {
        throw new OpenGammaRuntimeException("Portfolio name omitted, cannot persist to OpenGamma masters");
      }
      
      s_logger.info("Write option specified, will persist to OpenGamma masters in portfolio '" + portfolioName + "'");

      // Configure the OG platform
      PlatformConfigUtils.configureSystemProperties(runMode);
      applicationContext = new ClassPathXmlApplicationContext("com/opengamma/examples/portfolioloader/loaderContext.xml");
      
      // Get an OG loader context, which will provide access to any required masters/sources
      applicationContext.start();
      LoaderContext loaderContext = (LoaderContext) applicationContext.getBean("loaderContext");
  
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      portfolioWriter = new MasterPortfolioWriter(portfolioName, loaderContext);

    } else {
      s_logger.info("Write option omitted, will pretty-print instead of persisting to OpenGamma masters");
      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      portfolioWriter = new DummyPortfolioWriter();         
    }

    
    /*
     * Set up reading side
     */
    
    String extension = filename.substring(filename.lastIndexOf('.'));
    if (extension.equalsIgnoreCase(".csv")) {
     
      // Check that the asset class was specified on the command line
      String assetClass = cmdLine.getOptionValue(ASSET_CLASS_OPT);
      if (assetClass == null) {
        throw new OpenGammaRuntimeException("Could not import as no asset class was specified for file " + filename + " (use '-a')");
      }
      
      // Open input file for reading
      FileInputStream fileInputStream;
      try {
        fileInputStream = new FileInputStream(filename);
      } catch (FileNotFoundException ex) {
        throw new OpenGammaRuntimeException("Could not open file " + filename + " for reading, exiting immediately.");
      }
  
      try {
        // Identify the appropriate portfolio loader class from the ZIP entry's file name
        String className = CLASS_PREFIX + assetClass + CLASS_POSTFIX;
        Class<?> loaderClass = Class.forName(className);
  
        // Find the constructor
        Constructor<?> constructor = loaderClass.getConstructor(SheetReader.class);
        
        // Set up a sheet reader for the current CSV file in the ZIP archive
        SheetReader sheet = new CsvSheetReader(fileInputStream);
        
        // Dynamically load the corresponding type of portfolio loader for the current sheet
        portfolioLoader = (PortfolioLoader) constructor.newInstance(sheet);
        
        s_logger.info("Processing " + filename + " with " + className);

      } catch (Throwable ex) {
        //throw new OpenGammaRuntimeException("Could not identify an appropriate loader for ZIP entry " + entry.getName());
        throw new OpenGammaRuntimeException("Could not identify an appropriate loader for file " + filename);
      }

    } else if (extension.equalsIgnoreCase(".zip")) {
      
      // Create zipped multi-asset class loader
      portfolioLoader = new ZippedPortfolioLoader(filename);
      
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .ZIP");
    }

    
    /*
     * Do the actual reading and writing
     */
    
    // Load in and write the securities, positions and trades
    portfolioLoader.writeTo(portfolioWriter);
    
    // Flush changes to portfolio master
    portfolioWriter.flush();

    
    /*
     * Clean up and shut down
     */
    
    if (cmdLine.hasOption(WRITE_OPT)) {
      // Shut down active context
      applicationContext.close();
    }
    
    s_logger.info(TOOL_NAME + " is finished.");
  }
  
  
  /**
   * Builds the command line option list
   * @return the command line options
   */
  private static Options getOptions() {
    Options options = new Options();
    Option filenameOption = new Option(FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (CSV or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);

    Option portfolioNameOption = new Option(PORTFOLIO_NAME_OPT, "name", true, "The name of the destination OpenGamma portfolio");
    options.addOption(portfolioNameOption);

    Option runModeOption = new Option(RUN_MODE_OPT, "runmode", true, "The OpenGamma run mode: shareddev, standalone");
    runModeOption.setRequired(true);
    options.addOption(runModeOption);

    Option writeOption = new Option(WRITE_OPT, "write", false, "Actually persists the portfolio to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);

    Option assetClassOption = new Option(ASSET_CLASS_OPT, "assetclass", true, "Specifies the asset class to be found in an input CSV file (ignored if ZIP file is specified)");
    options.addOption(assetClassOption);
    
    return options;
  }

}
