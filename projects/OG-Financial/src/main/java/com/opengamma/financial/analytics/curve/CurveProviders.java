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

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CurveProviders {

  public static Set<ValueRequirement> getValueRequirements(final ComputationTargetSpecification targetSpec, final InstrumentExposureConfiguration exposureConfiguration,
      final ComputationTargetSpecification yieldCurveSpec, final String curveCalculationMethod) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    final MyVisitor visitor = new MyVisitor(curveCalculationMethod);
    for (final Map.Entry<CurveConfigurationSpecification, Collection<CurveConfiguration>> entry : exposureConfiguration.getConfigurationsForTargets().entrySet()) {
      if (entry.getKey().getTargetSpec().equals(targetSpec)) {
        for (final CurveConfiguration configuration : entry.getValue()) {
          requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, yieldCurveSpec, configuration.accept(visitor)));
        }
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

  private static class MyVisitor implements CurveConfigurationVisitor<ValueProperties> {
    private final String _curveCalculationMethod;

    public MyVisitor(final String curveCalculationMethod) {
      ArgumentChecker.notNull(curveCalculationMethod, "curve calculation method");
      _curveCalculationMethod = curveCalculationMethod;
    }

    @Override
    public ValueProperties visitDiscountingCurveConfiguration(final DiscountingCurveConfiguration configuration) {
      return ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, configuration.getCurveName())
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, configuration.getCurveName())
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationMethod)
          .get();
    }

    @Override
    public ValueProperties visitOvernightCurveConfiguration(final OvernightCurveConfiguration configuration) {
      return ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, configuration.getCurveName())
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, configuration.getCurveName())
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationMethod)
          .get();
    }

    @Override
    public ValueProperties visitForwardIborCurveConfiguration(final ForwardIborCurveConfiguration configuration) {
      return ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, configuration.getCurveName())
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, configuration.getCurveName())
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _curveCalculationMethod)
          .get();
    }

  }
}
