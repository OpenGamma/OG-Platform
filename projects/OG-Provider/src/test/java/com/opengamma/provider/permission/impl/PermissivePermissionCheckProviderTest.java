/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PermissivePermissionCheckProviderTest {

  @Test
  public void allTrueWithRequest() {
    PermissivePermissionCheckProvider test = new PermissivePermissionCheckProvider();
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(ExternalIdBundle.of("A", "B"), "127.0.0.1", "A", "B", "C");
    PermissionCheckProviderResult resultHolder = test.isPermitted(request);
    assertNotNull(resultHolder);
    assertNotNull(resultHolder.getPermissionCheckResult());
    
    Map<String, Boolean> permissionCheckResult = resultHolder.getPermissionCheckResult();
    assertPermissionResult(permissionCheckResult);
  }

  private void assertPermissionResult(Map<String, Boolean> permissionCheckResult) {
    assertEquals(3, permissionCheckResult.size());
    assertTrue(permissionCheckResult.get("A"));
    assertTrue(permissionCheckResult.get("B"));
    assertTrue(permissionCheckResult.get("C"));
  }

  public void allTrueWithId_IpAdress_Permissions() {
    PermissivePermissionCheckProvider test = new PermissivePermissionCheckProvider();
    Map<String, Boolean> result = test.isPermitted(ExternalIdBundle.of("A", "B"), "127.0.0.1", Sets.newHashSet("A", "B", "C"));
    assertNotNull(result);
    assertPermissionResult(result);
  }

}
