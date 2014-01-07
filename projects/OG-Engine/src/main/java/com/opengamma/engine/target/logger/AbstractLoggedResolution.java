/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.logger;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Partial implementation of a resolution logging wrapper.
 * 
 * @param <T> the wrapped type
 */
public abstract class AbstractLoggedResolution<T extends UniqueIdentifiable> implements UniqueIdentifiable {

  private final T _underlying;
  private final ResolutionLogger _logger;

  protected AbstractLoggedResolution(final T underlying, final ResolutionLogger logger) {
    _underlying = underlying;
    _logger = logger;
  }

  protected T getUnderlying() {
    return _underlying;
  }

  protected ResolutionLogger getLogger() {
    return _logger;
  }

  /**
   * Logs an object resolution. This is logged as a resolution of the object identifier to a specific version.
   * 
   * @param type the type of the object, not null
   * @param resolved the resolved object, not null
   */
  protected void log(final ComputationTargetType type, final UniqueIdentifiable resolved) {
    final UniqueId resolvedId = resolved.getUniqueId();
    log(new ComputationTargetSpecification(type, resolvedId.toLatest()), resolvedId);
  }

  /**
   * Logs a resolution.
   * 
   * @param reference the unversioned reference that was resolved, not null
   * @param uniqueId the resolved identifier, not null
   */
  protected void log(final ComputationTargetReference reference, final UniqueId uniqueId) {
    getLogger().log(reference, uniqueId);
  }

  // UniqueIdentifiable

  @Override
  public UniqueId getUniqueId() {
    return getUnderlying().getUniqueId();
  }

}
