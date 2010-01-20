/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import org.junit.Assert;
import org.junit.Test;

import com.opengamma.security.user.Authority;
import com.opengamma.security.user.User;
import com.opengamma.security.user.UserGroup;

/**
 * 
 *
 * @author pietari
 */
public class UserTest {
  
  @Test
  public void testPermissioning() {
    User user = new User();
    
    UserGroup group1 = new UserGroup("group1");
    group1.getAuthorities().add(new Authority("/Portfolio/123456/*"));
    
    UserGroup group2 = new UserGroup("group2");
    group2.getAuthorities().add(new Authority("/Portfolio/7890/Read"));
    
    user.getUserGroups().add(group1);
    user.getUserGroups().add(group2);
    
    Assert.assertTrue(user.hasPermission("/Portfolio/123456/Read"));
    Assert.assertTrue(user.hasPermission("/Portfolio/123456/Write"));
    Assert.assertTrue(user.hasPermission("/Portfolio/7890/Read"));
    Assert.assertFalse(user.hasPermission("/Portfolio/7890/Write"));
    Assert.assertFalse(user.hasPermission("/Portfolio/Foo/Read"));
  }
  

}
