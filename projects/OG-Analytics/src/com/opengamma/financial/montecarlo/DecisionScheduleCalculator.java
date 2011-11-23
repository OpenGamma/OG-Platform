/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.montecarlo;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponFloating;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Calculator of decision schedule for different instruments. Used in particular for Monte Carlo pricing.
 */
public class DecisionScheduleCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, DecisionSchedule> {

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
  public DecisionSchedule visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public DecisionSchedule visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    double[] decisionTime = new double[] {swaption.getTimeToExpiry()};
    AnnuityPaymentFixed cfe = CFEC.visit(swaption.getUnderlyingSwap(), curves);
    double[][] impactTime = new double[1][cfe.getNumberOfPayments()];
    double[][] impactAmount = new double[1][cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      impactTime[0][loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
      impactAmount[0][loopcf] = cfe.getNthPayment(loopcf).getAmount();
    }
    DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

  @Override
  public DecisionSchedule visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    double[] decisionTime = new double[] {swaption.getTimeToExpiry()};
    AnnuityPaymentFixed cfeIbor = CFEC.visit(swaption.getUnderlyingSwap().getSecondLeg(), curves);
    int nbCfeIbor = cfeIbor.getNumberOfPayments();
    int nbCpnFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    double[][] impactTime = new double[1][nbCpnFixed + nbCfeIbor];
    double[][] impactAmount = new double[1][nbCpnFixed + nbCfeIbor];
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
    DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

  @Override
  public DecisionSchedule visitCapFloorIbor(final CapFloorIbor payment, final YieldCurveBundle curves) {
    double[] decisionTime = new double[] {payment.getFixingTime()};
    double fixingStartTime = payment.getFixingPeriodStartTime();
    double fixingEndTime = payment.getFixingPeriodEndTime();
    double paymentTime = payment.getPaymentTime();
    double[][] impactTime = new double[1][];
    impactTime[0] = new double[] {fixingStartTime, fixingEndTime, paymentTime};
    double[][] impactAmount = new double[1][];
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(payment.getFundingCurveName());
    final double beta = forwardCurve.getDiscountFactor(fixingStartTime) / forwardCurve.getDiscountFactor(fixingEndTime) * discountingCurve.getDiscountFactor(fixingEndTime)
        / discountingCurve.getDiscountFactor(fixingStartTime);
    impactAmount[0] = new double[] {beta, -1.0, 1.0};
    DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

  @Override
  public DecisionSchedule visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final YieldCurveBundle curves) {
    int nbCpn = annuity.getNumberOfPayments();
    double[] decisionTime = new double[nbCpn];
    double[][] impactTime = new double[nbCpn][];
    double[][] impactAmount = new double[nbCpn][];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
      AnnuityPaymentFixed cfe = CFEC.visit(annuity.getNthPayment(loopcpn), curves);
      decisionTime[loopcpn] = annuity.isFixed()[loopcpn] ? 0.0 : ((CouponFloating) annuity.getNthPayment(loopcpn)).getFixingTime();
      impactTime[loopcpn] = new double[cfe.getNumberOfPayments()];
      impactAmount[loopcpn] = new double[cfe.getNumberOfPayments()];
      for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
        impactTime[loopcpn][loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
        impactAmount[loopcpn][loopcf] = cfe.getNthPayment(loopcf).getAmount();
      }
    }
    DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

}
