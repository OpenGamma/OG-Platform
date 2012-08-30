/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilitySensitivityBlackForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function calculating the total Black volatility sensitivity.
 */
public class FXOneLookBarrierOptionBlackVegaFunction extends FXOneLookBarrierOptionBlackFunction {

  public FXOneLookBarrierOptionBlackVegaFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  /**
   * The relevant calculator.
   */
  private static final PresentValueBlackVolatilitySensitivityBlackForexCalculator CALCULATOR = PresentValueBlackVolatilitySensitivityBlackForexCalculator.getInstance();

  @Override
  protected Object computeValues(Set<ForexOptionVanilla> vanillaOptions, ForexOptionDataBundle<?> market) {
    double sum = 0.0;
    for (ForexOptionVanilla derivative : vanillaOptions) {
      if (market instanceof SmileDeltaTermStructureDataBundle) {
        final PresentValueForexBlackVolatilitySensitivity result = CALCULATOR.visit(derivative, market);
        final CurrencyAmount vegaValue = result.toSingleValue();
        sum += vegaValue.getAmount();
      }
      throw new OpenGammaRuntimeException("Can only calculate vega for surfaces with smiles");
    }
    return sum;
  }

}
