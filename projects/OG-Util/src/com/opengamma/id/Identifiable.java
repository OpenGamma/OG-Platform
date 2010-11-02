/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import com.opengamma.util.PublicSPI;

/**
 * Provides uniform access to objects that can supply a standard identifier.
 */
@PublicSPI
public interface Identifiable {

  /**
   * Gets the identifier for the instance.
   * @return the identifier, may be null
   */
  Identifier getIdentityKey();

}
