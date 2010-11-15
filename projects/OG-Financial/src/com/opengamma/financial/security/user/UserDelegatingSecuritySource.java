/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.user;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.DelegatingSecuritySource;

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
