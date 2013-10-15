/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to computes the present value and sensitivities of physical delivery European swaptions with the Hull-White one factor model.
 * Reference: Henrard, M. (2003). Explicit bond option and swaption formula in Heath-Jarrow-Morton one-factor model.
 * International Journal of Theoretical and Applied Finance, 6(1):57--72.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborHullWhiteMethod}
 */
@Deprecated
public class SwaptionPhysicalFixedIborHullWhiteMethod implements PricingMethod {

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
   * The normal distribution implementation.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Computes the present value of the Physical delivery swaption.
   * @param swaption The swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    ArgumentChecker.notNull(swaption, "swaption");
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, hwData);
    return presentValue(swaption, cfe, hwData);
  }

  /**
   * Computes the present value of the Physical delivery swaption.
   * @param swaption The swaption.
   * @param cfe The swaption cash flow equivalent.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final AnnuityPaymentFixed cfe, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(hwData, "Hull-White data");
    final double expiryTime = swaption.getTimeToExpiry();
    final double[] alpha = new double[cfe.getNumberOfPayments()];
    final double[] df = new double[cfe.getNumberOfPayments()];
    final double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alpha[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime());
      df[loopcf] = hwData.getCurve(cfe.getDiscountCurve()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }
    final double kappa = MODEL.kappa(discountedCashFlow, alpha);
    final double omega = (swaption.getUnderlyingSwap().getFixedLeg().isPayer() ? -1.0 : 1.0);
    double pv = 0.0;
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      pv += discountedCashFlow[loopcf] * NORMAL.getCDF(omega * (kappa + alpha[loopcf]));
    }
    return CurrencyAmount.of(swaption.getUnderlyingSwap().getFirstLeg().getCurrency(), pv * (swaption.isLong() ? 1.0 : -1.0));
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    ArgumentChecker.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue((SwaptionPhysicalFixedIbor) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  /**
   * Present value sensitivity to Hull-White volatility parameters. The present value is computed using the explicit formula.
   * @param swaption The physical delivery swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value Hull-White parameters sensitivity.
   */
  public double[] presentValueHullWhiteSensitivity(final SwaptionPhysicalFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(hwData, "Hull-White data");
    final int nbSigma = hwData.getHullWhiteParameter().getVolatility().length;
    final double[] sigmaBar = new double[nbSigma];
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, hwData);
    final YieldAndDiscountCurve dsc = hwData.getCurve(cfe.getDiscountCurve());
    //Forward sweep
    final double expiryTime = swaption.getTimeToExpiry();
    final double[] alpha = new double[cfe.getNumberOfPayments()];
    final double[][] alphaDerivatives = new double[cfe.getNumberOfPayments()][nbSigma];
    final double[] df = new double[cfe.getNumberOfPayments()];
    final double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alpha[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime(), alphaDerivatives[loopcf]);
      df[loopcf] = dsc.getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }
    final double kappa = MODEL.kappa(discountedCashFlow, alpha);
    final double omega = (swaption.getUnderlyingSwap().getFixedLeg().isPayer() ? -1.0 : 1.0);
    //Backward sweep
    final double pvBar = 1.0;
    final double[] alphaBar = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alphaBar[loopcf] = discountedCashFlow[loopcf] * NORMAL.getPDF(omega * (kappa + alpha[loopcf])) * omega * pvBar;
    }
    for (int loopsigma = 0; loopsigma < nbSigma; loopsigma++) {
      for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
        sigmaBar[loopsigma] += alphaDerivatives[loopcf][loopsigma] * alphaBar[loopcf];
      }
    }
    return sigmaBar;
  }

  /**
   * Present value sensitivity to the curves. The present value is computed using the explicit formula.
   * @param swaption The physical delivery swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(hwData, "Hull-White data");
    final int nbSigma = hwData.getHullWhiteParameter().getVolatility().length;
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, hwData);
    //Forward sweep
    final double expiryTime = swaption.getTimeToExpiry();
    final double[] alpha = new double[cfe.getNumberOfPayments()];
    final double[][] alphaDerivatives = new double[cfe.getNumberOfPayments()][nbSigma];
    final double[] df = new double[cfe.getNumberOfPayments()];
    final double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alpha[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime(), alphaDerivatives[loopcf]);
      df[loopcf] = hwData.getCurve(cfe.getDiscountCurve()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }
    final double kappa = MODEL.kappa(discountedCashFlow, alpha);
    final double omega = (swaption.getUnderlyingSwap().getFixedLeg().isPayer() ? -1.0 : 1.0);
    final double[] ncdf = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      ncdf[loopcf] = NORMAL.getCDF(omega * (kappa + alpha[loopcf]));
    }
    //Backward sweep
    final double pvBar = 1.0;
    final double[] discountedCashFlowBar = new double[cfe.getNumberOfPayments()];
    final double[] dfBar = new double[cfe.getNumberOfPayments()];
    final double[] cfeAmountBar = new double[cfe.getNumberOfPayments()];
    final List<DoublesPair> listDfSensi = new ArrayList<>();
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      discountedCashFlowBar[loopcf] = ncdf[loopcf] * pvBar;
      dfBar[loopcf] = cfe.getNthPayment(loopcf).getAmount() * discountedCashFlowBar[loopcf];
      cfeAmountBar[loopcf] = df[loopcf] * discountedCashFlowBar[loopcf];
      final DoublesPair dfSensi = DoublesPair.of(cfe.getNthPayment(loopcf).getPaymentTime(), -cfe.getNthPayment(loopcf).getPaymentTime() * df[loopcf] * dfBar[loopcf]);
      listDfSensi.add(dfSensi);
    }
    final Map<String, List<DoublesPair>> pvsDF = new HashMap<>();
    pvsDF.put(cfe.getDiscountCurve(), listDfSensi);
    InterestRateCurveSensitivity sensitivity = new InterestRateCurveSensitivity(pvsDF);
    final Map<Double, InterestRateCurveSensitivity> cfeCurveSensi = swaption.getUnderlyingSwap().accept(CFECSC, hwData);
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      final InterestRateCurveSensitivity sensiCfe = cfeCurveSensi.get(cfe.getNthPayment(loopcf).getPaymentTime());
      if (!(sensiCfe == null)) { // There is some sensitivity to that cfe.
        sensitivity = sensitivity.plus(sensiCfe.multipliedBy(cfeAmountBar[loopcf]));
      }
    }
    return sensitivity;
  }

}
