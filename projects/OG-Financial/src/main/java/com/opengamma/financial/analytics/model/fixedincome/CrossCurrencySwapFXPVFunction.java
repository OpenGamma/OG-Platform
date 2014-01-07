/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of a cross-currency swap.
 */
@Deprecated
public class CrossCurrencySwapFXPVFunction extends CrossCurrencySwapFunction {

  /** Present value calculator */
  private static final PresentValueMCACalculator CALCULATOR = PresentValueMCACalculator.getInstance();

  /**
   * Default constructor.
   */
  public CrossCurrencySwapFXPVFunction() {
    super(ValueRequirementNames.FX_PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle, final ComputationTargetSpecification targetSpec,
      final ValueProperties properties) {
    final MultipleCurrencyAmount pv = derivative.accept(CALCULATOR, bundle);
    final ValueSpecification spec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, properties);
    return Collections.singleton(new ComputedValue(spec, FXUtils.getMultipleCurrencyAmountAsMatrix(pv)));
  }

}
