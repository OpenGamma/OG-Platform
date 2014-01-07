/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingPVSwaptionFunction;

/**
 * Calculates the present value of swaptions using the Black method.
 * @deprecated Use {@link BlackDiscountingPVSwaptionFunction}
 */
@Deprecated
public class SwaptionBlackPresentValueFunction extends SwaptionBlackFunction {
  /** The present value calculator */
  private static final PresentValueBlackCalculator CALCULATOR = PresentValueBlackCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PRESENT_VALUE}
   */
  public SwaptionBlackPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double pv = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, pv));
  }
}
