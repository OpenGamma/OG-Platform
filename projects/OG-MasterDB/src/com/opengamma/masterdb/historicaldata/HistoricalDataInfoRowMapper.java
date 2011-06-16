/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaldata;

import static com.opengamma.masterdb.historicaldata.DbHistoricalDataMasterConstants.BUNDLE_ID_COLUMN;
import static com.opengamma.masterdb.historicaldata.DbHistoricalDataMasterConstants.DATA_FIELD_COLUMN;
import static com.opengamma.masterdb.historicaldata.DbHistoricalDataMasterConstants.DATA_PROVIDER_COLUMN;
import static com.opengamma.masterdb.historicaldata.DbHistoricalDataMasterConstants.DATA_SOURCE_COLUMN;
import static com.opengamma.masterdb.historicaldata.DbHistoricalDataMasterConstants.EARLIEST_COLUMN;
import static com.opengamma.masterdb.historicaldata.DbHistoricalDataMasterConstants.LATEST_COLUMN;
import static com.opengamma.masterdb.historicaldata.DbHistoricalDataMasterConstants.OBSERVATION_TIME_COLUMN;
import static com.opengamma.masterdb.historicaldata.DbHistoricalDataMasterConstants.TS_ID_COLUMN;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.time.calendar.LocalDate;

import org.springframework.jdbc.core.RowMapper;

import com.opengamma.util.ArgumentChecker;

/**
 * Maps returned SQL rows to info objects.
 */
/*package*/ class HistoricalDataInfoRowMapper implements RowMapper<Info> {

  /**
   * The master.
   */
  private final DbHistoricalDataMaster _master;
  /**
   * Whether to load the dates.
   */
  private boolean _loadEarliestLatest;

  /**
   * Creates an instance.
   * 
   * @param master  the master, not null
   */
  public HistoricalDataInfoRowMapper(DbHistoricalDataMaster master) {
    ArgumentChecker.notNull(master, "master");
    _master = master;    
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the loadDates field.
   * 
   * @param loadEarliestLatest  whether to load the dates
   */
  public void setLoadEarliestLatest(boolean loadEarliestLatest) {
    _loadEarliestLatest = loadEarliestLatest;
  }

  //-------------------------------------------------------------------------
  @Override
  public Info mapRow(ResultSet rs, int rowNum) throws SQLException {
    Info result = new Info();
    result.setHistoricalDataId(rs.getLong(TS_ID_COLUMN));
    result.setDataSource(rs.getString(DATA_SOURCE_COLUMN));
    result.setDataProvider(rs.getString(DATA_PROVIDER_COLUMN));
    result.setDataField(rs.getString(DATA_FIELD_COLUMN));
    result.setObservationTime(rs.getString(OBSERVATION_TIME_COLUMN));
    result.setIdentifierBundleId(rs.getLong(BUNDLE_ID_COLUMN));
    if (_loadEarliestLatest) {
      LocalDate earliestDate = _master.getDate(rs, EARLIEST_COLUMN);
      LocalDate latestDate = _master.getDate(rs, LATEST_COLUMN);
      result.setEarliestDate(earliestDate);
      result.setLatestDate(latestDate);
    }
    return result;
  }

}
