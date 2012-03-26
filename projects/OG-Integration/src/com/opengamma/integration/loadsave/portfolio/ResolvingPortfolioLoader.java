/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.integration.loadsave.portfolio;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergIdentifierProvider;


import com.opengamma.bbg.BloombergSecurityMaster;
import com.opengamma.bbg.ReferenceDataProvider;

import com.opengamma.bbg.loader.BloombergHistoricalLoader;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.integration.loadsave.portfolio.reader.PortfolioReader;
import com.opengamma.integration.loadsave.portfolio.reader.SingleSheetSimplePortfolioReader;
import com.opengamma.integration.loadsave.portfolio.rowparser.ExchangeTradedRowParser;
import com.opengamma.integration.loadsave.portfolio.writer.DummyPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * This is a portfolio loader that also attempts to resolve and load related time series 
 */
public class ResolvingPortfolioLoader {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolvingPortfolioLoader.class);

  private final BloombergSecurityMaster _bbgSecurityMaster;
  private final HistoricalTimeSeriesMaster _htsMaster;
  private final HistoricalTimeSeriesSource _bbgHtsSource;
  private final ReferenceDataProvider _bbgRefDataProvider;
  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;
  private final SecurityMaster _securityMaster;

  public ResolvingPortfolioLoader(BloombergSecurityMaster bbgSecurityMaster,
                                  HistoricalTimeSeriesMaster htsMaster,
                                  HistoricalTimeSeriesSource bbgHtsSource,
                                  ReferenceDataProvider bbgRefDataProvider,
                                  PortfolioMaster portfolioMaster,
                                  PositionMaster positionMaster,
                                  SecurityMaster securityMaster) {
    ArgumentChecker.notNull(bbgSecurityMaster, "bbgSecurityMaster");
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(bbgHtsSource, "bbgHtsSource");
    ArgumentChecker.notNull(bbgRefDataProvider, "bbgRefDataProvider");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _bbgSecurityMaster = bbgSecurityMaster;
    _htsMaster = htsMaster;
    _bbgHtsSource = bbgHtsSource;
    _bbgRefDataProvider = bbgRefDataProvider;
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
  }

  public void loadPortfolio(String portfolioName,
                            String fileName,
                            InputStream portfolioFileStream,
                            String dataSource,
                            String dataProvider,
                            String[] dataFields,
                            String observationTime,
                            boolean persist) {
    // Set up writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(portfolioName, persist);

    PortfolioReader portfolioReader = constructPortfolioReader(fileName, portfolioFileStream, _bbgSecurityMaster);

    // Get bbg hts loader
    BloombergIdentifierProvider bbgIdentifierProvider = new BloombergIdentifierProvider(_bbgRefDataProvider);
    BloombergHistoricalLoader bbgLoader = new BloombergHistoricalLoader(_htsMaster, _bbgHtsSource, bbgIdentifierProvider);

    // Load in and write the securities, positions and trades, additionally loading related time series
    while (true) {
      
      // Read in next row
      ObjectsPair<ManageablePosition, ManageableSecurity[]> next = portfolioReader.readNext();
      
      // Check for EOF
      if (next == null) {
        break;
      }
      
      // If position and security data is available, send it to the writer
      if (next.getFirst() != null && next.getSecond() != null) {
        for (ManageableSecurity security : next.getSecond()) {
          portfolioWriter.writeSecurity(security);
          
          // Load this security's relevant HTSes
          if (dataFields != null) {
            for (String dataField : dataFields) {
              Set<ExternalId> id = new HashSet<ExternalId>();
              id.add(security.getExternalIdBundle().getExternalId(ExternalScheme.of("BLOOMBERG_TICKER")));
              s_logger.warn("Loading historical time series " + id.toString() + ", fields " + dataField + 
                  " from " + dataProvider + ": " + bbgLoader.addTimeSeries(id, dataProvider, dataField, null, null));
            }
          }
        }
        portfolioWriter.writePosition(next.getFirst());
      }
    }
    
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

  // TODO take a stream as well as the file name, BBG master
  private static PortfolioReader constructPortfolioReader(String filename, InputStream stream, BloombergSecurityMaster bbgSecurityMaster) {
    String extension = filename.substring(filename.lastIndexOf('.'));
    
    // Single CSV or XLS file extension
    if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
      // Check that the asset class was specified on the command line
      return new SingleSheetSimplePortfolioReader(filename, stream, new ExchangeTradedRowParser(bbgSecurityMaster));
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .XLS");
    }
  }

}
