/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.util.test.DBTest;

/**
 * Tests QueryHolidayDbHolidayMasterWorker.
 */
public class QueryHolidayDbHolidayMasterWorkerGetTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryHolidayDbHolidayMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public QueryHolidayDbHolidayMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getHoliday_nullUID() {
    _holMaster.get(null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getHoliday_versioned_notFoundId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "0", "0");
    _holMaster.get(uid);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getHoliday_versioned_notFoundVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101", "1");
    _holMaster.get(uid);
  }

  @Test
  public void test_getHoliday_versioned_oneHolidayDate() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101", "0");
    HolidayDocument test = _holMaster.get(uid);
    assert101(test);
  }

  @Test
  public void test_getHoliday_versioned_twoHolidayDates() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "102", "0");
    HolidayDocument test = _holMaster.get(uid);
    assert102(test);
  }

  @Test
  public void test_getHoliday_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "201", "0");
    HolidayDocument test = _holMaster.get(uid);
    assert201(test);
  }

  @Test
  public void test_getHoliday_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "201", "1");
    HolidayDocument test = _holMaster.get(uid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getHoliday_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "0");
    _holMaster.get(uid);
  }

  @Test
  public void test_getHoliday_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbHol", "201");
    HolidayDocument test = _holMaster.get(oid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_holMaster.getClass().getSimpleName() + "[DbHol]", _holMaster.toString());
  }

}
