/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.util.ArgumentChecker;

/**
 * A target resolver implementation that forwards to a backing resolver.
 * <p>
 * This implements the decorator pattern.
 */
public abstract class ForwardingComputationTargetResolver implements ComputationTargetResolver {

  /**
   * The underlying resolver.
   */
  private final ComputationTargetResolver _underlying;

  /**
   * Creates an instance specifying the underlying resolver.
   * @param underlying  the underlying resolver, not null
   */
  public ForwardingComputationTargetResolver(final ComputationTargetResolver underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying resolver.
   * @return the underlying resolver, not null
   */
  protected ComputationTargetResolver getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public ComputationTarget resolve(ComputationTargetSpecification specification) {
    return getUnderlying().resolve(specification);
  }

}
