/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader;

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
import com.opengamma.util.PlatformConfigUtils;

/**
 * Provides standard portfolio loader functionality
 */
public class PortfolioLoaderTool {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioLoaderTool.class);

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

  /**
   * ENTRY POINT FOR COMMAND LINE TOOL
   * @param args  Command line args
   */
  public void run(String[] args) { 
    s_logger.info(TOOL_NAME + " is initialising...");
    s_logger.info("Current working directory is " + System.getProperty("user.dir"));
    
    // Parse command line arguments
    CommandLine cmdLine = getCmdLine(args, false);
    
    // Configure the OG platform
    PlatformConfigUtils.configureSystemProperties(cmdLine.getOptionValue(RUN_MODE_OPT));
    AbstractApplicationContext applicationContext = 
        new ClassPathXmlApplicationContext("com/opengamma/financial/loader/loaderContext.xml");
    
    // Get an OG loader context, which will provide access to any required masters/sources
    applicationContext.start();
    LoaderContext loaderContext = (LoaderContext) applicationContext.getBean("loaderContext");
    
    run(cmdLine, loaderContext);
    
    // Clean up and shut down
    applicationContext.close();
  }

  /**
   * ENTRY POINT FOR COMMAND LINE TOOL
   * @param args  Command line args
   * @param loaderContext  the loader context
   */
  public void run(String[] args, LoaderContext loaderContext) {
    s_logger.info(TOOL_NAME + " is initialising...");
    s_logger.info("Current working directory is " + System.getProperty("user.dir"));
    
    // Parse command line arguments
    CommandLine cmdLine = getCmdLine(args, true);
    
    run(cmdLine, loaderContext);
  }

  private void run(CommandLine cmdLine, LoaderContext loaderContext) {
    // Set up writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        cmdLine.getOptionValue(PORTFOLIO_NAME_OPT), 
        cmdLine.hasOption(WRITE_OPT),
        loaderContext);
    
     // Set up reader
    PortfolioReader portfolioReader = constructPortfolioReader(
        cmdLine.getOptionValue(FILE_NAME_OPT), 
        cmdLine.getOptionValue(ASSET_CLASS_OPT), 
        loaderContext);
    
    // Load in and write the securities, positions and trades
    portfolioReader.writeTo(portfolioWriter);
    
    // Flush changes to portfolio master
    portfolioWriter.flush();
    
    s_logger.info(TOOL_NAME + " is finished.");
  }
  
  
  private static CommandLine getCmdLine(String[] args, boolean contextProvided) {
    final Options options = getOptions(contextProvided);
    try {
      return new PosixParser().parse(options, args);
    } catch (ParseException e) {
      s_logger.warn(e.getMessage());
      (new HelpFormatter()).printHelp(" ", options);
      throw new OpenGammaRuntimeException("Could not parse the command line");
    }        
  }

  private static Options getOptions(boolean contextProvided) {
    Options options = new Options();
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (CSV or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option portfolioNameOption = new Option(
        PORTFOLIO_NAME_OPT, "name", true, "The name of the destination OpenGamma portfolio");
    options.addOption(portfolioNameOption);
    
    if (contextProvided == false) {
      Option runModeOption = new Option(
          RUN_MODE_OPT, "runmode", true, "The OpenGamma run mode: shareddev, standalone");
      runModeOption.setRequired(true);
      options.addOption(runModeOption);
    }
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the portfolio to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
    
    Option assetClassOption = new Option(
        ASSET_CLASS_OPT, "assetclass", true, 
        "Specifies the asset class to be found in an input CSV file (ignored if ZIP file is specified)");
    options.addOption(assetClassOption);
    
    return options;
  }

  private static PortfolioWriter constructPortfolioWriter(String portfolioName, boolean write,
      LoaderContext loaderContext) {
    
    if (write) {  
      // Check that the portfolio name was specified on the command line
      if (portfolioName == null) {
        throw new OpenGammaRuntimeException("Portfolio name omitted, cannot persist to OpenGamma masters");
      }
      
      s_logger.info("Write option specified, will persist to OpenGamma masters in portfolio '" + portfolioName + "'");
    
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterPortfolioWriter(portfolioName, loaderContext);
  
    } else {
      s_logger.info("Write option omitted, will pretty-print instead of persisting to OpenGamma masters");
      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyPortfolioWriter();         
    }

  }
  
  private static PortfolioReader constructPortfolioReader(String filename, String securityClass, 
      LoaderContext loaderContext) {
    
    String extension = filename.substring(filename.lastIndexOf('.'));
    
    // Single CSV or XLS file extension
    if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
      // Check that the asset class was specified on the command line
      if (securityClass == null) {
        throw new OpenGammaRuntimeException("Could not import as no asset class was specified for file " + filename + " (use '-a')");
      } else {
        return new SimplePortfolioReader(filename, securityClass, loaderContext);
      }
    // Multi-asset ZIP file extension
    } else if (extension.equalsIgnoreCase(".zip")) {
      // Create zipped multi-asset class loader
      return new ZippedPortfolioReader(filename, loaderContext);
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .ZIP");
    }
  }
  
}
