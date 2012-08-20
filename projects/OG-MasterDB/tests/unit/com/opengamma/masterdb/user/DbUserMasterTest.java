/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.UserDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DbTest;

/**
 * 
 */
public class DbUserMasterTest extends DbTest {
  private static final Logger s_logger = LoggerFactory.getLogger(DbUserMasterTest.class);

  private DbUserMaster _userMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbUserMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _userMaster = (DbUserMaster) context.getBean(getDatabaseType() + "DbUserMaster");
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _userMaster = null;
    super.tearDown();
  }

  @AfterSuite
  public static void closeAfterSuite() {
    DbMasterTestUtils.closeAfterSuite();
  }

  @Test
  public void test_basics() throws Exception {
    assertNotNull(_userMaster);
    assertEquals(true, _userMaster.getUniqueIdScheme().equals("DbUsr"));
    assertNotNull(_userMaster.getDbConnector());
    assertNotNull(_userMaster.getTimeSource());
  }

  @Test
  public void test_example() throws Exception {
    ManageableOGUser user = new ManageableOGUser();
    user.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D")));
    user.setName("Test");
    user.setPasswordHash("PASSWORD_HASH");
    UserDocument addDoc = new UserDocument(user);
    UserDocument added = _userMaster.add(addDoc);
    
    UserDocument loaded = _userMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }
}
