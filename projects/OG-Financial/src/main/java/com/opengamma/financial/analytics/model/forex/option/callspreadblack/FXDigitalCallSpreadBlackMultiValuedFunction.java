/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackMultiValuedFunction;
import com.opengamma.financial.currency.CurrencyPair;

/**
 *
 */
public abstract class FXDigitalCallSpreadBlackMultiValuedFunction extends FXOptionBlackMultiValuedFunction {

  public FXDigitalCallSpreadBlackMultiValuedFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> spreads = constraints.getValues(CalculationPropertyNamesAndValues.PROPERTY_CALL_SPREAD_VALUE);
    if (spreads == null || spreads.size() != 1) {
      return null;
    }
    return super.getRequirements(context, target, desiredValue);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target)
        .withoutAny(CALCULATION_METHOD)
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.CALL_SPREAD_BLACK_METHOD)
        .withAny(CalculationPropertyNamesAndValues.PROPERTY_CALL_SPREAD_VALUE);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String putCurve, final String putCurveCalculationConfig,
      final String callCurve, final String callCurveCalculationConfig, final CurrencyPair baseQuotePair, final ValueProperties optionalProperties) {
    final ValueProperties.Builder properties = super.getResultProperties(target, putCurve, putCurveCalculationConfig, callCurve, callCurveCalculationConfig, baseQuotePair,
        optionalProperties)
        .withoutAny(CALCULATION_METHOD)
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.CALL_SPREAD_BLACK_METHOD)
        .withAny(CalculationPropertyNamesAndValues.PROPERTY_CALL_SPREAD_VALUE);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final String callSpread = desiredValue.getConstraint(CalculationPropertyNamesAndValues.PROPERTY_CALL_SPREAD_VALUE);
    final ValueProperties.Builder properties = super.getResultProperties(target, desiredValue, baseQuotePair)
        .withoutAny(CALCULATION_METHOD)
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.CALL_SPREAD_BLACK_METHOD)
        .with(CalculationPropertyNamesAndValues.PROPERTY_CALL_SPREAD_VALUE, callSpread);
    return properties;
  }
}
