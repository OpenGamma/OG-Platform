/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.user.UserPrincipals;
import com.opengamma.core.user.impl.SimpleUserPrincipals;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.util.auth.AuthUtils;
import com.opengamma.util.auth.ShiroPermissionResolver;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT, singleThreaded = true)
public class ProviderBasedPermissionTest {

  private static final Permission SHIRO_PERM = new ShiroPermissionResolver().resolvePermission("Data:12345");
  private static final ExternalIdBundle USER_BUNDLE = ExternalIdBundle.of("DATAUSER", "TEST");
  private static final UserPrincipals PRINCIPALS;
  static {
    SimpleUserPrincipals principals = new SimpleUserPrincipals();
    principals.setUserName("Tester");
    principals.setAlternateIds(USER_BUNDLE);
    principals.setNetworkAddress("1.1.1.1");
    PRINCIPALS = principals;
  }

  @BeforeMethod
  public void setUp() {
    ThreadContext.bind(new DefaultSecurityManager());
  }

  @AfterMethod
  public void tearDown() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
  }

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
  public void impliesFalseAgainstShiroPermission() {
    PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test.implies(SHIRO_PERM));
    assertFalse(SHIRO_PERM.implies(test));
  }

}
