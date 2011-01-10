/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import com.opengamma.util.PublicAPI;

/**
 * Provides uniform access to objects that can supply a standard identifier.
 * <p>
 * This interface makes no guarantees about the thread-safety of implementations.
 * However, wherever possible calls to this method should be thread-safe.
 */
@PublicAPI
public interface Identifiable {

  /**
   * Gets the identifier for the instance.
   * 
   * @return the identifier, may be null
   */
  Identifier getIdentityKey();

}
