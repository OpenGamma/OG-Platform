/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.shiro.authz.Permission;
import org.testng.annotations.Test;

import com.opengamma.util.auth.AuthUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergPermissionsTest {

  public void testCreateEidPermission() {
    Permission expected = AuthUtils.getPermissionResolver().resolvePermission(BloombergPermissions.EID_PREFIX + "1234");
    assertEquals(expected, BloombergPermissions.createEidPermission(1234));
  }

  public void testCreateEidPermissionString() {
    String expected = BloombergPermissions.EID_PREFIX + "1234";
    assertEquals(expected, BloombergPermissions.createEidPermissionString(1234));
  }

  public void testIsEid() {
    assertEquals(false, BloombergPermissions.isEid(null));
    assertEquals(false, BloombergPermissions.isEid(""));
    assertEquals(false, BloombergPermissions.isEid("1234"));
    assertEquals(false, BloombergPermissions.isEid("Data:1234"));
    assertEquals(true, BloombergPermissions.isEid(BloombergPermissions.EID_PREFIX + "1234"));
  }

  public void testExtractEid() {
    assertEquals(1234, BloombergPermissions.extractEid(BloombergPermissions.EID_PREFIX + "1234"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testExtractEid_null() {
    BloombergPermissions.extractEid(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testExtractEid_empty() {
    BloombergPermissions.extractEid("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testExtractEid_invalid() {
    BloombergPermissions.extractEid(BloombergPermissions.EID_PREFIX + "abcd");
  }

}
