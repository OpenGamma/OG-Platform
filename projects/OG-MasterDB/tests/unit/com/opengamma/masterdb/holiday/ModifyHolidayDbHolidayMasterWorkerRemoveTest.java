/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;

/**
 * Tests ModifyHolidayDbHolidayMasterWorker.
 */
public class ModifyHolidayDbHolidayMasterWorkerRemoveTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyHolidayDbHolidayMasterWorkerRemoveTest.class);

  private ModifyHolidayDbHolidayMasterWorker _worker;
  private DbHolidayMasterWorker _queryWorker;

  public ModifyHolidayDbHolidayMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new ModifyHolidayDbHolidayMasterWorker();
    _worker.init(_holMaster);
    _queryWorker = new QueryHolidayDbHolidayMasterWorker();
    _queryWorker.init(_holMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_removeHoliday_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "0", "0");
    _worker.remove(uid);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101", "0");
    _worker.remove(uid);
    HolidayDocument test = _queryWorker.get(uid);
    
    assertEquals(uid, test.getHolidayId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uid, holiday.getUniqueIdentifier());
    assertEquals("TestHoliday101", test.getName());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals("GBP", holiday.getCurrency().getISOCode());
    assertEquals(null, holiday.getRegionId());
    assertEquals(null, holiday.getExchangeId());
    assertEquals(Arrays.asList(LocalDate.of(2010, 1, 1)), holiday.getHolidayDates());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbHol]", _worker.toString());
  }

}
