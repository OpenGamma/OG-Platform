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
public class DbHistoricalTimeSeriesMasterWorkerCorrectTimeSeriesTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerCorrectTimeSeriesTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerCorrectTimeSeriesTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_nullOID() {
    _htsMaster.correctTimeSeriesDataPoints((ObjectId) null, new ArrayLocalDateDoubleTimeSeries());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_nullSeries() {
    _htsMaster.correctTimeSeriesDataPoints(ObjectId.of("DbHts", "DP101"), null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_versioned_notFoundId() {
    ObjectId oid = ObjectId.of("DbHts", "DP0");
    _htsMaster.correctTimeSeriesDataPoints(oid, new ArrayLocalDateDoubleTimeSeries());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_correct_101_startsFull() {
    LocalDate[] dates = {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 2)};
    double[] values = {0.1d, 0.2d};
    LocalDateDoubleTimeSeries series = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    UniqueId uniqueId = _htsMaster.correctTimeSeriesDataPoints(oid, series);
    
    ManageableHistoricalTimeSeries testCorrected = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId, testCorrected.getUniqueId());
    LocalDateDoubleTimeSeries timeSeries = testCorrected.getTimeSeries();
    assertEquals(3, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAt(0));
    assertEquals(0.1d, timeSeries.getValueAt(0), 0.001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAt(1));
    assertEquals(0.2d, timeSeries.getValueAt(1), 0.001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAt(2));
    assertEquals(3.33d, timeSeries.getValueAt(2), 0.001d);
  }

  @Test
  public void test_correct_101_insertNew() {
    LocalDate[] dates = {LocalDate.of(2010, 12, 31)};
    double[] values = {0.5d};
    LocalDateDoubleTimeSeries series = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    UniqueId uniqueId = _htsMaster.correctTimeSeriesDataPoints(oid, series);
    
    ManageableHistoricalTimeSeries testCorrected = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId, testCorrected.getUniqueId());
    LocalDateDoubleTimeSeries timeSeries = testCorrected.getTimeSeries();
    assertEquals(4, timeSeries.size());
    assertEquals(LocalDate.of(2010, 12, 31), timeSeries.getTimeAt(0));
    assertEquals(0.5d, timeSeries.getValueAt(0), 0.001d);
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAt(1));
    assertEquals(3.1d, timeSeries.getValueAt(1), 0.001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTimeAt(2));
    assertEquals(3.22d, timeSeries.getValueAt(2), 0.001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAt(3));
    assertEquals(3.33d, timeSeries.getValueAt(3), 0.001d);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
