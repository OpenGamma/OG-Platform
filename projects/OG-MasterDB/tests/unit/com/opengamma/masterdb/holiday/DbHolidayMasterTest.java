/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Arrays;
import java.util.TimeZone;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DBTest;

/**
 * Test DbHolidayMaster.
 */
public class DbHolidayMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbHolidayMasterTest.class);

  private DbHolidayMaster _holMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbHolidayMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _holMaster = (DbHolidayMaster) context.getBean(getDatabaseType() + "DbHolidayMaster");
  }

  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    _holMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_holMaster);
    assertEquals(true, _holMaster.getUniqueIdScheme().equals("DbHol"));
    assertNotNull(_holMaster.getDbSource());
    assertNotNull(_holMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    ManageableHoliday hol = new ManageableHoliday(Currency.GBP, Arrays.asList(LocalDate.of(2010, 2, 3)));
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
