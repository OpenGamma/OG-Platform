/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
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
    copy(portfolioReader, portfolioWriter, null);
  }

  @Override
  public void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter, PortfolioCopierVisitor visitor) {

    ArgumentChecker.notNull(portfolioWriter, "portfolioWriter");
    ArgumentChecker.notNull(portfolioReader, "portfolioReader");
    
    // Get bbg hts loader
    BloombergIdentifierProvider bbgIdentifierProvider = new BloombergIdentifierProvider(_bbgRefDataProvider);
    BloombergHistoricalLoader bbgLoader = new BloombergHistoricalLoader(_htsMaster, _bbgHtsSource, bbgIdentifierProvider);

    ObjectsPair<ManageablePosition, ManageableSecurity[]> next;

    // Read in next row, checking for EOF
    while ((next = portfolioReader.readNext()) != null) {
      
      ManageablePosition writtenPosition;
      List<ManageableSecurity> writtenSecurities = new LinkedList<ManageableSecurity>();
      
      // Is position and security data is available for the current row?
      if (next.getFirst() != null && next.getSecond() != null) {
        
        // Set current path
        String[] path = portfolioReader.getCurrentPath();
        portfolioWriter.setPath(path);
        
        // Write position and security data
        for (ManageableSecurity security : next.getSecond()) {
          writtenSecurities.add(portfolioWriter.writeSecurity(security));
          
          // Load this security's relevant HTSes
          for (String dataField : _dataFields) {
            Set<ExternalId> ids = new HashSet<ExternalId>();
            ids = security.getExternalIdBundle().getExternalIds();
            Map<ExternalId, UniqueId> tsMap = null;
            for (ExternalId id : ids) {
              tsMap = bbgLoader.addTimeSeries(Collections.singleton(id), _dataProvider, dataField, null, null);
              String message = "historical time series " + id.toString() + ", fields " + dataField + 
                  " from " + _dataProvider;
              if (tsMap.size() > 0) {
                s_logger.info("Loaded " + message + ": " + tsMap);
                if (visitor != null) {
                  visitor.info("Loaded " + message);
                }
                break;
              }
            }
            if (tsMap == null || tsMap.size() == 0) {
              s_logger.warn("Could not load historical time series for security " + security);
              if (visitor != null) {
                visitor.error("Could not load historical time series for security " + security);
              }
            }
          }
        }
        writtenPosition = portfolioWriter.writePosition(next.getFirst());
        
        if (visitor != null) {
          visitor.info(StringUtils.arrayToDelimitedString(path, "/"), writtenPosition, writtenSecurities);
        }
      } else {
        if (visitor != null) {
          visitor.error("Could not load" + (next.getFirst() == null ? " position" : "") + (next.getSecond() == null ? " security" : ""));
        }
      }
      
    }

    // Flush changes to portfolio master
    portfolioWriter.flush();
  }
  
}
