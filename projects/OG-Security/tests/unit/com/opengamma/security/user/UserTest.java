/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 *
 * @author pietari
 */
public class UserTest {
  
  @Test
  public void testPermissioning() {
    User user = new User();
    
    UserGroup group1 = new UserGroup(0L, "group1");
    group1.getAuthorities().add(new Authority(0L, "/Portfolio/123456/*"));
    
    UserGroup group2 = new UserGroup(1L, "group2");
    group2.getAuthorities().add(new Authority(1L, "/Portfolio/7890/Read"));
    
    user.getUserGroups().add(group1);
    user.getUserGroups().add(group2);
    
    Assert.assertTrue(user.hasPermission("/Portfolio/123456/Read"));
    Assert.assertTrue(user.hasPermission("/Portfolio/123456/Write"));
    Assert.assertTrue(user.hasPermission("/Portfolio/7890/Read"));
    Assert.assertFalse(user.hasPermission("/Portfolio/7890/Write"));
    Assert.assertFalse(user.hasPermission("/Portfolio/Foo/Read"));
  }
  
  @Test
  public void password() {
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
