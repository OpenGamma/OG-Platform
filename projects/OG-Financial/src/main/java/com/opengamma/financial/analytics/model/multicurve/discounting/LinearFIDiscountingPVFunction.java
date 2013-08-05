/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class LinearFixedIncomeDiscountingPVFunction extends DiscountingFunction {

  public LinearFixedIncomeDiscountingPVFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected InstrumentDerivativeVisitor<MulticurveProviderInterface, MultipleCurrencyAmount> getCalculator() {
    return PresentValueDiscountingCalculator.getInstance();
  }

}
