/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCurveSensitivityCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Pricing method of a CMS coupon in the Hull-White (extended Vasicek) model by approximation.
 * <P> Reference: M. Henrard. CMS Swaps and Caps in One-Factor Gaussian Models, SSRN working paper 985551, February 2008. 
 * Available at http://ssrn.com/abstract=985551
 */
public final class CouponCMSHullWhiteApproximationMethod {

  /**
   * The method unique instance.
   */
  private static final CouponCMSHullWhiteApproximationMethod INSTANCE = new CouponCMSHullWhiteApproximationMethod();

  /**
   * Private constructor.
   */
  private CouponCMSHullWhiteApproximationMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponCMSHullWhiteApproximationMethod getInstance() {
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
   * The cash flow equivalent curve sensitivity calculator used in computations.
   */
  private static final CashFlowEquivalentCurveSensitivityCalculator CFECSC = CashFlowEquivalentCurveSensitivityCalculator.getInstance();

  /**
   * Compute the present value of a CMS coupon with the Hull-White (extended Vasicek) model by approximation.
   * @param cms The CMS coupon.
   * @param multicurvesHW The Hull-White and multi-curves provider.
   * @return The coupon price.
   */
  public MultipleCurrencyAmount presentValue(final CouponCMS cms, final HullWhiteOneFactorProviderInterface multicurvesHW) {
    ArgumentChecker.notNull(cms, "CMS");
    ArgumentChecker.notNull(multicurvesHW, "Hull-White provider");
    Currency ccy = cms.getCurrency();
    HullWhiteOneFactorPiecewiseConstantParameters parameters = multicurvesHW.getHullWhiteParameters();
    MulticurveProviderInterface multicurves = multicurvesHW.getMulticurveProvider();
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
    final double a0 = MODEL.swapRate(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    final double a2 = MODEL.swapRateDx2(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    final double pv = (a0 + a2 / 2) * dfPayment * cms.getNotional() * cms.getPaymentYearFraction();
    return MultipleCurrencyAmount.of(cms.getCurrency(), pv);
  }

  /**
   * Compute the present value of a CMS coupon with the Hull-White (extended Vasicek) model by approximation.
   * @param cms The CMS coupon.
   * @param multicurvesHW The Hull-White and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CouponCMS cms, final HullWhiteOneFactorProviderInterface multicurvesHW) {
    ArgumentChecker.notNull(cms, "CMS");
    ArgumentChecker.notNull(multicurvesHW, "Hull-White provider");
    Currency ccy = cms.getCurrency();
    HullWhiteOneFactorPiecewiseConstantParameters parameters = multicurvesHW.getHullWhiteParameters();
    MulticurveProviderInterface multicurves = multicurvesHW.getMulticurveProvider();
    final double expiryTime = cms.getFixingTime();
    final SwapFixedCoupon<? extends Payment> swap = cms.getUnderlyingSwap();
    final double payTimeCMS = cms.getPaymentTime();
    final double dfPayment = multicurves.getDiscountFactor(ccy, payTimeCMS);
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
    final int nbIbor = cfeIbor.getNumberOfPayments();
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
    final double a0 = MODEL.swapRate(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    final double a2 = MODEL.swapRateDx2(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    //    final double pv = (a0 + a2 / 2) * dfPayment * cms.getNotional() * cms.getPaymentYearFraction();
    // Backward sweep
    final double pvBar = 1.0;
    final double a2Bar = 0.5 * dfPayment * cms.getNotional() * cms.getPaymentYearFraction() * pvBar;
    final double a0Bar = dfPayment * cms.getNotional() * cms.getPaymentYearFraction() * pvBar;
    final double[] discountedCashFlowAdjIborBar0 = MODEL.swapRateDdcfi1(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    final double[] discountedCashFlowAdjFixedBar0 = MODEL.swapRateDdcff1(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    final Pair<double[], double[]> discountedCashFlowAdjXBar2 = MODEL.swapRateDx2Ddcf1(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor);
    final double[] discountedCashFlowAdjFixedBar2 = discountedCashFlowAdjXBar2.getFirst();
    final double[] discountedCashFlowAdjIborBar2 = discountedCashFlowAdjXBar2.getSecond();

    final double[] discountedCashFlowIborBar = new double[nbIbor];
    for (int loopcf = 0; loopcf < nbIbor; loopcf++) {
      discountedCashFlowIborBar[loopcf] = discountedCashFlowAdjIborBar0[loopcf] * a0Bar + discountedCashFlowAdjIborBar2[loopcf] * a2Bar;
    }
    final double[] dfIborBar = new double[nbIbor];
    for (int loopcf = 0; loopcf < nbIbor; loopcf++) {
      dfIborBar[loopcf] = cfeIbor.getNthPayment(loopcf).getAmount() * discountedCashFlowIborBar[loopcf];
    }
    final double[] cfeIborAmountBar = new double[nbIbor];
    for (int loopcf = 0; loopcf < nbIbor; loopcf++) {
      cfeIborAmountBar[loopcf] = dfIbor[loopcf] * discountedCashFlowIborBar[loopcf]; // OK
    }

    final double[] discountedCashFlowFixedBar = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      discountedCashFlowFixedBar[loopcf] = discountedCashFlowAdjFixedBar0[loopcf] * a0Bar + discountedCashFlowAdjFixedBar2[loopcf] * a2Bar;
    }
    final double[] dfFixedBar = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      dfFixedBar[loopcf] = swap.getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction() * swap.getFixedLeg().getNthPayment(loopcf).getNotional() *
          discountedCashFlowFixedBar[loopcf];
    }

    final List<DoublesPair> listDfSensi = new ArrayList<>();
    for (int loopcf = 0; loopcf < nbIbor; loopcf++) {
      final DoublesPair dfSensi = DoublesPair.of(cfeIbor.getNthPayment(loopcf).getPaymentTime(), -cfeIbor.getNthPayment(loopcf).getPaymentTime() * dfIbor[loopcf] * dfIborBar[loopcf]);
      listDfSensi.add(dfSensi);
    }
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      final DoublesPair dfSensi = DoublesPair.of(swap.getFixedLeg().getNthPayment(loopcf).getPaymentTime(), -swap.getFixedLeg().getNthPayment(loopcf).getPaymentTime() * dfFixed[loopcf] *
          dfFixedBar[loopcf]);
      listDfSensi.add(dfSensi);
    }
    final Map<String, List<DoublesPair>> pvsDF = new HashMap<>();
    pvsDF.put(multicurvesHW.getMulticurveProvider().getName(ccy), listDfSensi);
    final double dfPaymentBar = (a0 + a2 / 2) * cms.getNotional() * cms.getPaymentYearFraction() * pvBar;
    final DoublesPair dfPaymentSensi = DoublesPair.of(payTimeCMS, -payTimeCMS * dfPayment * dfPaymentBar); // Sensi to dfPayment
    listDfSensi.add(dfPaymentSensi);
    MulticurveSensitivity sensitivity = MulticurveSensitivity.ofYieldDiscounting(pvsDF);
    sensitivity = sensitivity.cleaned();
    // Sensitivity from the CFE
    final Map<Double, MulticurveSensitivity> cfeCurveSensi = swap.accept(CFECSC, multicurvesHW.getMulticurveProvider());
    for (int loopcf = 0; loopcf < nbIbor; loopcf++) {
      final MulticurveSensitivity sensiCfe = cfeCurveSensi.get(cfeIbor.getNthPayment(loopcf).getPaymentTime());
      if (!(sensiCfe == null)) { // There is some sensitivity to that cfe. No sensi of the last cf.
        sensitivity = sensitivity.plus(sensiCfe.multipliedBy(cfeIborAmountBar[loopcf]));
      }
    }
    sensitivity = sensitivity.cleaned();
    return MultipleCurrencyMulticurveSensitivity.of(ccy, sensitivity);
  }

}
