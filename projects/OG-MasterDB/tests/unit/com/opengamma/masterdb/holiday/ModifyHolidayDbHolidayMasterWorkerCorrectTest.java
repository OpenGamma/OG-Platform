/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.junit.Assert.assertEquals;

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
import com.opengamma.core.common.Currency;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.ManageableHoliday;

/**
 * Tests ModifyHolidayDbHolidayMasterWorker.
 */
public class ModifyHolidayDbHolidayMasterWorkerCorrectTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyHolidayDbHolidayMasterWorkerCorrectTest.class);

  private ModifyHolidayDbHolidayMasterWorker _worker;
  private DbHolidayMasterWorker _queryWorker;

  public ModifyHolidayDbHolidayMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
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
  public void test_correctHoliday_nullDocument() {
    _worker.correct(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_correct_noHolidayId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101");
    ManageableHoliday holiday = new ManageableHoliday(Currency.getInstance("USD"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueIdentifier(uid);
    HolidayDocument doc = new HolidayDocument(holiday);
    doc.setHolidayId(null);
    _worker.correct(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_correct_noHoliday() {
    HolidayDocument doc = new HolidayDocument();
    doc.setHolidayId(UniqueIdentifier.of("DbHol", "101", "0"));
    _worker.correct(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_correct_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "0", "0");
    ManageableHoliday holiday = new ManageableHoliday(Currency.getInstance("USD"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueIdentifier(uid);
    HolidayDocument doc = new HolidayDocument(holiday);
    _worker.correct(doc);
  }

//  @Test(expected = IllegalArgumentException.class)
//  public void test_correct_notLatestCorrection() {
//    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "201", "0");
//    ManageableHoliday holiday = new ManageableHoliday(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
//    HolidayDocument doc = new HolidayDocument(holiday);
//    _worker.correct(doc);
//  }

  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101", "0");
    HolidayDocument base = _queryWorker.get(uid);
    ManageableHoliday holiday = new ManageableHoliday(Currency.getInstance("USD"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueIdentifier(uid);
    HolidayDocument input = new HolidayDocument(holiday);
    
    HolidayDocument corrected = _worker.correct(input);
    assertEquals(false, base.getHolidayId().equals(corrected.getHolidayId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getHoliday(), corrected.getHoliday());
    
    HolidayDocument old = _queryWorker.get(UniqueIdentifier.of("DbHol", "101", "0"));
    assertEquals(base.getHolidayId(), old.getHolidayId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getHoliday(), old.getHoliday());
    
    HolidayHistoryRequest search = new HolidayHistoryRequest(base.getHolidayId(), now, null);
    HolidayHistoryResult searchResult = _queryWorker.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbHol]", _worker.toString());
  }

}
