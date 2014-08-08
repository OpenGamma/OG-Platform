/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.tuple.Pair;

/**
 * HTS source which delegates all serviceable requests to the HTS provider.
 */
public abstract class MarketDataProviderHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {

  /**
   * Logger.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataProviderHistoricalTimeSeriesSource.class);

  /**
   * The id supplier service.
   */
  private final UniqueIdSupplier _uniqueIdSupplier;
  /**
   * The provider.
   */
  private final HistoricalTimeSeriesProvider _provider;
  /**
   * The name of the provider.
   */
  private final String _providerName;

  /**
   * Constructor for the class taking the provider to be used for all requests.
   *
   * @param providerName the name of the data provider
   * @param uniqueIdSupplier the supplier for unique ids
   * @param provider the provider of HTS data
   */
  public MarketDataProviderHistoricalTimeSeriesSource(String providerName,
                                                      UniqueIdSupplier uniqueIdSupplier,
                                                      HistoricalTimeSeriesProvider provider) {

    ArgumentChecker.notNull(providerName, "providerName");
    ArgumentChecker.notNull(uniqueIdSupplier, "uniqueIdSupplier");
    ArgumentChecker.notNull(provider, "provider");
    _providerName = providerName;
    _uniqueIdSupplier = uniqueIdSupplier;
    _provider = provider;

  }

  /**
   * Exception to be thrown if operation cannot be performed due to unique id.
   */
  private UnsupportedOperationException createUniqueIdException() {
    return new UnsupportedOperationException(
        "Unable to retrieve historical time-series from " + _providerName + " using unique identifier");
  }

  /**
   * Exception to be thrown if operation cannot be performed due to config.
   */
  private UnsupportedOperationException createConfigException() {
    return new UnsupportedOperationException(
        "Unable to retrieve historical time-series from " + _providerName + " using config");
  }

  /**
   * Exception to be thrown if operation cannot be performed due to validity date.
   */
  private UnsupportedOperationException createValidityDateException() {
    return new UnsupportedOperationException(
        "Unable to retrieve historical time-series from " + _providerName + " using identifier validity date");
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException("Change events not supported");
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      ExternalIdBundle externalIdBundle, String dataSource, String dataProvider, String dataField,
      LocalDateRange dateRange, Integer maxPoints) {

    s_logger.info("Getting HistoricalTimeSeries for security {}", externalIdBundle);

    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(externalIdBundle, dataSource, dataProvider, dataField, dateRange);
    request.setMaxPoints(maxPoints);
    HistoricalTimeSeriesProviderGetResult result = _provider.getHistoricalTimeSeries(request);
    LocalDateDoubleTimeSeries timeSeries = result.getResultMap().get(externalIdBundle);
    if (timeSeries == null) {
      s_logger.info("Unable to get HistoricalTimeSeries for {}", externalIdBundle);
      return null;
    }
    return new SimpleHistoricalTimeSeries(_uniqueIdSupplier.get(), timeSeries);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    throw createUniqueIdException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {


    throw createUniqueIdException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw createUniqueIdException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    throw createUniqueIdException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw createUniqueIdException();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField) {
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, LocalDateRange.ALL, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    LocalDate effectiveStart = start;
    if (!includeStart && effectiveStart != null) {
      effectiveStart = effectiveStart.plusDays(1);
    }
    LocalDateRange dateRange = LocalDateRange.ofNullUnbounded(effectiveStart, end, includeEnd);
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, dateRange, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    LocalDate effectiveStart = start;
    if (!includeStart && effectiveStart != null) {
      effectiveStart = effectiveStart.plusDays(1);
    }
    LocalDateRange dateRange = LocalDateRange.ofNullUnbounded(effectiveStart, end, includeEnd);
    Integer maxPointsVal = (maxPoints == 0 ? null : maxPoints);
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, dateRange, maxPointsVal);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    throw createValidityDateException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifiers,
      LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw createValidityDateException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw createValidityDateException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    throw createValidityDateException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw createValidityDateException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    throw createValidityDateException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    throw createValidityDateException();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, String resolutionKey) {
    throw createConfigException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw createConfigException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey) {
    throw createConfigException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw createConfigException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd, int maxPoints) {
    throw createConfigException();
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw createConfigException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    throw createConfigException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw createConfigException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    throw createConfigException();
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    throw createConfigException();
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> externalIdBundles, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    
    LocalDate effectiveStart = start;
    if (!includeStart && effectiveStart != null) {
      effectiveStart = effectiveStart.plusDays(1);
    }
    LocalDateRange dateRange = LocalDateRange.ofNullUnbounded(effectiveStart, end, includeEnd);
    s_logger.info("Getting HistoricalTimeSeries for securities {}", externalIdBundles);

    Map<ExternalIdBundle, LocalDateDoubleTimeSeries> map = _provider.getHistoricalTimeSeries(externalIdBundles, dataSource, dataProvider, dataField, dateRange);
    Map<ExternalIdBundle, HistoricalTimeSeries> result = Maps.newHashMap();
    for (ExternalIdBundle bundle : map.keySet()) {
      LocalDateDoubleTimeSeries ts = map.get(bundle);
      HistoricalTimeSeries hts = null;
      if (ts != null) {
        hts = new SimpleHistoricalTimeSeries(_uniqueIdSupplier.get(), ts);
      }
      result.put(bundle, hts);
    }
    return result;
  }

  @Override
  public ExternalIdBundle getExternalIdBundle(UniqueId uniqueId) {
    throw createConfigException();
  }
}
