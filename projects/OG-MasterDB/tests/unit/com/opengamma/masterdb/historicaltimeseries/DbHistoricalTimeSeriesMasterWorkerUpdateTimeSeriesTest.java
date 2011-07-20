/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TimeZone;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterWorkerUpdateTimeSeriesTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerUpdateTimeSeriesTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbHistoricalTimeSeriesMasterWorkerUpdateTimeSeriesTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_nullOID() {
    _htsMaster.updateTimeSeriesDataPoints((ObjectIdentifier) null, new ArrayLocalDateDoubleTimeSeries());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_nullSeries() {
    _htsMaster.updateTimeSeriesDataPoints(ObjectIdentifier.of("DbHts", "DP101"), null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_versioned_notFoundId() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "DP0");
    _htsMaster.updateTimeSeriesDataPoints(oid, new ArrayLocalDateDoubleTimeSeries());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_beforeAllExistingPoints() {
    LocalDate[] dates = {LocalDate.of(2010, 12, 1)};
    double[] values = {0.9d};
    LocalDateDoubleTimeSeries series = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    _htsMaster.updateTimeSeriesDataPoints(ObjectIdentifier.of("DbHts", "DP101"), series);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_atExistingPoint() {
    LocalDate[] dates = {LocalDate.of(2011, 1, 2)};
    double[] values = {0.9d};
    LocalDateDoubleTimeSeries series = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    _htsMaster.updateTimeSeriesDataPoints(ObjectIdentifier.of("DbHts", "DP101"), series);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_update_102_startsEmpty() {
    LocalDate[] dates = {LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 2), LocalDate.of(2011, 7, 4)};
    double[] values = {1.1d, 2.2d, 3.3d};
    LocalDateDoubleTimeSeries series = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "DP102");
    UniqueIdentifier uid = _htsMaster.updateTimeSeriesDataPoints(oid, series);
    
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(uid, null, null);
    assertEquals(uid, test.getUniqueId());
    assertEquals(series, test.getTimeSeries());
  }

  @Test
  public void test_update_101_startsFull() {
    LocalDate[] dates = {LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 2), LocalDate.of(2011, 7, 4)};
    double[] values = {1.1d, 2.2d, 3.3d};
    LocalDateDoubleTimeSeries series = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "DP101");
    UniqueIdentifier uid = _htsMaster.updateTimeSeriesDataPoints(oid, series);
    
    ManageableHistoricalTimeSeries testAdded = _htsMaster.getTimeSeries(uid, LocalDate.of(2011, 7, 1), null);
    assertEquals(uid, testAdded.getUniqueId());
    assertEquals(series, testAdded.getTimeSeries());
    
    ManageableHistoricalTimeSeries testAll = _htsMaster.getTimeSeries(uid, null, null);
    assertEquals(uid, testAll.getUniqueId());
    assertEquals(6, testAll.getTimeSeries().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
