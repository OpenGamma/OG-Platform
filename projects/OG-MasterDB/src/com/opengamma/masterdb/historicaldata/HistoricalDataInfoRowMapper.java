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
 * Maps returned SQL rows to meta-data objects.
 */
/*package*/ class HistoricalDataInfoRowMapper implements RowMapper<MetaData> {

  /**
   * The master.
   */
  private final DbHistoricalDataMaster _rowStoreMaster;
  /**
   * Whether to load the dates.
   */
  private boolean _loadDates;

  /**
   * Creates an instance.
   * 
   * @param rowStoreMaster  the master, not null
   */
  public HistoricalDataInfoRowMapper(DbHistoricalDataMaster rowStoreMaster) {
    ArgumentChecker.notNull(rowStoreMaster, "rowStoreMaster");
    _rowStoreMaster = rowStoreMaster;    
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the loadDates field.
   * 
   * @param loadDates  whether to load the dates
   */
  public void setLoadDates(boolean loadDates) {
    _loadDates = loadDates;
  }

  //-------------------------------------------------------------------------
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
      LocalDate earliestDate = _rowStoreMaster.getDate(rs, EARLIEST_COLUMN);
      LocalDate latestDate = _rowStoreMaster.getDate(rs, LATEST_COLUMN);
      result.setEarliestDate(earliestDate);
      result.setLatestDate(latestDate);
    }
    return result;
  }

}
