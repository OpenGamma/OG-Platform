/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.user;

import com.opengamma.engine.position.DelegatingPositionSource;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;

/**
 * Delegates between a {@link UserPositionSource} and a default {@link PositionSource}. Just a wrapper to aid
 * registration.
 */
public class UserDelegatingPositionSource extends DelegatingPositionSource {
  
  /**
   * Constructs a new {@link UserDelegatingPositionSource}.
   * 
   * @param userMaster  the {@link UserPositionSource}
   * @param defaultMaster  the underlying {@link PositionSource}.
   */
  public UserDelegatingPositionSource(UserPositionSource userMaster, PositionSource defaultMaster) {
    super(defaultMaster);
    registerDelegate(UserUniqueIdentifierUtils.getUserScheme(), userMaster);
  }
  
}
