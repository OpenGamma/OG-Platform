/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXDigitalCallSpreadBlackCurveSensitivityFunctionDeprecated
 */
@Deprecated
public class FXDigitalCallSpreadBlackCurveSensitivityFunctionDeprecated extends FXDigitalCallSpreadBlackSingleValuedFunctionDeprecated {

  public FXDigitalCallSpreadBlackCurveSensitivityFunctionDeprecated() {
    super(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxDigital, final double spread, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final PresentValueCurveSensitivityCallSpreadBlackForexCalculator calculator = new PresentValueCurveSensitivityCallSpreadBlackForexCalculator(spread);
    final MultipleCurrencyInterestRateCurveSensitivity result = fxDigital.accept(calculator, data);
    ArgumentChecker.isTrue(result.getCurrencies().size() == 1, "Only one currency");
    return Collections.singleton(new ComputedValue(spec, result));
  }

}
