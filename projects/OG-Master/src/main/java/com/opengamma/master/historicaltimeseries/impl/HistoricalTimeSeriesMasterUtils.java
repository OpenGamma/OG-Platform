/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides functionality to ensure that a time-series is present in a historical time-series master while avoiding
 * duplicates.
 */
public class HistoricalTimeSeriesMasterUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalTimeSeriesMasterUtils.class);
  
  private final HistoricalTimeSeriesMaster _htsMaster;
  
  public HistoricalTimeSeriesMasterUtils(HistoricalTimeSeriesMaster htsMaster) {
    _htsMaster = htsMaster;
  }

  /**
   * Updates an existing time-series in the master.
   * If the time series provided has overlaps with the existing time series, the old
   * versions of intersecting points will be corrected to the new ones.
   * After that, points later than the existing latest point of the time series will
   * be appended.
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

    if (existingTs.isEmpty()) {
      uId = _htsMaster.updateTimeSeriesDataPoints(oId, timeSeries);
      s_logger.debug("Updating time series " + oId + "[" + dataField + "] with all as currently emtpy)");
    } else {
      // There is a non-empty matching time-series already in the master so update it to reflect the new time-series
      // 1: 'correct' any differences in the subseries already present
      LocalDateDoubleTimeSeries tsIntersection = timeSeries.subSeries(existingTs.getEarliestTime(), true, existingTs.getLatestTime(), true);
      if (!tsIntersection.equals(existingTs)) {
        s_logger.debug("Correcting time series " + oId + "[" + dataField + "] from " + existingTs.getEarliestTime() + " to " + existingTs.getLatestTime());
        uId = _htsMaster.correctTimeSeriesDataPoints(oId, tsIntersection);
      }
      // 2: 'update' the time-series to add any new, later points
      if (existingTs.getLatestTime().isBefore(timeSeries.getLatestTime())) {
        LocalDateDoubleTimeSeries newSeries = timeSeries.subSeries(existingTs.getLatestTime(), false, timeSeries.getLatestTime(), true);
        if (newSeries.size() > 0) {
          s_logger.debug("Updating time series " + oId + "[" + dataField + "] from " + newSeries.getEarliestTime() + " to " + newSeries.getLatestTime());
          uId = _htsMaster.updateTimeSeriesDataPoints(oId, newSeries);
        }
      }
    }
    return uId;
  }
  
  /**
   * Updates an existing time-series in the master.
   * @param uniqueId  the unique identifier of the time-series to be updated, not null
   * @param timeSeries  the time-series, not null
   * @return the unique identifier of the time-series
   */
  public UniqueId writeTimeSeries(UniqueId uniqueId, LocalDateDoubleTimeSeries timeSeries) {
    
    ManageableHistoricalTimeSeries existingManageableTs = _htsMaster.getTimeSeries(uniqueId);
    LocalDateDoubleTimeSeries existingTs = existingManageableTs.getTimeSeries();
    if (existingTs.isEmpty()) {
      _htsMaster.updateTimeSeriesDataPoints(uniqueId, timeSeries);
      s_logger.debug("Updating time series " + uniqueId + " with all as currently emtpy)");
    } else {
      // There is a matching time-series already in the master so update it to reflect the new time-series
      // 1: 'correct' any differences in the subseries already present
      LocalDateDoubleTimeSeries tsIntersection = timeSeries.subSeries(existingTs.getEarliestTime(), true, existingTs.getLatestTime(), true);
      if (!tsIntersection.equals(existingTs)) {
        s_logger.debug("Correcting time series " + uniqueId + " from " + existingTs.getEarliestTime() + " to " + existingTs.getLatestTime());
        uniqueId = _htsMaster.correctTimeSeriesDataPoints(uniqueId.getObjectId(), tsIntersection);
      }
      // 2: 'update' the time-series to add any new, later points
      if (existingTs.getLatestTime().isBefore(timeSeries.getLatestTime())) {
        LocalDateDoubleTimeSeries newSeries = timeSeries.subSeries(existingTs.getLatestTime(), false, timeSeries.getLatestTime(), true);
        if (newSeries.size() > 0) {
          s_logger.debug("Updating time series " + uniqueId + " from " + newSeries.getEarliestTime() + " to " + newSeries.getLatestTime());
          uniqueId = _htsMaster.updateTimeSeriesDataPoints(uniqueId, newSeries);
        }
      }
    }
    return uniqueId;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Adds or updates a time-series in the master.  Can be a sub-set of the data points present and will not 'erase' 
   * points that are missing, only supplement them.
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
    return writeTimeSeries(description, dataSource, dataProvider, dataField, observationTime, externalIdBundle, null,
        timeSeries);
  }
  
  /**
   * Adds or updates a time-series in the master.  Can be a sub-set of the data points present and will not 'erase' 
   * points that are missing, only supplement them.
   * 
   * @param description  a description of the time-series for display purposes, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param observationTime  the descriptive observation time key, e.g. LONDON_CLOSE, not null
   * @param externalIdBundle  the external identifiers with which the time-series is associated, not null
   * @param externalIdSearchType  the external identifier search type for matching an existing time-series, null to use the default
   * @param timeSeries  the time-series, not null
   * @return the unique identifier of the time-series
   */
  public UniqueId writeTimeSeries(String description, String dataSource, String dataProvider, String dataField,
      String observationTime, ExternalIdBundle externalIdBundle, ExternalIdSearchType externalIdSearchType,
      LocalDateDoubleTimeSeries timeSeries) {
    ArgumentChecker.notNull(description, "description");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(observationTime, "observationTime");
    ArgumentChecker.notNull(externalIdBundle, "externalIdBundle");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    
    HistoricalTimeSeriesInfoSearchRequest htsSearchReq = new HistoricalTimeSeriesInfoSearchRequest();
    ExternalIdSearch idSearch = ExternalIdSearch.of(externalIdBundle);
    if (externalIdSearchType != null) {
      idSearch = idSearch.withSearchType(externalIdSearchType);
    }
    htsSearchReq.setExternalIdSearch(idSearch);
    htsSearchReq.setDataSource(dataSource);
    htsSearchReq.setDataProvider(dataProvider);
    htsSearchReq.setDataField(dataField);
    htsSearchReq.setObservationTime(observationTime);
    HistoricalTimeSeriesInfoSearchResult searchResult = _htsMaster.search(htsSearchReq);
    if (searchResult.getDocuments().size() > 0) {
      if (searchResult.getDocuments().size() > 1) {
        s_logger.warn("Found multiple time-series matching search. Will only update the first. Search {} returned {}", htsSearchReq, searchResult.getInfoList());
      }
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
      s_logger.debug("Adding time series " + externalIdBundle + " from " + timeSeries.getEarliestTime() + " to " + timeSeries.getLatestTime());
      return _htsMaster.updateTimeSeriesDataPoints(addedInfoDoc.getInfo().getTimeSeriesObjectId(), timeSeries);
    }
  }
  
  /**
   * Adds or updates a time-series in the master.  Will not "erase" any existing point, just
   * used to add a new point.
   * 
   * @param description  a description of the time-series for display purposes, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param observationTime  the descriptive observation time key, e.g. LONDON_CLOSE, not null
   * @param externalIdBundle  the external identifiers with which the time-series is associated, not null
   * @param date  the date, not null
   * @param value the value, not null
   * @return the unique identifier of the time-series
   */
  public UniqueId writeTimeSeriesPoint(String description, String dataSource, String dataProvider, String dataField,
      String observationTime, ExternalIdBundle externalIdBundle, LocalDate date, double value) {
    LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(date, value);
    return writeTimeSeries(description, dataSource, dataProvider, dataField, observationTime, externalIdBundle, ts);
  }

}
