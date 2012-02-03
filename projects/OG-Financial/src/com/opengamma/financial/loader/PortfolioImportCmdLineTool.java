/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader;

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
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Command line harness for portfolio import functionality
 */
public class PortfolioImportCmdLineTool {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioImportCmdLineTool.class);

  /** Path strings for constructing a fully qualified parser class name **/
  private static final String CLASS_PREFIX = "com.opengamma.financial.loader.rowparsers.";
  private static final String CLASS_POSTFIX = "Parser";

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
   * @param args
   */
  public static void main(String[] args) { //CSIGNORE

    s_logger.info(TOOL_NAME + " is initialising...");
    s_logger.info("Current working directory is " + System.getProperty("user.dir"));

    // Parse command line arguments
    CommandLine cmdLine = getCmdLine(args);
    
    // Set up writing side
    ObjectsPair<PortfolioWriter, AbstractApplicationContext> result = constructPortfolioWriter(
        cmdLine.getOptionValue(PORTFOLIO_NAME_OPT), 
        cmdLine.getOptionValue(RUN_MODE_OPT), 
        cmdLine.hasOption(WRITE_OPT));
    PortfolioWriter portfolioWriter = result.getFirst();
    AbstractApplicationContext applicationContext = result.getSecond();
    
     // Set up reading side
    PortfolioReader portfolioLoader = constructPortfolioLoader(
        cmdLine.getOptionValue(FILE_NAME_OPT), 
        cmdLine.getOptionValue(ASSET_CLASS_OPT));
    
    // Load in and write the securities, positions and trades
    portfolioLoader.writeTo(portfolioWriter);
    
    // Flush changes to portfolio master
    portfolioWriter.flush();
    
    // Clean up and shut down
    shutDown(portfolioWriter, applicationContext, cmdLine.hasOption(WRITE_OPT));

    s_logger.info(TOOL_NAME + " is finished.");
  }
  
  
  private static CommandLine getCmdLine(String[] args) {
    final Options options = getOptions();
    try {
      return new PosixParser().parse(options, args);
    } catch (ParseException e) {
      s_logger.warn(e.getMessage());
      (new HelpFormatter()).printHelp(" ", options);
      throw new OpenGammaRuntimeException("Could not parse the command line");
    }        
  }

  private static Options getOptions() {
    Options options = new Options();
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (CSV or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);

    Option portfolioNameOption = new Option(
        PORTFOLIO_NAME_OPT, "name", true, "The name of the destination OpenGamma portfolio");
    options.addOption(portfolioNameOption);

    Option runModeOption = new Option(
        RUN_MODE_OPT, "runmode", true, "The OpenGamma run mode: shareddev, standalone");
    runModeOption.setRequired(true);
    options.addOption(runModeOption);

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

  private static ObjectsPair<PortfolioWriter, AbstractApplicationContext> constructPortfolioWriter(
      String portfolioName, String runMode, boolean write) {
    
    AbstractApplicationContext applicationContext;
    
    if (write) {  
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
      return new ObjectsPair<PortfolioWriter, AbstractApplicationContext>(
          new MasterPortfolioWriter(portfolioName, loaderContext), 
          applicationContext);
  
    } else {
      s_logger.info("Write option omitted, will pretty-print instead of persisting to OpenGamma masters");
      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new ObjectsPair<PortfolioWriter, AbstractApplicationContext>(new DummyPortfolioWriter(), null);         
    }

  }
  
  private static PortfolioReader constructPortfolioLoader(String filename, String assetClass) {
    
    String extension = filename.substring(filename.lastIndexOf('.'));
    
    // Single CSV or XLS file extension
    if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
           
      // Check that the asset class was specified on the command line
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
        // Identify the appropriate parser class from the asset class command line option
        String className = CLASS_PREFIX + assetClass + CLASS_POSTFIX;
        Class<?> parserClass = Class.forName(className);
        
        // Find the constructor
        Constructor<?> constructor = parserClass.getConstructor();

        
        // Set up a sheet reader for the specified CSV/XLS file
        SheetReader sheet;
        if (extension.equalsIgnoreCase(".csv")) {
          sheet = new CsvSheetReader(fileInputStream);
        } else {
          sheet = new SimpleXlsSheetReader(fileInputStream, 0);
        }
        
        // Create a generic simple portfolio loader for the current sheet, using the dynamically loaded row parser class
        return new SimplePortfolioReader(sheet, (RowParser) constructor.newInstance(), sheet.getColumns());
        
      } catch (Throwable ex) {
        throw new OpenGammaRuntimeException("Could not identify an appropriate loader for file " + filename);
      }

    // Multi-asset ZIP file extension
    } else if (extension.equalsIgnoreCase(".zip")) {
            
      // Create zipped multi-asset class loader
      return new ZippedPortfolioReader(filename);
      
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .ZIP");
    }
  }
  
  private static void shutDown(PortfolioWriter portfolioWriter, AbstractApplicationContext applicationContext, boolean write) {
    if (write) {
      // Shut down active context
      applicationContext.close();
    }
  }
  
}
