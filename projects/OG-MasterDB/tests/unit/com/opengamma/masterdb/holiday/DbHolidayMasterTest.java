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

import javax.time.calendar.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.opengamma.core.common.Currency;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Test DbHolidayMaster.
 */
public class DbHolidayMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbHolidayMasterTest.class);

  private DbHolidayMaster _holMaster;

  public DbHolidayMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _holMaster = (DbHolidayMaster) context.getBean(getDatabaseType() + "DbHolidayMaster");
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _holMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_holMaster);
    assertEquals(true, _holMaster.getIdentifierScheme().equals("DbHol"));
    assertNotNull(_holMaster.getDbSource());
    assertNotNull(_holMaster.getTimeSource());
    assertNotNull(_holMaster.getWorkers());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    ManageableHoliday hol = new ManageableHoliday(Currency.getInstance("GBP"), Arrays.asList(LocalDate.of(2010, 2, 3)));
    HolidayDocument addDoc = new HolidayDocument(hol);
    HolidayDocument added = _holMaster.add(addDoc);
    
    HolidayDocument loaded = _holMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbHolidayMaster[DbHol]", _holMaster.toString());
  }

}
