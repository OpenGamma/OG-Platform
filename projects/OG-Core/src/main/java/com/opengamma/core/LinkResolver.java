/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * Resolver capable of resolving a link to the target.
 * <p>
 * A {@link Link} is a flexible connection from one entity to another in the object model.
 * In order to be useful, the link must be resolved to provide the target object.
 * Resolution may involve a database, cache or calculation as necessary.
 * <p>
 * This interface makes no guarantees about the thread-safety of implementations.
 * However, it is strongly recommended that implementations are thread-safe.
 * 
 * @param <T> the type being linked to
 */
@PublicAPI
public interface LinkResolver<T extends UniqueIdentifiable> {

  /**
   * Resolves the link to the target object.
   * 
   * @param link  the link to be resolver, not null
   * @return the resolved target, not null
   * @throws DataNotFoundException if the target could not be resolved
   * @throws RuntimeException if an error occurs
   */
  T resolve(Link<T> link);

}
