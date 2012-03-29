/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueForexVegaQuoteSensitivityCalculator;
import com.opengamma.analytics.financial.forex.method.PresentValueVolatilityQuoteSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.VegaMatrixHelper;

/**
 * 
 */
public class ForexOptionBlackVegaQuoteMatrixFunction extends ForexOptionBlackSingleValuedFunction {
  private static final PresentValueForexVegaQuoteSensitivityCalculator CALCULATOR = PresentValueForexVegaQuoteSensitivityCalculator.getInstance();

  public ForexOptionBlackVegaQuoteMatrixFunction() {
    super(ValueRequirementNames.VEGA_QUOTE_MATRIX);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final PresentValueVolatilityQuoteSensitivityDataBundle result = CALCULATOR.visit(fxOption, data);
    return Collections.singleton(new ComputedValue(spec, VegaMatrixHelper.getVegaFXQuoteMatrixInStandardForm(result)));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target);
    properties.with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String putCurveName, final String putForwardCurveName, final String putCurveCalculationMethod, final String callCurveName,
      final String callForwardCurveName, final String callCurveCalculationMethod, final String surfaceName, final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(putCurveName, putForwardCurveName, putCurveCalculationMethod, callCurveName,
        callForwardCurveName, callCurveCalculationMethod, surfaceName, target);
    properties.with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
    return properties;
  }
}
