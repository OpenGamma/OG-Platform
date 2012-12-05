/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.pure;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;

import java.util.Set;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;

/**
 *
 */
public abstract class PureBlackVolatilitySurfaceNoDividendCorrectionFunction extends PureBlackVolatilitySurfaceFunction {
  /** No dividends */
  public static final String NO_DIVIDENDS = "None";

  /**
   * Spline interpolator function for pure Black volatility surfaces
   */
  public static class Spline extends PureBlackVolatilitySurfaceNoDividendCorrectionFunction {

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> specificRequirements = BlackVolatilitySurfacePropertyUtils.ensureSplineVolatilityInterpolatorProperties(desiredValue.getConstraints());
      if (specificRequirements == null) {
        return null;
      }
      final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      requirements.addAll(specificRequirements);
      return requirements;
    }

    @Override
    protected ValueProperties getResultProperties() {
      return BlackVolatilitySurfacePropertyUtils.addSplineVolatilityInterpolatorProperties(createValueProperties().get())
          .withAny(SURFACE)
          .withAny(CURVE)
          .withAny(CURVE_CURRENCY)
          .withAny(CURVE_CALCULATION_CONFIG)
          .with(PROPERTY_DIVIDEND_TREATMENT, NO_DIVIDENDS).get();
    }

    @Override
    protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
      final String surfaceName = desiredValue.getConstraint(SURFACE);
      final String curveName = desiredValue.getConstraint(CURVE);
      final String currency = desiredValue.getConstraint(CURVE_CURRENCY);
      final String curveCalculationConfig = desiredValue.getConstraint(CURVE_CALCULATION_CONFIG);
      return BlackVolatilitySurfacePropertyUtils.addSplineVolatilityInterpolatorProperties(desiredValue.getConstraints(), desiredValue)
          .with(SURFACE, surfaceName)
          .with(CURVE, curveName)
          .with(CURVE_CURRENCY, currency)
          .with(CURVE_CALCULATION_CONFIG, curveCalculationConfig)
          .with(PROPERTY_DIVIDEND_TREATMENT, NO_DIVIDENDS).get();
    }
  }

  @Override
  protected AffineDividends getDividends(final FunctionInputs inputs) {
    return AffineDividends.noDividends();
  }
}
