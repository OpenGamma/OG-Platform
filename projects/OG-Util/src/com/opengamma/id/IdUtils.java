/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

/**
 * Utility class for working with identifiers.
 * <p>
 * This class is a thread-safe static utility class.
 */
public final class IdUtils {

  /**
   * Restricted constructor.
   */
  private IdUtils() {
  }

  /**
   * Sets the unique identifier of an object if it implements {@code MutableUniqueIdentifiable}.
   * <p>
   * This provides uniform access to objects that support having their unique identifier
   * updated after construction.
   * <p>
   * For example, code in the database layer will need to update the unique identifier
   * when the object is stored.
   * 
   * @param object  the object to set into
   * @param uniqueId  the unique identifier to set, may be null
   */
  public static void setInto(Object object, UniqueId uniqueId) {
    if (object instanceof MutableUniqueIdentifiable) {
      ((MutableUniqueIdentifiable) object).setUniqueId(uniqueId);
    }
  }

}
