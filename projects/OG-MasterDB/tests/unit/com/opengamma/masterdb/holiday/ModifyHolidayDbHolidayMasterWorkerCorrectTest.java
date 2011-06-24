/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DBTest;

/**
 * Tests ModifyHolidayDbHolidayMasterWorker.
 */
public class ModifyHolidayDbHolidayMasterWorkerCorrectTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyHolidayDbHolidayMasterWorkerCorrectTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public ModifyHolidayDbHolidayMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correctHoliday_nullDocument() {
    _holMaster.correct(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noHolidayId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101");
    ManageableHoliday holiday = new ManageableHoliday(Currency.USD, Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueId(uid);
    HolidayDocument doc = new HolidayDocument(holiday);
    doc.setUniqueId(null);
    _holMaster.correct(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noHoliday() {
    HolidayDocument doc = new HolidayDocument();
    doc.setUniqueId(UniqueIdentifier.of("DbHol", "101", "0"));
    _holMaster.correct(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "0", "0");
    ManageableHoliday holiday = new ManageableHoliday(Currency.USD, Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueId(uid);
    HolidayDocument doc = new HolidayDocument(holiday);
    _holMaster.correct(doc);
  }

//  @Test(expected = IllegalArgumentException.class)
//  public void test_correct_notLatestCorrection() {
//    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "201", "0");
//    ManageableHoliday holiday = new ManageableHoliday(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
//    HolidayDocument doc = new HolidayDocument(holiday);
//    _holMaster.correct(doc);
//  }

  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101", "0");
    HolidayDocument base = _holMaster.get(uid);
    ManageableHoliday holiday = new ManageableHoliday(Currency.USD, Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueId(uid);
    HolidayDocument input = new HolidayDocument(holiday);
    
    HolidayDocument corrected = _holMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getHoliday(), corrected.getHoliday());
    
    HolidayDocument old = _holMaster.get(UniqueIdentifier.of("DbHol", "101", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getHoliday(), old.getHoliday());
    
    HolidayHistoryRequest search = new HolidayHistoryRequest(base.getUniqueId(), now, null);
    HolidayHistoryResult searchResult = _holMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_holMaster.getClass().getSimpleName() + "[DbHol]", _holMaster.toString());
  }

}
