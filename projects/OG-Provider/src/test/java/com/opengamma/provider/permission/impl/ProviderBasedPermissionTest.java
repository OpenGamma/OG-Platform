/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthenticatedException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.core.user.UserPrincipals;
import com.opengamma.core.user.impl.SimpleUserPrincipals;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.auth.AuthUtils;
import com.opengamma.util.auth.ShiroPermissionResolver;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ProviderBasedPermissionTest {

  private static final Permission WILDCARD_PERM = new ShiroPermissionResolver().resolvePermission("Data:12345");
  private static final ExternalIdBundle USER_BUNDLE = ExternalIdBundle.of("DATAUSER", "TEST");
  private static final UserPrincipals PRINCIPALS;
  static {
    TestUtils.initSecurity();
    SimpleUserPrincipals principals = new SimpleUserPrincipals();
    principals.setUserName("Tester");
    principals.setAlternateIds(USER_BUNDLE);
    principals.setNetworkAddress("1.1.1.1");
    PRINCIPALS = principals;
  }

  @AfterMethod
  public void tearDown() {
    AuthUtils.getSubject().getSession().removeAttribute(UserPrincipals.ATTRIBUTE_KEY);
  }

  //-------------------------------------------------------------------------
  @Test
  public void impliesTrue() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    when(provider.isPermitted(USER_BUNDLE, "1.1.1.1", "Data:12345")).thenReturn(Boolean.TRUE);
    ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test1.implies(test2));
  }

  @Test
  public void impliesFalse() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    when(provider.isPermitted(USER_BUNDLE, "1.1.1.1", "Data:12345")).thenReturn(Boolean.FALSE);
    ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test1.implies(test2));
  }

  @Test
  public void impliesFalseAgainstWildcardPermission() {
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test.implies(WILDCARD_PERM));
    assertFalse(WILDCARD_PERM.implies(test));
  }

  //-------------------------------------------------------------------------
  @Test
  public void checkImpliesTrue() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.TRUE));
    when(provider.isPermitted(request)).thenReturn(result);
    ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test1.checkImplies(test2));
  }

  @Test
  public void checkImpliesFalse() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.FALSE));
    when(provider.isPermitted(request)).thenReturn(result);
    ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test1.checkImplies(test2));
  }

  @Test(expectedExceptions = UnauthenticatedException.class)
  public void checkImpliesUnauthenticated() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthenticationError("Not authenticated");
    when(provider.isPermitted(request)).thenReturn(result);
    ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    test1.checkImplies(test2);
  }

  @Test(expectedExceptions = AuthorizationException.class)
  public void checkImpliesUnauthorized() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthorizationError("Not authorized");
    when(provider.isPermitted(request)).thenReturn(result);
    ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    test1.checkImplies(test2);
  }

  @Test
  public void checkImpliesFalseAgainstWildcardPermission() {
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test.checkImplies(WILDCARD_PERM));
  }

  //-------------------------------------------------------------------------
  @Test
  public void checkImpliesAllOneTrue() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.TRUE));
    when(provider.isPermitted(request)).thenReturn(result);
    ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1), true));
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1), false));
  }

  @Test
  public void checkImpliesAllManyTrue() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(
        USER_BUNDLE, "1.1.1.1", "Data:12345", "Data:67890");
    PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.TRUE, "Data:67890", Boolean.TRUE));
    when(provider.isPermitted(request)).thenReturn(result);
    ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    Permission required2 = new ProviderBasedPermission(provider, "Data:67890");
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1, required2), true));
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1, required2), false));
  }

  @Test
  public void checkImpliesAllOneFalse() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.FALSE));
    when(provider.isPermitted(request)).thenReturn(result);
    ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test.checkImpliesAll(ImmutableList.of(required1), true));
    assertFalse(test.checkImpliesAll(ImmutableList.of(required1), false));
  }

  @Test
  public void checkImpliesAllManyFalse() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(
        USER_BUNDLE, "1.1.1.1", "Data:12345", "Data:67890");
    PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.FALSE, "Data:67890", Boolean.TRUE));
    when(provider.isPermitted(request)).thenReturn(result);
    ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    Permission required2 = new ProviderBasedPermission(provider, "Data:67890");
    assertFalse(test.checkImpliesAll(ImmutableList.of(required1, required2), true));
    assertFalse(test.checkImpliesAll(ImmutableList.of(required1, required2), false));
  }

  @Test(expectedExceptions = UnauthenticatedException.class)
  public void checkImpliesAllUnauthenticated() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthenticationError("Not authenticated");
    when(provider.isPermitted(request)).thenReturn(result);
    ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1), true));
  }

  @Test(expectedExceptions = AuthorizationException.class)
  public void checkImpliesAllUnauthorized() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthorizationError("Not authorized");
    when(provider.isPermitted(request)).thenReturn(result);
    ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1), true));
  }

  @Test
  public void checkImpliesAllAgainstWildcardPermission() {
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data:12345");
    Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    assertEquals(null, test.checkImpliesAll(ImmutableList.of(required1, WILDCARD_PERM), true));
    assertEquals(null, test.checkImpliesAll(ImmutableList.of(required1, WILDCARD_PERM), false));
  }

}
