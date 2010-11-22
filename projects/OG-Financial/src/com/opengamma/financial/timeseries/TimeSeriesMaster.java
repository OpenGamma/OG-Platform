/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

import java.util.List;

import javax.time.calendar.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.UniqueIdentifier;

/**
 * A general-purpose Timeseries master.
 * <p>
 * The timeseries master provides a uniform view over the timeseries database
 * This interface provides methods that allow the master to be searched and updated.
 * 
 * @param <T> Type of time series (LocalDate/Date) to operate on
 */
public interface TimeSeriesMaster<T> {
  /**
   * Value for unknown data provider in the database
   */
  String UNKNOWN_PROVIDER = "UNKNOWN";
  /**
   * Value for Unknown observation time in the database
   */
  String UNKNOWN_OBSERVATION_TIME = "UNKNOWN";
  /**
   * Create or get a data source with description  
   * @param dataSource the datsource name, not-null
   * @param description the description
   * @return the datasource bean, not-null
   */
  DataSourceBean getOrCreateDataSource(String dataSource, String description);
  
  List<DataSourceBean> getDataSources();
  
  DataProviderBean getOrCreateDataProvider(String dataProvider, String description);
  
  List<DataProviderBean> getDataProviders();
  
  DataFieldBean getOrCreateDataField(String field, String description);
  
  List<DataFieldBean> getDataFields();
  
  ObservationTimeBean getOrCreateObservationTime(String observationTime, String description);
    
  List<ObservationTimeBean> getObservationTimes();
  
  SchemeBean getOrCreateScheme(String scheme, String descrption);
  
  List<SchemeBean> getSchemes();
  
  List<IdentifierBundleWithDates> getAllIdentifiers();
  
  /**
   * Searches for timeseries matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  TimeSeriesSearchResult<T> searchTimeSeries(TimeSeriesSearchRequest<T> request);

  /**
   * Gets a Timeseries by unique identifier.
   * 
   * @param uid  the unique identifier, not null
   * @return the timeseries document, not null
   * @throws IllegalArgumentException if the identifier is not from this timeseries master
   * @throws DataNotFoundException if there is no timeseries with that unique identifier
   */
  TimeSeriesDocument<T> getTimeSeries(UniqueIdentifier uid);
    
  /**
   * Adds a TimeSeries to the data store.
   * <p>
   * The specified document must contain the timeseries.
   * It must not contain the unique identifier.
   * 
   * @param document  the document, not null
   * @return the updated timeseries document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  TimeSeriesDocument<T> addTimeSeries(TimeSeriesDocument<T> document);
  
  /**
   * Updates a timeseries in the data store.
   * <p>
   * The specified document must contain the timeseries and the unique identifier.
   * 
   * @param document  the document, not null
   * @return the updated timeseries document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no timeseries with that unique identifier
   */
  TimeSeriesDocument<T> updateTimeSeries(TimeSeriesDocument<T> document);

  /**
   * Removes a timeseries from the data store.
   * 
   * @param uid  the timeseries unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no timeseries with that unique identifier
   */
  void removeTimeSeries(final UniqueIdentifier uid);

  /**
   * Searches for timeseries matching the specified search criteria.
   * <p>
   * The request must contain an object identifier that must not have a version.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  TimeSeriesSearchHistoricResult<T> searchHistoric(TimeSeriesSearchHistoricRequest request);
  
  /**
   * Adds a data point to existing timeseries
   * <p>
   * The specified document must contain the timeseries Uid
   * It must not contain the unique identifier.
   * 
   * @param document  the document, not null
   * @return the updated timeseriesDataPoint document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  DataPointDocument<T> addDataPoint(DataPointDocument<T> document);
  
  /**
   * Gets a DataPoint by unique identifier.
   * <p> 
   * The dataPoint UID is <TimeSeriesUID-YYYYMMDD>
   * 
   * @param uid  the unique identifier, not null
   * @return the datapoint document, not null
   * @throws IllegalArgumentException if the identifier is not from this security master
   * @throws DataNotFoundException if there is no data point with that unique identifier
   */
  DataPointDocument<T> getDataPoint(UniqueIdentifier uid);
  
  /**
   * Removes a timeseries datapoint.
   * <p> 
   * The dataPoint UID is <TimeSeriesUID-YYYYMMDD>
   * 
   * @param uid  the timeseries datapoint unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no timeseries datapoint with that unique identifier
   */
  void removeDataPoint(final UniqueIdentifier uid);
  
  /**
   * Updates a datapoint.
   * <p>
   * The specified document must contain the value and the unique identifier.
   * 
   * @param document  the document, not null
   * @return the updated datapoint document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no datapoint with that unique identifier
   */
  DataPointDocument<T> updateDataPoint(DataPointDocument<T> document);
  
  /**
   * Append datapoints to an existing timeseries
   * <p>
   * Document must contain the Unique identifier for the timeseries
   * 
   * @param document the document not null
   * @throws IllegalArgumentException if the document has no Unique identifier for timeseries
   * @throws DataNotFoundException if there is no timeseries with that unique identifier
   */
  void appendTimeSeries(TimeSeriesDocument<T> document);
  
  /**
   * Finds the Timeseries UID
   * 
   * @param identifiers the identifier bundle, not-null
   * @param dataSource the datasource, not-null
   * @param dataProvider the dataprovider, not-null
   * @param dataField the dataField, not-null
   * @return the UID if found or null
   */
  UniqueIdentifier resolveIdentifier(IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField);
  
  /**
   * Finds the Timeseries UID
   * 
   * @param identifiers the identifier bundle, not-null
   * @param currentDate the current trade date if applicable
   * @param dataSource the datasource, not-null
   * @param dataProvider the dataprovider, not-null
   * @param dataField the dataField, not-null
   * @return the UID if found or null
   */
  UniqueIdentifier resolveIdentifier(IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField);
  
  /**
   * Removes all data points before the given date.
   * 
   * @param timeSeriesUid Time series to operate on
   * @param firstDateToRetain Remove all data points before this date
   */
  void removeDataPoints(UniqueIdentifier timeSeriesUid, T firstDateToRetain);
  
}
