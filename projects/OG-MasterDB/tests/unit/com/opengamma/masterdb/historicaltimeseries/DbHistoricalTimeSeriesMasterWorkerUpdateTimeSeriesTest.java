/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterWorkerUpdateTimeSeriesTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerUpdateTimeSeriesTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerUpdateTimeSeriesTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_nullOID() {
    _htsMaster.updateTimeSeriesDataPoints((ObjectId) null, new ArrayLocalDateDoubleTimeSeries());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_nullSeries() {
    _htsMaster.updateTimeSeriesDataPoints(ObjectId.of("DbHts", "DP101"), null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_versioned_notFoundId() {
    ObjectId oid = ObjectId.of("DbHts", "DP0");
    _htsMaster.updateTimeSeriesDataPoints(oid, new ArrayLocalDateDoubleTimeSeries());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_beforeAllExistingPoints() {
    LocalDate[] dates = {LocalDate.of(2010, 12, 1)};
    double[] values = {0.9d};
    LocalDateDoubleTimeSeries series = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    _htsMaster.updateTimeSeriesDataPoints(ObjectId.of("DbHts", "DP101"), series);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_atExistingPoint() {
    LocalDate[] dates = {LocalDate.of(2011, 1, 3)};
    double[] values = {0.9d};
    LocalDateDoubleTimeSeries series = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    _htsMaster.updateTimeSeriesDataPoints(ObjectId.of("DbHts", "DP101"), series);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_update_102_startsEmpty() {
    LocalDate[] dates = {LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 2), LocalDate.of(2011, 7, 4)};
    double[] values = {1.1d, 2.2d, 3.3d};
    LocalDateDoubleTimeSeries series = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    ObjectId oid = ObjectId.of("DbHts", "DP102");
    UniqueId uniqueId = _htsMaster.updateTimeSeriesDataPoints(oid, series);
    
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(series, test.getTimeSeries());
  }

  @Test
  public void test_update_101_startsFull() {
    LocalDate[] dates = {LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 2), LocalDate.of(2011, 7, 4)};
    double[] values = {1.1d, 2.2d, 3.3d};
    LocalDateDoubleTimeSeries series = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    UniqueId uniqueId = _htsMaster.updateTimeSeriesDataPoints(oid, series);
    
    ManageableHistoricalTimeSeries testAdded = _htsMaster.getTimeSeries(uniqueId, HistoricalTimeSeriesGetFilter.ofRange(LocalDate.of(2011, 7, 1), null));
    assertEquals(uniqueId, testAdded.getUniqueId());
    assertEquals(series, testAdded.getTimeSeries());
    
    ManageableHistoricalTimeSeries testAll = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId, testAll.getUniqueId());
    assertEquals(6, testAll.getTimeSeries().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
