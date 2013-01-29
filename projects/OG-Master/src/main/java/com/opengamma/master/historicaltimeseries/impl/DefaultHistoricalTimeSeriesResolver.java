/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.paging.PagingRequest;

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
  public HistoricalTimeSeriesResolutionResult resolve(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider,
      final String dataField, final String resolutionKey) {
    if (identifierBundle != null) {
      Collection<ManageableHistoricalTimeSeriesInfo> timeSeriesCandidates = search(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField);
      ManageableHistoricalTimeSeriesInfo selectedResult = select(timeSeriesCandidates, resolutionKey);
      if (selectedResult == null) {
        s_logger.warn("Resolver failed to find any time-series for {} using {}/{}", new Object[] {identifierBundle, dataField, resolutionKey });
        return null;
      }
      return new HistoricalTimeSeriesResolutionResult(selectedResult);
    } else {
      return search(dataSource, dataProvider, dataField);
    }
  }

  protected ManageableHistoricalTimeSeriesInfo select(Collection<ManageableHistoricalTimeSeriesInfo> timeSeriesCandidates, String resolutionKey) {
    return getSelector().select(timeSeriesCandidates, resolutionKey);
  }

  protected Collection<ManageableHistoricalTimeSeriesInfo> search(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    final HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest(identifierBundle);
    searchRequest.setValidityDate(identifierValidityDate);
    searchRequest.setDataSource(dataSource);
    searchRequest.setDataProvider(dataProvider);
    searchRequest.setDataField(dataField);
    final HistoricalTimeSeriesInfoSearchResult searchResult = _master.search(searchRequest);
    return searchResult.getInfoList();
  }

  protected HistoricalTimeSeriesResolutionResult search(final String dataSource, final String dataProvider, final String dataField) {
    final HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    searchRequest.setDataSource(dataSource);
    searchRequest.setDataProvider(dataProvider);
    searchRequest.setDataField(dataField);
    searchRequest.setPagingRequest(PagingRequest.NONE);
    if (_master.search(searchRequest).getPaging().getTotalItems() > 0) {
      return new HistoricalTimeSeriesResolutionResult(null, null);
    } else {
      return null;
    }
  }
  
  private HistoricalTimeSeriesSelector getSelector() {
    return _selector;
  }

}
