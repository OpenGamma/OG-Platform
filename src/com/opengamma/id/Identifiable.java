/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

/**
 * Provides uniform access to objects that can supply a standard identifier.
 */
public interface Identifiable {

  /**
   * Gets the identifier for the instance.
   * @return the identifier, may be null
   */
  Identifier getIdentityKey();

}
