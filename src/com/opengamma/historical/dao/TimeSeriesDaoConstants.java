/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.historical.dao;


/**
 * TimeSeriesDAO Constants
 *
 * 
 */
public interface TimeSeriesDaoConstants {
  /**
   * value for invalid row id
   */
  int INVALID_KEY = -1;
  /**
   * loadTimeSeriesWithDates key in named sql map
   */
  String LOAD_TIME_SERIES_WITH_DATES = "loadTimeSeriesWithDates";
  String SELECT_QUOTED_OBJECT_FROM_IDENTIFIERS = "selectQuotedObjectFromIdentifiers";
  String LOAD_ALL_DATA_PROVIDER = "loadAllDataProvider";
  String SELECT_DATA_PROVIDER_ID = "selectDataProviderID";
  String LOAD_TIME_SERIES_DELTA = "loadTimeSeriesDelta";
  String DELETE_DATA_POINT = "deleteDataPoint";
  String INSERT_TIME_SERIES_DELTA_U = "insertTimeSeriesDeltaU";
  String UPDATE_TIME_SERIES = "updateTimeSeries";
  String FIND_DATA_POINT_BY_DATE_AND_ID = "findDataPointByDateAndID";
  String INSERT_TIME_SERIES_KEY = "insertTimeSeriesKey";
  String INSERT_TIME_SERIES = "insertTimeSeries";
  String GET_TIME_SERIES_KEY_ID_BY_IDENTIFIER = "getTimeSeriesKeyIDByIdentifier";
  String GET_TIME_SERIES_KEY_ID_BY_QUOTED_OBJECT = "getTimeSeriesKeyIDByQuotedObject";
  String GET_TIME_SERIES_BY_ID = "getTimeSeriesByID";
  String DELETE_TIME_SERIES_BY_ID = "deleteTimeSeriesByID";
  String FIND_DOMAIN_SPEC_IDENTIFIERS_BY_QUOTED_OBJECT = "findDomainSpecIdentifiersByQuotedObject";
  String FIND_DATA_PROVIDER_BY_ID = "findDataProviderByID";
  String FIND_DATA_SOURCE_BY_ID = "findDataSourceByID";
  String FIND_OBSERVATION_TIME_BY_ID = "findObservationTimeByID";
  String FIND_QUOTED_OBJECT_BY_ID = "findQuotedObjectByID";
  String FIND_DATA_FIELD_BY_ID = "findDataFieldByID";
  String FIND_DOMAIN_BY_ID = "findDomainByID";
  
  String INSERT_TIME_SERIES_DELTA_D = "insertTimeSeriesDeltaD";
  String INSERT_TIME_SERIES_DELTA_I = "insertTimeSeriesDeltaI";
  
  String INSERT_DATA_PROVIDER = "insertDataProvider";
  String INSERT_DATA_FIELD = "insertDataField";
  String INSERT_OBSERVATION_TIME = "insertObservationTime";
  String INSERT_QUOTED_OBJECT = "insertQuotedObject";
  String INSERT_DATA_SOURCE = "insertDataSource";
  String INSERT_DOMAIN = "insertDomain";
  
  String NAME_COLUMN = "name";
  String IDENTIFIER_COLUMN = "identifier";

  long MILLIS_IN_DAY = 86400000L;

  String SELECT_DATA_SOURCE_ID = "selectDataSourceID";
  String SELECT_DATA_FIELD_ID = "selectDataFieldID";
  String SELECT_OBSERVATION_TIME_ID = "selectObservationTimeID";
  String SELECT_QUOTED_OBJECT_ID = "selectQuotedObjectID";
  String LOAD_ALL_OBSERVATION_TIMES = "loadAllObservationTimes";
  String LOAD_ALL_QUOTED_OBJECTS = "loadAllQuotedObjects";
  String LOAD_ALL_DATA_FIELDS = "loadAllDataFields";
  String LOAD_ALL_DATA_SOURCES = "loadAllDataSources";
  String SELECT_DOMAIN_ID = "selectDomainID";
  String LOAD_ALL_DOMAIN = "loadAllDomain";
  
  /**
   * List of keys expected in the Map containing SQL queries injected to TimeSeriesDao
   */
  String[] SQL_MAP_KEYS = {
    LOAD_ALL_DATA_PROVIDER,
    LOAD_ALL_DATA_SOURCES,
    LOAD_ALL_DOMAIN,
    LOAD_ALL_OBSERVATION_TIMES,
    LOAD_ALL_QUOTED_OBJECTS,
    LOAD_TIME_SERIES_DELTA,
    LOAD_TIME_SERIES_WITH_DATES,
    SELECT_DATA_FIELD_ID,
    SELECT_DATA_PROVIDER_ID,
    SELECT_DATA_SOURCE_ID,
    SELECT_DOMAIN_ID,
    SELECT_OBSERVATION_TIME_ID,
    SELECT_QUOTED_OBJECT_FROM_IDENTIFIERS,
    SELECT_QUOTED_OBJECT_ID,
    INSERT_DATA_FIELD,
    INSERT_DATA_PROVIDER,
    INSERT_DATA_SOURCE,
    INSERT_DOMAIN,
    INSERT_OBSERVATION_TIME,
    INSERT_QUOTED_OBJECT,
    INSERT_TIME_SERIES,
    INSERT_TIME_SERIES_DELTA_D,
    INSERT_TIME_SERIES_DELTA_I,
    INSERT_TIME_SERIES_DELTA_U,
    INSERT_TIME_SERIES_KEY,
    FIND_DATA_FIELD_BY_ID,
    FIND_DATA_POINT_BY_DATE_AND_ID,
    FIND_DATA_PROVIDER_BY_ID,
    FIND_DATA_SOURCE_BY_ID,
    FIND_DOMAIN_BY_ID,
    FIND_DOMAIN_SPEC_IDENTIFIERS_BY_QUOTED_OBJECT,
    FIND_OBSERVATION_TIME_BY_ID,
    FIND_QUOTED_OBJECT_BY_ID,
    UPDATE_TIME_SERIES,
    GET_TIME_SERIES_BY_ID,
    GET_TIME_SERIES_KEY_ID_BY_IDENTIFIER,
    GET_TIME_SERIES_KEY_ID_BY_QUOTED_OBJECT,
  };
}
