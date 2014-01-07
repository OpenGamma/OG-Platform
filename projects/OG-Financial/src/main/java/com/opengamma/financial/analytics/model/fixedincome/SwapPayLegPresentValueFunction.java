/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import static com.opengamma.engine.value.ValueRequirementNames.PAY_LEG_PRESENT_VALUE;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Function that calculated the present value of the pay leg of a swap.
 * @deprecated The parent class is deprecated.
 */
@Deprecated
public class SwapPayLegPresentValueFunction extends InterestRateInstrumentFunction {
  /** The calculator */
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PAY_LEG_PRESENT_VALUE}
   */
  public SwapPayLegPresentValueFunction() {
    super(PAY_LEG_PRESENT_VALUE);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getSecurity() instanceof SwapSecurity;
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle, final FinancialSecurity security, final ComputationTarget target,
      final String curveCalculationConfigName, final String currency) {
    if (!(derivative instanceof Swap<?, ?>)) {
      throw new OpenGammaRuntimeException("Expected a swap, have " + derivative.getClass());
    }
    @SuppressWarnings("unchecked")
    final Swap<? extends Payment, ? extends Payment> swap = (Swap<? extends Payment, ? extends Payment>) derivative;
    double presentValue;
    if (swap.getFirstLeg().isPayer()) {
      presentValue = swap.getFirstLeg().accept(CALCULATOR, bundle);
    } else {
      presentValue = swap.getSecondLeg().accept(CALCULATOR, bundle);
    }
    return Collections.singleton(new ComputedValue(getResultSpec(target, curveCalculationConfigName, currency), presentValue));
  }

}
