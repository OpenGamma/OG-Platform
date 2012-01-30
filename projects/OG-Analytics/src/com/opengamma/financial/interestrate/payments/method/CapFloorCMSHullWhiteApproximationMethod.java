/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Pricing method of a CMS coupon in the Hull-White (extended Vasicek) model by approximation.
 * <P> Reference: M. Henrard. CMS Swaps and Caps in One-Factor Gaussian Models, SSRN working paper 985551, February 2008. 
 * Available at http://ssrn.com/abstract=985551
 */
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
    double expiryTime = cms.getFixingTime();
    FixedCouponSwap<? extends Payment> swap = cms.getUnderlyingSwap();
    double dfPayment = hwData.getCurve(swap.getFirstLeg().getDiscountCurve()).getDiscountFactor(cms.getPaymentTime());
    int nbFixed = cms.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    double[] alphaFixed = new double[nbFixed];
    double[] dfFixed = new double[nbFixed];
    double[] discountedCashFlowFixed = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      alphaFixed[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, swap.getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      dfFixed[loopcf] = hwData.getCurve(swap.getFixedLeg().getNthPayment(loopcf).getFundingCurveName()).getDiscountFactor(swap.getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swap.getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction() * swap.getFixedLeg().getNthPayment(loopcf).getNotional();
    }
    AnnuityPaymentFixed cfeIbor = CFEC.visit(swap.getSecondLeg(), hwData);
    double[] alphaIbor = new double[cfeIbor.getNumberOfPayments()];
    double[] dfIbor = new double[cfeIbor.getNumberOfPayments()];
    double[] discountedCashFlowIbor = new double[cfeIbor.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      alphaIbor[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime());
      dfIbor[loopcf] = hwData.getCurve(cfeIbor.getDiscountCurve()).getDiscountFactor(cfeIbor.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount();
    }
    double alphaPayment = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cms.getPaymentTime());
    double x0 = -alphaPayment;
    double a0 = MODEL.swapRate(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor) - cms.getStrike();
    double a1 = MODEL.swapRateD1(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    double a2 = MODEL.swapRateD2(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);

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

    double kappa = -a0 / a1 - alphaPayment; // approximation
    double kappatilde = kappa + alphaPayment;
    double omega = (cms.isCap() ? 1.0 : -1.0);
    double s2pi = 1.0 / Math.sqrt(2.0 * Math.PI);
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
