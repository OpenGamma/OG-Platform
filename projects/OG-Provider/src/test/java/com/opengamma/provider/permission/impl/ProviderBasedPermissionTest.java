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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.opengamma.core.user.UserPrincipals;
import com.opengamma.core.user.impl.SimpleUserPrincipals;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.permission.PermissionCheckProvider;
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

}
