/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.forex.calculator.PresentValueCurveSensitivityBlackForexCalculator;
import com.opengamma.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexOptionPresentValueCurveSensitivityFunction extends ForexOptionBlackFunction {
  private static final PresentValueCurveSensitivityBlackForexCalculator CALCULATOR = PresentValueCurveSensitivityBlackForexCalculator.getInstance();

  public ForexOptionPresentValueCurveSensitivityFunction() {
    super(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final MultipleCurrencyInterestRateCurveSensitivity result = CALCULATOR.visit(fxOption, data);
    Validate.isTrue(result.getCurrencies().size() == 1, "Only one currency");
    final Currency ccy = result.getCurrencies().iterator().next();
    return Collections.singleton(new ComputedValue(spec, result.getSensitivity(ccy)));
  }

}
