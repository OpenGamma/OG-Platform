/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.basicblack;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.ConstantBlackDiscountingPVSwaptionFunction;

/**
 * Calculates the present value of a swaption using the Black method with no volatility modelling assumptions.
 * The implied volatility is read directly from the market data system.
 * <p>
 * Produces a result for {@link ValueRequirementNames#PRESENT_VALUE} using {@link PresentValueBlackCalculator}.
 * @deprecated Use {@link ConstantBlackDiscountingPVSwaptionFunction}
 */
@Deprecated
public class SwaptionBasicBlackPresentValueFunction extends SwaptionBasicBlackFunction {
  /** The calculator */
  private static final PresentValueBlackCalculator CALCULATOR = PresentValueBlackCalculator.getInstance();

  /**
   * Sets {@link ValueRequirementNames#PRESENT_VALUE} as the result.
   */
  public SwaptionBasicBlackPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double pv = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, pv));
  }
}
