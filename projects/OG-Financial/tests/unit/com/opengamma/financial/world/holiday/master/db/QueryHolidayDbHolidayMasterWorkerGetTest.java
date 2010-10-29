/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master.db;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.world.holiday.master.HolidayDocument;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests QueryHolidayDbHolidayMasterWorker.
 */
public class QueryHolidayDbHolidayMasterWorkerGetTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryHolidayDbHolidayMasterWorkerGetTest.class);

  private DbHolidayMasterWorker _worker;

  public QueryHolidayDbHolidayMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryHolidayDbHolidayMasterWorker();
    _worker.init(_holMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_getHoliday_nullUID() {
    _worker.get(null);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_getHoliday_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "0", "0");
    _worker.get(uid);
  }

  @Test
  public void test_getHoliday_versioned_oneHolidayDate() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101", "0");
    HolidayDocument test = _worker.get(uid);
    assert101(test);
  }

  @Test
  public void test_getHoliday_versioned_twoHolidayDates() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "102", "0");
    HolidayDocument test = _worker.get(uid);
    assert102(test);
  }

  @Test
  public void test_getHoliday_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "201", "0");
    HolidayDocument test = _worker.get(uid);
    assert201(test);
  }

  @Test
  public void test_getHoliday_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "201", "1");
    HolidayDocument test = _worker.get(uid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_getHoliday_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "0");
    _worker.get(uid);
  }

  @Test
  public void test_getHoliday_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbHol", "201");
    HolidayDocument test = _worker.get(oid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbHol]", _worker.toString());
  }

}
