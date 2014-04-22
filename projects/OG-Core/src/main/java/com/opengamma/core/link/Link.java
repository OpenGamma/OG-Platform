/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.DataNotFoundException;

/**
 * Represents a link to an object using the object's identifier and type which
 * can be resolved on demand. Use of links allows provision of objects by remote
 * servers while maintaining the ability to capture updates to the linked resources
 * on each subsequent resolution.
 *
 * @param <T> type of the config
 */
public interface Link<T> {

  /**
   * Resolve the link and get the underlying object.
   *
   * @return the target of the link, not null
   * @throws DataNotFoundException if the link is not resolved
   */
  T resolve();

  /**
   * Get the type of the object that this link targets.
   *
   * @return the type of the object, not null
   */
  Class<T> getTargetType();

  // TODO - do we want a method to generate a resolved version of a config object e.g. new ResolvedConfigLink(resolver.resolve())
}
