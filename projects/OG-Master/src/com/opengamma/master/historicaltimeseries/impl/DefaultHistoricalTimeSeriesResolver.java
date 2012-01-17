/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Collection;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple implementation of {@link HistoricalTimeSeriesResolver} which backs directly onto retrieves candidates from an underlying master.
 */
public class DefaultHistoricalTimeSeriesResolver implements HistoricalTimeSeriesResolver {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultHistoricalTimeSeriesResolver.class);
  
  private final HistoricalTimeSeriesSelector _selector;
  private final HistoricalTimeSeriesMaster _master;
  
  public DefaultHistoricalTimeSeriesResolver(HistoricalTimeSeriesSelector selector, HistoricalTimeSeriesMaster master) {
    _selector = selector;
    _master = master;
  }

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      String resolutionKey) {
    Collection<ManageableHistoricalTimeSeriesInfo> timeSeriesCandidates = search(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField);
    ManageableHistoricalTimeSeriesInfo selectedResult = select(timeSeriesCandidates, resolutionKey);
    if (selectedResult == null) {
      s_logger.warn("Resolver failed to find any time-series for {} using {}/{}", new Object[] {identifierBundle, dataField, resolutionKey});
      return null;
    }
    return new HistoricalTimeSeriesResolutionResult(selectedResult);
  }
  
  protected ManageableHistoricalTimeSeriesInfo select(Collection<ManageableHistoricalTimeSeriesInfo> timeSeriesCandidates, String resolutionKey) {
    return getSelector().select(timeSeriesCandidates, resolutionKey);
  }
  
  protected Collection<ManageableHistoricalTimeSeriesInfo> search(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    
    HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest(identifierBundle);
    searchRequest.setValidityDate(identifierValidityDate);
    searchRequest.setDataSource(dataSource);
    searchRequest.setDataProvider(dataProvider);
    searchRequest.setDataField(dataField);
    HistoricalTimeSeriesInfoSearchResult searchResult = _master.search(searchRequest);
    return searchResult.getInfoList();    
  }
  
  //-------------------------------------------------------------------------
  private HistoricalTimeSeriesSelector getSelector() {
    return _selector;
  }
  
}
