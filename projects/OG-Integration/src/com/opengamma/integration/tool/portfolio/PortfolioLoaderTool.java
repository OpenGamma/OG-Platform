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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.loadsave.portfolio.PortfolioCopier;
import com.opengamma.integration.loadsave.portfolio.SimplePortfolioCopier;
import com.opengamma.integration.loadsave.portfolio.reader.PortfolioReader;
import com.opengamma.integration.loadsave.portfolio.reader.SingleSheetSimplePortfolioReader;
import com.opengamma.integration.loadsave.portfolio.reader.ZippedPortfolioReader;
import com.opengamma.integration.loadsave.portfolio.writer.DummyPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.loadsave.sheet.SheetFormat;
import com.opengamma.integration.tool.AbstractIntegrationTool;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;

/**
 * The portfolio loader tool
 */
public class PortfolioLoaderTool extends AbstractIntegrationTool {

  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Portfolio name option flag*/
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Asset class flag */
  private static final String SECURITY_TYPE_OPT = "s";

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
    IntegrationToolContext context = getToolContext();

    // Create portfolio writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        getCommandLine().getOptionValue(PORTFOLIO_NAME_OPT), 
        context.getPortfolioMaster(), 
        context.getPositionMaster(), 
        context.getSecurityMaster(),
        getCommandLine().hasOption(WRITE_OPT)
    );

    // Construct portfolio reader
    PortfolioReader portfolioReader = constructPortfolioReader(
        getCommandLine().getOptionValue(FILE_NAME_OPT), 
        getCommandLine().getOptionValue(SECURITY_TYPE_OPT)
    );

    // Construct portfolio copier
    PortfolioCopier portfolioCopier = new SimplePortfolioCopier();
        
    // Copy portfolio
    portfolioCopier.copy(portfolioReader, portfolioWriter);

    // close stuff
    portfolioReader.close();
    portfolioWriter.close();
  }
 
  private static PortfolioWriter constructPortfolioWriter(String portfolioName, PortfolioMaster portfolioMaster,
      PositionMaster positionMaster, SecurityMaster securityMaster, boolean write) {
    if (write) {  
      // Check that the portfolio name was specified on the command line
      if (portfolioName == null) {
        throw new OpenGammaRuntimeException("Portfolio name omitted, cannot persist to OpenGamma masters");
      }
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterPortfolioWriter(
          portfolioName, 
          portfolioMaster, 
          positionMaster, 
          securityMaster,
          false);
    } else {
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyPortfolioWriter();         
    }  
  }

  private PortfolioReader constructPortfolioReader(String filename, String securityClass) {

    SheetFormat sheetFormat = SheetFormat.of(filename);
    switch (sheetFormat) {
      
      case CSV:
      case XLS:
        // Check that the asset class was specified on the command line
        if (securityClass == null) {
          throw new OpenGammaRuntimeException("Could not import as no asset class was specified for file " + filename + " (use '-a')");
        } else {
          InputStream inputStream;
          try {
            inputStream = new BufferedInputStream(new FileInputStream(filename));
          } catch (FileNotFoundException e) {
            throw new OpenGammaRuntimeException("Could not open file " + filename + " for reading: " + e);
          }
          return new SingleSheetSimplePortfolioReader(sheetFormat, inputStream, securityClass);
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
    
    return options;
  }

}
