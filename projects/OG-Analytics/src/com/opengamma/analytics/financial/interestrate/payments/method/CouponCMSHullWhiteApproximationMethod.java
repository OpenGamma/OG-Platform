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
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Pricing method of a CMS coupon in the Hull-White (extended Vasicek) model by approximation.
 * <P> Reference: M. Henrard. CMS Swaps and Caps in One-Factor Gaussian Models, SSRN working paper 985551, February 2008. 
 * Available at http://ssrn.com/abstract=985551
 */
public final class CouponCMSHullWhiteApproximationMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponCMSHullWhiteApproximationMethod INSTANCE = new CouponCMSHullWhiteApproximationMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponCMSHullWhiteApproximationMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponCMSHullWhiteApproximationMethod() {
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
   * Compute the present value of a CMS coupon with the Hull-White (extended Vasicek) model by approximation.
   * @param cmsCoupon The CMS coupon.
   * @param hwData The Hull-White parameters and the curves.
   * @return The coupon price.
   */
  public CurrencyAmount presentValue(final CouponCMS cmsCoupon, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    Validate.notNull(cmsCoupon);
    Validate.notNull(hwData);
    double expiryTime = cmsCoupon.getFixingTime();
    FixedCouponSwap<? extends Payment> swap = cmsCoupon.getUnderlyingSwap();
    double dfPayment = hwData.getCurve(swap.getFirstLeg().getDiscountCurve()).getDiscountFactor(cmsCoupon.getPaymentTime());
    int nbFixed = cmsCoupon.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
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
    double alphaPayment = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cmsCoupon.getPaymentTime());
    double x0 = -alphaPayment;
    double a0 = MODEL.swapRate(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    double a2 = MODEL.swapRateD2(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    double pv = (a0 + a2 / 2) * dfPayment * cmsCoupon.getNotional() * cmsCoupon.getPaymentYearFraction();
    return CurrencyAmount.of(cmsCoupon.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponCMS, "Coupon CMS");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Curves with HW data");
    return presentValue((CouponCMS) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }
}
