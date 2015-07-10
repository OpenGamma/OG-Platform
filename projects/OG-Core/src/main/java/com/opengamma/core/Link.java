/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * A flexible link between two parts of the system.
 * <p>
 * A link represents a connection from one entity to another in the object model.
 * The connection can be held by an {@code ObjectId} or an {@code ExternalIdBundle}.
 * The link is resolved using a resolver.
 * <p>
 * This interface makes no guarantees about the thread-safety of implementations.
 * However, it is strongly recommended that the methods in this interface are individually thread-safe.
 * 
 * @param <T> the target type of the link
 */
@PublicAPI
public interface Link<T extends UniqueIdentifiable> extends ObjectIdentifiable {

  /**
   * Gets the object identifier that strongly references the target.
   * 
   * @return the object identifier, may be null
   */
  @Override
  ObjectId getObjectId();

  /**
   * Gets the external identifier bundle that references the target.
   * An empty bundle is used if not referencing a target by external bundle.
   * 
   * @return the external identifier bundle, not null
   */
  ExternalIdBundle getExternalId();

  /**
   * Resolves the link to the target object.
   * <p>
   * This is normally implemented by calling {@link LinkResolver#resolve(Link)}.
   * 
   * @param resolver  the resolver capable of finding the target, not null
   * @return the resolved target, null if unable to resolve
   * @throws DataNotFoundException if the target could not be resolved
   * @throws RuntimeException if an error occurs
   */
  T resolve(LinkResolver<T> resolver);

}
