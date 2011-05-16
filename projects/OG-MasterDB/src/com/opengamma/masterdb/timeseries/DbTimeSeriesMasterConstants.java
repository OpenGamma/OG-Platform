/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.timeseries;

/**
 * Constants used for time-series meta-data.
 */
public final class DbTimeSeriesMasterConstants {

  /**
   * Restricted constructor.
   */
  private DbTimeSeriesMasterConstants() {
  }

  //named sql map key names
  /**
   * key name  for deactivateMetaData SQL
   */
  public static final String DEACTIVATE_META_DATA = "deactivateMetaData";
  /**
   * key name  for deleteDataPoint SQL
   */
  public static final String DELETE_DATA_POINT = "deleteDataPoint";
  /**
   * key name  for deleteDataPointsByDate SQL
   */
  public static final String DELETE_DATA_POINTS_BY_DATE = "deleteDataPointsByDate";
  /**
   * key name  for deleteTimeSeriesByID SQL
   */
  public static final String DELETE_TIME_SERIES_BY_ID = "deleteTimeSeriesByID";
  /**
   * key name  for findDataPointByDateAndID SQL
   */
  public static final String FIND_DATA_POINT_BY_DATE_AND_ID = "findDataPointByDateAndID";
  /**
   * key name  for getActiveMetaDataByOid SQL
   */
  public static final String GET_ACTIVE_META_DATA_BY_OID = "getActiveMetaDataByOid";
  /**
   * key name  for getTimeSeriesDateRangeByOid SQL
   */
  public static final String GET_TS_DATE_RANGE_BY_OID = "getTimeSeriesDateRangeByOid";
  /**
   * key name  for getActiveMetaDataByParameters SQL
   */
  public static final String GET_ACTIVE_META_DATA_BY_PARAMETERS = "getActiveMetaDataByParameters";
  /**
   * key name  for getTimeSeriesByID SQL
   */
  public static final String GET_TIME_SERIES_BY_ID = "getTimeSeriesByID";
  /**
   * key name  for getTimeSeriesKeyByID SQL
   */
  public static final String GET_ACTIVE_TIME_SERIES_KEY_BY_ID = "getActiveTimeSeriesKeyByID";
  /**
   * Key name for getActiveTimeSeriesKey SQL
   */
  public static final String GET_ACTIVE_TIME_SERIES_KEY = "getActiveTimeSeriesKey";
  /**
   * key name  for insertDataField SQL
   */
  public static final String INSERT_DATA_FIELD = "insertDataField";
  /**
   * key name  for insertDataProvider SQL
   */
  public static final String INSERT_DATA_PROVIDER = "insertDataProvider";
  /**
   * key name  for insertDataSource SQL
   */
  public static final String INSERT_DATA_SOURCE = "insertDataSource";
  /**
   * key name  for insertScheme SQL
   */
  public static final String INSERT_SCHEME = "insertScheme";
  /**
   * key name  for insertIdentifier SQL
   */
  public static final String INSERT_IDENTIFIER = "insertIdentifier";
  /**
   * key name  for insertObservationTime SQL
   */
  public static final String INSERT_OBSERVATION_TIME = "insertObservationTime";
  /**
   * key name  for insertQuotedObject SQL
   */
  public static final String INSERT_QUOTED_OBJECT = "insertQuotedObject";
  /**
   * key name  for insertTimeSeries SQL
   */
  public static final String INSERT_TIME_SERIES = "insertTimeSeries";
  /**
   * key name  for insertTimeSeriesDeltaD SQL
   */
  public static final String INSERT_TIME_SERIES_DELTA_D = "insertTimeSeriesDeltaD";
  /**
   * key name  for insertTimeSeriesDeltaI SQL
   */
  public static final String INSERT_TIME_SERIES_DELTA_I = "insertTimeSeriesDeltaI";
  /**
   * key name  for insertTimeSeriesDeltaU SQL
   */
  public static final String INSERT_TIME_SERIES_DELTA_U = "insertTimeSeriesDeltaU";
  /**
   * key name  for insertTimeSeriesKey SQL
   */
  public static final String INSERT_TIME_SERIES_KEY = "insertTimeSeriesKey";
  /**
   * key name  for loadAllDataFields SQL
   */
  public static final String LOAD_ALL_DATA_FIELDS = "loadAllDataFields";
  /**
   * key name  for loadAllDataProvider SQL
   */
  public static final String LOAD_ALL_DATA_PROVIDER = "loadAllDataProvider";
  /**
   * key name  for loadAllDataSources SQL
   */
  public static final String LOAD_ALL_DATA_SOURCES = "loadAllDataSources";
  /**
   * key name  for loadAllIdentifiers SQL
   */
  public static final String LOAD_ALL_IDENTIFIERS = "loadAllIdentifiers";
  /**
   * key name  for loadAllObservationTimes SQL
   */
  public static final String LOAD_ALL_OBSERVATION_TIMES = "loadAllObservationTimes";
  /**
   * key name  for loadAllScheme SQL
   */
  public static final String LOAD_ALL_SCHEME = "loadAllScheme";
  /**
   * key name  for loadTimeSeriesDelta SQL
   */
  public static final String LOAD_TIME_SERIES_DELTA = "loadTimeSeriesDelta";
  /**
   * key name  for loadTimeSeriesWithDates SQL
   */
  public static final String LOAD_TIME_SERIES_WITH_DATES = "loadTimeSeriesWithDates";
  /**
   * key name  for selectDataFieldID SQL
   */
  public static final String SELECT_DATA_FIELD_ID = "selectDataFieldID";
  /**
   * key name  for selectDataProviderID SQL
   */
  public static final String SELECT_DATA_PROVIDER_ID = "selectDataProviderID";
  /**
   * key name  for selectDataSourceID SQL
   */
  public static final String SELECT_DATA_SOURCE_ID = "selectDataSourceID";
  /**
   * key name  for selectSchemeID SQL
   */
  public static final String SELECT_SCHEME_ID = "selectSchemeID";
  /**
   * key name  for selectObservationTimeID SQL
   */
  public static final String SELECT_OBSERVATION_TIME_ID = "selectObservationTimeID";
  /**
   * key name  for selectBundleFromIdentifiers SQL
   */
  public static final String SELECT_BUNDLE_FROM_IDENTIFIERS = "selectBundleFromIdentifiers";
  /**
   * key name  for selectQuotedObjectID SQL
   */
  public static final String SELECT_QUOTED_OBJECT_ID = "selectQuotedObjectID";
  /**
   * key name  for updateTimeSeries SQL
   */
  public static final String UPDATE_TIME_SERIES = "updateTimeSeries";
  /**
   * key name  for getActiveMetaDataWithDates SQL
   */
  public static final String GET_ACTIVE_META_DATA_WITH_DATES = "getActiveMetaDataWithDates";
  /**
   * key name  for getActiveMetaDataWithDatesByIdentifiers SQL
   */
  public static final String GET_ACTIVE_META_DATA_WITH_DATES_BY_IDENTIFIERS = "getActiveMetaDataWithDatesByIdentifiers";
  /**
   * key name  for getActiveMetaData SQL
   */
  public static final String GET_ACTIVE_META_DATA = "getActiveMetaData";
  /**
   * key name  for getActiveMetaDataByIdentifiers SQL
   */
  public static final String GET_ACTIVE_META_DATA_BY_IDENTIFIERS = "getActiveMetaDataByIdentifiers";

  
  //column names
  /**
   * TimeSeries_ID COLUMN NAME
   */
  public static final String TS_ID_COLUMN = "tsKey";
  /**
   * dataSource COLUMN NAME
   */
  public static final String DATA_SOURCE_COLUMN = "dataSource";
  /**
   * dataProvider COLUMN NAME
   */
  public static final String DATA_PROVIDER_COLUMN = "dataProvider";
  /**
   * dataField COLUMN NAME
   */
  public static final String DATA_FIELD_COLUMN = "dataField";
  /**
   * observationTime COLUMN NAME
   */
  public static final String OBSERVATION_TIME_COLUMN = "observationTime";
  /**
   * bundleId COLUMN NAME
   */
  public static final String BUNDLE_ID_COLUMN = "bundleId";
  /**
   * earliest date COLUMN NAME
   */
  public static final String EARLIEST_COLUMN = "earliest";
  /**
   * latest date COLUMN NAME
   */
  public static final String LATEST_COLUMN = "latest";
  /**
   * Identifier value COLUMN NAME
   */
  public static final String IDENTIFIER_VALUE_COLUMN = "identifier_value";
  /**
   * Valid_from COLUMN NAME
   */
  public static final String VALID_FROM = "valid_from";
  /**
   * Valid_to COLUMN NAME
   */
  public static final String VALID_TO = "valid_to";
  
  /**
   * Identifier scheme COLUMN NAME
   */
  public static final String SCHEME = "scheme";
  
  /**
   * value for invalid row id
   */
  public static final long INVALID_KEY = Long.MIN_VALUE;

}
