/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.id.ExternalId;

/**
 * 
 */
public class CurveNodeCurveProvider extends CurveProvider<CurveSpecification> {
  private final ConventionSource _conventionSource;

  public CurveNodeCurveProvider(final ConventionSource conventionSource) {
    _conventionSource = conventionSource;
  }

  @Override
  public Set<ValueRequirement> getValueRequirements(final CurveSpecification target, final InstrumentExposureConfiguration exposureConfiguration) {
    final List<ExternalId> ids = new ArrayList<>();
    final CurveConfigurationForCurveNodeVisitor perNodeVisitor = new CurveConfigurationForCurveNodeVisitor(_conventionSource);
    for (final CurveNodeWithIdentifier node : target.getNodes()) {
      ids.add(node.getIdentifier());
      ids.addAll(node.getCurveNode().accept(perNodeVisitor));
    }
    for (final ExternalId id : ids) {
      final Set<ValueRequirement> requirements = getValueRequirements(id, exposureConfiguration);
      if (!requirements.isEmpty()) {
        return requirements;
      }
    }
    throw new OpenGammaRuntimeException("Could not get yield curve value requirements for " + target);
  }

}
