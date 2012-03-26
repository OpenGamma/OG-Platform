/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.portfolio;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.loadsave.portfolio.reader.PortfolioReader;
import com.opengamma.integration.loadsave.portfolio.reader.SingleSheetSimplePortfolioReader;
import com.opengamma.integration.loadsave.portfolio.reader.ZippedPortfolioReader;
import com.opengamma.integration.loadsave.portfolio.writer.DummyPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides portfolio loading functionality
 */
public class PortfolioLoader {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioLoader.class);

  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;
  private final SecurityMaster _securityMaster;

  public PortfolioLoader(PortfolioMaster portfolioMaster, PositionMaster positionMaster, SecurityMaster securityMaster) {
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
  }

  public void run(String portfolioName, String fileName, InputStream portfolioFileStream, String securityType, boolean persist) {
    
    // Set up writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(portfolioName, persist);
    
     // Set up reader
    PortfolioReader portfolioReader = constructPortfolioReader(fileName, portfolioFileStream, securityType);
    
    // Load in and write the securities, positions and trades
    portfolioReader.writeTo(portfolioWriter);
    
    // Flush changes to portfolio master & close
    portfolioWriter.flush();
    portfolioWriter.close();
  }

  private PortfolioWriter constructPortfolioWriter(String portfolioName, boolean write) {
    if (write) {
      // Check that the portfolio name was specified on the command line
      if (portfolioName == null) {
        throw new OpenGammaRuntimeException("Portfolio name omitted, cannot persist to OpenGamma masters");
      }
      
      s_logger.info("Write option specified, will persist to OpenGamma masters in portfolio '" + portfolioName + "'");
      
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterPortfolioWriter(portfolioName, _portfolioMaster, _positionMaster, _securityMaster);
      
    } else {
      s_logger.info("Write option omitted, will pretty-print instead of persisting to OpenGamma masters");
      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyPortfolioWriter();
    }

  }

  private PortfolioReader constructPortfolioReader(String filename, InputStream portfolioFileStream, String securityClass) {
    String extension = filename.substring(filename.lastIndexOf('.'));
    
    // Single CSV or XLS file extension
    if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
      // Check that the asset class was specified on the command line
      if (securityClass == null) {
        throw new OpenGammaRuntimeException("Could not import as no asset class was specified for file " + filename + " (use '-a')");
      } else {
        return new SingleSheetSimplePortfolioReader(filename, portfolioFileStream, securityClass);
      }
    // Multi-asset ZIP file extension
    } else if (extension.equalsIgnoreCase(".zip")) {
      // Create zipped multi-asset class loader
    return new ZippedPortfolioReader(filename);
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV, .XLS or .ZIP");
    }
  }

}
