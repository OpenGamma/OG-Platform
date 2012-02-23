/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Provides functionality to ensure that a time-series is present in a historical time-series master while avoiding
 * duplicates.
 */
public class HistoricalTimeSeriesWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalTimeSeriesWriter.class);
  
  private final HistoricalTimeSeriesMaster _htsMaster;
  
  public HistoricalTimeSeriesWriter(HistoricalTimeSeriesMaster htsMaster) {
    _htsMaster = htsMaster;
  }

  /**
   * Updates an existing time-series in the master.
   * 
   * @param description  a description of the time-series for display purposes, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param observationTime  the descriptive observation time key, e.g. LONDON_CLOSE, not null
   * @param oId  the unique identifier of the time-series to be updated, not null
   * @param timeSeries  the time-series, not null
   * @return the unique identifier of the time-series
   */
  public UniqueId writeTimeSeries(String description, String dataSource, String dataProvider, String dataField,
      String observationTime, ObjectId oId, LocalDateDoubleTimeSeries timeSeries) {
    
    UniqueId uId = oId.atLatestVersion();
    
    ManageableHistoricalTimeSeries existingManageableTs = _htsMaster.getTimeSeries(uId);
    LocalDateDoubleTimeSeries existingTs = existingManageableTs.getTimeSeries();
   
    // There is a matching time-series already in the master so update it to reflect the new time-series
    // 1: 'correct' any differences in the subseries already present
    LocalDateDoubleTimeSeries tsIntersection = timeSeries.subSeries(existingTs.getEarliestTime(), true, existingTs.getLatestTime(), true);
    if (!tsIntersection.equals(existingTs)) {
      s_logger.info("Correcting time series " + oId + "[" + dataField + "] from " + existingTs.getEarliestTime() + " to " + existingTs.getLatestTime());
      uId = _htsMaster.correctTimeSeriesDataPoints(oId, tsIntersection);
    }
    // 2: 'update' the time-series to add any new, later points
    if (existingTs.getLatestTime().isBefore(timeSeries.getLatestTime())) {
      LocalDateDoubleTimeSeries newSeries = timeSeries.subSeries(existingTs.getLatestTime(), false, timeSeries.getLatestTime(), true);
      if (newSeries.size() > 0) {
        s_logger.info("Updating time series " + oId + "[" + dataField + "] from " + newSeries.getEarliestTime() + " to " + newSeries.getLatestTime());
        uId = _htsMaster.updateTimeSeriesDataPoints(oId, newSeries);
      }
    }
    
    return uId;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Adds or updates a time-series in the master.
   * 
   * @param description  a description of the time-series for display purposes, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param observationTime  the descriptive observation time key, e.g. LONDON_CLOSE, not null
   * @param externalIdBundle  the external identifiers with which the time-series is associated, not null
   * @param timeSeries  the time-series, not null
   * @return the unique identifier of the time-series
   */
  public UniqueId writeTimeSeries(String description, String dataSource, String dataProvider, String dataField,
      String observationTime, ExternalIdBundle externalIdBundle, LocalDateDoubleTimeSeries timeSeries) {
    ArgumentChecker.notNull(description, "description");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(observationTime, "observationTime");
    ArgumentChecker.notNull(externalIdBundle, "externalIdBundle");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    
    HistoricalTimeSeriesInfoSearchRequest htsSearchReq = new HistoricalTimeSeriesInfoSearchRequest();
    ExternalIdSearch idSearch = new ExternalIdSearch(externalIdBundle);
    htsSearchReq.setExternalIdSearch(idSearch);
    htsSearchReq.setDataSource(dataSource);
    htsSearchReq.setDataProvider(dataProvider);
    htsSearchReq.setDataField(dataField);
    HistoricalTimeSeriesInfoSearchResult searchResult = _htsMaster.search(htsSearchReq);
    if (searchResult.getDocuments().size() > 0) {
      // update existing time series
      HistoricalTimeSeriesInfoDocument existingTsDoc = searchResult.getFirstDocument();
      return writeTimeSeries(description, dataSource, dataProvider, dataField, observationTime, existingTsDoc.getObjectId(), timeSeries);
    } else {
      // add new time series
      ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
      info.setDataField(dataField);
      info.setDataSource(dataSource);
      info.setDataProvider(dataProvider);
      info.setObservationTime(observationTime);
      info.setExternalIdBundle(ExternalIdBundleWithDates.of(externalIdBundle));
      info.setName(description);
      HistoricalTimeSeriesInfoDocument htsInfoDoc = new HistoricalTimeSeriesInfoDocument();
      htsInfoDoc.setInfo(info);
      
      HistoricalTimeSeriesInfoDocument addedInfoDoc = _htsMaster.add(htsInfoDoc);
      s_logger.info("Adding time series " + externalIdBundle + " from " + timeSeries.getEarliestTime() + " to " + timeSeries.getLatestTime());
      return _htsMaster.updateTimeSeriesDataPoints(addedInfoDoc.getObjectId(), timeSeries);
    }
  }
  
}
