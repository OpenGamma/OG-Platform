/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.portfolio;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.integration.loadsave.portfolio.reader.PortfolioReader;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A simple portfolio copier that copies positions from readers to the specified writer.
 */
public class ResolvingPortfolioCopier implements PortfolioCopier {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolvingPortfolioCopier.class);

  private HistoricalTimeSeriesMaster _htsMaster;
  private HistoricalTimeSeriesSource _bbgHtsSource;
  private ReferenceDataProvider _bbgRefDataProvider;
  private String _dataProvider;
  private String[] _dataFields;
  
  public ResolvingPortfolioCopier(
      HistoricalTimeSeriesMaster htsMaster,
      HistoricalTimeSeriesSource bbgHtsSource,
      ReferenceDataProvider bbgRefDataProvider,
      String dataProvider,
      String[] dataFields) {
    
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(bbgHtsSource, "bbgHtsSource");
    ArgumentChecker.notNull(bbgRefDataProvider, "bbgRefDataProvider");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataFields, "dataFields");

    _htsMaster = htsMaster;
    _bbgHtsSource = bbgHtsSource;
    _bbgRefDataProvider = bbgRefDataProvider;
    _dataProvider = dataProvider;
    _dataFields = dataFields;    
  }
  
  @Override
  public void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter) {
      
    ArgumentChecker.notNull(portfolioWriter, "portfolioWriter");
    ArgumentChecker.notNull(portfolioReader, "portfolioReader");

    // Get bbg hts loader
    BloombergIdentifierProvider bbgIdentifierProvider = new BloombergIdentifierProvider(_bbgRefDataProvider);
    BloombergHistoricalLoader bbgLoader = new BloombergHistoricalLoader(_htsMaster, _bbgHtsSource, bbgIdentifierProvider);

    // Load in and write the securities, positions and trades, additionally loading related time series
    ObjectsPair<ManageablePosition, ManageableSecurity[]> next;

    // Read in next row, checking for EOF
    while ((next = portfolioReader.readNext()) != null) { 
      
      // If position and security data is available, send it to the writer
      if (next.getFirst() != null && next.getSecond() != null) {
        for (ManageableSecurity security : next.getSecond()) {
          portfolioWriter.writeSecurity(security);
          
          // Load this security's relevant HTSes
          for (String dataField : _dataFields) {
            Set<ExternalId> id = new HashSet<ExternalId>();
            id.add(security.getExternalIdBundle().getExternalId(ExternalScheme.of("BLOOMBERG_TICKER")));
            s_logger.warn("Loading historical time series " + id.toString() + ", fields " + dataField + 
                " from " + _dataProvider + ": " + bbgLoader.addTimeSeries(id, _dataProvider, dataField, null, null));
          }
        }
        portfolioWriter.writePosition(next.getFirst());
      }
    }
    
    // Flush changes to portfolio master & close
    portfolioWriter.flush();
  }
  
}
