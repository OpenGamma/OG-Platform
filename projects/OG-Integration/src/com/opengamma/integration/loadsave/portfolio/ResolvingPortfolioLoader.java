/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.integration.loadsave.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.loadsave.portfolio.reader.PortfolioReader;
import com.opengamma.integration.loadsave.portfolio.reader.SingleSheetSimplePortfolioReader;
import com.opengamma.integration.loadsave.portfolio.rowparser.ExchangeTradedRowParser;
import com.opengamma.integration.loadsave.portfolio.writer.DummyPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.tool.IntegrationToolContext;

/**
 * This is a portfolio loader that also attempts to resolve and load related time series 
 * (such as PX_LAST, and normally supplied on the tool's command line) will be pre-loaded.
 */
public class ResolvingPortfolioLoader {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolvingPortfolioLoader.class);

  public void run(String portfolioName, String fileName, String dataSource, String dataProvider, 
      String dataField, String observationTime, boolean persist, IntegrationToolContext toolContext) {
    
    // Set up writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        portfolioName, 
        persist,
        toolContext);
    
     // Set up reader
    PortfolioReader portfolioReader = constructPortfolioReader(
        fileName, 
        toolContext);
    
    // Load in and write the securities, positions and trades, additionally loading related time series
    // TODO open up portfolioReader/Writer
    portfolioReader.writeTo(portfolioWriter);
    
    // Flush changes to portfolio master & close
    portfolioWriter.flush();
    portfolioWriter.close();
  }

  private static PortfolioWriter constructPortfolioWriter(String portfolioName, boolean write, ToolContext toolContext) {
    if (write) {  
      // Check that the portfolio name was specified on the command line
      if (portfolioName == null) {
        throw new OpenGammaRuntimeException("Portfolio name omitted, cannot persist to OpenGamma masters");
      }
      
      s_logger.info("Write option specified, will persist to OpenGamma masters in portfolio '" + portfolioName + "'");
      
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterPortfolioWriter(portfolioName, toolContext);
      
    } else {
      s_logger.info("Write option omitted, will pretty-print instead of persisting to OpenGamma masters");
      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyPortfolioWriter();         
    }
  }

  private static PortfolioReader constructPortfolioReader(String filename, IntegrationToolContext toolContext) {
    String extension = filename.substring(filename.lastIndexOf('.'));
    
    // Single CSV or XLS file extension
    if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
      // Check that the asset class was specified on the command line
      return new SingleSheetSimplePortfolioReader(filename, new ExchangeTradedRowParser(toolContext));
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .XLS");
    }
  }

}
