/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.listener;

import com.opengamma.util.PublicSPI;

/**
 * Trait added to those masters that can send events whenever they are changed.
 * <p>
 * This trait allows listeners to be registered for the events.
 */
@PublicSPI
public interface NotifyingMaster {

  /**
   * Gets the change manager that handles events.
   * 
   * @return the change manager, not null
   */
  MasterChangeManager changeManager();

}
