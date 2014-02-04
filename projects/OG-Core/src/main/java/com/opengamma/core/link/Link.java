/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

/**
 * Represents a link to an object using an identifier for the object. The link can be
 * resolved on demand. Use of links allows provision of objects by remote servers while
 * maintaining the ability to capture updates to the linked resources on each subsequent
 * resolution.
 *
 * @param <I> the type of the identifier for the linked object
 * @param <T> the type of the object being linked to
 */
public interface Link<I, T> {

  /**
   * Resolve the link and get the underlying object.
   * @return the config
   */
  T resolve();

  /**
   * Get the identifier on which the link is based
   * @return the identifier
   */
  I getIdentifier();

  /**
   * Get the type on which the link is based
   * @return the type
   */
  Class<T> getType();
}
