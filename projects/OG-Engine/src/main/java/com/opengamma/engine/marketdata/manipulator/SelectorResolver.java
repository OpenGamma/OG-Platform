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
 *
 */
public interface SelectorResolver {

  Security resolveSecurity(ExternalId id);
}

/* package */ class DefaultSelectorResolver implements SelectorResolver {

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
