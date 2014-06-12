/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PermissionCheckProviderResultTest {

  @Test
  public void isPermitted() {
    ImmutableMap<String, Boolean> checked = ImmutableMap.of("Data:12345", Boolean.FALSE, "Data:67890", Boolean.TRUE);
    PermissionCheckProviderResult result = PermissionCheckProviderResult.of(checked);
    assertFalse(result.isPermitted("Data:12345"));
    assertTrue(result.isPermitted("Data:67890"));
    assertFalse(result.isPermittedAll(ImmutableList.of("Data:12345")));
    assertTrue(result.isPermittedAll(ImmutableList.of("Data:67890")));
    assertFalse(result.isPermittedAll(ImmutableList.of("Data:12345", "Data:67890")));
  }

  //-------------------------------------------------------------------------
  @Test
  public void isPermittedUnauthenticated() {
    PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthenticationError("Not authenticated");
    assertFalse(result.isPermitted("Data:12345"));
    assertFalse(result.isPermittedAll(ImmutableList.of("Data:12345")));
  }

  @Test(expectedExceptions = UnauthenticatedException.class)
  public void checkPermittedUnauthenticated() {
    PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthenticationError("Not authenticated");
    result.checkPermitted("Data:12345");
  }

  @Test(expectedExceptions = UnauthenticatedException.class)
  public void checkErrorsUnauthenticated() {
    PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthenticationError("Not authenticated");
    result.checkErrors();
  }

  //-------------------------------------------------------------------------
  @Test
  public void isPermittedUnauthorized() {
    PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthorizationError("Not authorized");
    assertFalse(result.isPermitted("Data:12345"));
    assertFalse(result.isPermittedAll(ImmutableList.of("Data:12345")));
  }

  @Test(expectedExceptions = AuthorizationException.class)
  public void checkPermittedUnauthorized() {
    PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthorizationError("Not authorized");
    result.checkPermitted("Data:12345");
  }

  @Test(expectedExceptions = AuthorizationException.class)
  public void checkErrorsUnauthorized() {
    PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthorizationError("Not authorized");
    result.checkErrors();
  }

}
