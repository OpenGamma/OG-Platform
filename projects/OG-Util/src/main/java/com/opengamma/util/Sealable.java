/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * An interface by which an object can be marked as immutable.
 * This allows for beans using setter-injection to be made immutable after setup has finished.
 */
public interface Sealable {

  /**
   * Seals the object.
   * <p>
   * After invoking this method, no changes to the object should be permitted.
   */
  void seal();

  /**
   * Determine whether this instance is currently sealed.
   * <p>
   * This will return false before {@link #seal()} is invoked and true after it is invoked.
   * 
   * @return true if the instance is sealed
   */
  boolean isSealed();

}
