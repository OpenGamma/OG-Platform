/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.historical.dao;

import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.engine.historicaldata.HistoricalDataProvider;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 *
 */
public interface TimeSeriesDao extends HistoricalDataProvider {
  
  int createDomain(String domain, String description);
  
  String findDomainByID(int id);
  
  int getDomainID(String name);
  
  Set<String> getAllDomains();
  
  int createDataSource(String dataSource, String description);
  
  String findDataSourceByID(int id);
  
  int getDataSourceID(String name);
  
  Set<String> getAllDataSources();
  
  int createDataProvider(String dataProvider, String description);
  
  String findDataProviderByID(int id);
  
  int getDataProviderID(String name);
  
  Set<String> getAllDataProviders();
  
  int createDataField(String field, String description);
  
  String findDataFieldByID(int id);
  
  int getDataFieldID(String name);
  
  Set<String> getAllTimeSeriesFields();
  
  int createObservationTime(String observationTime, String description);
  
  int getObservationTimeID(String name);
  
  String findObservationTimeByID(int id);
  
  Set<String> getAllObservationTimes();
  
  int createQuotedObject(String name, String description);
  
  int getQuotedObjectID(String name);
  
  String findQuotedObjectByID(int id);
  
  Set<String> getAllQuotedObjects();
  
  void createDomainSpecIdentifiers(IdentifierBundle identifiers, String quotedObj);
  
  void createTimeSeriesKey(String quotedObject, String dataSource, String dataProvider, String dataField, String observationTime);
  
  IdentifierBundle findDomainSpecIdentifiersByQuotedObject(String name);

  UniqueIdentifier addTimeSeries(IdentifierBundle identifiers, String dataSource, String dataProvider, String field,  String observationTime, LocalDateDoubleTimeSeries timeSeries);
  
  void deleteTimeSeries(IdentifierBundle identifiers, String dataSource, String dataProvider, String field,  String observationTime);
  
  void updateDataPoint(IdentifierBundle identifiers, String dataSource, String dataProvider, String field,  String observationTime, LocalDate date, Double value);
  
  void deleteDataPoint(IdentifierBundle identifiers, String dataSource, String dataProvider, String field,  String observationTime, LocalDate date);
  
  LocalDateDoubleTimeSeries getTimeSeriesSnapShot(IdentifierBundle identifiers, String dataSource, String dataProvider, String field,  String observationTime, ZonedDateTime timeStamp);
  
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(UniqueIdentifier uid);
  
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(UniqueIdentifier uid, LocalDate start, LocalDate end);
  
  UniqueIdentifier resolveIdentifier(Identifier identifier, String dataSource, String dataProvider, String field);
  

}
