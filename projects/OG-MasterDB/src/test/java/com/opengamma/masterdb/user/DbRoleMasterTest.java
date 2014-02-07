/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import static com.google.common.collect.Sets.newHashSet;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.core.user.OGEntitlement;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.id.ExternalId;
import com.opengamma.master.user.ManageableOGRole;
import com.opengamma.master.user.RoleDocument;
import com.opengamma.master.user.RoleHistoryRequest;
import com.opengamma.master.user.RoleHistoryResult;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
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
public class DbRoleMasterTest extends AbstractDbTest {
  private static final Logger s_logger = LoggerFactory.getLogger(DbRoleMasterTest.class);

  final private TestFixture fixture;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbRoleMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    fixture = new TestFixture(databaseType, databaseVersion);
  }

  @Override
  public void doSetUp() throws Exception {
    fixture.init();
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
  public void test_users() throws Exception {
    UserDocument loaded = fixture._userMaster.get(fixture.insertedUser.getUniqueId());

    fixture._userMaster.setRoles(loaded, fixture.inserted_document_a, fixture.inserted_document_c);

    RoleSearchResult roleSearchResult = fixture._roleMaster.search(RoleSearchRequest.byUserUid(loaded.getUniqueId()));
    assertTrue(roleSearchResult.getRoles().contains(fixture.inserted_document_a.getRole()));
    assertTrue(roleSearchResult.getRoles().contains(fixture.inserted_document_c.getRole()));
    assertTrue(roleSearchResult.getDocuments().contains(fixture.inserted_document_a));
    assertTrue(roleSearchResult.getDocuments().contains(fixture.inserted_document_c));
  }

  @Test
  public void test_get_role_by_user_and_entitlement() throws Exception {
    UserDocument loaded = fixture._userMaster.get(fixture.insertedUser.getUniqueId());

    fixture._userMaster.setRoles(loaded, fixture.inserted_document_a, fixture.inserted_document_b);

    RoleSearchRequest roleSearchRequest = RoleSearchRequest.byUserUid(loaded.getUniqueId());
    roleSearchRequest.setResourceId(ExternalId.of("Prt", "p1").toString());
    roleSearchRequest.setResourceAccess(ResourceAccess.READ);
    RoleSearchResult roleSearchResult = fixture._roleMaster.search(roleSearchRequest);

    assertEquals(newHashSet(Arrays.asList(fixture.inserted_document_a.getRole(), fixture.inserted_document_b.getRole())), newHashSet(roleSearchResult.getRoles()));
  }

  @Override
  public void doTearDown() throws Exception {
    fixture._roleMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(fixture._roleMaster);
    assertEquals(true, fixture._roleMaster.getUniqueIdScheme().equals("DbUsr"));
    assertNotNull(fixture._roleMaster.getDbConnector());
    assertNotNull(fixture._roleMaster.getClock());
  }

  @Test
  public void test_entitlements() throws Exception {
    RoleDocument loaded = fixture._roleMaster.get(fixture.inserted_document_a.getUniqueId());
    assertEquals(fixture.inserted_document_a, loaded);
  }

  @Test
  public void test_search_roles_with_given_entitlements() throws Exception {

    RoleSearchRequest roleSearchRequest = new RoleSearchRequest();
    roleSearchRequest.setResourceId(ExternalId.of("Prt", "p1").toString());
    roleSearchRequest.setResourceAccess(ResourceAccess.READ);

    RoleSearchResult searchResult = fixture._roleMaster.search(roleSearchRequest);

    assertEquals(2, searchResult.getDocuments().size());
    assertEquals(newHashSet(Arrays.asList(fixture.role_a, fixture.role_b)), newHashSet(searchResult.getRoles()));
  }

  @Test
  public void test_search_roles_with_given_entitlements_and_name() throws Exception {

    RoleSearchRequest roleSearchRequest = new RoleSearchRequest();
    roleSearchRequest.setResourceId(ExternalId.of("Prt", "p1").toString());
    roleSearchRequest.setResourceAccess(ResourceAccess.READ);
    roleSearchRequest.setName("Role_A");

    RoleSearchResult searchResult = fixture._roleMaster.search(roleSearchRequest);

    assertEquals(1, searchResult.getDocuments().size());
    assertEquals(fixture.role_a, searchResult.getFirstRole());
  }

  @Test
  public void test_search_roles_with_nonexistent_entitlement() throws Exception {

    RoleSearchRequest roleSearchRequest = new RoleSearchRequest();
    roleSearchRequest.setResourceId(ExternalId.of("Prt", "p1").toString());
    roleSearchRequest.setResourceAccess(ResourceAccess.QUERY);

    RoleSearchResult searchResult = fixture._roleMaster.search(roleSearchRequest);

    assertEquals(searchResult.getDocuments().size(), 0);
  }

  @Test
  public void test_search_role_by_name() throws Exception {
    RoleSearchRequest roleSearchRequest = new RoleSearchRequest();
    roleSearchRequest.setName("Role*");
    assertEquals(3, fixture._roleMaster.search(roleSearchRequest).getDocuments().size());

    roleSearchRequest.setName("Role_A");
    assertEquals(1, fixture._roleMaster.search(roleSearchRequest).getDocuments().size());

    roleSearchRequest.setName("Role_B");
    assertEquals(1, fixture._roleMaster.search(roleSearchRequest).getDocuments().size());

    roleSearchRequest.setName("Role_ZZZ");
    assertEquals(0, fixture._roleMaster.search(roleSearchRequest).getDocuments().size());
  }

  @Test
  public void test_history() throws Exception {
    OGEntitlement ent1 = new OGEntitlement(ExternalId.of("Prt", "p3").toString(), "portfolio", ResourceAccess.READ);
    //OGEntitlement ent2 = new OGEntitlement(ExternalId.of("Prt", "p3").toString(), "portfolio", ResourceAccess.WRITE);
    //OGEntitlement ent3 = new OGEntitlement(ExternalId.of("Prt", "p3").toString(), "portfolio", ResourceAccess.QUERY);
    fixture.inserted_document_a.getRole().setEntitlements(ent1);
    fixture._roleMaster.replaceVersion(fixture.inserted_document_a);

    RoleHistoryRequest rhr = new RoleHistoryRequest();
    rhr.setObjectId(fixture.inserted_document_a.getObjectId());
    RoleHistoryResult result = fixture._roleMaster.history(rhr);

    assertEquals(2, result.getRoles().size());
    assertEquals(result.getRoles().get(0).getKey(), result.getRoles().get(1).getKey());
    assertEquals(result.getRoles().get(0).getName(), result.getRoles().get(1).getName());
    assertNotSame(result.getRoles().get(0).getUniqueId(), result.getRoles().get(1).getUniqueId());
    assertEquals(1, result.getRoles().get(0).getEntitlements().size()); // the recent one
    assertEquals(3, result.getRoles().get(1).getEntitlements().size()); // the previous one

    assertEquals(ResourceAccess.READ, result.getRoles().get(1).getEntitlements().iterator().next().getAccess());
    assertEquals(ResourceAccess.READ, result.getRoles().get(0).getEntitlements().iterator().next().getAccess());
    assertEquals("portfolio", result.getRoles().get(0).getEntitlements().iterator().next().getType());
    assertEquals(ExternalId.of("Prt", "p3").toString(), result.getRoles().get(0).getEntitlements().iterator().next().getResourceId());
  }

  protected RoleDocument addRole(String roleId, String name, String key, OGEntitlement... entitlements) {
    ManageableOGRole role = new ManageableOGRole(roleId);
    role.setEntitlements(newHashSet(entitlements));
    role.setKey(key);
    role.setName(name);

    RoleDocument addDoc = new RoleDocument(role);
    RoleDocument added = fixture._roleMaster.add(addDoc);
    return added;
  }

}
