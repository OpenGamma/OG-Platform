/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;

/**
 * Wrapper around a {@link ComputationTargetResolver} that can be used to provide source interfaces.
 * 
 * @deprecated This is an interim solution until [PLAT-2782] is resolved
 */
@Deprecated
public class ComputationTargetResolverWrapper {

  private final ComputationTargetResolver.AtVersionCorrection _resolver;

  public ComputationTargetResolverWrapper(final ComputationTargetResolver.AtVersionCorrection resolver) {
    _resolver = resolver;
  }

  public ComputationTargetResolver.AtVersionCorrection getResolver() {
    return _resolver;
  }

  public Object get(ComputationTargetType type, ExternalId identifier) {
    final ComputationTargetSpecification specification = getResolver().getSpecificationResolver().getTargetSpecification(new ComputationTargetRequirement(type, identifier));
    if (specification == null) {
      return null;
    }
    final ComputationTarget target = getResolver().resolve(specification);
    if (target == null) {
      return null;
    }
    return target.getValue();
  }

}
