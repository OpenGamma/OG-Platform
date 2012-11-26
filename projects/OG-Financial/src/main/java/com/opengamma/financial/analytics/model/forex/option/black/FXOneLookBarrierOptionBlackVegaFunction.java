/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Set;

import org.apache.commons.lang.Validate;

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
  protected Object computeValues(final Set<ForexOptionVanilla> vanillaOptions, final ForexOptionDataBundle<?> market) {
    Validate.isTrue(market instanceof SmileDeltaTermStructureDataBundle, "FXOneLookBarrierOptionBlackVegaFunction requires a Vol surface with a smile.");
    double sum = 0.0;
    for (final ForexOptionVanilla derivative : vanillaOptions) {
      final PresentValueForexBlackVolatilitySensitivity result = derivative.accept(CALCULATOR, market);
      final CurrencyAmount vegaValue = result.toSingleValue();
      sum += vegaValue.getAmount();
    }
    return sum;
  }

}
