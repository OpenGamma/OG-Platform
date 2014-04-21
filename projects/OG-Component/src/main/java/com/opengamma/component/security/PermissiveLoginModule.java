/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.security;

import javax.security.auth.spi.LoginModule;

import org.eclipse.jetty.plus.jaas.spi.AbstractLoginModule;
import org.eclipse.jetty.plus.jaas.spi.UserInfo;
import org.eclipse.jetty.util.security.Credential;

import com.google.common.collect.ImmutableList;

/**
 * Permissive implementation of {@link LoginModule} which allows access to any user.
 */
public class PermissiveLoginModule extends AbstractLoginModule {

  @Override
  public UserInfo getUserInfo(String username) throws Exception {
    return new UserInfo(username, new OpenGammaCredential(), ImmutableList.of("user"));
  }
  
  /**
   * Permissive credential.
   */
  public class OpenGammaCredential extends Credential {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean check(Object credentials) {
      return true;
    }
    
  }

}
