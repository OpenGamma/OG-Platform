/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryHolidayDbHolidayMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryHolidayDbHolidayMasterWorkerGetTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryHolidayDbHolidayMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryHolidayDbHolidayMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getHoliday_nullUID() {
    _holMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getHoliday_versioned_notFoundId() {
    UniqueId uniqueId = UniqueId.of("DbHol", "0", "0");
    _holMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getHoliday_versioned_notFoundVersion() {
    UniqueId uniqueId = UniqueId.of("DbHol", "101", "1");
    _holMaster.get(uniqueId);
  }

  @Test
  public void test_getHoliday_versioned_oneHolidayDate() {
    UniqueId uniqueId = UniqueId.of("DbHol", "101", "0");
    HolidayDocument test = _holMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getHoliday_versioned_twoHolidayDates() {
    UniqueId uniqueId = UniqueId.of("DbHol", "102", "0");
    HolidayDocument test = _holMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getHoliday_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbHol", "201", "0");
    HolidayDocument test = _holMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getHoliday_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbHol", "201", "1");
    HolidayDocument test = _holMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getHoliday_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbHol", "0");
    _holMaster.get(uniqueId);
  }

  @Test
  public void test_getHoliday_unversioned() {
    UniqueId oid = UniqueId.of("DbHol", "201");
    HolidayDocument test = _holMaster.get(oid);
    assert202(test);
  }

}
