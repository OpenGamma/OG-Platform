/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.integration.loadsave.portfolio.reader.ZippedPortfolioReader;
import com.opengamma.integration.loadsave.portfolio.writer.DummyPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;

/**
 * Provides portfolio loading functionality
 */
public class PortfolioLoader {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioLoader.class);

  public void run(String portfolioName, String fileName, String securityType, boolean persist, ToolContext toolContext) {
    
    // Set up writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        portfolioName, 
        persist,
        toolContext);
    
     // Set up reader
    PortfolioReader portfolioReader = constructPortfolioReader(
        fileName, 
        securityType, 
        toolContext);
    
    // Load in and write the securities, positions and trades
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

  private static PortfolioReader constructPortfolioReader(String filename, String securityClass, ToolContext toolContext) {
    String extension = filename.substring(filename.lastIndexOf('.'));
    
    // Single CSV or XLS file extension
    if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
      // Check that the asset class was specified on the command line
      if (securityClass == null) {
        throw new OpenGammaRuntimeException("Could not import as no asset class was specified for file " + filename + " (use '-a')");
      } else {
        return new SingleSheetSimplePortfolioReader(filename, securityClass, toolContext);
      }
    // Multi-asset ZIP file extension
    } else if (extension.equalsIgnoreCase(".zip")) {
      // Create zipped multi-asset class loader
      return new ZippedPortfolioReader(filename, toolContext);
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV, .XLS or .ZIP");
    }
  }

}
