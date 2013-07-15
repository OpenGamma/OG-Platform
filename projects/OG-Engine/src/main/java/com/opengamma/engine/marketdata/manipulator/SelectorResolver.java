/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Used by {@link MarketDataSelector} implementations for resolving {@link ExternalId}s into the objects
 * they identify.
 */
public interface SelectorResolver {

  /**
   * Resolves an ID to a security.
   * @param id An ID
   * @return The security identified by the ID or null if the ID is unknown or doesn't identify a security
   */
  Security resolveSecurity(ExternalId id);
}

/**
 * Resolver that uses a {@link ComputationTargetResolver.AtVersionCorrection} to resolve IDs to objects.
 */
/* package */ class DefaultSelectorResolver implements SelectorResolver {

  /** Performs the resolution. */
  private final ComputationTargetResolver.AtVersionCorrection _resolver;

  /* package */ DefaultSelectorResolver(ComputationTargetResolver.AtVersionCorrection resolver) {
    ArgumentChecker.notNull(resolver, "resolver");
    _resolver = resolver;
  }

  @Override
  public Security resolveSecurity(ExternalId id) {
    ComputationTargetRequirement securityReq = new ComputationTargetRequirement(ComputationTargetType.SECURITY, id);
    ComputationTargetSpecification securitySpec =
        _resolver.getSpecificationResolver().getTargetSpecification(securityReq);
    ComputationTarget target = _resolver.resolve(securitySpec);
    if (target == null) {
      return null;
    }
    return target.getSecurity();
  }
}
