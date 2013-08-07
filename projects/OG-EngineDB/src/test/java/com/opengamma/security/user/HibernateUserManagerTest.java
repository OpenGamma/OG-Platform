/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.security.user.Authority;
import com.opengamma.security.user.HibernateUserManager;
import com.opengamma.security.user.HibernateUserManagerFiles;
import com.opengamma.security.user.User;
import com.opengamma.security.user.UserGroup;
import com.opengamma.util.db.DbConnectorFactoryBean;
import com.opengamma.util.db.HibernateMappingFiles;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT_DB, singleThreaded = true)
public class HibernateUserManagerTest extends AbstractDbTest {

  private HibernateUserManager _userManager;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public HibernateUserManagerTest(String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Class<?> dbConnectorScope() {
    return HibernateUserManagerTest.class;
  }

  @Override
  protected void initDbConnectorFactory(DbConnectorFactoryBean factory) {
    factory.setHibernateMappingFiles(new HibernateMappingFiles[] {new HibernateUserManagerFiles() });
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _userManager = new HibernateUserManager(getDbConnector());
  }

  //-------------------------------------------------------------------------
  @Test
  public void testUserManagement() {
    // Try to get non-existent
    User user = _userManager.getUser("nonexistentuser");
    assertNull(user);
    
    // Clear DB as necessary
    user = _userManager.getUser("testuser");
    if (user != null) {
      _userManager.deleteUser(user);      
    }
    
    // Add
    user = getTestUser();
    Set<UserGroup> userGroups = new HashSet<UserGroup>();
    UserGroup userGroup = _userManager.getUserGroup("testusergroup");
    if (userGroup == null) {
      userGroup = new UserGroup(null, "testusergroup");
      userGroup.getAuthorities().add(new Authority("testauthority"));
    }
    userGroup.getUsers().add(user);
    user.setUserGroups(userGroups);
    _userManager.addUser(user);
    
    // Update
    user.setPassword("modifiedtestpw");
    _userManager.updateUser(user);
    user = _userManager.getUser("testuser"); 
    assertNotNull(user);
    assertTrue(user.checkPassword("modifiedtestpw"));
    
    // Delete
    _userManager.deleteUser(user);
    user = _userManager.getUser("testuser");
    assertNull(user);
  }

  private User getTestUser() {
    return new User(null, "testuser", "testpw", new HashSet<UserGroup>(), new Date());
  }

  @Test
  public void testUserGroupManagement() {
    // Try to get non-existent
    UserGroup userGroup = _userManager.getUserGroup("nonexistentusergroup");
    assertNull(userGroup);
    
    // Clear DB as necessary
    userGroup = _userManager.getUserGroup("testusergroup");
    if (userGroup != null) {
      _userManager.deleteUserGroup(userGroup);
    }
    
    // Add
    userGroup = new UserGroup(null, "testusergroup");
    
    Authority authority = _userManager.getAuthority("testauthority");
    if (authority == null) {
      authority = new Authority("testauthority");
      _userManager.addAuthority(authority);
    }
    userGroup.getAuthorities().add(authority);
    
    User user = _userManager.getUser("testuser");
    if (user == null) {
      user = getTestUser();
      _userManager.addUser(user);
    }
    
    _userManager.addUserGroup(userGroup);
    
    // Update
    Authority additionalAuthority = _userManager.getAuthority("additionalauthority"); 
    if (additionalAuthority == null) {
      additionalAuthority = new Authority("additionalauthority");
      _userManager.addAuthority(additionalAuthority);
    }
    userGroup.getAuthorities().add(additionalAuthority);
    _userManager.updateUserGroup(userGroup);
    userGroup = _userManager.getUserGroup("testusergroup");
    assertNotNull(userGroup);
    assertTrue(userGroup.getAuthorities().contains(additionalAuthority));
    
    // Delete
    _userManager.deleteUserGroup(userGroup);
    userGroup  = _userManager.getUserGroup("testusergroup");
    assertNull(userGroup);
  }

  @Test
  public void testAuthorityManagement() {
    // Try to get non-existent
    Authority authority = _userManager.getAuthority("nonexistentauthority");
    assertNull(authority);
    
    // Clear DB as necessary
    authority = _userManager.getAuthority("authority");
    if (authority != null) {
      _userManager.deleteAuthority(authority);
    }
    
    // Add
    authority = new Authority("regex");
    _userManager.addAuthority(authority);
    
    // Update
    authority.setRegex("newregex");
    _userManager.updateAuthority(authority);
    authority = _userManager.getAuthority("regex");
    assertNull(authority);
    authority = _userManager.getAuthority("newregex");
    assertNotNull(authority);
    assertEquals("newregex", authority.getRegex());
    
    // Delete
    _userManager.deleteAuthority(authority);
    authority = _userManager.getAuthority("newregex");
    assertNull(authority);
  }

}
