/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CurveProviders {
  private static final ValueRequirementVisitor VALUE_REQUIREMENTS = new ValueRequirementVisitor();
  private static final CurveConfigurationForSecurityVisitor PER_SECURITY_VISITOR = new CurveConfigurationForSecurityVisitor();

  public static Set<ValueRequirement> getValueRequirements(final Object target, final InstrumentExposureConfiguration exposureConfiguration) {
    final List<ExternalId> ids = new ArrayList<>();
    if (target instanceof FinancialSecurity) {
      final FinancialSecurity security = (FinancialSecurity) target;
      ids.addAll(security.accept(PER_SECURITY_VISITOR));
    } else if (target instanceof Trade && ((Trade) target).getSecurity() instanceof FinancialSecurity) {
      final FinancialSecurity security = (FinancialSecurity) ((Trade) target).getSecurity();
      ids.addAll(security.accept(PER_SECURITY_VISITOR));
    } else {
      throw new OpenGammaRuntimeException("Unhandled type " + target);
    }
    for (final ExternalId id : ids) {
      final Set<ValueRequirement> requirements = getValueRequirements(id, exposureConfiguration);
      if (!requirements.isEmpty()) {
        return requirements;
      }
    }
    throw new OpenGammaRuntimeException("Could not get yield curve value requirements for " + target);
  }

  public static Set<ValueRequirement> getValueRequirements(final CurveSpecification target, final InstrumentExposureConfiguration exposureConfiguration, final ConventionSource conventionSource) {
    final List<ExternalId> ids = new ArrayList<>();
    final CurveConfigurationForCurveNodeVisitor perNodeVisitor = new CurveConfigurationForCurveNodeVisitor(conventionSource);
    final CurveSpecification specification = target;
    for (final CurveNodeWithIdentifier node : specification.getNodes()) {
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

  private static Set<ValueRequirement> getValueRequirements(final ExternalId id, final InstrumentExposureConfiguration exposureConfiguration) {
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

  public static MulticurveProviderDiscount getCurveProvider(final InstrumentExposureConfiguration exposureConfiguration, final FXMatrix fxMatrix) {
    final Map<Currency, YieldAndDiscountCurve> discountingCurves;
    final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves;
    final Map<IndexON, YieldAndDiscountCurve> forwardONCurves;
    return null;
    //return new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
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
