/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Arrays;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.obligor.CreditRatingFitch;
import com.opengamma.core.obligor.CreditRatingMoodys;
import com.opengamma.core.obligor.CreditRatingStandardAndPoors;
import com.opengamma.core.obligor.Region;
import com.opengamma.core.obligor.Sector;
import com.opengamma.master.orgs.ManageableOrganisation;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.masterdb.orgs.DbOrganisationMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DbTest;

/**
 * Test DbOrganisationMaster.
 */
public class DbOrganisationMasterTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbOrganisationMasterTest.class);

  private DbOrganisationMaster _orgMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbOrganisationMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _orgMaster = (DbOrganisationMaster) context.getBean(getDatabaseType() + "DbOrganisationMaster");
  }

  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    _orgMaster = null;
  }

  @AfterSuite
  public static void closeAfterSuite() {
    DbMasterTestUtils.closeAfterSuite();
  }


  @Test
  public void test_basics() throws Exception {
    assertNotNull(_orgMaster);
    assertEquals(true, _orgMaster.getUniqueIdScheme().equals("DbOrg"));
    assertNotNull(_orgMaster.getDbConnector());
    assertNotNull(_orgMaster.getTimeSource());
  }


  @Test
  public void test_example() throws Exception {
    ManageableOrganisation org = new ManageableOrganisation("TestOrganisation101",
        "RED_code_101",
        "ticker_101",
        Region.AFRICA,
        "CountryA",
        Sector.BASICMATERIALS,
        CreditRating.A,
        CreditRating.A,
        CreditRatingFitch.A,
        CreditRatingMoodys.A,
        CreditRatingStandardAndPoors.A,
        false);
    OrganisationDocument addDoc = new OrganisationDocument(org);
    OrganisationDocument added = _orgMaster.add(addDoc);
    
    OrganisationDocument loaded = _orgMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }


  @Test
  public void test_toString() {
    assertEquals("DbOrganisationMaster[DbOrg]", _orgMaster.toString());
  }

}
