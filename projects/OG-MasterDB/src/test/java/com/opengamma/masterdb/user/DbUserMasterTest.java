/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.google.common.collect.ImmutableList;
import com.opengamma.DataDuplicationException;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.user.DateStyle;
import com.opengamma.core.user.TimeStyle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.HistoryEventType;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserEventHistoryResult;
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
  private static final String TEST_USER = "bob";
  private static final String TEST_USER2 = "david";

  private DbUserMaster _userMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbUserMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _userMaster = new DbUserMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _userMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_userMaster);
    assertEquals(true, _userMaster.getUniqueIdScheme().equals("DbUsr"));
    assertNotNull(_userMaster.getDbConnector());
    assertNotNull(_userMaster.getClock());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    ManageableUser user = createUser();
    assertEquals(false, _userMaster.nameExists(user.getUserName()));
    UniqueId uid = _userMaster.add(user);
    assertEquals(true, _userMaster.nameExists(user.getUserName()));
    user.setUniqueId(uid);
    ManageableUser loaded = _userMaster.getById(uid.getObjectId());
    assertEquals(user, loaded);
  }

  @Test
  public void test_no_alternateId() throws Exception {
    ManageableUser user = createUser();
    user.setAlternateIds(ExternalIdBundle.EMPTY);
    UniqueId uid = _userMaster.add(user);
    user.setUniqueId(uid);
    ManageableUser loaded = _userMaster.getById(uid.getObjectId());
    assertEquals(user, loaded);
  }

  @Test
  public void test_no_permissions() throws Exception {
    ManageableUser user = createUser();
    user.getAssociatedPermissions().clear();
    UniqueId uid = _userMaster.add(user);
    user.setUniqueId(uid);
    ManageableUser loaded = _userMaster.getById(uid.getObjectId());
    assertEquals(user, loaded);
  }

  @Test
  public void test_no_extensions() throws Exception {
    ManageableUser user = createUser();
    user.getProfile().getExtensions().clear();
    UniqueId uid = _userMaster.add(user);
    user.setUniqueId(uid);
    ManageableUser loaded = _userMaster.getById(uid.getObjectId());
    assertEquals(user, loaded);
  }

  @Test
  public void test_no_subTables() throws Exception {
    ManageableUser user = createUser();
    user.setAlternateIds(ExternalIdBundle.EMPTY);
    user.getAssociatedPermissions().clear();
    user.getProfile().getExtensions().clear();
    UniqueId uid = _userMaster.add(user);
    user.setUniqueId(uid);
    ManageableUser loaded = _userMaster.getById(uid.getObjectId());
    assertEquals(user, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_addNameExists() throws Exception {
    ManageableUser user = createUser();
    _userMaster.add(user);
    assertNotNull(_userMaster.getByName(TEST_USER));
    try {
      _userMaster.add(user);
      fail();
    } catch (DataDuplicationException ex) {
      // expected
    }
    assertNotNull(_userMaster.getByName(TEST_USER));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_removeByName() throws Exception {
    ManageableUser user = createUser();
    UniqueId uid = _userMaster.add(user);
    assertNotNull(_userMaster.getById(uid.getObjectId()));
    assertNotNull(_userMaster.getByName(TEST_USER));
    _userMaster.removeByName(TEST_USER);
    try {
      _userMaster.getByName(TEST_USER);
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    try {
      _userMaster.getById(uid.getObjectId());
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    _userMaster.removeByName(TEST_USER);  // idempotent
    _userMaster.removeById(uid.getObjectId());  // idempotent
    
    UserEventHistoryResult events = _userMaster.eventHistory(new UserEventHistoryRequest(TEST_USER));
    assertEquals(2, events.getEvents().size());
    assertEquals(HistoryEventType.ADDED, events.getEvents().get(0).getType());
    assertEquals(0, events.getEvents().get(0).getChanges().size());
    assertEquals(HistoryEventType.REMOVED, events.getEvents().get(1).getType());
    assertEquals(0, events.getEvents().get(1).getChanges().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_removeById() throws Exception {
    ManageableUser user = createUser();
    UniqueId uid = _userMaster.add(user);
    assertNotNull(_userMaster.getById(uid.getObjectId()));
    assertNotNull(_userMaster.getByName(TEST_USER));
    _userMaster.removeById(uid.getObjectId());
    try {
      _userMaster.getById(uid.getObjectId());
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    try {
      _userMaster.getByName(TEST_USER);
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    _userMaster.removeById(uid.getObjectId());  // idempotent
    _userMaster.removeByName(TEST_USER);  // idempotent
    
    UserEventHistoryResult events = _userMaster.eventHistory(new UserEventHistoryRequest(uid.getObjectId()));
    assertEquals(2, events.getEvents().size());
    assertEquals(HistoryEventType.ADDED, events.getEvents().get(0).getType());
    assertEquals(0, events.getEvents().get(0).getChanges().size());
    assertEquals(HistoryEventType.REMOVED, events.getEvents().get(1).getType());
    assertEquals(0, events.getEvents().get(1).getChanges().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_update_main() throws Exception {
    ManageableUser user = createUser();
    UniqueId uid1 = _userMaster.add(user);
    user = _userMaster.getById(uid1.getObjectId());
    user.setEmailAddress("tester@test.com");
    UniqueId uid2 = _userMaster.update(user);
    user.setUniqueId(uid2);
    ManageableUser loaded = _userMaster.getById(uid2.getObjectId());
    assertEquals(user, loaded);
  }

  @Test
  public void test_update_linked() throws Exception {
    ManageableUser user = createUser();
    UniqueId uid1 = _userMaster.add(user);
    user = _userMaster.getById(uid1.getObjectId());
    user.getAssociatedPermissions().remove("PERMISSION-2");
    user.getAssociatedPermissions().add("NEWPERMISSION");
    user.getProfile().getExtensions().remove("A");
    user.getProfile().getExtensions().put("X", "Y");
    user.addAlternateId(ExternalId.of("M", "N"));
    UniqueId uid2 = _userMaster.update(user);
    user.setUniqueId(uid2);
    ManageableUser loaded = _userMaster.getById(uid2.getObjectId());
    assertEquals(user, loaded);
    
    UserEventHistoryResult events = _userMaster.eventHistory(new UserEventHistoryRequest(uid2.getObjectId()));
    assertEquals(2, events.getEvents().size());
    assertEquals(HistoryEventType.ADDED, events.getEvents().get(0).getType());
    assertEquals(0, events.getEvents().get(0).getChanges().size());
    assertEquals(HistoryEventType.CHANGED, events.getEvents().get(1).getType());
    assertEquals(5, events.getEvents().get(1).getChanges().size());
  }

  @Test
  public void test_update_rename_succeed() throws Exception {
    ManageableUser user = createUser();
    UniqueId uid1 = _userMaster.add(user);
    user = _userMaster.getById(uid1.getObjectId());
    
    user.setUserName("bobjones");
    UniqueId uid2 = _userMaster.update(user);
    user.setUniqueId(uid2);
    
    assertEquals(user, _userMaster.getById(uid2.getObjectId()));
    assertEquals(user, _userMaster.getByName(TEST_USER));
    assertEquals(user, _userMaster.getByName("bobjones"));
  }

  @Test
  public void test_update_rename_fail() throws Exception {
    ManageableUser user1 = createUser();
    UniqueId uid1 = _userMaster.add(user1);
    user1 = _userMaster.getById(uid1.getObjectId());
    ManageableUser user2 = createUser2();
    UniqueId uid2 = _userMaster.add(user2);
    user2 = _userMaster.getById(uid2.getObjectId());
    
    user2.setUserName(TEST_USER);
    try {
      _userMaster.update(user2);
      fail();
    } catch (DataDuplicationException ex) {
      // expected
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search() throws Exception {
    ManageableUser user1 = createUser();
    UniqueId uid1 = _userMaster.add(user1);
    user1 = _userMaster.getById(uid1.getObjectId());
    ManageableUser user2 = createUser2();
    UniqueId uid2 = _userMaster.add(user2);
    user2 = _userMaster.getById(uid2.getObjectId());
    
    UserSearchResult result = _userMaster.search(new UserSearchRequest());
    assertEquals(2, result.getUsers().size());
    assertEquals(user1, result.getUsers().get(0));
    assertEquals(user2, result.getUsers().get(1));
  }

  @Test
  public void test_search_noObjectIds() throws Exception {
    ManageableUser user = createUser();
    UniqueId uid1 = _userMaster.add(user);
    user = _userMaster.getById(uid1.getObjectId());
    
    UserSearchRequest request = new UserSearchRequest();
    request.setObjectIds(ImmutableList.<ObjectId>of());
    UserSearchResult result = _userMaster.search(request);
    assertEquals(0, result.getUsers().size());
  }

  @Test
  public void test_search_objectIdNotFound() throws Exception {
    ManageableUser user = createUser();
    UniqueId uid1 = _userMaster.add(user);
    user = _userMaster.getById(uid1.getObjectId());
    
    UserSearchRequest request = new UserSearchRequest();
    request.setObjectIds(ImmutableList.of(ObjectId.of(_userMaster.getUniqueIdScheme(), "-87578")));
    UserSearchResult result = _userMaster.search(request);
    assertEquals(0, result.getUsers().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbUserMaster[DbUsr]", _userMaster.toString());
  }

  //-------------------------------------------------------------------------
  private ManageableUser createUser() {
    ManageableUser user = new ManageableUser(TEST_USER);
    user.setAlternateIds(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D")));
    user.setPasswordHash("PASSWORD_HASH");
    user.setEmailAddress("bob@test.com");
    user.getAssociatedPermissions().add("PERMISSION-1");
    user.getAssociatedPermissions().add("PERMISSION-2");
    user.getAssociatedPermissions().add("PERMISSION-3");
    user.getProfile().setDisplayName("Bob");
    user.getProfile().setLocale(Locale.FRANCE);
    user.getProfile().setZone(ZoneId.of("Europe/Paris"));
    user.getProfile().setDateStyle(DateStyle.STANDARD_EU);
    user.getProfile().setTimeStyle(TimeStyle.ISO);
    user.getProfile().getExtensions().put("A", "B");
    user.getProfile().getExtensions().put("C", "D");
    return user;
  }

  private ManageableUser createUser2() {
    ManageableUser user = new ManageableUser(TEST_USER2);
    user.setAlternateIds(ExternalIdBundle.of(ExternalId.of("A", "BB"), ExternalId.of("C", "DD")));
    user.setPasswordHash("PASSWORD_HASH");
    user.setEmailAddress("david@test.com");
    user.getAssociatedPermissions().add("PERMISSION-1");
    user.getAssociatedPermissions().add("PERMISSION-2");
    user.getProfile().setDisplayName("David");
    user.getProfile().setLocale(Locale.UK);
    user.getProfile().setZone(ZoneId.of("Europe/London"));
    user.getProfile().setDateStyle(DateStyle.ISO);
    user.getProfile().setTimeStyle(TimeStyle.ISO);
    user.getProfile().getExtensions().put("A", "B");
    return user;
  }

}
