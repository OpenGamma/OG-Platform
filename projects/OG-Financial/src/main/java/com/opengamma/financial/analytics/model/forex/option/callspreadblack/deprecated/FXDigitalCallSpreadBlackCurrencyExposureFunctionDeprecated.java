/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXDigitalCallSpreadBlackCurrencyExposureFunctionDeprecated
 */
@Deprecated
public class FXDigitalCallSpreadBlackCurrencyExposureFunctionDeprecated extends FXDigitalCallSpreadBlackSingleValuedFunctionDeprecated {

  public FXDigitalCallSpreadBlackCurrencyExposureFunctionDeprecated() {
    super(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxDigital, final double spread, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final CurrencyExposureCallSpreadBlackForexCalculator calculator = new CurrencyExposureCallSpreadBlackForexCalculator(spread);
    final MultipleCurrencyAmount result = fxDigital.accept(calculator, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }
}
