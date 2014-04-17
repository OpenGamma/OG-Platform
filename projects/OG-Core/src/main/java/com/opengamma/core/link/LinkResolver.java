/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.DataNotFoundException;

/**
 * Responsible for actually performing the resolve operation
 * for a {@link ResolvableLink} instance.
 *
 * @param <T> the type of object provided by the resolver
 */
public interface LinkResolver<T, I> {

  /**
   * Resolves the contents of a Link returning a concrete
   * instance of the required type. If the link has been
   * misconfigured and no instance can be found, then an
   * exception will be thrown.
   *
   * @return an instance of the type a Link actually points to
   * @param identifier
   * @throws DataNotFoundException if the link cannot be resolved
   */
  T resolve(I identifier);

  LinkResolver<T,I> withTargetType(Class<T> targetType);
}
