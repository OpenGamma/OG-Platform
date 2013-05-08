/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.id.ExternalId;

/**
 * 
 */
public abstract class CurveProvider<T> {
  private static final ValueRequirementVisitor VALUE_REQUIREMENTS = new ValueRequirementVisitor();

  public abstract Set<ValueRequirement> getValueRequirements(T target, final InstrumentExposureConfiguration exposureConfiguation);

  protected Set<ValueRequirement> getValueRequirements(final ExternalId id, final InstrumentExposureConfiguration exposureConfiguration) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    for (final Map.Entry<CurveConfigurationSpecification, Collection<CurveConfiguration>> entry : exposureConfiguration.getConfigurationsForTargets().entrySet()) {
      if (entry.getKey().getTargetId().equals(id)) {
        for (final CurveConfiguration configuration : entry.getValue()) {
          requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL, configuration.accept(VALUE_REQUIREMENTS)));
        }
        return requirements;
      }
    }
    return requirements;
  }

  private static class ValueRequirementVisitor implements CurveConfigurationVisitor<ValueProperties> {

    public ValueRequirementVisitor() {
    }

    @Override
    public ValueProperties visitDiscountingCurveConfiguration(final DiscountingCurveConfiguration configuration) {
      return ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, configuration.getCurveName())
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, configuration.getCalculationConfigurationName())
          .get();
    }

    @Override
    public ValueProperties visitOvernightCurveConfiguration(final OvernightCurveConfiguration configuration) {
      return ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, configuration.getCurveName())
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, configuration.getCalculationConfigurationName())
          .get();
    }

    @Override
    public ValueProperties visitForwardIborCurveConfiguration(final ForwardIborCurveConfiguration configuration) {
      return ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, configuration.getCurveName())
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, configuration.getCalculationConfigurationName())
          .get();
    }

  }
}
