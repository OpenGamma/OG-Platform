/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.montecarlo;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.CashFlowEquivalentCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * Calculator of decision schedule for different instruments. Used in particular for Monte Carlo pricing.
 */
public class DecisionScheduleDerivativeCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, DecisionScheduleDerivative> {

  /**
   * The cash-flow equivalent calculator.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  private static final CashFlowEquivalentCurveSensitivityCalculator CFECSC = CashFlowEquivalentCurveSensitivityCalculator.getInstance();

  /**
   * The unique instance of the calculator.
   */
  private static final DecisionScheduleDerivativeCalculator INSTANCE = new DecisionScheduleDerivativeCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static DecisionScheduleDerivativeCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  DecisionScheduleDerivativeCalculator() {
  }

  @Override
  public DecisionScheduleDerivative visit(final InterestRateDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public DecisionScheduleDerivative visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    double[] decisionTime = new double[] {swaption.getTimeToExpiry()};
    AnnuityPaymentFixed cfe = CFEC.visit(swaption.getUnderlyingSwap(), curves);
    double[][] impactTime = new double[1][cfe.getNumberOfPayments()];
    double[][] impactAmount = new double[1][cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      impactTime[0][loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
      impactAmount[0][loopcf] = cfe.getNthPayment(loopcf).getAmount();
    }
    ArrayList<Map<Double, PresentValueSensitivity>> impactAmountDerivative = new ArrayList<Map<Double, PresentValueSensitivity>>();
    impactAmountDerivative.add(CFECSC.visit(swaption.getUnderlyingSwap(), curves));
    DecisionScheduleDerivative decision = new DecisionScheduleDerivative(decisionTime, impactTime, impactAmount, impactAmountDerivative);
    return decision;
  }

}
