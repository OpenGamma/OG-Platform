/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.core.user.OGUser;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.impl.MasterUserSource;
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
    ManageableOGUser user = new ManageableOGUser("Test");
    user.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D")));
    user.setPasswordHash("PASSWORD_HASH");
    user.getEntitlements().add("ENTITLEMENT-1");
    user.getEntitlements().add("ENTITLEMENT-2");
    user.getEntitlements().add("ENTITLEMENT-3");
    UserDocument addDoc = new UserDocument(user);
    UserDocument added = _userMaster.add(addDoc);
    
    UserDocument loaded = _userMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  @Test
  public void test_noid() throws Exception {
    ManageableOGUser user = new ManageableOGUser("Test");
    user.setPasswordHash("PASSWORD_HASH");
    user.getEntitlements().add("ENTITLEMENT-1");
    user.getEntitlements().add("ENTITLEMENT-2");
    user.getEntitlements().add("ENTITLEMENT-3");
    UserDocument addDoc = new UserDocument(user);
    UserDocument added = _userMaster.add(addDoc);
    
    UserDocument loaded = _userMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  @Test
  public void test_no_entitlements() throws Exception {
    ManageableOGUser user = new ManageableOGUser("Test");
    user.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D")));
    user.setPasswordHash("PASSWORD_HASH");
    UserDocument addDoc = new UserDocument(user);
    UserDocument added = _userMaster.add(addDoc);
    
    UserDocument loaded = _userMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  @Test
  public void test_multiple_users() throws Exception {
    UserDocument doc1 = addUser("user-1", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "1"), ExternalId.of("B", "1")), "E-1", "E-2");
    UserDocument doc2 = addUser("user-2", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "2"), ExternalId.of("B", "1")), "E-1", "E-2");
    /*UserDocument doc3 = */ addUser("user-3", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "3"), ExternalId.of("B", "1")), "E-2", "E-3");
    UserDocument doc4 = addUser("user-4", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "4"), ExternalId.of("B", "2")), "E-2", "E-3");
    UserDocument doc5 = addUser("user-5", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "5"), ExternalId.of("B", "2")), "E-4", "E-5");
    UserDocument doc6 = addUser("user-6", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "5"), ExternalId.of("B", "2")), "E-4", "E-5", "E-6", "E-7", "E-8");
    
    MasterUserSource source = new MasterUserSource(_userMaster);
    
    Collection<? extends OGUser> docs = source.getUsers(ExternalIdBundle.of(ExternalId.of("A", "1")), VersionCorrection.LATEST);
    assertNotNull(docs);
    assertEquals(1, docs.size());
    assertEquals(doc1.getUniqueId(), docs.iterator().next().getUniqueId());
    
    docs = source.getUsers(ExternalIdBundle.of(ExternalId.of("A", "5")), VersionCorrection.LATEST);
    assertNotNull(docs);
    assertEquals(2, docs.size());
    assertTrue("Docs was " + docs, docs.contains(doc5.getUser()));
    assertTrue("Docs was " + docs, docs.contains(doc6.getUser()));
    
    docs = source.getUsers(ExternalIdBundle.of(ExternalId.of("A", "5"), ExternalId.of("B", "2")), VersionCorrection.LATEST);
    assertNotNull(docs);
    assertEquals(3, docs.size());
    assertTrue("Docs was " + docs, docs.contains(doc4.getUser()));
    assertTrue("Docs was " + docs, docs.contains(doc5.getUser()));
    assertTrue("Docs was " + docs, docs.contains(doc6.getUser()));
    
    OGUser found = source.getUser("user-2", VersionCorrection.LATEST);
    assertNotNull(found);
    assertEquals(doc2.getUser(), found);
  }

  protected UserDocument addUser(String userId, String passwordHash, ExternalIdBundle idBundle, String... entitlements) {
    ManageableOGUser user = new ManageableOGUser(userId);
    user.setExternalIdBundle(idBundle);
    user.setPasswordHash(passwordHash);
    for (String entitlement : entitlements) {
      user.getEntitlements().add(entitlement);
    }
    UserDocument addDoc = new UserDocument(user);
    UserDocument added = _userMaster.add(addDoc);
    return added;
  }

}
