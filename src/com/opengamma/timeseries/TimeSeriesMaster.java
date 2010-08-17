/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.List;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A general-purpose Timeseries master.
 * <p>
 * The timeseries master provides a uniform view over the timeseries database
 * This interface provides methods that allow the master to be searched and updated.
 */
public interface TimeSeriesMaster {
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
  
  List<Identifier> getAllIdentifiers();
  
  /**
   * Searches for timeseries matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  TimeSeriesSearchResult searchTimeSeries(TimeSeriesRequest request);

  /**
   * Gets a Timeseries by unique identifier.
   * 
   * @param uid  the unique identifier, not null
   * @return the timeseries document, not null
   * @throws IllegalArgumentException if the identifier is not from this timeseries master
   * @throws DataNotFoundException if there is no timeseries with that unique identifier
   */
  TimeSeriesDocument getTimeSeries(UniqueIdentifier uid);
    
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
  TimeSeriesDocument addTimeSeries(TimeSeriesDocument document);
  
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
  TimeSeriesDocument updateTimeSeries(TimeSeriesDocument document);

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
  TimeSeriesSearchHistoricResult searchHistoric(TimeSeriesSearchHistoricRequest request);
  
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
  DataPointDocument addDataPoint(DataPointDocument document);
  
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
  DataPointDocument getDataPoint(UniqueIdentifier uid);
  
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
  DataPointDocument updateDataPoint(DataPointDocument document);
  
  /**
   * Append datapoints to an existing timeseries
   * <p>
   * Document must contain the Unique identifier for the timeseries
   * 
   * @param document the document not null
   * @throws IllegalArgumentException if the document has no Unique identifier for timeseries
   * @throws DataNotFoundException if there is no timeseries with that unique identifier
   */
  void appendTimeSeries(TimeSeriesDocument document);
  
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

}
