/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.VegaMatrixHelper;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.forex.calculator.PresentValueForexVegaQuoteSensitivityCalculator;
import com.opengamma.financial.forex.method.PresentValueVolatilityQuoteSensitivityDataBundle;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public class ForexVanillaOptionVegaQuoteFunction extends ForexVanillaOptionFunction {
  private static final PresentValueForexVegaQuoteSensitivityCalculator CALCULATOR = PresentValueForexVegaQuoteSensitivityCalculator.getInstance();

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target,
      final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName, final String callForwardCurveName, final String surfaceName) {
    final PresentValueVolatilityQuoteSensitivityDataBundle result = CALCULATOR.visit(fxOption, data);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, putFundingCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, callFundingCurveName)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, ForexVolatilitySurfaceFunction.INSTRUMENT_TYPE).get();
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.VEGA_QUOTE_MATRIX, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(resultSpec, VegaMatrixHelper.getVegaFXQuoteMatrixInStandardForm(result)));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ValuePropertyNames.SURFACE)
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, ForexVolatilitySurfaceFunction.INSTRUMENT_TYPE).get();
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.VEGA_QUOTE_MATRIX, target.toSpecification(), properties);
    return Collections.singleton(resultSpec);
  }
}
