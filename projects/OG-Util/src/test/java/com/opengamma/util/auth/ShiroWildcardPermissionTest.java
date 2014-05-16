/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.auth;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.InvalidPermissionStringException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test.
 */
public class ShiroWildcardPermissionTest {

  @Test(expectedExceptions = InvalidPermissionStringException.class)
  public void test_of_null() {
    ShiroWildcardPermission.of(null);
  }

  @Test(expectedExceptions = InvalidPermissionStringException.class)
  public void test_of_empty() {
    ShiroWildcardPermission.of("");
  }

  @Test(expectedExceptions = InvalidPermissionStringException.class)
  public void test_of_blank() {
    ShiroWildcardPermission.of("   ");
  }

  @Test(expectedExceptions = InvalidPermissionStringException.class)
  public void test_of_onlyColons() {
    ShiroWildcardPermission.of("::");
  }

  @Test(expectedExceptions = InvalidPermissionStringException.class)
  public void test_of_onlyCommas() {
    ShiroWildcardPermission.of("a:,:b");
  }

  @Test(expectedExceptions = InvalidPermissionStringException.class)
  public void test_of_invalidWildcard() {
    ShiroWildcardPermission.of("a:beta*");
  }

  @Test
  public void test_of_caseInsensitive() {
    Permission p1 = ShiroWildcardPermission.of("something");
    Permission p2 = ShiroWildcardPermission.of("SOMETHING");
    assertEquals(p1, p2);
  }

  //-------------------------------------------------------------------------
  @DataProvider(name = "simplifications")
  Object[][] data_simplifications() {
    return new Object[][] {
        {"a", "a"},
        {"a:*", "a"},
        {"a:*:*", "a"},
        {"a:*:*:*", "a"},
        
        {"a,b", "a,b"},
        {"a,b:*", "a,b"},
        {"a,b:*:*", "a,b"},
        
        {"a,b:c,d", "a,b:c,d"},
        {"a,b:c,d:*", "a,b:c,d"},
        {"a,b:c,d:*:*", "a,b:c,d"},
        
        {"a,b:*:c,d", "a,b:*:c,d"},
        {"a,b:*:c,d:*", "a,b:*:c,d"},
        {"a,b:*:c,d:*:*", "a,b:*:c,d"},
        
        {"a,b:*:*:*:c,d", "a,b:*:*:*:c,d"},
        {"a,b:*:*:*:c,d:*", "a,b:*:*:*:c,d"},
        {"a,b:*:*:*:c,d:*:*", "a,b:*:*:*:c,d"},
        
        {"a,b:x,*:c,d", "a,b:*:c,d"},
        {"a,b:x,*:c,d:*", "a,b:*:c,d"},
        {"a,b:x,*:c,d:*:*", "a,b:*:c,d"},
    };
  }

  @Test(dataProvider = "simplifications")
  public void test_simplifications(String perm, String simplification) {
    Permission sp = ShiroWildcardPermission.of(perm);
    assertEquals(simplification, sp.toString());
  }

  //-------------------------------------------------------------------------
  @DataProvider(name = "permissions")
  Object[][] data_permissions() {
    return new Object[][] {
        {"a", "a", true, true},
        {"a", "b", false, false},
        
        {"a,b", "a,b", true, true},
        {"a,b", "a", true, false},
        
        {"a,b,c", "a", true, false},
        {"a,b,c", "a,b", true, false},
        {"a,b,c", "a,c", true, false},
        {"a,b,c", "b,c", true, false},
        
        {"c,a,b", "a", true, false},
        {"c,a,b", "a,b", true, false},
        {"c,a,b", "a,c", true, false},
        {"c,a,b", "b,c", true, false},
        
        {"c,a,b", "a", true, false},
        {"c,a,b", "a,b", true, false},
        {"c,a,b", "a,c", true, false},
        {"c,a,b", "b,c", true, false},
        
        {"a,b,c:d,e", "a:e", true, false},
        {"a,b,c:d,e", "b:e", true, false},
        {"a,b,c:d,e", "c:e", true, false},
        {"a,b,c:d,e", "a:d", true, false},
        {"a,b,c:d,e", "b:d", true, false},
        {"a,b,c:d,e", "c:d", true, false},
        {"a,b,c:d,e", "a:d,e", true, false},
        {"a,b,c:d,e", "b,c:d,e", true, false},
        {"a,b,c:d,e", "a,c:d,e", true, false},
        
        {"a,b:d,e:g,h", "a:e:h", true, false},
        
        {"a", "a:e", true, false},
        {"a", "a:e:h", true, false},
        {"a", "a:d,e:g,h", true, false},
        
        {"*", "*", true, true},
        {"*", "a", true, false},
        {"*", "a:d", true, false},
        {"*", "a:d:g", true, false},
        
        {"a:*", "*", false, true},
        {"a:*", "a", true, true},
        {"a:*", "a:d", true, false},
        {"a:*", "a:d,e", true, false},
        {"a:*", "a:d:g", true, false},
        
        {"a:*:*", "*", false, true},
        {"a:*:*", "a", true, true},
        {"a:*:*", "a:d", true, false},
        {"a:*:*", "a:d,e", true, false},
        {"a:*:*", "a:d:g", true, false},
        
        {"a:*:g,h", "*", false, true},
        {"a:*:g,h", "a", false, true},
        {"a:*:g,h", "a:d", false, false},
        {"a:*:g,h", "a:d,e", false, false},
        {"a:*:g,h", "a:d:g", true, false},
        {"a:*:g,h", "a:d:g,i", false, false},
        {"a:*:g,h", "a:d:g:x", true, false},
        
        {"a:b,*", "a:b", true, false},
        {"a:b,*", "a:c", true, false},
        {"a:b,*", "a:d", true, false},
    };
  }

  @Test(dataProvider = "permissions")
  public void test_permissions(String perm1, String perm2, boolean impliesForward, boolean impliesBackward) {
    Permission sp1 = ShiroWildcardPermission.of(perm1);
    Permission sp2 = ShiroWildcardPermission.of(perm2);
    assertEquals("Foward", impliesForward, sp1.implies(sp2));
    assertEquals("Backward", impliesBackward, sp2.implies(sp1));
  }

}
