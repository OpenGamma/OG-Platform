/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;

/**
 * A target resolver that does not resolve the targets immediately but returns a deferred handle. This is excellent for consumers of the target that only care about it's unique identifier and don't
 * need the resolution but can obtain it if they do.
 */
public final class LazyComputationTargetResolver implements ComputationTargetResolver {

  private final ComputationTargetResolver _underlying;

  public LazyComputationTargetResolver(final ComputationTargetResolver underlying) {
    _underlying = underlying;
  }

  protected ComputationTargetResolver getUnderlying() {
    return _underlying;
  }

  /**
   * If the specification is lazily resolvable, returns a target that will resolve it on demand. Otherwise it is resolved immediately.
   * 
   * @param underlying the underlying resolver to use for resolution
   * @param specification the specification to resolve
   * @return the target
   */
  public static ComputationTarget resolve(final ComputationTargetResolver underlying, final ComputationTargetSpecification specification) {
    switch (specification.getType()) {
      case PORTFOLIO_NODE:
        return new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new LazyTargetResolverPortfolioNode(underlying, specification));
      case POSITION:
        return new ComputationTarget(ComputationTargetType.POSITION, new LazyTargetResolverPosition(underlying, specification));
      case TRADE:
        return new ComputationTarget(ComputationTargetType.TRADE, new LazyTargetResolverTrade(underlying, specification));
      default:
        return underlying.resolve(specification);
    }
  }

  /**
   * Tests if the specification can be lazily resolved by a call to {@link #resolve}.
   * 
   * @param specification the specification to test
   * @return true if lazy resolution will happen, false if the underlying will be queried immediately
   */
  public static boolean isLazilyResolvable(final ComputationTargetSpecification specification) {
    switch (specification.getType()) {
      case PORTFOLIO_NODE:
      case POSITION:
      case TRADE:
        return true;
      default:
        return false;
    }
  }

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    return resolve(getUnderlying(), specification);
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
