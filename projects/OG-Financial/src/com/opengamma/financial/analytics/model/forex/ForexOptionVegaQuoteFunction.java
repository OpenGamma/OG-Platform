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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.VegaMatrixHelper;
import com.opengamma.financial.forex.calculator.PresentValueForexVegaQuoteSensitivityCalculator;
import com.opengamma.financial.forex.method.PresentValueVolatilityQuoteSensitivityDataBundle;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public class ForexOptionVegaQuoteFunction extends ForexOptionFunction {
  private static final PresentValueForexVegaQuoteSensitivityCalculator CALCULATOR = PresentValueForexVegaQuoteSensitivityCalculator.getInstance();

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target,
      final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName, final String callForwardCurveName, final String surfaceName) {
    final PresentValueVolatilityQuoteSensitivityDataBundle result = CALCULATOR.visit(fxOption, data);
    final ValueProperties.Builder properties = getResultProperties(putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName, target);
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get());
    return Collections.singleton(new ComputedValue(resultSpec, VegaMatrixHelper.getVegaFXQuoteMatrixInStandardForm(result)));
  }

  @Override
  public String getValueRequirementName() {
    return ValueRequirementNames.VEGA_QUOTE_MATRIX;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .withAny(PROPERTY_PUT_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_PUT_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_CALL_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_CALL_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_FX_VOLATILITY_SURFACE_NAME)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName,
      final String callForwardCurveName, final String surfaceName, final ComputationTarget target) {
    return createValueProperties()
        .with(PROPERTY_PUT_FUNDING_CURVE_NAME, putFundingCurveName)
        .with(PROPERTY_PUT_FORWARD_CURVE_NAME, putForwardCurveName)
        .with(PROPERTY_CALL_FUNDING_CURVE_NAME, callFundingCurveName)
        .with(PROPERTY_CALL_FORWARD_CURVE_NAME, callForwardCurveName)
        .with(PROPERTY_FX_VOLATILITY_SURFACE_NAME, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
  }
}
