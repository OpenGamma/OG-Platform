/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Pricing method of a CMS coupon in the Hull-White (extended Vasicek) model by approximation.
 * <P> Reference: M. Henrard. CMS Swaps and Caps in One-Factor Gaussian Models, SSRN working paper 985551, February 2008.
 * Available at http://ssrn.com/abstract=985551
 * @deprecated {@link com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorCMSHullWhiteApproximationMethod}
 */
@Deprecated
public final class CapFloorCMSHullWhiteApproximationMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final CapFloorCMSHullWhiteApproximationMethod INSTANCE = new CapFloorCMSHullWhiteApproximationMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CapFloorCMSHullWhiteApproximationMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CapFloorCMSHullWhiteApproximationMethod() {
  }

  /**
   * The model used in computations.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * The normal distribution implementation.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  public CurrencyAmount presentValue(final CapFloorCMS cms, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    Validate.notNull(cms);
    Validate.notNull(hwData);
    final double expiryTime = cms.getFixingTime();
    final SwapFixedCoupon<? extends Payment> swap = cms.getUnderlyingSwap();
    final double dfPayment = hwData.getCurve(swap.getFirstLeg().getDiscountCurve()).getDiscountFactor(cms.getPaymentTime());
    final int nbFixed = cms.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    final double[] alphaFixed = new double[nbFixed];
    final double[] dfFixed = new double[nbFixed];
    final double[] discountedCashFlowFixed = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      alphaFixed[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, swap.getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      dfFixed[loopcf] = hwData.getCurve(swap.getFixedLeg().getNthPayment(loopcf).getFundingCurveName()).getDiscountFactor(swap.getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swap.getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction() * swap.getFixedLeg().getNthPayment(loopcf).getNotional();
    }
    final AnnuityPaymentFixed cfeIbor = swap.getSecondLeg().accept(CFEC, hwData);
    final double[] alphaIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] dfIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] discountedCashFlowIbor = new double[cfeIbor.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      alphaIbor[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime());
      dfIbor[loopcf] = hwData.getCurve(cfeIbor.getDiscountCurve()).getDiscountFactor(cfeIbor.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount();
    }
    final double alphaPayment = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cms.getPaymentTime());
    final double x0 = -alphaPayment;
    final double a0 = MODEL.swapRate(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor) - cms.getStrike();
    final double a1 = MODEL.swapRateDx1(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    final double a2 = MODEL.swapRateDx2(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);

    //    AnnuityPaymentFixed cfe = CFEC.visit(swap.withCoupon(cms.getStrike()), hwData);
    //    double[] alpha = new double[cfe.getNumberOfPayments()];
    //    double[] df = new double[cfe.getNumberOfPayments()];
    //    double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    //    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
    //      alpha[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime());
    //      df[loopcf] = hwData.getCurve(cfe.getDiscountCurve()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
    //      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    //    }
    //    double kappaTest = MODEL.kappa(discountedCashFlow, alpha);

    final double kappa = -a0 / a1 - alphaPayment; // approximation
    final double kappatilde = kappa + alphaPayment;
    final double omega = (cms.isCap() ? 1.0 : -1.0);
    final double s2pi = 1.0 / Math.sqrt(2.0 * Math.PI);
    double pv = omega * (a0 + a2 / 2) * NORMAL.getCDF(-omega * kappatilde) + s2pi * Math.exp(-kappatilde * kappatilde / 2) * (a1 + a2 * kappatilde);
    pv *= dfPayment * cms.getNotional() * cms.getPaymentYearFraction();
    return CurrencyAmount.of(cms.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorCMS, "Coupon CMS");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Curves with HW data");
    return presentValue((CapFloorCMS) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }
}
