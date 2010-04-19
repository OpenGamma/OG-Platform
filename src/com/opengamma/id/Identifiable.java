/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

/**
 * Provides uniform access to objects that can supply a standard identifier.
 *
 * @author pietari
 */
public interface Identifiable {

  /**
   * Gets the identifier for the instance.
   * @return the identifier, not null
   */
  Identifier getIdentityKey();

}
