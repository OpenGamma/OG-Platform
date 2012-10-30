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
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterWorkerRemoveTimeSeriesTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerRemoveTimeSeriesTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerRemoveTimeSeriesTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_remove_nullOID() {
    _htsMaster.removeTimeSeriesDataPoints((ObjectId) null, LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_remove_dateOrder() {
    _htsMaster.removeTimeSeriesDataPoints(ObjectId.of("DbHts", "DP101"), LocalDate.of(2011, 7, 1), LocalDate.of(2011, 3, 1));
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_remove_versioned_notFoundId() {
    ObjectId oid = ObjectId.of("DbHts", "DP0");
    _htsMaster.removeTimeSeriesDataPoints(oid, LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_remove_101_removeOne() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    UniqueId uniqueId = _htsMaster.removeTimeSeriesDataPoints(oid, LocalDate.of(2011, 1, 2), LocalDate.of(2011, 1, 2));
    
    ManageableHistoricalTimeSeries testCorrected = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId, testCorrected.getUniqueId());
    LocalDateDoubleTimeSeries timeSeries = testCorrected.getTimeSeries();
    assertEquals(2, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAt(0));
    assertEquals(3.1d, timeSeries.getValueAt(0), 0.001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAt(1));
    assertEquals(3.33d, timeSeries.getValueAt(1), 0.001d);
  }

  @Test
  public void test_remove_101_removeRange() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    UniqueId uniqueId = _htsMaster.removeTimeSeriesDataPoints(oid, LocalDate.of(2010, 7, 3), LocalDate.of(2011, 1, 2));
    
    ManageableHistoricalTimeSeries testCorrected = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId, testCorrected.getUniqueId());
    LocalDateDoubleTimeSeries timeSeries = testCorrected.getTimeSeries();
    assertEquals(1, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTimeAt(0));
    assertEquals(3.33d, timeSeries.getValueAt(0), 0.001d);
  }

  @Test
  public void test_remove_101_removeRangeWithNullEnd() {
    ObjectId oid = ObjectId.of("DbHts", "DP101");
    UniqueId uniqueId = _htsMaster.removeTimeSeriesDataPoints(oid, LocalDate.of(2011, 1, 2), null);
    
    ManageableHistoricalTimeSeries testCorrected = _htsMaster.getTimeSeries(uniqueId);
    assertEquals(uniqueId, testCorrected.getUniqueId());
    LocalDateDoubleTimeSeries timeSeries = testCorrected.getTimeSeries();
    assertEquals(1, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTimeAt(0));
    assertEquals(3.1d, timeSeries.getValueAt(0), 0.001d);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
