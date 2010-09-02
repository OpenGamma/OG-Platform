/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.time.calendar.LocalDate;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import static com.opengamma.timeseries.TimeSeriesConstant.*;

/**
 * TimeSeriesMetaDataRowMapper maps returned SQL row to TimeSeriesMetaData object 
 */
/*package*/ class TimeSeriesMetaDataRowMapper implements ParameterizedRowMapper<MetaData> {
  
  private boolean _loadDates;
  
  /**
   * Sets the loadDates field.
   * @param loadDates  the loadDates
   */
  public void setLoadDates(boolean loadDates) {
    _loadDates = loadDates;
  }

  @Override
  public MetaData mapRow(ResultSet rs, int rowNum) throws SQLException {
    MetaData result = new MetaData();
    result.setTimeSeriesId(rs.getLong(TS_ID_COLUMN));
    result.setDataSource(rs.getString(DATA_SOURCE_COLUMN));
    result.setDataProvider(rs.getString(DATA_PROVIDER_COLUMN));
    result.setDataField(rs.getString(DATA_FIELD_COLUMN));
    result.setObservationTime(rs.getString(OBSERVATION_TIME_COLUMN));
    result.setIdentifierBundleId(rs.getLong(BUNDLE_ID_COLUMN));
    if (_loadDates) {
      Date earliestDate = rs.getDate(EARLIEST_COLUMN);
      Date latestDate = rs.getDate(LATEST_COLUMN);
      result.setEarliestDate(LocalDate.ofEpochDays(earliestDate.getTime() / MSEC_IN_DAY));
      result.setLatestDate(LocalDate.ofEpochDays(latestDate.getTime() / MSEC_IN_DAY));
    }
    return result;
  }

}
