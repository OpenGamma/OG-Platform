/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexOptionBlackCurrencyExposureFunction extends ForexOptionBlackSingleValuedFunction {
  private static final CurrencyExposureBlackForexCalculator CALCULATOR = CurrencyExposureBlackForexCalculator.getInstance();

  public ForexOptionBlackCurrencyExposureFunction() {
    super(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxOption, data);
    return Collections.singleton(new ComputedValue(spec, FXUtils.getMultipleCurrencyAmountAsMatrix(result)));
  }
}
