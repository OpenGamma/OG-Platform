/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * TimeSeriesMetaDataRowMapper maps returned SQL row to TimeSeriesMetaData object 
 */
/*package*/ class TimeSeriesMetaDataRowMapper implements ParameterizedRowMapper<TimeSeriesMetaData> {
  
  @Override
  public TimeSeriesMetaData mapRow(ResultSet rs, int rowNum) throws SQLException {
    TimeSeriesMetaData result = new TimeSeriesMetaData();
    result.setTimeSeriesId(rs.getLong("tsKey"));
    result.setDataSource(rs.getString("dataSource"));
    result.setDataProvider(rs.getString("dataProvider"));
    result.setDataField(rs.getString("dataField"));
    result.setObservationTime(rs.getString("observationTime"));
    return result;
  }

}
