/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;

/**
 * Tests ModifyHolidayDbHolidayMasterWorker.
 */
public class ModifyHolidayDbHolidayMasterWorkerAddTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyHolidayDbHolidayMasterWorkerAddTest.class);

  private ModifyHolidayDbHolidayMasterWorker _worker;
  private DbHolidayMasterWorker _queryWorker;

  public ModifyHolidayDbHolidayMasterWorkerAddTest(String databaseType, String databaseVersion) {
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
  @Test(expected = NullPointerException.class)
  public void test_addHoliday_nullDocument() {
    _worker.add(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_add_noHoliday() {
    HolidayDocument doc = new HolidayDocument();
    _worker.add(doc);
  }

  @Test
  public void test_add_add_currency() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    ManageableHoliday holiday = new ManageableHoliday(Currency.getInstance("USD"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    HolidayDocument doc = new HolidayDocument(holiday);
    String name = doc.getName();
    HolidayDocument test = _worker.add(doc);
    
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbHol", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday testHoliday = test.getHoliday();
    assertNotNull(testHoliday);
    assertEquals(uid, testHoliday.getUniqueIdentifier());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.CURRENCY, testHoliday.getType());
    assertEquals("USD", testHoliday.getCurrency().getISOCode());
    assertEquals(null, testHoliday.getRegionId());
    assertEquals(null, testHoliday.getExchangeId());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_add_bank() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    ManageableHoliday holiday = new ManageableHoliday(HolidayType.BANK, Identifier.of("A", "B"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    HolidayDocument doc = new HolidayDocument(holiday);
    String name = doc.getName();
    HolidayDocument test = _worker.add(doc);
    
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbHol", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday testHoliday = test.getHoliday();
    assertNotNull(testHoliday);
    assertEquals(uid, testHoliday.getUniqueIdentifier());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.BANK, testHoliday.getType());
    assertEquals(null, testHoliday.getCurrency());
    assertEquals(Identifier.of("A", "B"), testHoliday.getRegionId());
    assertEquals(null, testHoliday.getExchangeId());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_add_settlement() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    ManageableHoliday holiday = new ManageableHoliday(HolidayType.SETTLEMENT, Identifier.of("A", "B"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    HolidayDocument doc = new HolidayDocument(holiday);
    String name = doc.getName();
    HolidayDocument test = _worker.add(doc);
    
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbHol", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday testHoliday = test.getHoliday();
    assertNotNull(testHoliday);
    assertEquals(uid, testHoliday.getUniqueIdentifier());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.SETTLEMENT, testHoliday.getType());
    assertEquals(null, testHoliday.getCurrency());
    assertEquals(null, testHoliday.getRegionId());
    assertEquals(Identifier.of("A", "B"), testHoliday.getExchangeId());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_add_trading() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    ManageableHoliday holiday = new ManageableHoliday(HolidayType.TRADING, Identifier.of("A", "B"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    HolidayDocument doc = new HolidayDocument(holiday);
    String name = doc.getName();
    HolidayDocument test = _worker.add(doc);
    
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbHol", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHoliday testHoliday = test.getHoliday();
    assertNotNull(testHoliday);
    assertEquals(uid, testHoliday.getUniqueIdentifier());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.TRADING, testHoliday.getType());
    assertEquals(null, testHoliday.getCurrency());
    assertEquals(null, testHoliday.getRegionId());
    assertEquals(Identifier.of("A", "B"), testHoliday.getExchangeId());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_addThenGet() {
    ManageableHoliday holiday = new ManageableHoliday(Currency.getInstance("USD"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    HolidayDocument doc = new HolidayDocument(holiday);
    HolidayDocument added = _worker.add(doc);
    
    HolidayDocument test = _queryWorker.get(added.getUniqueId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbHol]", _worker.toString());
  }

}
