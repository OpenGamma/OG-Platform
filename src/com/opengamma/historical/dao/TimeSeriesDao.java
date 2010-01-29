/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.historical.dao;

import java.util.List;

import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 *
 * @author yomi
 */
public interface TimeSeriesDao {
  
  public void createDataSource(String dataSource);
  
  public int getDataSourceID(String dataSource);
  
  public List<String> getAllDataSources();
  
  public void createDataProvider(String dataProvider);
  
  public int getDataProviderID(String dataProvider);
  
  public List<String> getAllDataProviders();
  
  public void createTimeSeriesField(String field);
  
  public int getFieldID(String field);
  
  public List<String> getAllTimeSeriesFields();
  
  public void createObservationTime(String observationTime);
  
  public List<String> getAllObservationTimes();

  public void addDataPoints(String secDes, String dataSource, String dataProvider, String field,  String observationTime, DoubleTimeSeries timeSeries);
  
  //generic get timeseries call that allow nulls for unwanted fields
  public List<DoubleTimeSeries> getTimeSeries(String secDes, String dataSource, String dataProvider, String field,  String observationTime);
  
  public List<DoubleTimeSeries> getTimeSeriesByDataSource(String secDes, String dataSource);
  
  public List<DoubleTimeSeries> getTimeSeriesByDataProvider(String secDes, String dataProvider);
  
  public List<DoubleTimeSeries> getTimeSeriesByField(String secDes, String field);
  
  public List<DoubleTimeSeries> getTimeSeriesByObservationTime(String secDes, String observationTime);
  
}
