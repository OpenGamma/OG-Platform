/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.DataDuplicationException;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.impl.SimpleUserAccount;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.HistoryEventType;
import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.RoleEventHistoryRequest;
import com.opengamma.master.user.RoleEventHistoryResult;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbRoleMasterTest extends AbstractDbTest {
  private static final Logger s_logger = LoggerFactory.getLogger(DbRoleMasterTest.class);
  private static final String TEST_ROLE = "one";
  private static final String TEST_ROLE2 = "two";

  private DbRoleMaster _roleMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbRoleMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _roleMaster = new DbRoleMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _roleMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_roleMaster);
    assertEquals(true, _roleMaster.getUniqueIdScheme().equals("DbUsrRole"));
    assertNotNull(_roleMaster.getDbConnector());
    assertNotNull(_roleMaster.getClock());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    ManageableRole role = createRole();
    assertEquals(false, _roleMaster.nameExists(role.getRoleName()));
    UniqueId uid = _roleMaster.add(role);
    assertEquals(true, _roleMaster.nameExists(role.getRoleName()));
    role.setUniqueId(uid);
    ManageableRole loaded = _roleMaster.getById(uid.getObjectId());
    assertEquals(role, loaded);
  }

  @Test
  public void test_no_users() throws Exception {
    ManageableRole role = createRole();
    role.getAssociatedUsers().clear();;
    UniqueId uid = _roleMaster.add(role);
    role.setUniqueId(uid);
    ManageableRole loaded = _roleMaster.getById(uid.getObjectId());
    assertEquals(role, loaded);
  }

  @Test
  public void test_no_permissions() throws Exception {
    ManageableRole role = createRole();
    role.getAssociatedPermissions().clear();
    UniqueId uid = _roleMaster.add(role);
    role.setUniqueId(uid);
    ManageableRole loaded = _roleMaster.getById(uid.getObjectId());
    assertEquals(role, loaded);
  }

  @Test
  public void test_no_roles() throws Exception {
    ManageableRole role = createRole();
    role.getAssociatedRoles().clear();
    UniqueId uid = _roleMaster.add(role);
    role.setUniqueId(uid);
    ManageableRole loaded = _roleMaster.getById(uid.getObjectId());
    assertEquals(role, loaded);
  }

  @Test
  public void test_no_subTables() throws Exception {
    ManageableRole role = createRole();
    role.getAssociatedUsers().clear();
    role.getAssociatedPermissions().clear();
    role.getAssociatedRoles().clear();
    UniqueId uid = _roleMaster.add(role);
    role.setUniqueId(uid);
    ManageableRole loaded = _roleMaster.getById(uid.getObjectId());
    assertEquals(role, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_addNameExists() throws Exception {
    ManageableRole role = createRole();
    _roleMaster.add(role);
    assertNotNull(_roleMaster.getByName(TEST_ROLE));
    try {
      _roleMaster.add(role);
      fail();
    } catch (DataDuplicationException ex) {
      // expected
    }
    assertNotNull(_roleMaster.getByName(TEST_ROLE));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_removeByName() throws Exception {
    ManageableRole role = createRole();
    UniqueId uid = _roleMaster.add(role);
    assertNotNull(_roleMaster.getById(uid.getObjectId()));
    assertNotNull(_roleMaster.getByName(TEST_ROLE));
    _roleMaster.removeByName(TEST_ROLE);
    try {
      _roleMaster.getByName(TEST_ROLE);
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    try {
      _roleMaster.getById(uid.getObjectId());
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    _roleMaster.removeByName(TEST_ROLE);  // idempotent
    _roleMaster.removeById(uid.getObjectId());  // idempotent
    
    RoleEventHistoryResult events = _roleMaster.eventHistory(new RoleEventHistoryRequest(TEST_ROLE));
    assertEquals(2, events.getEvents().size());
    assertEquals(HistoryEventType.ADDED, events.getEvents().get(0).getType());
    assertEquals(0, events.getEvents().get(0).getChanges().size());
    assertEquals(HistoryEventType.REMOVED, events.getEvents().get(1).getType());
    assertEquals(0, events.getEvents().get(1).getChanges().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_removeById() throws Exception {
    ManageableRole role = createRole();
    UniqueId uid = _roleMaster.add(role);
    assertNotNull(_roleMaster.getById(uid.getObjectId()));
    assertNotNull(_roleMaster.getByName(TEST_ROLE));
    _roleMaster.removeById(uid.getObjectId());
    try {
      _roleMaster.getById(uid.getObjectId());
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    try {
      _roleMaster.getByName(TEST_ROLE);
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    _roleMaster.removeById(uid.getObjectId());  // idempotent
    _roleMaster.removeByName(TEST_ROLE);  // idempotent
    
    RoleEventHistoryResult events = _roleMaster.eventHistory(new RoleEventHistoryRequest(uid.getObjectId()));
    assertEquals(2, events.getEvents().size());
    assertEquals(HistoryEventType.ADDED, events.getEvents().get(0).getType());
    assertEquals(0, events.getEvents().get(0).getChanges().size());
    assertEquals(HistoryEventType.REMOVED, events.getEvents().get(1).getType());
    assertEquals(0, events.getEvents().get(1).getChanges().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_update_main() throws Exception {
    ManageableRole role = createRole();
    UniqueId uid1 = _roleMaster.add(role);
    role = _roleMaster.getById(uid1.getObjectId());
    role.setDescription("tester");
    UniqueId uid2 = _roleMaster.update(role);
    role.setUniqueId(uid2);
    ManageableRole loaded = _roleMaster.getById(uid2.getObjectId());
    assertEquals(role, loaded);
  }

  @Test
  public void test_update_linked() throws Exception {
    ManageableRole role = createRole();
    UniqueId uid1 = _roleMaster.add(role);
    role = _roleMaster.getById(uid1.getObjectId());
    role.getAssociatedUsers().remove("adam");
    role.getAssociatedUsers().add("david");
    role.getAssociatedPermissions().remove("PERMISSION-2");
    role.getAssociatedPermissions().add("NEWPERMISSION");
    role.getAssociatedRoles().add("two");
    UniqueId uid2 = _roleMaster.update(role);
    role.setUniqueId(uid2);
    ManageableRole loaded = _roleMaster.getById(uid2.getObjectId());
    assertEquals(role, loaded);
    
    RoleEventHistoryResult events = _roleMaster.eventHistory(new RoleEventHistoryRequest(uid2.getObjectId()));
    assertEquals(2, events.getEvents().size());
    assertEquals(HistoryEventType.ADDED, events.getEvents().get(0).getType());
    assertEquals(0, events.getEvents().get(0).getChanges().size());
    assertEquals(HistoryEventType.CHANGED, events.getEvents().get(1).getType());
    assertEquals(5, events.getEvents().get(1).getChanges().size());
  }

  @Test
  public void test_update_rename_succeed() throws Exception {
    ManageableRole role = createRole();
    UniqueId uid1 = _roleMaster.add(role);
    role = _roleMaster.getById(uid1.getObjectId());
    
    role.setRoleName("three");
    UniqueId uid2 = _roleMaster.update(role);
    role.setUniqueId(uid2);
    
    assertEquals(role, _roleMaster.getById(uid2.getObjectId()));
    assertEquals(role, _roleMaster.getByName(TEST_ROLE));
    assertEquals(role, _roleMaster.getByName("three"));
  }

  @Test
  public void test_update_rename_fail() throws Exception {
    ManageableRole role1 = createRole();
    UniqueId uid1 = _roleMaster.add(role1);
    role1 = _roleMaster.getById(uid1.getObjectId());
    ManageableRole role2 = createRole2();
    UniqueId uid2 = _roleMaster.add(role2);
    role2 = _roleMaster.getById(uid2.getObjectId());
    
    role2.setRoleName(TEST_ROLE);
    try {
      _roleMaster.update(role2);
      fail();
    } catch (DataDuplicationException ex) {
      // expected
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search() throws Exception {
    ManageableRole role1 = createRole();
    UniqueId uid1 = _roleMaster.add(role1);
    role1 = _roleMaster.getById(uid1.getObjectId());
    ManageableRole role2 = createRole2();
    UniqueId uid2 = _roleMaster.add(role2);
    role2 = _roleMaster.getById(uid2.getObjectId());
    
    RoleSearchResult result = _roleMaster.search(new RoleSearchRequest());
    assertEquals(2, result.getRoles().size());
    assertEquals(role1, result.getRoles().get(0));
    assertEquals(role2, result.getRoles().get(1));
  }

  @Test
  public void test_search_noObjectIds() throws Exception {
    ManageableRole role = createRole();
    UniqueId uid1 = _roleMaster.add(role);
    role = _roleMaster.getById(uid1.getObjectId());
    
    RoleSearchRequest request = new RoleSearchRequest();
    request.setObjectIds(ImmutableList.<ObjectId>of());
    RoleSearchResult result = _roleMaster.search(request);
    assertEquals(0, result.getRoles().size());
  }

  @Test
  public void test_search_objectIdNotFound() throws Exception {
    ManageableRole role = createRole();
    UniqueId uid1 = _roleMaster.add(role);
    role = _roleMaster.getById(uid1.getObjectId());
    
    RoleSearchRequest request = new RoleSearchRequest();
    request.setObjectIds(ImmutableList.of(ObjectId.of(_roleMaster.getUniqueIdScheme(), "-87578")));
    RoleSearchResult result = _roleMaster.search(request);
    assertEquals(0, result.getRoles().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_resolveAccount() throws Exception {
    ManageableRole roleA = new ManageableRole("A");
    roleA.getAssociatedUsers().add("adam");
    roleA.getAssociatedPermissions().add("PERMISSION-1");
    roleA.getAssociatedPermissions().add("PERMISSION-2");
    roleA.getAssociatedRoles().add("AA");
    roleA.getAssociatedRoles().add("AB");
    _roleMaster.add(roleA);
    
    ManageableRole roleAA = new ManageableRole("AA");
    roleAA.getAssociatedUsers().add("charles");
    roleAA.getAssociatedPermissions().add("PERMISSION-1");
    roleAA.getAssociatedPermissions().add("PERMISSION-3");
    roleAA.getAssociatedRoles().add("AAA");
    roleAA.getAssociatedRoles().add("AB");
    _roleMaster.add(roleAA);
    
    ManageableRole roleAAA = new ManageableRole("AAA");
    roleAAA.getAssociatedPermissions().add("PERMISSION-4");
    _roleMaster.add(roleAAA);
    
    ManageableRole roleAB = new ManageableRole("AB");
    roleAB.getAssociatedUsers().add("adam");
    roleAB.getAssociatedPermissions().add("PERMISSION-3");
    roleAB.getAssociatedPermissions().add("PERMISSION-4");
    roleAB.getAssociatedRoles().add("ABA");
    _roleMaster.add(roleAB);
    
    ManageableRole roleABA = new ManageableRole("ABA");
    roleABA.getAssociatedPermissions().add("PERMISSION-5");
    _roleMaster.add(roleABA);
    
    ManageableRole roleB = new ManageableRole("B");
    roleB.getAssociatedUsers().add("charles");
    roleB.getAssociatedPermissions().add("PERMISSION-6");
    _roleMaster.add(roleB);
    
    ManageableRole roleC = new ManageableRole("C");
    roleC.getAssociatedUsers().add("adam");
    roleC.getAssociatedPermissions().add("PERMISSION-7");
    _roleMaster.add(roleC);
    
    SimpleUserAccount account1 = new SimpleUserAccount("adam");
    account1.setEmailAddress("adam@test.com");
    UserAccount resolved1 = _roleMaster.resolveAccount(account1);
    assertEquals(6, resolved1.getRoles().size());
    assertEquals(true, resolved1.getRoles().contains("A"));
    assertEquals(true, resolved1.getRoles().contains("AA"));
    assertEquals(true, resolved1.getRoles().contains("AAA"));
    assertEquals(true, resolved1.getRoles().contains("AB"));
    assertEquals(true, resolved1.getRoles().contains("ABA"));
    assertEquals(true, resolved1.getRoles().contains("C"));
    assertEquals(6, resolved1.getPermissions().size());
    assertEquals(true, resolved1.getPermissions().contains("PERMISSION-1"));
    assertEquals(true, resolved1.getPermissions().contains("PERMISSION-2"));
    assertEquals(true, resolved1.getPermissions().contains("PERMISSION-3"));
    assertEquals(true, resolved1.getPermissions().contains("PERMISSION-4"));
    assertEquals(true, resolved1.getPermissions().contains("PERMISSION-5"));
    assertEquals(true, resolved1.getPermissions().contains("PERMISSION-7"));
    
    SimpleUserAccount account2 = new SimpleUserAccount("charles");
    account2.setEmailAddress("charles@test.com");
    UserAccount resolved2 = _roleMaster.resolveAccount(account2);
    assertEquals(5, resolved2.getRoles().size());
    assertEquals(true, resolved2.getRoles().contains("AA"));
    assertEquals(true, resolved2.getRoles().contains("AAA"));
    assertEquals(true, resolved2.getRoles().contains("AB"));
    assertEquals(true, resolved2.getRoles().contains("ABA"));
    assertEquals(true, resolved2.getRoles().contains("B"));
    assertEquals(5, resolved2.getPermissions().size());
    assertEquals(true, resolved2.getPermissions().contains("PERMISSION-1"));
    assertEquals(true, resolved2.getPermissions().contains("PERMISSION-3"));
    assertEquals(true, resolved2.getPermissions().contains("PERMISSION-4"));
    assertEquals(true, resolved2.getPermissions().contains("PERMISSION-5"));
    assertEquals(true, resolved2.getPermissions().contains("PERMISSION-6"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbRoleMaster[DbUsrRole]", _roleMaster.toString());
  }

  //-------------------------------------------------------------------------
  private ManageableRole createRole() {
    ManageableRole role = new ManageableRole(TEST_ROLE);
    role.setDescription("foobar");
    role.getAssociatedUsers().add("adam");
    role.getAssociatedUsers().add("charles");
    role.getAssociatedPermissions().add("PERMISSION-1");
    role.getAssociatedPermissions().add("PERMISSION-2");
    role.getAssociatedPermissions().add("PERMISSION-3");
    role.getAssociatedRoles().add("zero");
    return role;
  }

  private ManageableRole createRole2() {
    ManageableRole role = new ManageableRole(TEST_ROLE2);
    role.setDescription("foo");
    role.getAssociatedUsers().add("adam");
    role.getAssociatedUsers().add("charles");
    role.getAssociatedPermissions().add("PERMISSION-1");
    role.getAssociatedPermissions().add("PERMISSION-2");
    role.getAssociatedRoles().add("zero");
    return role;
  }

}
