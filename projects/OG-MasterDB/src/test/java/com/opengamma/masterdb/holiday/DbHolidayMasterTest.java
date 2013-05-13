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
import org.threeten.bp.LocalDate;

import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbHolidayMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHolidayMasterTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbHolidayMasterTest.class);

  private DbHolidayMaster _holMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHolidayMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _holMaster = new DbHolidayMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _holMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_holMaster);
    assertEquals(true, _holMaster.getUniqueIdScheme().equals("DbHol"));
    assertNotNull(_holMaster.getDbConnector());
    assertNotNull(_holMaster.getClock());
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
