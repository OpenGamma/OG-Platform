/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.ArgumentChecker;

/**
 * A target resolver implementation that delegates to a backing resolver.
 * <p>
 * This can be used to implement additional behavior on top of an underlying
 * resolver as per the decorator pattern.
 */
public abstract class DelegatingComputationTargetResolver implements ComputationTargetResolver {

  // TODO: move to com.opengamma.engine.target

  /**
   * The underlying resolver.
   */
  private final ComputationTargetResolver _underlying;

  /**
   * Creates an instance specifying the underlying resolver.
   * @param underlying  the underlying resolver, not null
   */
  public DelegatingComputationTargetResolver(final ComputationTargetResolver underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  /**
   * Gets the underlying resolver.
   * @return the underlying resolver, not null
   */
  protected ComputationTargetResolver getUnderlying() {
    return _underlying;
  }

  @Override
  public ComputationTarget resolve(ComputationTargetSpecification specification) {
    return getUnderlying().resolve(specification);
  }

  @Override
  public SecuritySource getSecuritySource() {
    return getUnderlying().getSecuritySource();
  }

  @Override
  public PositionSource getPositionSource() {
    return getUnderlying().getPositionSource();
  }

}
