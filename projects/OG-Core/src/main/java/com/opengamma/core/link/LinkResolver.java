/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.DataNotFoundException;

/**
 * Responsible for actually performing the resolve operation
 * for a {@link Link} instance.
 *
 * @param <I> the identifier for the linked object
 * @param <T> the type of object provided by the resolver
 */
public interface LinkResolver<I, T> {

  /**
   * Resolves the contents of a Link returning a concrete
   * instance of the required type. If the link has been
   * misconfigured and no instance can be found, then an
   * exception will be thrown.
   *
   * @param identifier the identifier for the linked object
   * @return an instance of the type a Link actually points to
   * @throws DataNotFoundException if the link cannot be resolved
   */
  T resolve(LinkIdentifier<I, T> identifier);

}
