/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.user;

import com.opengamma.engine.position.DelegatingPositionMaster;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;

/**
 * Delegates between a {@link UserPositionMaster} and a default {@link PositionMaster}. Just a wrapper to aid
 * registration.
 */
public class UserDelegatingPositionMaster extends DelegatingPositionMaster {
  
  /**
   * Constructs a new {@link UserDelegatingPositionMaster}.
   * 
   * @param userMaster  the {@link UserPositionMaster}
   * @param defaultMaster  the underlying {@link PositionMaster}.
   */
  public UserDelegatingPositionMaster(UserPositionMaster userMaster, PositionMaster defaultMaster) {
    super(defaultMaster);
    registerDelegate(UserUniqueIdentifierUtils.getUserScheme(), userMaster);
  }
  
}
