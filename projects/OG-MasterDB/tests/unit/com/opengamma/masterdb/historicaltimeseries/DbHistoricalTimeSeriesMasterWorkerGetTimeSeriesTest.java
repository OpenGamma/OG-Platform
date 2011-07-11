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
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.util.test.DBTest;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterWorkerGetTimeSeriesTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerGetTimeSeriesTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbHistoricalTimeSeriesMasterWorkerGetTimeSeriesTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_get_nullUID() {
    _htsMaster.getTimeSeries((UniqueIdentifier) null, null, null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_versioned_notFoundId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "DP0");
    _htsMaster.getTimeSeries(uid, null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_get_versioned_notFoundVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "DP101", "2010");
    _htsMaster.getTimeSeries(uid, null, null);
  }

  @Test
  public void test_get_UID_101() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(uid, null, null);
    assertEquals(uid.getObjectId(), test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(3, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTime(0));
    assertEquals(3.1d, timeSeries.getValueAt(0), 0.001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTime(1));
    assertEquals(3.22d, timeSeries.getValueAt(1), 0.001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTime(2));
    assertEquals(3.33d, timeSeries.getValueAt(2), 0.001d);
  }

  @Test
  public void test_get_UID_102() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "DP102");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(uid, null, null);
    assertEquals(uid.getObjectId(), test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(0, timeSeries.size());
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_UID_101_removed() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "101");
    _htsMaster.remove(uid);
    
    _htsMaster.getTimeSeries(UniqueIdentifier.of("DbHts", "DP101"), null, null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_get_OID_101_latest() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid, VersionCorrection.LATEST, null, null);
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(3, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTime(0));
    assertEquals(3.1d, timeSeries.getValueAt(0), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTime(1));
    assertEquals(3.22d, timeSeries.getValueAt(1), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTime(2));
    assertEquals(3.33d, timeSeries.getValueAt(2), 0.0001d);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_OID_101_pre1() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "DP101");
    _htsMaster.getTimeSeries(oid, VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(1)), null, null);
  }

  @Test
  public void test_get_OID_101_post1() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid, VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(1)), null, null);
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(1, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTime(0));
    assertEquals(3.1d, timeSeries.getValueAt(0), 0.0001d);
  }

  @Test
  public void test_get_OID_101_post2() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid, VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(1)), null, null);
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(3, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTime(0));
    assertEquals(3.1d, timeSeries.getValueAt(0), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTime(1));
    assertEquals(3.22d, timeSeries.getValueAt(1), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTime(2));
    assertEquals(3.33d, timeSeries.getValueAt(2), 0.0001d);
  }

  @Test
  public void test_get_OID_101_correctPost2() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "DP101");
    ManageableHistoricalTimeSeries test = _htsMaster.getTimeSeries(oid, VersionCorrection.of(_version2Instant.plusSeconds(1), _version3Instant.plusSeconds(1)), null, null);
    assertEquals(oid, test.getUniqueId().getObjectId());
    LocalDateDoubleTimeSeries timeSeries = test.getTimeSeries();
    assertEquals(3, timeSeries.size());
    assertEquals(LocalDate.of(2011, 1, 1), timeSeries.getTime(0));
    assertEquals(3.1d, timeSeries.getValueAt(0), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 2), timeSeries.getTime(1));
    assertEquals(3.21d, timeSeries.getValueAt(1), 0.0001d);
    assertEquals(LocalDate.of(2011, 1, 3), timeSeries.getTime(2));
    assertEquals(3.3d, timeSeries.getValueAt(2), 0.0001d);
  }

//  //-------------------------------------------------------------------------
//  @Test
//  public void test_get_versioned_notLatest() {
//    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "201", "0");
//    HistoricalTimeSeriesInfoDocument test = _htsMaster.getTimeSeries(uid);
//    assert201(test);
//  }
//
//  @Test
//  public void test_get_versioned_latestVersionNotLatestCorrection() {
//    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "201", "1");
//    HistoricalTimeSeriesInfoDocument test = _htsMaster.getTimeSeries(uid);
//    assert202(test);
//  }
//
//  @Test
//  public void test_get_versioned_latest() {
//    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "201", "2");
//    HistoricalTimeSeriesInfoDocument test = _htsMaster.getTimeSeries(uid);
//    assert203(test);
//  }
//
//  //-------------------------------------------------------------------------
//  @Test(expectedExceptions = DataNotFoundException.class)
//  public void test_get_unversioned_notFound() {
//    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "0");
//    _htsMaster.getTimeSeries(uid);
//  }
//
//  @Test
//  public void test_get_unversioned() {
//    UniqueIdentifier uid = UniqueIdentifier.of("DbHts", "201");
//    HistoricalTimeSeriesInfoDocument test = _htsMaster.getTimeSeries(uid);
//    assert203(test);
//  }
//
//  //-------------------------------------------------------------------------
//  @Test
//  public void test_getObjectIdentifier() {
//    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
//    HistoricalTimeSeriesInfoDocument test = _htsMaster.getTimeSeries(oid, VersionCorrection.LATEST);
//    assert203(test);
//  }
//
//  @Test
//  public void test_getObjectIdentifier_earlierCorrection() {
//    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
//    HistoricalTimeSeriesInfoDocument test = _htsMaster.getTimeSeries(oid, VersionCorrection.ofCorrectedTo(_version2Instant));
//    assert202(test);
//  }
//
//  @Test
//  public void test_getObjectIdentifier_earlierVersion() {
//    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
//    HistoricalTimeSeriesInfoDocument test = _htsMaster.getTimeSeries(oid, VersionCorrection.ofVersionAsOf(_version1Instant));
//    assert201(test);
//  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
