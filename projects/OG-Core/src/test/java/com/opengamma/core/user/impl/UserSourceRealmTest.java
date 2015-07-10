/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.user.UserSource;
import com.opengamma.util.auth.ShiroPermissionResolver;
import com.opengamma.util.test.TestGroup;

/**
 * Tests UserSourceRealm.
 */
@Test(groups = TestGroup.UNIT)
public class UserSourceRealmTest {

  private static final PrincipalCollection PRINCIPALS = new SimplePrincipalCollection();
  private static final Permission PERMISSION_OTHER_TYPE = new Permission() {
    @Override
    public boolean implies(Permission p) {
      return false;
    }
  };

  private UserSource _userSource;

  @BeforeMethod
  public void setUp() {
    _userSource = mock(UserSource.class);
    when(_userSource.changeManager()).thenReturn(new BasicChangeManager());
  }

  //-------------------------------------------------------------------------
  @Test
  public void testIsPermitted_true() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(true, realm.isPermitted(PRINCIPALS, "Master:view"));
  }

  @Test
  public void testIsPermitted_false() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(false, realm.isPermitted(PRINCIPALS, "Master:edit"));
  }

  @Test
  public void testIsPermitted_otherType() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(false, realm.isPermitted(PRINCIPALS, PERMISSION_OTHER_TYPE));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testIsPermittedAll_none() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(true, realm.isPermittedAll(PRINCIPALS));
  }

  @Test
  public void testIsPermittedAll_true() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(true, realm.isPermittedAll(PRINCIPALS, "Master:view", "Source:view"));
  }

  @Test
  public void testIsPermittedAll_false() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(false, realm.isPermittedAll(PRINCIPALS, "Master:view", "Source:edit"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testCheckPermission_true() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermission(PRINCIPALS, "Master:view");
  }

  @Test(expectedExceptions = UnauthorizedException.class)
  public void testCheckPermission_false() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermission(PRINCIPALS, "Master:edit");
  }

  @Test(expectedExceptions = UnauthorizedException.class)
  public void testCheckPermission_otherType() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermission(PRINCIPALS, PERMISSION_OTHER_TYPE);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testCheckPermissions_true() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermissions(PRINCIPALS, "Master:view", "Source:view");
  }

  @Test(expectedExceptions = UnauthorizedException.class)
  public void testCheckPermissions_false() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermissions(PRINCIPALS, "Master:view", "Source:edit");
  }

}
