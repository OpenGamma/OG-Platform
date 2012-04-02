/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.copier.portfolio.PortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.QuietPortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.SimplePortfolioCopier;
import com.opengamma.integration.copier.portfolio.VerbosePortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.ZippedPortfolioReader;
import com.opengamma.integration.copier.portfolio.writer.DummyPortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;

/**
 * The portfolio loader tool
 */
public class PortfolioLoaderTool extends AbstractTool {

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
  /** Asset class flag */
  private static final String SECURITY_TYPE_OPT = "s";

  private static ToolContext s_context;
  
  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new PortfolioLoaderTool().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio into the position master.
   */
  @Override
  protected void doRun() {
    s_context = getToolContext();

    // Create portfolio writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        getCommandLine().getOptionValue(PORTFOLIO_NAME_OPT), 
        getCommandLine().hasOption(WRITE_OPT),
        getCommandLine().hasOption(OVERWRITE_OPT)
    );

    // Construct portfolio reader
    PortfolioReader portfolioReader = constructPortfolioReader(
        getCommandLine().getOptionValue(FILE_NAME_OPT), 
        getCommandLine().getOptionValue(SECURITY_TYPE_OPT)
    );

    // Construct portfolio copier
    SimplePortfolioCopier portfolioCopier = new SimplePortfolioCopier();
        
    // Create visitor for verbose/quiet mode
    PortfolioCopierVisitor portfolioCopierVisitor; 
    if (getCommandLine().hasOption(VERBOSE_OPT)) {
      portfolioCopierVisitor = new VerbosePortfolioCopierVisitor();
    } else {
      portfolioCopierVisitor = new QuietPortfolioCopierVisitor();
    }
    
    // Call the portfolio loader with the supplied arguments
    portfolioCopier.copy(portfolioReader, portfolioWriter, portfolioCopierVisitor);

    // close stuff
    portfolioReader.close();
    portfolioWriter.close();
  }
 
  private static PortfolioWriter constructPortfolioWriter(String portfolioName, boolean write, boolean overwrite) {
    if (write) {  
      if (overwrite) {
        System.out.println("Write and overwrite options specified, will persist to portfolio '" + portfolioName + "'"); 
      } else {
        System.out.println("Write option specified, will persist to portfolio '" + portfolioName + "'");

      }
      // Check that the portfolio name was specified on the command line
      if (portfolioName == null) {
        throw new OpenGammaRuntimeException("Portfolio name omitted, cannot persist to OpenGamma masters");
      }
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterPortfolioWriter(
          portfolioName, 
          s_context.getPortfolioMaster(), 
          s_context.getPositionMaster(), 
          s_context.getSecurityMaster(),
          overwrite);
    } else {
      System.out.println("Write option not specified, not persisting to OpenGamma masters");

      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyPortfolioWriter();         
    }  
  }

  private PortfolioReader constructPortfolioReader(String filename, String securityType) {

    SheetFormat sheetFormat = SheetFormat.of(filename);
    switch (sheetFormat) {
      
      case CSV:
      case XLS:
        // Check that the asset class was specified on the command line
        if (securityType == null) {
          throw new OpenGammaRuntimeException("Could not import as no asset class was specified for file " + filename);
        } else {
//          if (securityType.equalsIgnoreCase("exchangetraded")) {
//            return new SingleSheetSimplePortfolioReader(filename, new ExchangeTradedRowParser(s_context.getBloombergSecuritySource()));            
//          } else {
          return new SingleSheetSimplePortfolioReader(filename, securityType);
//          }
        }
      
      case ZIP:
        // Create zipped multi-asset class loader
        return new ZippedPortfolioReader(filename);
        
      default:
        throw new OpenGammaRuntimeException("Input filename should end in .CSV, .XLS or .ZIP");
    }
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
    
    Option assetClassOption = new Option(
        SECURITY_TYPE_OPT, "security", true, 
        "The security type expected in the input CSV/XLS file (ignored if ZIP file is specified)");
    options.addOption(assetClassOption);
    
    Option overwriteOption = new Option(
        OVERWRITE_OPT, "overwrite", false, 
        "Deletes any existing matching securities, positions and portfolios and recreates them from input data");
    options.addOption(overwriteOption);

    Option verboseOption = new Option(
        VERBOSE_OPT, "verbose", false, 
        "Displays progress messages on the terminal");
    options.addOption(verboseOption);

    return options;
  }

}
