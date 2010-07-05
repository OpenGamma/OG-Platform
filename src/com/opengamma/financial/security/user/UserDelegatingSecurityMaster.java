/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.user;

import com.opengamma.engine.security.DelegatingSecurityMaster;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;

/**
 * Delegates between a {@link UserSecurityMaster} and a default {@link SecurityMaster}. Just a wrapper to aid
 * registration.
 */
public class UserDelegatingSecurityMaster extends DelegatingSecurityMaster {
  
  public UserDelegatingSecurityMaster(UserSecurityMaster userSecurityMaster, SecurityMaster defaultMaster) {
    super(defaultMaster);
    registerDelegate(UserUniqueIdentifierUtils.getUserScheme(), userSecurityMaster);
  }

}
