/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.RandomTimeSeriesGenerator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Loads random time-series to the time-series master for demo/testing purposes.
 */
public class RandomTimeSeriesLoader implements HistoricalTimeSeriesLoader {

  /** The data source name for the random data. */
  private static final String RANDOM_DATA_SOURCE_NAME = "RANDOM";
  /** Unknown data provider. */
  private static final String UNKNOWN_PROVIDER = "UNKNOWN";
  /** Unknown observation time. */
  private static final String UNKNOWN_OBSERVATION_TIME = "UNKNOWN";
  /** The time series master to populate. */
  private final HistoricalTimeSeriesMaster _htsMaster;

  /**
   * Creates an instance.
   * 
   * @param htsMaster  the time-series master, not null
   */
  public RandomTimeSeriesLoader(final HistoricalTimeSeriesMaster htsMaster) {
    ArgumentChecker.notNull(htsMaster, "timeseries master");
    _htsMaster = htsMaster;
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalId, UniqueId> addTimeSeries(Set<ExternalId> identifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate) {
    ArgumentChecker.notEmpty(identifiers, "identifiers");
    ArgumentChecker.notNull(dataField, "dataField");
    
    if (endDate == null) {
      endDate = DateUtils.previousWeekDay();
    }
    if (startDate == null) {
      startDate = endDate.minusYears(1);
    }
    checkValidDateRange(startDate, endDate);
    
    Set<ExternalId> missingIdentifiers = Sets.newHashSet();
    for (ExternalId identifier : identifiers) {
      if (!haveTimeSeries(identifier, dataProvider, dataField)) { 
        missingIdentifiers.add(identifier);
      }
    }
    return addToMaster(missingIdentifiers, dataProvider, dataField, startDate, endDate);
  }

  private Map<ExternalId, UniqueId> addToMaster(
      Set<ExternalId> missingIdentifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate) {
    Map<ExternalId, UniqueId> result = new HashMap<ExternalId, UniqueId>();
    for (ExternalId identifier : missingIdentifiers) {
      // add the info
      ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
      info.setExternalIdBundle(ExternalIdBundleWithDates.of(ExternalIdWithDates.of(identifier, null, null)));
      info.setDataField(dataField);
      if (dataProvider == null) {
        info.setDataProvider(UNKNOWN_PROVIDER);
        info.setObservationTime(UNKNOWN_OBSERVATION_TIME);
      } else {
        info.setDataProvider(dataProvider);
        String derivedObservationTime = BloombergHistoricalLoader.resolveObservationTime(dataProvider);
        info.setObservationTime(derivedObservationTime);
      }
      info.setDataSource(RANDOM_DATA_SOURCE_NAME);
      HistoricalTimeSeriesInfoDocument addedDoc = _htsMaster.add(new HistoricalTimeSeriesInfoDocument(info));
      // add the series
      LocalDateDoubleTimeSeries timeSeries = generateRandomHistoricalTimeSeries(startDate, endDate);
      UniqueId uid = _htsMaster.updateTimeSeriesDataPoints(addedDoc.getInfo().getTimeSeriesObjectId(), timeSeries);
      result.put(identifier, uid);
    }
    return result;
  }

  private void checkValidDateRange(LocalDate startDate, LocalDate endDate) {
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException(endDate + " is before " + startDate);
    }
  }

  private LocalDateDoubleTimeSeries generateRandomHistoricalTimeSeries(LocalDate startDate, LocalDate endDate) {
    int daysBetween = DateUtils.getDaysBetween(startDate, endDate);
    return RandomTimeSeriesGenerator.makeRandomTimeSeries(startDate, daysBetween);
  }

  /**
   * Checks if the time-series exists.
   * 
   * @param identifier  the identifier
   * @param dataProvider  the data provider
   * @param dataField  the data field
   * @return true if there is a time-series for the identifier, data provider and data field
   */
  private boolean haveTimeSeries(final ExternalId identifier, final String dataProvider, final String dataField) {
    HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest(identifier);
    searchRequest.setDataField(dataField);
    if (dataProvider == null) {
      searchRequest.setDataProvider(UNKNOWN_PROVIDER);
    } else {
      searchRequest.setDataProvider(dataProvider);
    }
    searchRequest.setDataSource(RANDOM_DATA_SOURCE_NAME);
    HistoricalTimeSeriesInfoSearchResult searchTimeSeries = _htsMaster.search(searchRequest);
    List<HistoricalTimeSeriesInfoDocument> documents = searchTimeSeries.getDocuments();
    return !documents.isEmpty();
  }

  @Override
  public boolean updateTimeSeries(UniqueId uniqueIdentifier) {
    throw new UnsupportedOperationException();
  }

}
