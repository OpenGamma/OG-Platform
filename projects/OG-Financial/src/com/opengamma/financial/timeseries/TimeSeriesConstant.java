/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

/**
 * TimeSeries Constants
 */
public interface TimeSeriesConstant {

  //named sql map key names
  /**
   * key name  for deactivateMetaData SQL
   */
  String DEACTIVATE_META_DATA = "deactivateMetaData";
  /**
   * key name  for deleteDataPoint SQL
   */
  String DELETE_DATA_POINT = "deleteDataPoint";
  /**
   * key name  for deleteDataPointsByDate SQL
   */
  String DELETE_DATA_POINTS_BY_DATE = "deleteDataPointsByDate";
  /**
   * key name  for deleteTimeSeriesByID SQL
   */
  String DELETE_TIME_SERIES_BY_ID = "deleteTimeSeriesByID";
  /**
   * key name  for findDataPointByDateAndID SQL
   */
  String FIND_DATA_POINT_BY_DATE_AND_ID = "findDataPointByDateAndID";
  /**
   * key name  for getActiveMetaDataByOid SQL
   */
  String GET_ACTIVE_META_DATA_BY_OID = "getActiveMetaDataByOid";
  /**
   * key name  for getTimeSeriesDateRangeByOid SQL
   */
  String GET_TS_DATE_RANGE_BY_OID = "getTimeSeriesDateRangeByOid";
  /**
   * key name  for getActiveMetaDataByParameters SQL
   */
  String GET_ACTIVE_META_DATA_BY_PARAMETERS = "getActiveMetaDataByParameters";
  /**
   * key name  for getTimeSeriesByID SQL
   */
  String GET_TIME_SERIES_BY_ID = "getTimeSeriesByID";
  /**
   * key name  for getTimeSeriesKey SQL
   */
  String GET_TIME_SERIES_KEY = "getTimeSeriesKey";
  /**
   * key name  for getTimeSeriesKeyByID SQL
   */
  String GET_TIME_SERIES_KEY_BY_ID = "getTimeSeriesKeyByID";
  /**
   * key name  for insertDataField SQL
   */
  String INSERT_DATA_FIELD = "insertDataField";
  /**
   * key name  for insertDataProvider SQL
   */
  String INSERT_DATA_PROVIDER = "insertDataProvider";
  /**
   * key name  for insertDataSource SQL
   */
  String INSERT_DATA_SOURCE = "insertDataSource";
  /**
   * key name  for insertScheme SQL
   */
  String INSERT_SCHEME = "insertScheme";
  /**
   * key name  for insertIdentifier SQL
   */
  String INSERT_IDENTIFIER = "insertIdentifier";
  /**
   * key name  for insertObservationTime SQL
   */
  String INSERT_OBSERVATION_TIME = "insertObservationTime";
  /**
   * key name  for insertQuotedObject SQL
   */
  String INSERT_QUOTED_OBJECT = "insertQuotedObject";
  /**
   * key name  for insertTimeSeries SQL
   */
  String INSERT_TIME_SERIES = "insertTimeSeries";
  /**
   * key name  for insertTimeSeriesDeltaD SQL
   */
  String INSERT_TIME_SERIES_DELTA_D = "insertTimeSeriesDeltaD";
  /**
   * key name  for insertTimeSeriesDeltaI SQL
   */
  String INSERT_TIME_SERIES_DELTA_I = "insertTimeSeriesDeltaI";
  /**
   * key name  for insertTimeSeriesDeltaU SQL
   */
  String INSERT_TIME_SERIES_DELTA_U = "insertTimeSeriesDeltaU";
  /**
   * key name  for insertTimeSeriesKey SQL
   */
  String INSERT_TIME_SERIES_KEY = "insertTimeSeriesKey";
  /**
   * key name  for loadAllDataFields SQL
   */
  String LOAD_ALL_DATA_FIELDS = "loadAllDataFields";
  /**
   * key name  for loadAllDataProvider SQL
   */
  String LOAD_ALL_DATA_PROVIDER = "loadAllDataProvider";
  /**
   * key name  for loadAllDataSources SQL
   */
  String LOAD_ALL_DATA_SOURCES = "loadAllDataSources";
  /**
   * key name  for loadAllIdentifiers SQL
   */
  String LOAD_ALL_IDENTIFIERS = "loadAllIdentifiers";
  /**
   * key name  for loadAllObservationTimes SQL
   */
  String LOAD_ALL_OBSERVATION_TIMES = "loadAllObservationTimes";
  /**
   * key name  for loadAllScheme SQL
   */
  String LOAD_ALL_SCHEME = "loadAllScheme";
  /**
   * key name  for loadTimeSeriesDelta SQL
   */
  String LOAD_TIME_SERIES_DELTA = "loadTimeSeriesDelta";
  /**
   * key name  for loadTimeSeriesWithDates SQL
   */
  String LOAD_TIME_SERIES_WITH_DATES = "loadTimeSeriesWithDates";
  /**
   * key name  for selectDataFieldID SQL
   */
  String SELECT_DATA_FIELD_ID = "selectDataFieldID";
  /**
   * key name  for selectDataProviderID SQL
   */
  String SELECT_DATA_PROVIDER_ID = "selectDataProviderID";
  /**
   * key name  for selectDataSourceID SQL
   */
  String SELECT_DATA_SOURCE_ID = "selectDataSourceID";
  /**
   * key name  for selectSchemeID SQL
   */
  String SELECT_SCHEME_ID = "selectSchemeID";
  /**
   * key name  for selectObservationTimeID SQL
   */
  String SELECT_OBSERVATION_TIME_ID = "selectObservationTimeID";
  /**
   * key name  for selectBundleFromIdentifiers SQL
   */
  String SELECT_BUNDLE_FROM_IDENTIFIERS = "selectBundleFromIdentifiers";
  /**
   * key name  for selectQuotedObjectID SQL
   */
  String SELECT_QUOTED_OBJECT_ID = "selectQuotedObjectID";
  /**
   * key name  for updateTimeSeries SQL
   */
  String UPDATE_TIME_SERIES = "updateTimeSeries";
  /**
   * key name  for getActiveMetaDataWithDates SQL
   */
  String GET_ACTIVE_META_DATA_WITH_DATES = "getActiveMetaDataWithDates";
  /**
   * key name  for getActiveMetaDataWithDatesByIdentifiers SQL
   */
  String GET_ACTIVE_META_DATA_WITH_DATES_BY_IDENTIFIERS = "getActiveMetaDataWithDatesByIdentifiers";
  /**
   * key name  for getActiveMetaData SQL
   */
  String GET_ACTIVE_META_DATA = "getActiveMetaData";
  /**
   * key name  for getActiveMetaDataByIdentifiers SQL
   */
  String GET_ACTIVE_META_DATA_BY_IDENTIFIERS = "getActiveMetaDataByIdentifiers";
  /**
   * key name  for selectAllIdentifierBundle SQL
   */
  String SELECT_ALL_BUNDLE = "selectAllIdentifierBundle";
  
  //column names
  /**
   * TimeSeries_ID COLUMN NAME
   */
  String TS_ID_COLUMN = "tsKey";
  /**
   * dataSource COLUMN NAME
   */
  String DATA_SOURCE_COLUMN = "dataSource";
  /**
   * dataProvider COLUMN NAME
   */
  String DATA_PROVIDER_COLUMN = "dataProvider";
  /**
   * dataField COLUMN NAME
   */
  String DATA_FIELD_COLUMN = "dataField";
  /**
   * observationTime COLUMN NAME
   */
  String OBSERVATION_TIME_COLUMN = "observationTime";
  /**
   * bundleId COLUMN NAME
   */
  String BUNDLE_ID_COLUMN = "bundleId";
  /**
   * earliest date COLUMN NAME
   */
  String EARLIEST_COLUMN = "earliest";
  /**
   * latest date COLUMN NAME
   */
  String LATEST_COLUMN = "latest";
  
  /**
   * value for invalid row id
   */
  long INVALID_KEY = Long.MIN_VALUE;
  /**
   * Millisec in day
   */
  long MILLIS_PER_DAY = 86400000L;
}
