/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.AllPermission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

/**
 * An Apache Shiro {@code SecurityManager} that permits everything.
 */
public class PermissiveSecurityManager extends DefaultWebSecurityManager {

  /**
   * Creates an instance.
   */
  public PermissiveSecurityManager() {
    setRealm(new PermissiveRealm());
  }

  //-------------------------------------------------------------------------
  @Override
  protected SubjectContext copy(SubjectContext subjectContext) {
    // this is the only way to trick the superclass into believing subject is always authenticated
    UsernamePasswordToken token = new UsernamePasswordToken("permissive", "nopassword");
    SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), "Permissive");
    subjectContext.setAuthenticated(true);
    subjectContext.setAuthenticationToken(token);
    subjectContext.setAuthenticationInfo(info);
    return subjectContext;
  }

  //-------------------------------------------------------------------------
  /**
   * An Apache Shiro {@code Realm} that permits everything.
   */
  public class PermissiveRealm extends AuthorizingRealm {

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
      return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
      SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
      info.addObjectPermission(new AllPermission());
      return info;
    }

  }

}
