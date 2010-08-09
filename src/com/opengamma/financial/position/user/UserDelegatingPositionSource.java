/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.user;

import com.opengamma.engine.position.DelegatingPositionSource;
import com.opengamma.engine.position.PositionSource;

/**
 * Delegates between a {@link UserPositionSource} and a default {@link PositionSource}. Just a wrapper to aid
 * registration.
 */
public class UserDelegatingPositionSource extends DelegatingPositionSource {
  
  /**
   * Constructs a new {@link UserDelegatingPositionSource}.
   * 
   * @param defaultSource  the underlying {@link PositionSource}
   * @param userScheme  the scheme of the user positions
   * @param userSource  the {@link UserPositionSource}
   */
  public UserDelegatingPositionSource(PositionSource defaultSource, String userScheme, PositionSource userSource) {
    super(defaultSource);
    registerDelegate(userScheme, userSource);
  }
  
}
