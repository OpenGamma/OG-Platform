/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;

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
public class ModifyHolidayDbHolidayMasterWorkerUpdateTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyHolidayDbHolidayMasterWorkerUpdateTest.class);

  private ModifyHolidayDbHolidayMasterWorker _worker;
  private DbHolidayMasterWorker _queryWorker;

  public ModifyHolidayDbHolidayMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
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
  public void test_updateHoliday_nullDocument() {
    _worker.update(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_update_noHolidayId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101");
    ManageableHoliday holiday = new ManageableHoliday(Currency.getInstance("USD"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueIdentifier(uid);
    HolidayDocument doc = new HolidayDocument();
    doc.setHoliday(holiday);
    _worker.update(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_update_noHoliday() {
    HolidayDocument doc = new HolidayDocument();
    doc.setHolidayId(UniqueIdentifier.of("DbHol", "101", "0"));
    _worker.update(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_update_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "0", "0");
    ManageableHoliday holiday = new ManageableHoliday(Currency.getInstance("USD"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueIdentifier(uid);
    HolidayDocument doc = new HolidayDocument(holiday);
    _worker.update(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "201", "0");
    ManageableHoliday holiday = new ManageableHoliday(Currency.getInstance("USD"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueIdentifier(uid);
    HolidayDocument doc = new HolidayDocument(holiday);
    _worker.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_holMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101", "0");
    HolidayDocument base = _queryWorker.get(uid);
    ManageableHoliday holiday = new ManageableHoliday(Currency.getInstance("USD"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueIdentifier(uid);
    HolidayDocument input = new HolidayDocument(holiday);
    
    HolidayDocument updated = _worker.update(input);
    assertEquals(false, base.getHolidayId().equals(updated.getHolidayId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getHoliday(), updated.getHoliday());
    
    HolidayDocument old = _queryWorker.get(uid);
    assertEquals(base.getHolidayId(), old.getHolidayId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getHoliday(), old.getHoliday());
    
    HolidayHistoryRequest search = new HolidayHistoryRequest(base.getHolidayId(), null, now);
    HolidayHistoryResult searchResult = _queryWorker.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    ModifyHolidayDbHolidayMasterWorker w = new ModifyHolidayDbHolidayMasterWorker() {
      @Override
      protected String sqlInsertDate() {
        return "INSERT";  // bad sql
      };
    };
    w.init(_holMaster);
    final HolidayDocument base = _queryWorker.get(UniqueIdentifier.of("DbHol", "101", "0"));
    UniqueIdentifier uid = UniqueIdentifier.of("DbHol", "101", "0");
    ManageableHoliday holiday = new ManageableHoliday(Currency.getInstance("USD"), Arrays.asList(LocalDate.of(2010, 6, 9)));
    holiday.setUniqueIdentifier(uid);
    HolidayDocument input = new HolidayDocument(holiday);
    try {
      w.update(input);
      fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final HolidayDocument test = _queryWorker.get(UniqueIdentifier.of("DbHol", "101", "0"));
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbHol]", _worker.toString());
  }

}
