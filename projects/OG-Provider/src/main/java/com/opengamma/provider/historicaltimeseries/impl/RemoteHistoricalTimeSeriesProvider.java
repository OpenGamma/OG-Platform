/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Provides access to a remote time-series provider.
 * <p>
 * This is a client that connects to a time-series provider at a remote URI.
 */
public class RemoteHistoricalTimeSeriesProvider extends AbstractRemoteClient implements HistoricalTimeSeriesProvider {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteHistoricalTimeSeriesProvider(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  // delegate convenience methods to request/result method
  // code copied from AbstractHistoricalTimeSeriesProvider due to lack of multiple inheritance
  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle externalIdBundle, String dataSource, String dataProvider, String dataField) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(externalIdBundle, dataSource, dataProvider, dataField);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    return result.getResultMap().get(externalIdBundle);
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle externalIdBundle, String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(externalIdBundle, dataSource, dataProvider, dataField, dateRange);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    return result.getResultMap().get(externalIdBundle);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle externalIdBundle, String dataSource, String dataProvider, String dataField) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetLatest(externalIdBundle, dataSource, dataProvider, dataField);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    LocalDateDoubleTimeSeries series = result.getResultMap().get(externalIdBundle);
    if (series == null || series.isEmpty()) {
      return null;
    }
    return Pairs.of(series.getLatestTime(), series.getLatestValueFast());
  }

  @Override
  public Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> externalIdBundleSet, String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(externalIdBundleSet, dataSource, dataProvider, dataField, dateRange);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    return result.getResultMap();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesProviderGetResult getHistoricalTimeSeries(HistoricalTimeSeriesProviderGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    URI uri = DataHistoricalTimeSeriesProviderResource.uriGet(getBaseUri());
    return accessRemote(uri).post(HistoricalTimeSeriesProviderGetResult.class, request);
  }

}
