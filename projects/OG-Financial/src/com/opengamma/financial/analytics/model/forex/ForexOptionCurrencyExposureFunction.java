/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.forex.calculator.CurrencyExposureBlackForexCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexOptionCurrencyExposureFunction extends ForexOptionFunction {
  private static final CurrencyExposureBlackForexCalculator CALCULATOR = CurrencyExposureBlackForexCalculator.getInstance();

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target,
      final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName, final String callForwardCurveName, final String surfaceName) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxOption, data);
    final ValueProperties.Builder properties = getResultProperties(putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName, target);
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get());
    return Collections.singleton(new ComputedValue(spec, ForexUtils.getMultipleCurrencyAmountAsMatrix(result)));
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.FX_CURRENCY_EXPOSURE;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .withAny(PROPERTY_PUT_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_PUT_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_CALL_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_CALL_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_FX_VOLATILITY_SURFACE_NAME);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName,
      final String callForwardCurveName, final String surfaceName, final ComputationTarget target) {
    return createValueProperties()
        .with(PROPERTY_PUT_FUNDING_CURVE_NAME, putFundingCurveName)
        .with(PROPERTY_PUT_FORWARD_CURVE_NAME, putForwardCurveName)
        .with(PROPERTY_CALL_FUNDING_CURVE_NAME, callFundingCurveName)
        .with(PROPERTY_CALL_FORWARD_CURVE_NAME, callForwardCurveName)
        .with(PROPERTY_FX_VOLATILITY_SURFACE_NAME, surfaceName);
  }
}
