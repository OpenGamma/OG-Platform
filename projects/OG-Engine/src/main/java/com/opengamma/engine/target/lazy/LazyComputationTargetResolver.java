/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DelegatingComputationTargetResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.function.BiFunction;

/**
 * A target resolver that does not resolve the targets immediately but returns a deferred handle. This is excellent for consumers of the target that only care about it's unique identifier and don't
 * need the resolution but can obtain it if they do.
 */
public final class LazyComputationTargetResolver extends DelegatingComputationTargetResolver {

  private static final ComputationTargetTypeMap<BiFunction<ComputationTargetResolver.AtVersionCorrection, ComputationTargetSpecification, UniqueIdentifiable>> s_resolvers;

  static {
    s_resolvers = new ComputationTargetTypeMap<BiFunction<ComputationTargetResolver.AtVersionCorrection, ComputationTargetSpecification, UniqueIdentifiable>>();
    s_resolvers.put(ComputationTargetType.PORTFOLIO_NODE, new BiFunction<ComputationTargetResolver.AtVersionCorrection, ComputationTargetSpecification, UniqueIdentifiable>() {
      @Override
      public UniqueIdentifiable apply(final ComputationTargetResolver.AtVersionCorrection underlying, final ComputationTargetSpecification specification) {
        return new LazyTargetResolverPortfolioNode(underlying, specification);
      }
    });
    s_resolvers.put(ComputationTargetType.POSITION, new BiFunction<ComputationTargetResolver.AtVersionCorrection, ComputationTargetSpecification, UniqueIdentifiable>() {
      @Override
      public UniqueIdentifiable apply(final ComputationTargetResolver.AtVersionCorrection underlying, final ComputationTargetSpecification specification) {
        return new LazyTargetResolverPosition(underlying, specification);
      }
    });
    s_resolvers.put(ComputationTargetType.TRADE, new BiFunction<ComputationTargetResolver.AtVersionCorrection, ComputationTargetSpecification, UniqueIdentifiable>() {
      @Override
      public UniqueIdentifiable apply(final ComputationTargetResolver.AtVersionCorrection underlying, final ComputationTargetSpecification specification) {
        return new LazyTargetResolverTrade(underlying, specification);
      }
    });
  }

  public LazyComputationTargetResolver(final ComputationTargetResolver underlying) {
    super(underlying);
  }

  /**
   * If the specification is lazily resolvable, returns a target that will resolve it on demand. Otherwise it is resolved immediately.
   * 
   * @param underlying the underlying resolver to use for resolution
   * @param specification the specification to resolve
   * @return the target
   */
  public static ComputationTarget resolve(final ComputationTargetResolver.AtVersionCorrection underlying, final ComputationTargetSpecification specification) {
    final BiFunction<ComputationTargetResolver.AtVersionCorrection, ComputationTargetSpecification, UniqueIdentifiable> resolver = s_resolvers.get(specification.getType());
    if (resolver != null) {
      final UniqueIdentifiable lazy = resolver.apply(underlying, specification);
      if (specification.getUniqueId().isVersioned()) {
        return new ComputationTarget(specification, lazy);
      } else {
        return new ComputationTarget(specification.replaceIdentifier(lazy.getUniqueId()), lazy);
      }
    } else {
      return underlying.resolve(specification);
    }
  }

  public static ComputationTarget resolve(final ComputationTargetResolver underlying, final ComputationTargetSpecification specification, final VersionCorrection versionCorrection) {
    final BiFunction<ComputationTargetResolver.AtVersionCorrection, ComputationTargetSpecification, UniqueIdentifiable> resolver = s_resolvers.get(specification.getType());
    if (resolver != null) {
      final UniqueIdentifiable lazy = resolver.apply(underlying.atVersionCorrection(versionCorrection), specification);
      if (specification.getUniqueId().isVersioned()) {
        return new ComputationTarget(specification, lazy);
      } else {
        return new ComputationTarget(specification.replaceIdentifier(lazy.getUniqueId()), lazy);
      }
    } else {
      return underlying.resolve(specification, versionCorrection);
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
  public ComputationTarget resolve(final ComputationTargetSpecification specification, final VersionCorrection versionCorrection) {
    return resolve(getUnderlying(), specification, versionCorrection);
  }

}
