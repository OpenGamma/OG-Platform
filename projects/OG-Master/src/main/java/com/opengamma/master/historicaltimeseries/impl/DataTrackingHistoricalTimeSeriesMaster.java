/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * HistoricalTimeSeries master which tracks accesses using UniqueIds.
 */
public class DataTrackingHistoricalTimeSeriesMaster extends AbstractDataTrackingMaster<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster> implements HistoricalTimeSeriesMaster {
  
  private static final String DATA_POINT_PREFIX = "DP";

  public DataTrackingHistoricalTimeSeriesMaster(HistoricalTimeSeriesMaster delegate) {
    super(delegate);
  }

  @Override
  public HistoricalTimeSeriesInfoSearchResult search(HistoricalTimeSeriesInfoSearchRequest request) {
    HistoricalTimeSeriesInfoSearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(HistoricalTimeSeriesInfoHistoryRequest request) {
    HistoricalTimeSeriesInfoHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(HistoricalTimeSeriesInfoMetaDataRequest request) {
    return delegate().metaData(request);
  }
  
  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId) {
    ManageableHistoricalTimeSeries timeSeries = delegate().getTimeSeries(uniqueId);
    //trackId(timeSeries.getUniqueId());
    return timeSeries;
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId, HistoricalTimeSeriesGetFilter filter) {
    ManageableHistoricalTimeSeries timeSeries = delegate().getTimeSeries(uniqueId, filter);
    trackId(timeSeries.getUniqueId());
    return timeSeries;
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ManageableHistoricalTimeSeries timeSeries = delegate().getTimeSeries(objectId, versionCorrection);
    trackId(timeSeries.getUniqueId());
    return timeSeries;
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection, HistoricalTimeSeriesGetFilter filter) {
    ManageableHistoricalTimeSeries timeSeries = delegate().getTimeSeries(objectId, versionCorrection, filter);
    trackId(timeSeries.getUniqueId());
    return timeSeries;
  }

  @Override
  public UniqueId updateTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    UniqueId id = delegate().updateTimeSeriesDataPoints(objectId, series);
    return trackId(id);
  }

  @Override
  public UniqueId correctTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    UniqueId id = delegate().correctTimeSeriesDataPoints(objectId, series);
    return trackId(id);
  }

  @Override
  public UniqueId removeTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    UniqueId id = delegate().removeTimeSeriesDataPoints(objectId, fromDateInclusive, toDateInclusive);
    return trackId(id);
  }
  
  
  /**
   * DP ids (internal to HTSMaster) should be ignored.
   * @param id the id
   * @return the id
   */
  protected synchronized UniqueId trackId(UniqueId id) {
    if (!isDPId(id)) {
      return super.trackId(id);
    }
    return id;
  }

  private boolean isDPId(UniqueId id) {
    return id != null && id.getValue().startsWith(DATA_POINT_PREFIX);
  }
  
  
}
