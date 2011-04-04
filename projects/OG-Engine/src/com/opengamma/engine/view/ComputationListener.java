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
  
  // REVIEW jonathan 2011-04-04 -- think about removing getUser() from this interface as it duplicates and fragments
  // the idea of a ViewClient having a single, associated user to apply to all data it receives. 
  
  /**
   * @return The user associated with this listener. This value must be final
   * - it must not change during the lifetime of the listener.
   */
  UserPrincipal getUser();
  
  // PL 27/5/2010: add methods to inform listener of losing/gaining access to a view
  // due to user permission changes

}
