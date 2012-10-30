/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import com.opengamma.util.PublicSPI;

/**
 * Trait added to entity managers that send events whenever their contents change.
 * <p>
 * This trait allows listeners to be registered for the events.
 */
@PublicSPI
public interface ChangeProvider {

  /**
   * Gets the change manager that handles events.
   * 
   * @return the change manager, not null
   */
  ChangeManager changeManager();

}
