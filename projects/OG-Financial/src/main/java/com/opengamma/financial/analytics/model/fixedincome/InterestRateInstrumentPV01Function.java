/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PV01Calculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.discounting.DiscountingPV01Function;

/**
 * Computes the PV01 of interest rate instruments.
 * @deprecated Use {@link DiscountingPV01Function}
 */
@Deprecated
public class InterestRateInstrumentPV01Function extends InterestRateInstrumentCurveSpecificFunction {
  /** The calculator */
  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PV01}
   */
  public InterestRateInstrumentPV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  public Set<ComputedValue> getResults(final InstrumentDerivative derivative, final String curveName, final YieldCurveBundle curves,
      final String curveCalculationConfigName, final String curveCalculationMethod, final FunctionInputs inputs, final ComputationTarget target,
      final ValueSpecification resultSpec) {
    final Map<String, Double> pv01 = CALCULATOR.visit(derivative, curves);
    if (!pv01.containsKey(curveName)) {
      if (derivative instanceof Swap) {
        @SuppressWarnings("unchecked")
        final Swap<Payment, Payment> swap = (Swap<Payment, Payment>) derivative;
        final Annuity<Payment> firstLeg = swap.getFirstLeg();
        final Annuity<Payment> secondLeg = swap.getSecondLeg();
        if ((firstLeg.getPayments().length <= 1 && secondLeg.getPayments().length == 1) || (firstLeg.getPayments().length == 1 && secondLeg.getPayments().length <= 1)) {
          boolean firstPaymentFixed = true;
          if (firstLeg.getNumberOfPayments() > 0) {
            final Payment lastPayment = firstLeg.getNthPayment(0);
            firstPaymentFixed = lastPayment instanceof CouponFixed || lastPayment instanceof PaymentFixed;
          }
          boolean secondPaymentFixed = true;
          if (secondLeg.getPayments().length > 0) {
            final Payment lastPayment = secondLeg.getNthPayment(0);
            secondPaymentFixed = lastPayment instanceof CouponFixed || lastPayment instanceof PaymentFixed;
          }
          if (firstPaymentFixed && secondPaymentFixed) {
            // This will happen in the case where the final floating payment has fixed and the PV01 to the forward curve is requested.
            // In this case, it is reasonable to return 0.
            return Collections.singleton(new ComputedValue(resultSpec, 0.));
          }
        }
      }
      throw new OpenGammaRuntimeException("Could not get PV01 for curve named " + curveName + "; should never happen");
    }
    return Collections.singleton(new ComputedValue(resultSpec, pv01.get(curveName)));
  }


}
