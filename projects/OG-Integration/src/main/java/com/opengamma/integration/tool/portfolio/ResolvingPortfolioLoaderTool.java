/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.integration.copier.portfolio.PortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.QuietPortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.ResolvingPortfolioCopier;
import com.opengamma.integration.copier.portfolio.VerbosePortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePositionReader;
import com.opengamma.integration.copier.portfolio.rowparser.ExchangeTradedRowParser;
import com.opengamma.integration.copier.portfolio.writer.MasterPositionWriter;
import com.opengamma.integration.copier.portfolio.writer.PositionWriter;
import com.opengamma.integration.copier.portfolio.writer.PrettyPrintingPositionWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.scripts.Scriptable;

/**
 * The portfolio loader tool
 */
@Scriptable
public class ResolvingPortfolioLoaderTool extends AbstractTool<IntegrationToolContext> {

  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Portfolio name option flag*/
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Overwrite option flag */
  private static final String OVERWRITE_OPT = "o";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";
  /** Time series data provider option flag*/
  private static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  /** Time series data field option flag*/
  private static final String TIME_SERIES_DATAFIELD_OPT = "d";
  /** Date format option flag */
  private static final String DATE_FORMAT_OPT = "df";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new ResolvingPortfolioLoaderTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio into the position master.
   */
  @Override
  protected void doRun() {      
    IntegrationToolContext context = getToolContext();

    // Create portfolio writer
    PositionWriter positionWriter = constructPortfolioWriter(
        getCommandLine().getOptionValue(PORTFOLIO_NAME_OPT), 
        context.getPortfolioMaster(), 
        context.getPositionMaster(), 
        context.getSecurityMaster(),
        getCommandLine().hasOption(WRITE_OPT),
        getCommandLine().hasOption(OVERWRITE_OPT)
    );
    
    String dateFormat = "yyyy-MM-dd";
    if (getCommandLine().hasOption(DATE_FORMAT_OPT)) {
      dateFormat = getCommandLine().getOptionValue(DATE_FORMAT_OPT);
    }
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    try {
      builder.appendPattern(dateFormat);
    } catch (IllegalArgumentException iae) {
      System.err.println("Cannot parse date format " + dateFormat);
      System.exit(1);
    }
    
    // Construct portfolio reader
    PositionReader positionReader = constructPortfolioReader(
        getCommandLine().getOptionValue(FILE_NAME_OPT), 
        context.getSecurityProvider(),
        builder.toFormatter()
    );
    
    // Create portfolio copier
    ResolvingPortfolioCopier portfolioCopier = new ResolvingPortfolioCopier(
        context.getHistoricalTimeSeriesMaster(),
        context.getHistoricalTimeSeriesProvider(),
        context.getBloombergReferenceDataProvider(),
        getOptionValue(TIME_SERIES_DATAPROVIDER_OPT, "CMPL"),
        getCommandLine().getOptionValues(TIME_SERIES_DATAFIELD_OPT) == null ? 
            new String[]{"PX_LAST"} : getCommandLine().getOptionValues(TIME_SERIES_DATAFIELD_OPT)
    );
    
    // Create visitor for verbose/quiet mode
    PortfolioCopierVisitor portfolioCopierVisitor; 
    if (getCommandLine().hasOption(VERBOSE_OPT)) {
      portfolioCopierVisitor = new VerbosePortfolioCopierVisitor();
    } else {
      portfolioCopierVisitor = new QuietPortfolioCopierVisitor();
    }
    
    // Call the portfolio loader with the supplied arguments
    portfolioCopier.copy(positionReader, positionWriter, portfolioCopierVisitor);
    
    // close stuff
    positionReader.close();
    positionWriter.close();
  }

  private static PositionWriter constructPortfolioWriter(String portfolioName, PortfolioMaster portfolioMaster,
      PositionMaster positionMaster, SecurityMaster securityMaster, boolean write, boolean overwrite) {
    if (write) {
      if (overwrite) {
        System.out.println("Write and overwrite options specified, will persist to OpenGamma masters"); 
      } else {
        System.out.println("Write option specified, persisting to OpenGamma masters");
      }
      
      // Check that the portfolio name was specified on the command line
      if (portfolioName == null) {
        throw new OpenGammaRuntimeException("Portfolio name omitted, cannot persist to OpenGamma masters");
      }
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterPositionWriter(portfolioName, portfolioMaster, positionMaster, securityMaster, false, false, false);
    } else {
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new PrettyPrintingPositionWriter(true);
    }  
  }
  
  // TODO take a stream as well as the file name, BBG master
  private static PositionReader constructPortfolioReader(String filename, SecurityProvider securityProvider, DateTimeFormatter dateFormatter) {
    InputStream stream;
    try {
      stream = new BufferedInputStream(new FileInputStream(filename));
    } catch (FileNotFoundException e) {
      throw new OpenGammaRuntimeException("Could not open file " + filename + " for reading: " + e);
    }
    
    SheetFormat sheetFormat = SheetFormat.of(filename);
    switch (sheetFormat) {
      case XLS:
      case CSV:
        // Check that the asset class was specified on the command line
        return new SingleSheetSimplePositionReader(sheetFormat, stream, new ExchangeTradedRowParser(securityProvider, dateFormatter));

      default:
        throw new OpenGammaRuntimeException("Input filename should end in .CSV or .XLS");
    }
  }

  
  private String getOptionValue(String optionName, String defaultValue) {
    return getCommandLine().getOptionValue(optionName) == null ? defaultValue : getCommandLine().getOptionValue(optionName);
  }

  
  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (CSV, XLS or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option portfolioNameOption = new Option(
        PORTFOLIO_NAME_OPT, "name", true, "The name of the destination OpenGamma portfolio");
    options.addOption(portfolioNameOption);
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the portfolio to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
 
    Option overwriteOption = new Option(
        OVERWRITE_OPT, "overwrite", false, 
        "Deletes any existing matching securities, positions and portfolios and recreates them from input data");
    options.addOption(overwriteOption);

    Option verboseOption = new Option(
        VERBOSE_OPT, "verbose", false, 
        "Displays progress messages on the terminal");
    options.addOption(verboseOption);
   
    Option timeSeriesDataProviderOption = new Option(
        TIME_SERIES_DATAPROVIDER_OPT, "provider", true, "The name of the time series data provider");
    options.addOption(timeSeriesDataProviderOption);
    
    Option timeSeriesDataFieldOption = new Option(
        TIME_SERIES_DATAFIELD_OPT, "field", true, "The name of the time series data field");
    options.addOption(timeSeriesDataFieldOption);
    
    Option dateFormatter = new Option(
        DATE_FORMAT_OPT, "dateFormat", true, "The dateFormat (e.g MM/dd/yyyy) if not ISO (yyyy-MM-dd)");
    options.addOption(dateFormatter);
    
    return options;
  }


}
