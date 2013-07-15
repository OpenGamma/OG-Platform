/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class UserTest {

  public void testPermissioning() {
    User user = new User();
    
    UserGroup group1 = new UserGroup(0L, "group1");
    group1.getAuthorities().add(new Authority(0L, "/Portfolio/123456/*"));
    
    UserGroup group2 = new UserGroup(1L, "group2");
    group2.getAuthorities().add(new Authority(1L, "/Portfolio/7890/Read"));
    
    user.getUserGroups().add(group1);
    user.getUserGroups().add(group2);
    
    assertTrue(user.hasPermission("/Portfolio/123456/Read"));
    assertTrue(user.hasPermission("/Portfolio/123456/Write"));
    assertTrue(user.hasPermission("/Portfolio/7890/Read"));
    assertFalse(user.hasPermission("/Portfolio/7890/Write"));
    assertFalse(user.hasPermission("/Portfolio/Foo/Read"));
  }

  public void testPassword() {
    String password = "crpty&@\uFFFD9,3 % (4/10)";
    User user = new User();
    user.setPassword(password);
    try {
      user.getPassword();
      fail();
    } catch (UnsupportedOperationException e) {
    }
    assertFalse(password.equals(user.getPasswordHash()));
    assertTrue(user.checkPassword(password));
    assertFalse(user.checkPassword("goog"));
  }

}
