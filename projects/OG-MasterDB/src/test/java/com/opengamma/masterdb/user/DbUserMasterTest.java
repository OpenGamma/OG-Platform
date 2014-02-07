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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.core.user.OGUser;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbUserMasterTest extends AbstractDbTest {
  private static final Logger s_logger = LoggerFactory.getLogger(DbUserMasterTest.class);

  final private TestFixture fixture;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbUserMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    fixture = new TestFixture(databaseType, databaseVersion);
  }


  @Override
  public void doSetUp() throws Exception {
    fixture.init();
  }

  @Override
  public void doTearDown() throws Exception {
    fixture._userMaster = null;
    super.tearDown();
  }
  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(fixture._userMaster);
    assertEquals(true, fixture._userMaster.getUniqueIdScheme().equals("DbUsr"));
    assertNotNull(fixture._userMaster.getDbConnector());
    assertNotNull(fixture._userMaster.getClock());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    UserDocument loaded = fixture._userMaster.get(fixture.insertedUser.getUniqueId());
    assertEquals(fixture.insertedUser, loaded);
  }

  @Test
  public void test_roles() throws Exception {
    UserDocument loaded = fixture._userMaster.get(fixture.insertedUser.getUniqueId());

    fixture._userMaster.setRoles(loaded, fixture.inserted_document_a, fixture.inserted_document_c);
    UserSearchResult userSearchResult_a = fixture._userMaster.search(UserSearchRequest.byRoleOid(fixture.inserted_document_a.getUniqueId().getObjectId()));
    UserSearchResult userSearchResult_b = fixture._userMaster.search(UserSearchRequest.byRoleOid(fixture.inserted_document_c.getUniqueId().getObjectId()));
    assertTrue(userSearchResult_a.getUsers().contains(loaded.getUser()));
    assertTrue(userSearchResult_b.getUsers().contains(loaded.getUser()));
  }

  @Test
  public void test_noid() throws Exception {
    ManageableOGUser user = new ManageableOGUser("Test");
    user.setPasswordHash("PASSWORD_HASH");
    UserDocument addDoc = new UserDocument(user);
    UserDocument added = fixture._userMaster.add(addDoc);

    UserDocument loaded = fixture._userMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  @Test
  public void test_no_entitlements() throws Exception {
    ManageableOGUser user = new ManageableOGUser("Test");
    user.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D")));
    user.setPasswordHash("PASSWORD_HASH");
    UserDocument addDoc = new UserDocument(user);
    UserDocument added = fixture._userMaster.add(addDoc);

    UserDocument loaded = fixture._userMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_multiple_users() throws Exception {
    UserDocument doc1 = addUser("user-1", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "1"), ExternalId.of("B", "1")), "E-1", "E-2");
    UserDocument doc2 = addUser("user-2", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "2"), ExternalId.of("B", "1")), "E-1", "E-2");
    /*UserDocument doc3 = */
    addUser("user-3", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "3"), ExternalId.of("B", "1")), "E-2", "E-3");
    UserDocument doc4 = addUser("user-4", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "4"), ExternalId.of("B", "2")), "E-2", "E-3");
    UserDocument doc5 = addUser("user-5", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "5"), ExternalId.of("B", "2")), "E-4", "E-5");
    UserDocument doc6 = addUser("user-6", "pw-1", ExternalIdBundle.of(ExternalId.of("A", "5"), ExternalId.of("B", "2")), "E-4", "E-5", "E-6", "E-7", "E-8");

    UserDocument user = fixture._userMaster.get(doc6.getUniqueId());
    assertNotNull(user);

    Collection<? extends OGUser> users = findUsers(ExternalIdBundle.of(ExternalId.of("A", "1")), VersionCorrection.LATEST);
    assertNotNull(users);
    assertEquals(1, users.size());
    assertEquals(doc1.getUniqueId(), users.iterator().next().getUniqueId());

    users = findUsers(ExternalIdBundle.of(ExternalId.of("A", "5")), VersionCorrection.LATEST);
    assertNotNull(users);
    assertEquals(2, users.size());
    assertTrue("Docs was " + users, users.contains(doc5.getUser()));
    assertTrue("Docs was " + users, users.contains(doc6.getUser()));

    users = findUsers(ExternalIdBundle.of(ExternalId.of("A", "5"), ExternalId.of("B", "2")), VersionCorrection.LATEST);
    user = fixture._userMaster.get(doc6.getUniqueId());
    assertNotNull(users);
    assertEquals(3, users.size());
    assertTrue("Docs was " + users, users.contains(doc4.getUser()));
    assertTrue("Docs was " + users, users.contains(doc5.getUser()));
    assertTrue("Docs was " + users + doc6, users.contains(doc6.getUser()));

    UserSearchRequest searchRequest = new UserSearchRequest();
    searchRequest.setUsername("user-2");
    searchRequest.setVersionCorrection(VersionCorrection.LATEST);
    UserSearchResult result = fixture._userMaster.search(searchRequest);
    assertEquals(1, result.getDocuments().size());
    assertEquals(result.getFirstUser(), doc2.getUser());
  }

  private List<ManageableOGUser> findUsers(ExternalIdBundle bundle, VersionCorrection vc) {
    UserSearchRequest searchRequest = new UserSearchRequest(bundle);
    searchRequest.setVersionCorrection(vc);
    return fixture._userMaster.search(searchRequest).getUsers();
  }

  protected UserDocument addUser(String userId, String passwordHash, ExternalIdBundle idBundle, String... roles) {
    ManageableOGUser user = new ManageableOGUser(userId);
    user.setExternalIdBundle(idBundle);
    user.setPasswordHash(passwordHash);
    UserDocument addDoc = new UserDocument(user);
    UserDocument added = fixture._userMaster.add(addDoc);
    return added;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbUserMaster[DbUsr]", fixture._userMaster.toString());
  }

}
