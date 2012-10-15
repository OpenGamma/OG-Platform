/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetResolverUtils;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.util.functional.Function2;

/**
 * A target resolver that does not resolve the targets immediately but returns a deferred handle. This is excellent for consumers of the target that only care about it's unique identifier and don't
 * need the resolution but can obtain it if they do.
 */
public final class LazyComputationTargetResolver implements ComputationTargetResolver {

  private static final ComputationTargetTypeMap<Function2<ComputationTargetResolver, ComputationTargetSpecification, ComputationTarget>> s_resolvers;

  static {
    s_resolvers = new ComputationTargetTypeMap<Function2<ComputationTargetResolver, ComputationTargetSpecification, ComputationTarget>>();
    s_resolvers.put(ComputationTargetType.PORTFOLIO_NODE, new Function2<ComputationTargetResolver, ComputationTargetSpecification, ComputationTarget>() {
      @Override
      public ComputationTarget execute(final ComputationTargetResolver underlying, final ComputationTargetSpecification specification) {
        return ComputationTargetResolverUtils.createResolvedTarget(specification, new LazyTargetResolverPortfolioNode(underlying, specification));
      }
    });
    s_resolvers.put(ComputationTargetType.POSITION, new Function2<ComputationTargetResolver, ComputationTargetSpecification, ComputationTarget>() {
      @Override
      public ComputationTarget execute(final ComputationTargetResolver underlying, final ComputationTargetSpecification specification) {
        return ComputationTargetResolverUtils.createResolvedTarget(specification, new LazyTargetResolverPosition(underlying, specification));
      }
    });
    s_resolvers.put(ComputationTargetType.TRADE, new Function2<ComputationTargetResolver, ComputationTargetSpecification, ComputationTarget>() {
      @Override
      public ComputationTarget execute(final ComputationTargetResolver underlying, final ComputationTargetSpecification specification) {
        return ComputationTargetResolverUtils.createResolvedTarget(specification, new LazyTargetResolverTrade(underlying, specification));
      }
    });
  }

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
    final Function2<ComputationTargetResolver, ComputationTargetSpecification, ComputationTarget> resolver = s_resolvers.get(specification.getType());
    if (resolver != null) {
      return resolver.execute(underlying, specification);
    } else {
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
    return s_resolvers.get(specification.getType()) != null;
  }

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    return resolve(getUnderlying(), specification);
  }

  @Override
  public ComputationTargetType simplifyType(final ComputationTargetType type) {
    return getUnderlying().simplifyType(type);
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
