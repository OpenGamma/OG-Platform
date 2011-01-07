/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

/**
 * Provides uniform access to objects that support having their unique identifier
 * updated after construction.
 * <p>
 * For example, code in the database layer will need to update the unique identifier
 * when the object is stored.
 * <p>
 * This class is a thread-safe static utility class.
 */
public final class UniqueIdentifiables {

  /**
   * Restricted constructor.
   */
  private UniqueIdentifiables() {
  }

  /**
   * Sets the unique identifier of an object if it implements {@code MutableUniqueIdentifiable}.
   * 
   * @param object  the object to set into
   * @param uid  the unique identifier to set
   */
  public static void setInto(Object object, UniqueIdentifier uid) {
    if (object instanceof MutableUniqueIdentifiable) {
      ((MutableUniqueIdentifiable) object).setUniqueId(uid);
    }
  }

}
