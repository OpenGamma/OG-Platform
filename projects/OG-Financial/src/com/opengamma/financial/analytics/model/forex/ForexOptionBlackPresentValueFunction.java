/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.forex.calculator.PresentValueBlackForexCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexOptionBlackPresentValueFunction extends ForexOptionBlackFunction {
  private static final PresentValueBlackForexCalculator CALCULATOR = PresentValueBlackForexCalculator.getInstance();

  public ForexOptionBlackPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxOption, data);
    ArgumentChecker.isTrue(result.size() == 1, "result size must be one; have {}", result.size());
    final CurrencyAmount ca = result.getCurrencyAmounts()[0];
    final double amount = ca.getAmount();
    return Collections.singleton(new ComputedValue(spec, amount));
  }

}
