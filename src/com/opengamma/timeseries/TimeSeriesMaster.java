/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.List;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.historicaldata.TimeSeriesSource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 *
 */
public interface TimeSeriesMaster extends TimeSeriesSource {
    
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
  TimeSeriesSearchResult search(TimeSeriesRequest request);

  /**
   * Gets a Timeseries by unique identifier.
   * 
   * @param uid  the unique identifier, not null
   * @return the timeseries document, not null
   * @throws IllegalArgumentException if the identifier is not from this security master
   * @throws DataNotFoundException if there is no timeseries with that unique identifier
   */
  TimeSeriesDocument get(UniqueIdentifier uid);
  
//  int createQuotedObject(String name, String description);
//  
//  int getQuotedObjectID(String name);
//  
//  String findQuotedObjectByID(int id);
//  
//  Set<String> getAllQuotedObjects();
//  
//  void createDomainSpecIdentifiers(IdentifierBundle identifiers, String quotedObj);
//  
//  void createTimeSeriesKey(String quotedObject, String dataSource, String dataProvider, String dataField, String observationTime);
//  
//  IdentifierBundle findDomainSpecIdentifiersByQuotedObject(String name);
  
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
  TimeSeriesDocument add(TimeSeriesDocument document);
  
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
  TimeSeriesDocument update(TimeSeriesDocument document);

  /**
   * Removes a timeseries from the data store.
   * 
   * @param uid  the timeseries unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no timeseries with that unique identifier
   */
  void remove(final UniqueIdentifier uid);

  //-------------------------------------------------------------------------
  /**
   * Searches for timeseries matching the specified search criteria.
   * <p>
   * The request must contain an object identifier that must not have a version.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  TimeSeriesSearchHistoricResult searchHistoric(TimeSeriesSearchHistoricRequest request);

//  UniqueIdentifier addTimeSeries(IdentifierBundle identifiers, String dataSource, String dataProvider, String field,  String observationTime, LocalDateDoubleTimeSeries timeSeries);
//  
//  void deleteTimeSeries(IdentifierBundle identifiers, String dataSource, String dataProvider, String field,  String observationTime);
//  
//  void updateDataPoint(IdentifierBundle identifiers, String dataSource, String dataProvider, String field,  String observationTime, LocalDate date, Double value);
//  
//  void deleteDataPoint(IdentifierBundle identifiers, String dataSource, String dataProvider, String field,  String observationTime, LocalDate date);
//  
//  LocalDateDoubleTimeSeries getTimeSeriesSnapShot(IdentifierBundle identifiers, String dataSource, String dataProvider, String field,  String observationTime, ZonedDateTime timeStamp);
//  
//  LocalDateDoubleTimeSeries getHistoricalTimeSeries(UniqueIdentifier uid);
//  
//  LocalDateDoubleTimeSeries getHistoricalTimeSeries(UniqueIdentifier uid, LocalDate start, LocalDate end);
//  
//  UniqueIdentifier resolveIdentifier(Identifier identifier, String dataSource, String dataProvider, String field);

}
