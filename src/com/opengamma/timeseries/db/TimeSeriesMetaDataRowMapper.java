/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import static com.opengamma.timeseries.TimeSeriesConstant.BUNDLE_ID_COLUMN;
import static com.opengamma.timeseries.TimeSeriesConstant.DATA_FIELD_COLUMN;
import static com.opengamma.timeseries.TimeSeriesConstant.DATA_PROVIDER_COLUMN;
import static com.opengamma.timeseries.TimeSeriesConstant.DATA_SOURCE_COLUMN;
import static com.opengamma.timeseries.TimeSeriesConstant.EARLIEST_COLUMN;
import static com.opengamma.timeseries.TimeSeriesConstant.LATEST_COLUMN;
import static com.opengamma.timeseries.TimeSeriesConstant.OBSERVATION_TIME_COLUMN;
import static com.opengamma.timeseries.TimeSeriesConstant.TS_ID_COLUMN;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import com.opengamma.util.ArgumentChecker;

/**
 * TimeSeriesMetaDataRowMapper maps returned SQL row to TimeSeriesMetaData object 
 */
/*package*/ class TimeSeriesMetaDataRowMapper<T> implements ParameterizedRowMapper<MetaData<T>> {
  
  private final RowStoreTimeSeriesMaster<T> _rowStoreMaster;
  private boolean _loadDates;
  
  public TimeSeriesMetaDataRowMapper(RowStoreTimeSeriesMaster<T> rowStoreMaster) {
    ArgumentChecker.notNull(rowStoreMaster, "rowStoreMaster");
    _rowStoreMaster = rowStoreMaster;    
  }
  
  /**
   * Sets the loadDates field.
   * @param loadDates  the loadDates
   */
  public void setLoadDates(boolean loadDates) {
    _loadDates = loadDates;
  }

  @Override
  public MetaData<T> mapRow(ResultSet rs, int rowNum) throws SQLException {
    MetaData<T> result = new MetaData<T>();
    result.setTimeSeriesId(rs.getLong(TS_ID_COLUMN));
    result.setDataSource(rs.getString(DATA_SOURCE_COLUMN));
    result.setDataProvider(rs.getString(DATA_PROVIDER_COLUMN));
    result.setDataField(rs.getString(DATA_FIELD_COLUMN));
    result.setObservationTime(rs.getString(OBSERVATION_TIME_COLUMN));
    result.setIdentifierBundleId(rs.getLong(BUNDLE_ID_COLUMN));
    if (_loadDates) {
      T earliestDate = _rowStoreMaster.getDate(rs, EARLIEST_COLUMN);
      T latestDate = _rowStoreMaster.getDate(rs, LATEST_COLUMN);
      result.setEarliestDate(earliestDate);
      result.setLatestDate(latestDate);
    }
    return result;
  }

}
