/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

/**
 * Provides uniform access to objects that can supply a unique identifier.
 */
public interface UniqueIdentifiable {

  /**
   * Gets the unique identifier for this item.
   * @return the unique identifier, may be null
   */
  UniqueIdentifier getUniqueIdentifier();

}
