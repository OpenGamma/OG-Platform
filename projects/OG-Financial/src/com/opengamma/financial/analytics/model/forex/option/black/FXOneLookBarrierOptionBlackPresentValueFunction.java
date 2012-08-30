/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackTermStructureForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackForexTermStructureBundle;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/** Produces the PV in Quote Currency, as opposed to Base */
public class FXOneLookBarrierOptionBlackPresentValueFunction extends FXOneLookBarrierOptionBlackFunction {

  private static final PresentValueBlackTermStructureForexCalculator FLAT_CALCULATOR = PresentValueBlackTermStructureForexCalculator.getInstance();
  private static final PresentValueBlackSmileForexCalculator SMILE_CALCULATOR = PresentValueBlackSmileForexCalculator.getInstance();

  public FXOneLookBarrierOptionBlackPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Object computeValues(Set<ForexOptionVanilla> vanillas, ForexOptionDataBundle<?> market) {
    double sum = 0.0;
    for (ForexOptionVanilla derivative : vanillas) {
      final MultipleCurrencyAmount result;
      if (market instanceof YieldCurveWithBlackForexTermStructureBundle) {
        result = FLAT_CALCULATOR.visit(derivative, market);
      } else {
        result = SMILE_CALCULATOR.visit(derivative, market);
      }
      ArgumentChecker.isTrue(result.size() == 1, "result size must be one; have {}", result.size());
      final CurrencyAmount ca = result.getCurrencyAmounts()[0];
      sum +=  ca.getAmount();
    }
    return sum;
  }

}
