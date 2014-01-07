/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the present value of a cross-currency swap.
 * @deprecated Return a double which is the sum of the values of the two legs without conversion. Should be removed.
 */
@Deprecated
public class CrossCurrencySwapPVFunction extends CrossCurrencySwapFunction {
  /** Present value calculator */
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  /**
   * Default constructor
   */
  public CrossCurrencySwapPVFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle, final ComputationTargetSpecification targetSpec,
      final ValueProperties properties) {
    final double pv = derivative.accept(CALCULATOR, bundle);
    final ValueSpecification spec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, properties);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

}
