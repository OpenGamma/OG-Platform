/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyHolidayDbHolidayMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyHolidayDbHolidayMasterWorkerRemoveTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyHolidayDbHolidayMasterWorkerRemoveTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyHolidayDbHolidayMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeHoliday_versioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbHol", "0", "0");
    _holMaster.remove(uniqueId);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_holMaster.getClock());
    
    UniqueId uniqueId = UniqueId.of("DbHol", "101", "0");
    _holMaster.remove(uniqueId);
    HolidayDocument test = _holMaster.get(uniqueId);
    
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday holiday = test.getHoliday();
    assertNotNull(holiday);
    assertEquals(uniqueId, holiday.getUniqueId());
    assertEquals("TestHoliday101", test.getName());
    assertEquals(HolidayType.CURRENCY, holiday.getType());
    assertEquals("GBP", holiday.getCurrency().getCode());
    assertEquals(null, holiday.getRegionExternalId());
    assertEquals(null, holiday.getExchangeExternalId());
    assertEquals(Arrays.asList(LocalDate.of(2010, 1, 1)), holiday.getHolidayDates());
  }

}
