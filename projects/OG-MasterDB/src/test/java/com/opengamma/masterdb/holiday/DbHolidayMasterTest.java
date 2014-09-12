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

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
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


  @Test
  public void test_custom_holidays() throws Exception {

    LocalDate newYears = LocalDate.of(2010, 1, 1);
    LocalDate mayDay = LocalDate.of(2010, 5, 1);
    LocalDate christmas = LocalDate.of(2010, 12, 25);

    ExternalId firstId = ExternalId.of("TEST", "first");
    ManageableHoliday first = new ManageableHoliday();
    first.setType(HolidayType.CUSTOM);
    first.setCustomExternalId(firstId);
    first.getHolidayDates().add(newYears);
    first.getHolidayDates().add(mayDay);
    HolidayDocument firstDoc = new HolidayDocument(first);
    _holMaster.add(firstDoc);

    ExternalId secondId = ExternalId.of("TEST", "second");
    ManageableHoliday second = new ManageableHoliday();
    second.setType(HolidayType.CUSTOM);
    second.setCustomExternalId(secondId);
    second.getHolidayDates().add(christmas);
    HolidayDocument secondDoc = new HolidayDocument(second);
    _holMaster.add(secondDoc);

    HolidaySearchRequest firstSearch = new HolidaySearchRequest();
    firstSearch.setType(HolidayType.CUSTOM);
    firstSearch.addCustomExternalId(firstId);
    HolidaySearchResult firstResult = _holMaster.search(firstSearch);

    assertEquals(firstResult.getDocuments().size(), 1);
    assertEquals(firstResult.getFirstHoliday().getType(), HolidayType.CUSTOM);
    assertEquals(firstResult.getFirstHoliday().customExternalId().get().getValue(), firstId.getValue());
    assertEquals(firstResult.getFirstHoliday().getHolidayDates().size(), 2);
    assertEquals(firstResult.getFirstHoliday().getHolidayDates().contains(mayDay), true);
    assertEquals(firstResult.getFirstHoliday().getHolidayDates().contains(christmas), false);

    firstResult.getFirstHoliday().getHolidayDates().add(christmas);
    assertEquals(firstResult.getFirstHoliday().getHolidayDates().contains(christmas), true);

    HolidaySearchRequest secondSearch = new HolidaySearchRequest();
    secondSearch.setType(HolidayType.CUSTOM);
    secondSearch.addCustomExternalId(secondId);
    HolidaySearchResult secondResult = _holMaster.search(secondSearch);

    assertEquals(secondResult.getDocuments().size(), 1);
    assertEquals(secondResult.getFirstHoliday().getType(), HolidayType.CUSTOM);
    assertEquals(secondResult.getFirstHoliday().customExternalId().get().getValue(), secondId.getValue());
    assertEquals(secondResult.getFirstHoliday().getHolidayDates().size(), 1);

    HolidaySearchRequest bothSearch = new HolidaySearchRequest();
    bothSearch.setType(HolidayType.CUSTOM);
    HolidaySearchResult bothResult = _holMaster.search(bothSearch);

    assertEquals(bothResult.getDocuments().size(), 2);
    assertEquals(bothResult.getFirstHoliday().getType(), HolidayType.CUSTOM);

  }


  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbHolidayMaster[DbHol]", _holMaster.toString());
  }

}
