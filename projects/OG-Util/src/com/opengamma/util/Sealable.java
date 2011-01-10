/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * An interface by which a JavaBean can be marked as immutable.
 * This allows for beans using setter-injection to be made immutable
 * after setup has finished.
 */
public interface Sealable {

  /**
   * Instruct the bean that after invoking this method, no changes
   * should be permitted.
   */
  void seal();

  /**
   * Determine whether this instance is currently sealed.
   * Instances should start out unsealed, and after {@link #seal()} is
   * invoked, should return {@code true}.
   * @return true if the instance is sealed
   */
  boolean isSealed();

}
