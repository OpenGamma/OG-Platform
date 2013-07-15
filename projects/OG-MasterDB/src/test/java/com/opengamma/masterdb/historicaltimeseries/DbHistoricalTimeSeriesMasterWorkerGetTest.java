/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHistoricalTimeSeriesMasterWorkerGetTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_get_nullUID() {
    _htsMaster.get((UniqueId) null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_versioned_notFoundId() {
    UniqueId uniqueId = UniqueId.of("DbHts", "0", "0");
    _htsMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_versioned_notFoundVersion() {
    UniqueId uniqueId = UniqueId.of("DbHts", "101", "1");
    _htsMaster.get(uniqueId);
  }

  @Test
  public void test_get_versioned101() {
    UniqueId uniqueId = UniqueId.of("DbHts", "101", "0");
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_get_versioned102() {
    UniqueId uniqueId = UniqueId.of("DbHts", "102", "0");
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(uniqueId);
    assert102(test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_get_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbHts", "201", "0");
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_get_versioned_latestVersionNotLatestCorrection() {
    UniqueId uniqueId = UniqueId.of("DbHts", "201", "1");
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(uniqueId);
    assert202(test);
  }

  @Test
  public void test_get_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbHts", "201", "2");
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(uniqueId);
    assert203(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbHts", "0");
    _htsMaster.get(uniqueId);
  }

  @Test
  public void test_get_unversioned() {
    UniqueId uniqueId = UniqueId.of("DbHts", "201");
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(uniqueId);
    assert203(test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getObjectId() {
    ObjectId oid = ObjectId.of("DbHts", "201");
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(oid, VersionCorrection.LATEST);
    assert203(test);
  }

  @Test
  public void test_getObjectId_earlierCorrection() {
    ObjectId oid = ObjectId.of("DbHts", "201");
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(oid, VersionCorrection.ofCorrectedTo(_version2Instant));
    assert202(test);
  }

  @Test
  public void test_getObjectId_earlierVersion() {
    ObjectId oid = ObjectId.of("DbHts", "201");
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(oid, VersionCorrection.ofVersionAsOf(_version1Instant));
    assert201(test);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getObjectId_tooEarly() {
    ObjectId oid = ObjectId.of("DbHts", "201");
    _htsMaster.get(oid, VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(1)));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
