/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;

/**
 *
 */
public abstract class EquityIndexOptionBAWFunction extends EquityIndexOptionFunction {

  public EquityIndexOptionBAWFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (!(target.getSecurity() instanceof EquityIndexOptionSecurity)) {
      return false;
    }
    return ((EquityIndexOptionSecurity) target.getSecurity()).getExerciseType() instanceof AmericanExerciseType;
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BAW_METHOD)
        .with(CalculationPropertyNamesAndValues.PROPERTY_MODEL_TYPE, CalculationPropertyNamesAndValues.ANALYTIC)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR)
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String fundingCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String volSurfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String smileInterpolatorName = desiredValue.getConstraint(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
    final ValueProperties.Builder builder = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BAW_METHOD)
        .with(CalculationPropertyNamesAndValues.PROPERTY_MODEL_TYPE, CalculationPropertyNamesAndValues.ANALYTIC)
        .with(ValuePropertyNames.CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveConfigName)
        .with(ValuePropertyNames.SURFACE, volSurfaceName)
        .with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, smileInterpolatorName)
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    return builder;
  }
}
