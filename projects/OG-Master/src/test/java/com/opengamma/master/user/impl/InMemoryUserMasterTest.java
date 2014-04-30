/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataDuplicationException;
import com.opengamma.DataNotFoundException;
import com.opengamma.DataVersionException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryUserMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryUserMasterTest {

  private static final String TEST_USER_1 = "testuser1";
  private static final String TEST_USER_2 = "testuser2";
  private static final String EMAIL_ADDRESS = "info@opengamma.com";
  private static final ExternalId BLOOMBERG_SID = ExternalId.of("BloombergSid", "837283");
  private static final ExternalId OTHER_USER_ID1 = ExternalId.of("OtherUserId", "sk03e47s");
  private static final ExternalId OTHER_USER_ID2 = ExternalId.of("OtherUserId", "352378");
  private static final ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(BLOOMBERG_SID, OTHER_USER_ID1);

  private InMemoryUserMaster master;
  private ManageableUser addedUser1;
  private ManageableUser addedUser2;

  @BeforeMethod
  public void setUp() {
    master = new InMemoryUserMaster();
    addedUser1 = new ManageableUser(TEST_USER_1);
    addedUser1.setAlternateIds(BUNDLE_FULL);
    addedUser1.setEmailAddress(EMAIL_ADDRESS);
    UniqueId addedId1 = master.add(addedUser1);
    addedUser1.setUniqueId(addedId1);
    addedUser2 = new ManageableUser(TEST_USER_2);
    addedUser2.setAlternateIds(OTHER_USER_ID2.toBundle());
    UniqueId addedId2 = master.add(addedUser2);
    addedUser2.setUniqueId(addedId2);
  }

  //-------------------------------------------------------------------------
  public void test_getByName_match1() {
    ManageableUser result = master.getByName(TEST_USER_1);
    assertEquals(UniqueId.of("MemUsr", "1", "1"), result.getUniqueId());
    assertEquals(addedUser1, result);
  }

  public void test_getByName_match2() {
    ManageableUser result = master.getByName(TEST_USER_2);
    assertEquals(UniqueId.of("MemUsr", "2", "1"), result.getUniqueId());
    assertEquals(addedUser2, result);
  }

  public void test_getByName_matchCaseInsensitive() {
    ManageableUser result = master.getByName("TestUser1");
    assertEquals(UniqueId.of("MemUsr", "1", "1"), result.getUniqueId());
    assertEquals(addedUser1, result);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getByName_noMatch() {
    master.getByName("notfound");
  }

  //-------------------------------------------------------------------------
  public void test_getById_match() {
    ManageableUser result = master.getById(addedUser1.getObjectId());
    assertEquals(UniqueId.of("MemUsr", "1", "1"), result.getUniqueId());
    assertEquals(addedUser1, result);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getById_noMatch() {
    master.getById(ObjectId.of("A", "B"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_add() {
    ManageableUser user = new ManageableUser("newuser");
    UniqueId uniqueId = master.add(user);
    user.setUniqueId(uniqueId);
    assertEquals(user, master.getByName("newuser"));
    assertEquals(addedUser1, master.getByName(TEST_USER_1));
    assertEquals(addedUser2, master.getByName(TEST_USER_2));
  }

  @Test(expectedExceptions = DataDuplicationException.class)
  public void test_add_alreadyExists() {
    master.add(new ManageableUser(TEST_USER_1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_update() {
    ManageableUser updatedUser = addedUser1.clone();
    updatedUser.setPasswordHash("foo");
    UniqueId uniqueId = master.update(updatedUser);
    updatedUser.setUniqueId(uniqueId);
    assertEquals(UniqueId.of("MemUsr", "1", "2"), uniqueId);
    assertEquals(updatedUser, master.getByName(TEST_USER_1));
    assertEquals(addedUser2, master.getByName(TEST_USER_2));
  }

  @Test
  public void test_update_rename() {
    ManageableUser updatedUser = addedUser1.clone();
    updatedUser.setUserName("newuser");
    UniqueId uniqueId = master.update(updatedUser);
    updatedUser.setUniqueId(uniqueId);
    assertEquals(UniqueId.of("MemUsr", "1", "2"), uniqueId);
    assertEquals(updatedUser, master.getByName("newuser"));
    assertEquals(updatedUser, master.getByName(TEST_USER_1));
    assertEquals(addedUser2, master.getByName(TEST_USER_2));
  }

  @Test(expectedExceptions = DataVersionException.class)
  public void test_update_badVersion() {
    ManageableUser updatedUser = addedUser1.clone();
    updatedUser.setUniqueId(UniqueId.of("MemUsr", "1", "9"));
    master.update(updatedUser);
  }

  @Test(expectedExceptions = DataDuplicationException.class)
  public void test_update_rename_alreadyExists() {
    ManageableUser updatedUser = addedUser1.clone();
    updatedUser.setUserName(TEST_USER_2);
    master.update(updatedUser);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_removeByName() {
    master.removeByName(TEST_USER_1);
    assertEquals(addedUser2, master.getByName(TEST_USER_2));
    try {
      master.getByName(TEST_USER_1);
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    try {
      master.getById(addedUser1.getObjectId());
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeByName_notFound() {
    master.removeByName("notfound");
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_removeById() {
    master.removeById(addedUser1.getObjectId());
    assertEquals(addedUser2, master.getByName(TEST_USER_2));
    try {
      master.getById(addedUser1.getObjectId());
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    try {
      master.getByName(TEST_USER_1);
      fail();
    } catch (DataNotFoundException ex) {
      // expected
    }
    master.removeById(addedUser1.getObjectId());  // idempotent
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeById_notFound() {
    master.removeById(ObjectId.of("NOT", "FOUND"));
  }

}
