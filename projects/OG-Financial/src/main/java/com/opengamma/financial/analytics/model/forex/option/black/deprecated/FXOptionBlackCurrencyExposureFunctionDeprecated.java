/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureBlackSmileForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackCurrencyExposureFunction;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXOptionBlackCurrencyExposureFunction
 */
@Deprecated
public class FXOptionBlackCurrencyExposureFunctionDeprecated extends FXOptionBlackMultiValuedFunctionDeprecated {

  private static final CurrencyExposureBlackSmileForexCalculator CALCULATOR = CurrencyExposureBlackSmileForexCalculator.getInstance();

  public FXOptionBlackCurrencyExposureFunctionDeprecated() {
    super(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final MultipleCurrencyAmount result = fxOption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }
}
