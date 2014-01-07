/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilitySensitivityBlackForexCalculator;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function calculating the total Black volatility sensitivity.
 * @deprecated The parent of this class is deprecated
 */
@Deprecated
public class FXOptionBlackVegaFunction extends FXOptionBlackSingleValuedFunction {

  /**
   * The relevant calculator.
   */
  private static final PresentValueBlackVolatilitySensitivityBlackForexCalculator CALCULATOR = PresentValueBlackVolatilitySensitivityBlackForexCalculator.getInstance();

  public FXOptionBlackVegaFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final PresentValueForexBlackVolatilitySensitivity result = forex.accept(CALCULATOR, data);
      final CurrencyAmount vegaValue = result.toSingleValue();
      final double scale = Double.parseDouble(desiredValues.iterator().next().getConstraint(ValuePropertyNames.SCALE));
      return Collections.singleton(new ComputedValue(spec, vegaValue.getAmount() * scale));
    }
    throw new OpenGammaRuntimeException("Can only calculate vega for surfaces with smiles");
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return super.getResultProperties(target)
        .withAny(ValuePropertyNames.SCALE);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String putCurve, final String putCurveCalculationConfig,
      final String callCurve, final String callCurveCalculationConfig, final CurrencyPair baseQuotePair, final ValueProperties optionalProperties) {
    final Set<String> scale = optionalProperties.getValues(ValuePropertyNames.SCALE);
    final ValueProperties.Builder properties = super.getResultProperties(target, putCurve, putCurveCalculationConfig, callCurve, callCurveCalculationConfig, baseQuotePair,
        optionalProperties);
    if (scale == null || scale.isEmpty()) {
      // return properties.with(ValuePropertyNames.SCALE, Double.toString(1));
      return properties.withAny(ValuePropertyNames.SCALE);
    }
    return properties.with(ValuePropertyNames.SCALE, scale);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final Set<String> scale = desiredValue.getConstraints().getValues(ValuePropertyNames.SCALE);
    final ValueProperties.Builder properties = super.getResultProperties(target, desiredValue, baseQuotePair);
    if (scale == null || scale.isEmpty()) {
      return properties.with(ValuePropertyNames.SCALE, Double.toString(1));
    }
    return properties.with(ValuePropertyNames.SCALE, Iterables.getOnlyElement(scale));
  }
}
