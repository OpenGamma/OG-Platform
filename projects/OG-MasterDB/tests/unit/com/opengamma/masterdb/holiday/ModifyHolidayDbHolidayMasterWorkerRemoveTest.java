/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

  public ModifyHolidayDbHolidayMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_removeHoliday_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "0", "0");
    _holMaster.remove(uid);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101", "0");
    _holMaster.remove(uid);
    HolidayDocument test = _holMaster.get(uid);
    
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uid, holiday.getUniqueId());
    assertEquals("TestHoliday101", test.getName());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals("GBP", holiday.getCurrency().getISOCode());
    assertEquals(null, holiday.getRegionKey());
    assertEquals(null, holiday.getExchangeKey());
    assertEquals(Arrays.asList(LocalDate.of(2010, 1, 1)), holiday.getHolidayDates());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_holMaster.getClass().getSimpleName() + "[DbHol]", _holMaster.toString());
  }

}
