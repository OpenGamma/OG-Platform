/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.MarginPriceVisitor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Provides the reference margin price for interest rate futures options.
 * @deprecated Use {@link com.opengamma.financial.analytics.model.MarginPriceFunction}, which
 * handles instruments other than interest rate future options.
 */
@Deprecated
public class MarginPriceFunction extends InterestRateFutureOptionBlackFunction {
  /** The calculator */
  private static MarginPriceVisitor s_priceVisitor = MarginPriceVisitor.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#DAILY_PRICE}
   */
  public MarginPriceFunction() {
    super(ValueRequirementNames.DAILY_PRICE, true);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec,
      final Set<ValueRequirement> desiredValues) {
    final Double price = irFutureOption.accept(s_priceVisitor);
    return Collections.singleton(new ComputedValue(spec, price));
  }
}
