/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.obligor.CreditRatingFitch;
import com.opengamma.core.obligor.CreditRatingMoodys;
import com.opengamma.core.obligor.CreditRatingStandardAndPoors;
import com.opengamma.core.obligor.Region;
import com.opengamma.core.obligor.Sector;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.masterdb.orgs.DbOrganizationMaster;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbOrganizationMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbOrganizationMasterTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbOrganizationMasterTest.class);

  private DbOrganizationMaster _orgMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbOrganizationMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _orgMaster = new DbOrganizationMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _orgMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_orgMaster);
    assertEquals(true, _orgMaster.getUniqueIdScheme().equals("DbOrg"));
    assertNotNull(_orgMaster.getDbConnector());
    assertNotNull(_orgMaster.getClock());
  }


  @Test
  public void test_example() throws Exception {
    ManageableOrganization org = new ManageableOrganization("TestOrganization101",
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
    OrganizationDocument addDoc = new OrganizationDocument(org);
    OrganizationDocument added = _orgMaster.add(addDoc);
    
    OrganizationDocument loaded = _orgMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }


  @Test
  public void test_toString() {
    assertEquals("DbOrganizationMaster[DbOrg]", _orgMaster.toString());
  }

}
