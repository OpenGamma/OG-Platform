/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

/**
 * Interface to get view's user and inform a listener of losing/gaining access to a view due to user permission changes.  The latter is not yet implemented.
 */
@PublicAPI
public interface ComputationListener {
  
  /**
   * @return The user associated with this listener. This value must be final
   * - it must not change during the lifetime of the listener.
   */
  UserPrincipal getUser();
  
  // PL 27/5/2010: add methods to inform listener of losing/gaining access to a view
  // due to user permission changes

}
