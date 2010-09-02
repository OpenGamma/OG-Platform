/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.user;

import com.opengamma.engine.security.DelegatingSecuritySource;
import com.opengamma.engine.security.SecuritySource;

/**
 * Delegates between a {@link UserSecuritySource} and a default {@link SecuritySource}. Just a wrapper to aid
 * registration.
 */
public class UserDelegatingSecuritySource extends DelegatingSecuritySource {
  
  public UserDelegatingSecuritySource(UserSecuritySource userSecurityMaster, SecuritySource defaultMaster) {
    super(defaultMaster);
    registerDelegate(userSecurityMaster.getScheme(), userSecurityMaster);
  }

}
