/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFloating;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Calculator of decision schedule for different instruments. Used in particular for Monte Carlo pricing.
 * @deprecated Use {@link com.opengamma.analytics.financial.montecarlo.provider.DecisionScheduleCalculator}
 */
@Deprecated
public class DecisionScheduleCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, DecisionSchedule> {

  /**
   * The cash-flow equivalent calculator.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();

  /**
   * The unique instance of the calculator.
   */
  private static final DecisionScheduleCalculator INSTANCE = new DecisionScheduleCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static DecisionScheduleCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  DecisionScheduleCalculator() {
  }

  @Override
  public DecisionSchedule visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    final double[] decisionTime = new double[] {swaption.getTimeToExpiry() };
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, curves);
    final double[][] impactTime = new double[1][cfe.getNumberOfPayments()];
    final double[][] impactAmount = new double[1][cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      impactTime[0][loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
      impactAmount[0][loopcf] = cfe.getNthPayment(loopcf).getAmount();
    }
    final DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

  @Override
  public DecisionSchedule visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    final double[] decisionTime = new double[] {swaption.getTimeToExpiry() };
    final AnnuityPaymentFixed cfeIbor = swaption.getUnderlyingSwap().getSecondLeg().accept(CFEC, curves);
    final int nbCfeIbor = cfeIbor.getNumberOfPayments();
    final int nbCpnFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    final double[][] impactTime = new double[1][nbCpnFixed + nbCfeIbor];
    final double[][] impactAmount = new double[1][nbCpnFixed + nbCfeIbor];
    // Fixed leg
    for (int loopcf = 0; loopcf < nbCpnFixed; loopcf++) {
      impactTime[0][loopcf] = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime();
      impactAmount[0][loopcf] = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction()
          * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getNotional();
    }
    // Ibor leg
    for (int loopcf = 0; loopcf < nbCfeIbor; loopcf++) {
      impactTime[0][nbCpnFixed + loopcf] = cfeIbor.getNthPayment(loopcf).getPaymentTime();
      impactAmount[0][nbCpnFixed + loopcf] = cfeIbor.getNthPayment(loopcf).getAmount();
    }
    final DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

  @Override
  public DecisionSchedule visitCapFloorIbor(final CapFloorIbor payment, final YieldCurveBundle curves) {
    final double[] decisionTime = new double[] {payment.getFixingTime() };
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final double paymentTime = payment.getPaymentTime();
    final double[][] impactTime = new double[1][];
    impactTime[0] = new double[] {fixingStartTime, fixingEndTime, paymentTime };
    final double[][] impactAmount = new double[1][];
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(payment.getFundingCurveName());
    final double beta = forwardCurve.getDiscountFactor(fixingStartTime) / forwardCurve.getDiscountFactor(fixingEndTime) * discountingCurve.getDiscountFactor(fixingEndTime)
        / discountingCurve.getDiscountFactor(fixingStartTime);
    impactAmount[0] = new double[] {beta, -1.0, 1.0 };
    final DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

  @Override
  public DecisionSchedule visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final YieldCurveBundle curves) {
    final int nbCpn = annuity.getNumberOfPayments();
    final double[] decisionTime = new double[nbCpn];
    final double[][] impactTime = new double[nbCpn][];
    final double[][] impactAmount = new double[nbCpn][];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      final AnnuityPaymentFixed cfe = annuity.getNthPayment(loopcpn).accept(CFEC, curves);
      decisionTime[loopcpn] = annuity.isFixed()[loopcpn] ? 0.0 : ((CouponFloating) annuity.getNthPayment(loopcpn)).getFixingTime();
      impactTime[loopcpn] = new double[cfe.getNumberOfPayments()];
      impactAmount[loopcpn] = new double[cfe.getNumberOfPayments()];
      for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
        impactTime[loopcpn][loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
        impactAmount[loopcpn][loopcf] = cfe.getNthPayment(loopcf).getAmount();
      }
    }
    final DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

}
