/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.marketdata;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;

/**
 * Helper class for obtaining the maximal {@link ExternalIdBundle} for a target. This is intended for use by market data integrations that must obtain external identifier appropriate to that provider
 * from a more restricted target reference or resolved object.
 */
public class ExternalIdBundleResolver implements ComputationTargetReferenceVisitor<ExternalIdBundle> {

  private final ComputationTargetResolver.AtVersionCorrection _targetResolver;
  private final ComputationTargetSpecificationResolver.AtVersionCorrection _specificationResolver;

  public ExternalIdBundleResolver(final ComputationTargetResolver.AtVersionCorrection resolver) {
    _targetResolver = resolver;
    _specificationResolver = resolver.getSpecificationResolver();
  }

  public ComputationTargetSpecification getTargetSpecification(final ComputationTargetReference reference) {
    return _specificationResolver.getTargetSpecification(reference);
  }

  public ExternalIdBundle getExternalIdBundle(final ComputationTargetReference reference) {
    return reference.accept(this);
  }

  @Override
  public ExternalIdBundle visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
    final ComputationTargetSpecification specification = _specificationResolver.getTargetSpecification(requirement);
    if (specification != null) {
      return visitComputationTargetSpecification(specification);
    } else {
      return requirement.getIdentifiers();
    }
  }

  @Override
  public ExternalIdBundle visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
    final ComputationTarget target = _targetResolver.resolve(specification);
    if (target == null) {
      // Note: Don't convert the unique identifier to an external identifier
      return null;
    }
    if (target.getValue() instanceof ExternalBundleIdentifiable) {
      return ((ExternalBundleIdentifiable) target.getValue()).getExternalIdBundle();
    } else if (target.getValue() instanceof ExternalIdentifiable) {
      return ((ExternalIdentifiable) target.getValue()).getExternalId().toBundle();
    } else {
      // Note: Don't convert the unique identifier to an external identifier.
      return null;
    }
  }

}
