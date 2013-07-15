/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Pricing method of a CMS coupon in the Hull-White (extended Vasicek) model by approximation.
 * <P> Reference: M. Henrard. CMS Swaps and Caps in One-Factor Gaussian Models, SSRN working paper 985551, February 2008. 
 * Available at http://ssrn.com/abstract=985551
 */
public final class CapFloorCMSHullWhiteApproximationMethod {

  /**
   * The method unique instance.
   */
  private static final CapFloorCMSHullWhiteApproximationMethod INSTANCE = new CapFloorCMSHullWhiteApproximationMethod();

  /**
   * Private constructor.
   */
  private CapFloorCMSHullWhiteApproximationMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CapFloorCMSHullWhiteApproximationMethod getInstance() {
    return INSTANCE;
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

  /**
   * Compute the present value of a CMS cap/floor with the Hull-White (extended Vasicek) model by approximation.
   * @param cms The CMS cap/floor.
   * @param hwMulticurves The Hull-White and multi-curves provider.
   * @return The cap/floor price.
   */
  public MultipleCurrencyAmount presentValue(final CapFloorCMS cms, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    ArgumentChecker.notNull(cms, "CMS");
    ArgumentChecker.notNull(hwMulticurves, "Hull-White provider");
    Currency ccy = cms.getCurrency();
    HullWhiteOneFactorPiecewiseConstantParameters parameters = hwMulticurves.getHullWhiteParameters();
    MulticurveProviderInterface multicurves = hwMulticurves.getMulticurveProvider();
    final double expiryTime = cms.getFixingTime();
    final SwapFixedCoupon<? extends Payment> swap = cms.getUnderlyingSwap();
    final double dfPayment = multicurves.getDiscountFactor(ccy, cms.getPaymentTime());
    final int nbFixed = cms.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    final double[] alphaFixed = new double[nbFixed];
    final double[] dfFixed = new double[nbFixed];
    final double[] discountedCashFlowFixed = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      alphaFixed[loopcf] = MODEL.alpha(parameters, 0.0, expiryTime, expiryTime, swap.getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      dfFixed[loopcf] = multicurves.getDiscountFactor(ccy, swap.getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swap.getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction() * swap.getFixedLeg().getNthPayment(loopcf).getNotional();
    }
    final AnnuityPaymentFixed cfeIbor = swap.getSecondLeg().accept(CFEC, multicurves);
    final double[] alphaIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] dfIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] discountedCashFlowIbor = new double[cfeIbor.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      alphaIbor[loopcf] = MODEL.alpha(parameters, 0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime());
      dfIbor[loopcf] = multicurves.getDiscountFactor(ccy, cfeIbor.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount();
    }
    final double alphaPayment = MODEL.alpha(parameters, 0.0, expiryTime, expiryTime, cms.getPaymentTime());
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
    return MultipleCurrencyAmount.of(cms.getCurrency(), pv);
  }

}
