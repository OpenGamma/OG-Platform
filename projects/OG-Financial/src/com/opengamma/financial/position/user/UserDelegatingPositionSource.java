/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.user;

import com.opengamma.core.position.impl.DelegatingPositionSource;
import com.opengamma.core.position.impl.PositionSource;

/**
 * Delegates between a user position source and a default.
 * Just a wrapper to aid registration.
 */
public class UserDelegatingPositionSource extends DelegatingPositionSource {

  /**
   * Creates an instance combining a user position source with a default.
   * 
   * @param defaultSource  the default source to fall back to, not null
   * @param userScheme  the scheme of the user positions, not null
   * @param userSource  the user position source, not null
   */
  public UserDelegatingPositionSource(PositionSource defaultSource, String userScheme, PositionSource userSource) {
    super(defaultSource);
    registerDelegate(userScheme, userSource);
  }

}
