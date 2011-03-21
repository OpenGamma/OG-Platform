/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.money.Currency;

/**
 * Tests ModifyHolidayDbHolidayMasterWorker.
 */
public class ModifyHolidayDbHolidayMasterWorkerAddTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyHolidayDbHolidayMasterWorkerAddTest.class);

  public ModifyHolidayDbHolidayMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_addHoliday_nullDocument() {
    _holMaster.add(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_add_noHoliday() {
    HolidayDocument doc = new HolidayDocument();
    _holMaster.add(doc);
  }

  @Test
  public void test_add_add_currency() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    ManageableHoliday holiday = new ManageableHoliday(Currency.USD, Arrays.asList(LocalDate.of(2010, 6, 9)));
    HolidayDocument doc = new HolidayDocument(holiday);
    String name = doc.getName();
    HolidayDocument test = _holMaster.add(doc);
    
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
    assertEquals(uid, testHoliday.getUniqueId());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.CURRENCY, testHoliday.getType());
    assertEquals("USD", testHoliday.getCurrency().getCode());
    assertEquals(null, testHoliday.getRegionKey());
    assertEquals(null, testHoliday.getExchangeKey());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_add_bank() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    ManageableHoliday holiday = new ManageableHoliday(HolidayType.BANK, Identifier.of("A", "B"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    HolidayDocument doc = new HolidayDocument(holiday);
    String name = doc.getName();
    HolidayDocument test = _holMaster.add(doc);
    
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
    assertEquals(uid, testHoliday.getUniqueId());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.BANK, testHoliday.getType());
    assertEquals(null, testHoliday.getCurrency());
    assertEquals(Identifier.of("A", "B"), testHoliday.getRegionKey());
    assertEquals(null, testHoliday.getExchangeKey());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_add_settlement() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    ManageableHoliday holiday = new ManageableHoliday(HolidayType.SETTLEMENT, Identifier.of("A", "B"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    HolidayDocument doc = new HolidayDocument(holiday);
    String name = doc.getName();
    HolidayDocument test = _holMaster.add(doc);
    
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
    assertEquals(uid, testHoliday.getUniqueId());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.SETTLEMENT, testHoliday.getType());
    assertEquals(null, testHoliday.getCurrency());
    assertEquals(null, testHoliday.getRegionKey());
    assertEquals(Identifier.of("A", "B"), testHoliday.getExchangeKey());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_add_trading() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    ManageableHoliday holiday = new ManageableHoliday(HolidayType.TRADING, Identifier.of("A", "B"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    HolidayDocument doc = new HolidayDocument(holiday);
    String name = doc.getName();
    HolidayDocument test = _holMaster.add(doc);
    
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
    assertEquals(uid, testHoliday.getUniqueId());
    assertEquals(name, test.getName());
    assertEquals(HolidayType.TRADING, testHoliday.getType());
    assertEquals(null, testHoliday.getCurrency());
    assertEquals(null, testHoliday.getRegionKey());
    assertEquals(Identifier.of("A", "B"), testHoliday.getExchangeKey());
    assertEquals(Arrays.asList(LocalDate.of(2010, 6, 9)), testHoliday.getHolidayDates());
  }

  @Test
  public void test_add_addThenGet() {
    ManageableHoliday holiday = new ManageableHoliday(Currency.USD, Arrays.asList(LocalDate.of(2010, 6, 9)));
    HolidayDocument doc = new HolidayDocument(holiday);
    HolidayDocument added = _holMaster.add(doc);
    
    HolidayDocument test = _holMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_holMaster.getClass().getSimpleName() + "[DbHol]", _holMaster.toString());
  }

}
