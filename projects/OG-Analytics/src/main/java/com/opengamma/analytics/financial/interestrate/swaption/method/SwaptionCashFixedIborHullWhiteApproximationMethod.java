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

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the present value of cash-settled European swaptions with the Hull-White one factor model by a third order approximation.
 * Reference: Henrard, M., Cash-Settled Swaptions: How Wrong are We? (November 2010). Available at SSRN: http://ssrn.com/abstract=1703846
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborHullWhiteApproximationMethod}
 */
@Deprecated
public class SwaptionCashFixedIborHullWhiteApproximationMethod implements PricingMethod {

  /**
   * The model used in computations.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * The cash flow equivalent sensitivity calculator used in computations.
   */
  private static final CashFlowEquivalentCurveSensitivityCalculator CFECSC = CashFlowEquivalentCurveSensitivityCalculator.getInstance();
  /**
   * The normal distribution implementation.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Present value method using a third order approximation.
   * @param swaption The cash-settled swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    final double expiryTime = swaption.getTimeToExpiry();
    final int nbFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    final double[] alphaFixed = new double[nbFixed];
    final double[] dfFixed = new double[nbFixed];
    final double[] discountedCashFlowFixed = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      alphaFixed[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      dfFixed[loopcf] = hwData.getCurve(swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getFundingCurveName()).getDiscountFactor(
          swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction()
          * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getNotional();
    }
    final AnnuityPaymentFixed cfeIbor = swaption.getUnderlyingSwap().getSecondLeg().accept(CFEC, hwData);
    final double[] alphaIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] dfIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] discountedCashFlowIbor = new double[cfeIbor.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      alphaIbor[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime());
      dfIbor[loopcf] = hwData.getCurve(cfeIbor.getDiscountCurve()).getDiscountFactor(cfeIbor.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount();
    }
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, hwData);
    final double[] alpha = new double[cfe.getNumberOfPayments()];
    final double[] df = new double[cfe.getNumberOfPayments()];
    final double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alpha[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime());
      df[loopcf] = hwData.getCurve(cfe.getDiscountCurve()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }
    final double kappa = MODEL.kappa(discountedCashFlow, alpha);
    final int nbFixedPaymentYear = (int) Math.round(1.0 / swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    final double[] derivativesRate = new double[3];
    final double[] derivativesAnnuity = new double[3];
    final double x0 = 0.0; //    (swaption.getUnderlyingSwap().getFixedLeg().isPayer()) ? Math.max(kappa, 0) : Math.min(kappa, 0);
    final double rate = swapRate(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor, derivativesRate);
    final double annuity = annuityCash(rate, nbFixedPaymentYear, swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments(), derivativesAnnuity);
    final double[] u = new double[4];
    u[0] = annuity * (swaption.getStrike() - rate);
    u[1] = (swaption.getStrike() - rate) * derivativesAnnuity[0] * derivativesRate[0] - derivativesRate[0] * annuity;
    u[2] = (swaption.getStrike() - rate) * (derivativesAnnuity[0] * derivativesRate[1] + derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[0]) - 2 * derivativesAnnuity[0]
        * derivativesRate[0] * derivativesRate[0] - annuity * derivativesRate[1];
    u[3] = -3
        * derivativesRate[0]
        * (derivativesAnnuity[0] * derivativesRate[1] + derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[0])
        - 2
        * derivativesAnnuity[0]
        * derivativesRate[0]
        * derivativesRate[1]
        + (swaption.getStrike() - rate)
        * (derivativesAnnuity[0] * derivativesRate[2] + 3 * derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[1] + derivativesAnnuity[2] * derivativesRate[0] * derivativesRate[0]
            * derivativesRate[0]) - rate * derivativesRate[2];
    final double kappatilde = kappa + alphaIbor[0];
    final double alpha0tilde = alphaIbor[0] + x0;
    double pv;
    if (!swaption.getUnderlyingSwap().getFixedLeg().isPayer()) {
      pv = (u[0] - u[1] * alpha0tilde + u[2] * (1 + alpha[0] * alpha[0]) / 2.0 - u[3] * (alpha0tilde * alpha0tilde * alpha0tilde + 3.0 * alpha0tilde) / 6.0) * NORMAL.getCDF(kappatilde)
          + (-u[1] - u[2] * (-2.0 * alpha0tilde + kappatilde) / 2.0 + u[3] * (-3 * alpha0tilde * alpha0tilde + 3.0 * kappatilde * alpha0tilde - kappatilde * kappatilde - 2.0) / 6.0)
          * NORMAL.getPDF(kappatilde);
    } else {
      pv = -(u[0] - u[1] * alpha0tilde + u[2] * (1 + alpha[0] * alpha[0]) / 2.0 - u[3] * (alpha0tilde * alpha0tilde * alpha0tilde + 3.0 * alpha0tilde) / 6.0) * NORMAL.getCDF(-kappatilde)
          + (-u[1] - u[2] * (-2.0 * alpha0tilde + kappatilde) / 2.0 + u[3] * (-3 * alpha0tilde * alpha0tilde + 3.0 * kappatilde * alpha0tilde - kappatilde * kappatilde - 2.0) / 6.0)
          * NORMAL.getPDF(kappatilde);
    }
    final double notional = Math.abs(swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getNotional());
    return CurrencyAmount.of(swaption.getCurrency(), pv * notional * dfIbor[0] * (swaption.isLong() ? 1.0 : -1.0));
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionCashFixedIbor, "Cash delivery swaption");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue((SwaptionCashFixedIbor) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  /**
   * Present value sensitivity to Hull-White volatility parameters. The present value is computed using a third order approximation.
   * @param swaption The cash-settled swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value HullWhite parameters sensitivity.
   */
  public double[] presentValueHullWhiteSensitivity(final SwaptionCashFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    // Forward sweep
    final double expiryTime = swaption.getTimeToExpiry();
    final int nbFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    final double[] alphaFixed = new double[nbFixed];
    final double[] dfFixed = new double[nbFixed];
    final double[] discountedCashFlowFixed = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      alphaFixed[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      dfFixed[loopcf] = hwData.getCurve(swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getFundingCurveName()).getDiscountFactor(
          swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction()
          * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getNotional();
    }
    final AnnuityPaymentFixed cfeIbor = swaption.getUnderlyingSwap().getSecondLeg().accept(CFEC, hwData);
    final double[] alphaIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] dfIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] discountedCashFlowIbor = new double[cfeIbor.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      alphaIbor[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime());
      dfIbor[loopcf] = hwData.getCurve(cfeIbor.getDiscountCurve()).getDiscountFactor(cfeIbor.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount();
    }
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, hwData);
    final double[] alpha = new double[cfe.getNumberOfPayments()];
    final double[] df = new double[cfe.getNumberOfPayments()];
    final double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alpha[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime());
      df[loopcf] = hwData.getCurve(cfe.getDiscountCurve()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }
    final double kappa = MODEL.kappa(discountedCashFlow, alpha);
    final int nbFixedPaymentYear = (int) Math.round(1.0 / swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    final double[] derivativesRate = new double[3];
    final double[] derivativesAnnuity = new double[3];
    final double x0 = 0.0; //    (swaption.getUnderlyingSwap().getFixedLeg().isPayer()) ? Math.max(kappa, 0) : Math.min(kappa, 0);
    final double rate = swapRate(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor, derivativesRate);
    final double annuity = annuityCash(rate, nbFixedPaymentYear, swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments(), derivativesAnnuity);
    final double[] u = new double[4];
    u[0] = annuity * (swaption.getStrike() - rate);
    u[1] = (swaption.getStrike() - rate) * derivativesAnnuity[0] * derivativesRate[0] - derivativesRate[0] * annuity;
    u[2] = (swaption.getStrike() - rate) * (derivativesAnnuity[0] * derivativesRate[1] + derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[0]) - 2 * derivativesAnnuity[0]
        * derivativesRate[0] * derivativesRate[0] - annuity * derivativesRate[1];
    u[3] = (-3 * derivativesRate[0] * (derivativesAnnuity[0] * derivativesRate[1] + derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[0]))
        - (2 * derivativesAnnuity[0] * derivativesRate[0] * derivativesRate[1])
        + ((swaption.getStrike() - rate) * (derivativesAnnuity[0] * derivativesRate[2] + 3 * derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[1] + derivativesAnnuity[2]
            * derivativesRate[0] * derivativesRate[0] * derivativesRate[0])) - (rate * derivativesRate[2]);
    final double kappatilde = kappa + alphaIbor[0];
    final double alpha0tilde = alphaIbor[0] + x0;
    double ncdf;
    final double npdf = NORMAL.getPDF(kappatilde);
    if (!swaption.getUnderlyingSwap().getFixedLeg().isPayer()) {
      ncdf = NORMAL.getCDF(kappatilde);
    } else {
      ncdf = NORMAL.getCDF(-kappatilde);
    }
    final double notional = Math.abs(swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getNotional());
    // Backward sweep
    final double pvTotalBar = 1.0;
    final double pvBar = notional * dfIbor[0] * (swaption.isLong() ? 1.0 : -1.0) * pvTotalBar;
    double alpha0tildeBar = 0.0;
    double kappatildeBar = 0.0;
    final double[] uBar = new double[4];
    if (!swaption.getUnderlyingSwap().getFixedLeg().isPayer()) {
      alpha0tildeBar = ((-u[1] - u[3] * (3 * alpha0tilde * alpha0tilde + 3.0) / 6.0) * ncdf + (u[2] + u[3] * (-6.0 * alpha0tilde + 3.0 * kappatilde) / 6.0) * npdf) * pvBar;
      kappatildeBar = ((u[0] - u[1] * alpha0tilde + u[2] * (1 + alpha[0] * alpha[0]) / 2.0 - u[3] * (alpha0tilde * alpha0tilde * alpha0tilde + 3.0 * alpha0tilde) / 6.0) * npdf
          + (-u[2] / 2.0 + u[3] * (3.0 * alpha0tilde - 2.0 * kappatilde) / 6.0) * npdf + (-u[1] - u[2] * (-2.0 * alpha0tilde + kappatilde) / 2.0 + u[3]
          * (-3 * alpha0tilde * alpha0tilde + 3.0 * kappatilde * alpha0tilde - kappatilde * kappatilde - 2.0) / 6.0)
          * npdf * -kappatilde)
          * pvBar;
      uBar[0] = ncdf * pvBar;
      uBar[1] = (-alpha0tilde * ncdf - npdf) * pvBar;
      uBar[2] = ((1 + alpha[0] * alpha[0]) / 2.0 * ncdf - (-2.0 * alpha0tilde + kappatilde) / 2.0 * npdf) * pvBar;
      uBar[3] = (-(alpha0tilde * alpha0tilde * alpha0tilde + 3.0 * alpha0tilde) / 6.0 * ncdf + (-3 * alpha0tilde * alpha0tilde + 3.0 * kappatilde * alpha0tilde - kappatilde * kappatilde - 2.0) / 6.0
          * npdf)
          * pvBar;
    } else {
      alpha0tildeBar = (-(-u[1] - u[3] * (3 * alpha0tilde * alpha0tilde + 3.0) / 6.0) * ncdf + (u[2] + u[3] * (-6.0 * alpha0tilde + 3.0 * kappatilde) / 6.0) * npdf) * pvBar;
      kappatildeBar = ((u[0] - u[1] * alpha0tilde + u[2] * (1 + alpha[0] * alpha[0]) / 2.0 - u[3] * (alpha0tilde * alpha0tilde * alpha0tilde + 3.0 * alpha0tilde) / 6.0) * npdf
          + (-u[2] / 2.0 + u[3] * (3.0 * alpha0tilde - 2 * kappatilde) / 6.0) * npdf + (-u[1] - u[2] * (-2.0 * alpha0tilde + kappatilde) / 2.0 + u[3]
          * (-3 * alpha0tilde * alpha0tilde + 3.0 * kappatilde * alpha0tilde - kappatilde * kappatilde - 2.0) / 6.0)
          * npdf * -kappatilde)
          * pvBar;
      uBar[0] = -ncdf * pvBar;
      uBar[1] = (+alpha0tilde * ncdf - npdf) * pvBar;
      uBar[2] = (-(1 + alpha[0] * alpha[0]) / 2.0 * ncdf - (-2.0 * alpha0tilde + kappatilde) / 2.0 * npdf) * pvBar;
      uBar[3] = ((alpha0tilde * alpha0tilde * alpha0tilde + 3.0 * alpha0tilde) / 6.0 * ncdf + (-3 * alpha0tilde * alpha0tilde + 3.0 * kappatilde * alpha0tilde - kappatilde * kappatilde - 2.0) / 6.0
          * npdf)
          * pvBar;
    }
    final double annuityBar = (swaption.getStrike() - rate) * uBar[0] - derivativesRate[0] * uBar[1] + -derivativesRate[1] * uBar[2];
    final double[] derivativesAnnuityBar = new double[3];
    derivativesAnnuityBar[0] = (swaption.getStrike() - rate) * derivativesRate[0] * uBar[1] + ((swaption.getStrike() - rate) * derivativesRate[1] - 2.0 * derivativesRate[0] * derivativesRate[0])
        * uBar[2] + (-3 * derivativesRate[0] * derivativesRate[1] - 2 * derivativesRate[0] * derivativesRate[1] + (swaption.getStrike() - rate) * derivativesRate[2]) * uBar[3];
    derivativesAnnuityBar[1] = (swaption.getStrike() - rate) * derivativesRate[0] * derivativesRate[0] * uBar[2]
        + (-3 * derivativesRate[0] * derivativesRate[0] * derivativesRate[0] + (swaption.getStrike() - rate) * 3 * derivativesRate[0] * derivativesRate[1]) * uBar[3];
    derivativesAnnuityBar[2] = (swaption.getStrike() - rate) * derivativesRate[0] * derivativesRate[0] * derivativesRate[0] * uBar[3];
    final double rateBar = (derivativesAnnuity[1] * derivativesAnnuityBar[0])
        + (derivativesAnnuity[2] * derivativesAnnuityBar[1])
        + (derivativesAnnuity[0] * annuityBar)
        - (annuity * uBar[0])
        - (derivativesAnnuity[0] * derivativesRate[0] * uBar[1])
        - ((derivativesAnnuity[0] * derivativesRate[1] + derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[0]) * uBar[2])
        - (((derivativesAnnuity[0] * derivativesRate[2] + 3 * derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[1] + derivativesAnnuity[2] * derivativesRate[0] * derivativesRate[0]
            * derivativesRate[0]) + derivativesRate[2]) * uBar[3]);
    final double[] derivativesRateBar = new double[3];
    derivativesRateBar[0] = ((swaption.getStrike() - rate) * derivativesAnnuity[0] - annuity)
        * uBar[1]
        + ((swaption.getStrike() - rate) * (2.0 * derivativesAnnuity[1] * derivativesRate[0]) - 4 * derivativesAnnuity[0] * derivativesRate[0])
        * uBar[2]
        + (-3 * (derivativesAnnuity[0] * derivativesRate[1] + 3.0 * derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[0]) - 2 * derivativesAnnuity[0] * derivativesRate[1] + (swaption
            .getStrike() - rate) * (3 * derivativesAnnuity[1] * derivativesRate[1] + derivativesAnnuity[2] * 3.0 * derivativesRate[0] * derivativesRate[0])) * uBar[3];
    derivativesRateBar[1] = ((swaption.getStrike() - rate) * (derivativesAnnuity[0]) - annuity) * uBar[2]
        + (-3 * derivativesRate[0] * (derivativesAnnuity[0]) - 2 * derivativesAnnuity[0] * derivativesRate[0] + (swaption.getStrike() - rate) * (3 * derivativesAnnuity[1] * derivativesRate[0]))
        * uBar[3];
    derivativesRateBar[2] = ((swaption.getStrike() - rate) * derivativesAnnuity[0] - rate) * uBar[3];
    //    double kappaBar = 0.0;
    final double[] alphaFixedBar = new double[nbFixed];
    final double[] alphaIborBar = new double[cfeIbor.getNumberOfPayments()];
    swapRateAdjointAlpha(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor, rateBar, derivativesRateBar, derivativesRate, alphaFixedBar, alphaIborBar);
    alphaIborBar[0] += kappatildeBar + alpha0tildeBar;
    final double[] pvsensi = new double[hwData.getHullWhiteParameter().getVolatility().length];
    final double[] partialDerivatives = new double[hwData.getHullWhiteParameter().getVolatility().length];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime(), partialDerivatives);
      for (int loopsigma = 0; loopsigma < hwData.getHullWhiteParameter().getVolatility().length; loopsigma++) {
        pvsensi[loopsigma] += alphaFixedBar[loopcf] * partialDerivatives[loopsigma];
      }
    }
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime(), partialDerivatives);
      for (int loopsigma = 0; loopsigma < hwData.getHullWhiteParameter().getVolatility().length; loopsigma++) {
        pvsensi[loopsigma] += alphaIborBar[loopcf] * partialDerivatives[loopsigma];
      }
    }
    return pvsensi;
  }

  /**
   * Present value curve sensitivity. The present value is computed using a third order approximation.
   * @param swaption The cash-settled swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionCashFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    // Forward sweep
    final String fundingCurveName = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getFundingCurveName();
    final double expiryTime = swaption.getTimeToExpiry();
    final int nbFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    final double[] alphaFixed = new double[nbFixed];
    final double[] dfFixed = new double[nbFixed];
    final double[] discountedCashFlowFixed = new double[nbFixed];
    final double[] testdiscountedCashFlowFixed = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      alphaFixed[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      dfFixed[loopcf] = hwData.getCurve(fundingCurveName).getDiscountFactor(swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction()
          * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getNotional();
      testdiscountedCashFlowFixed[loopcf] = discountedCashFlowFixed[loopcf];
    }
    testdiscountedCashFlowFixed[0] += 1.0;
    final AnnuityPaymentFixed cfeIbor = swaption.getUnderlyingSwap().getSecondLeg().accept(CFEC, hwData);
    final double[] alphaIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] dfIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] discountedCashFlowIbor = new double[cfeIbor.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      alphaIbor[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime());
      dfIbor[loopcf] = hwData.getCurve(cfeIbor.getDiscountCurve()).getDiscountFactor(cfeIbor.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount();
    }
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, hwData);
    final double[] alpha = new double[cfe.getNumberOfPayments()];
    final double[] df = new double[cfe.getNumberOfPayments()];
    final double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alpha[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime());
      df[loopcf] = hwData.getCurve(cfe.getDiscountCurve()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }
    final double kappa = MODEL.kappa(discountedCashFlow, alpha);
    final int nbFixedPaymentYear = (int) Math.round(1.0 / swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    final double[] derivativesRate = new double[3];
    final double[] derivativesAnnuity = new double[3];
    final double x0 = 0.0; //    (swaption.getUnderlyingSwap().getFixedLeg().isPayer()) ? Math.max(kappa, 0) : Math.min(kappa, 0);
    final double rate = swapRate(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor, derivativesRate);
    final double annuity = annuityCash(rate, nbFixedPaymentYear, swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments(), derivativesAnnuity);
    final double[] u = new double[4];
    u[0] = annuity * (swaption.getStrike() - rate);
    u[1] = (swaption.getStrike() - rate) * derivativesAnnuity[0] * derivativesRate[0] - derivativesRate[0] * annuity;
    u[2] = (swaption.getStrike() - rate) * (derivativesAnnuity[0] * derivativesRate[1] + derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[0]) - 2 * derivativesAnnuity[0]
        * derivativesRate[0] * derivativesRate[0] - annuity * derivativesRate[1];
    u[3] = (-3 * derivativesRate[0] * (derivativesAnnuity[0] * derivativesRate[1] + derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[0]))
        - (2 * derivativesAnnuity[0] * derivativesRate[0] * derivativesRate[1])
        + ((swaption.getStrike() - rate) * (derivativesAnnuity[0] * derivativesRate[2] + 3 * derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[1] + derivativesAnnuity[2]
            * derivativesRate[0] * derivativesRate[0] * derivativesRate[0])) - (rate * derivativesRate[2]);
    final double kappatilde = kappa + alphaIbor[0];
    final double alpha0tilde = alphaIbor[0] + x0;
    double ncdf;
    final double npdf = NORMAL.getPDF(kappatilde);
    double pv;
    if (!swaption.getUnderlyingSwap().getFixedLeg().isPayer()) {
      ncdf = NORMAL.getCDF(kappatilde);
      pv = (u[0] - u[1] * alpha0tilde + u[2] * (1 + alpha[0] * alpha[0]) / 2.0 - u[3] * (alpha0tilde * alpha0tilde * alpha0tilde + 3.0 * alpha0tilde) / 6.0) * ncdf
          + (-u[1] - u[2] * (-2.0 * alpha0tilde + kappatilde) / 2.0 + u[3] * (-3 * alpha0tilde * alpha0tilde + 3.0 * kappatilde * alpha0tilde - kappatilde * kappatilde - 2.0) / 6.0) * npdf;
    } else {
      ncdf = NORMAL.getCDF(-kappatilde);
      pv = -(u[0] - u[1] * alpha0tilde + u[2] * (1 + alpha[0] * alpha[0]) / 2.0 - u[3] * (alpha0tilde * alpha0tilde * alpha0tilde + 3.0 * alpha0tilde) / 6.0) * ncdf
          + (-u[1] - u[2] * (-2.0 * alpha0tilde + kappatilde) / 2.0 + u[3] * (-3 * alpha0tilde * alpha0tilde + 3.0 * kappatilde * alpha0tilde - kappatilde * kappatilde - 2.0) / 6.0) * npdf;
    }
    final double notional = Math.abs(swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getNotional());
    // Backward sweep
    final double pvTotalBar = 1.0;
    final double pvBar = notional * dfIbor[0] * (swaption.isLong() ? 1.0 : -1.0) * pvTotalBar;
    final double[] uBar = new double[4];
    if (!swaption.getUnderlyingSwap().getFixedLeg().isPayer()) {
      uBar[0] = ncdf * pvBar;
      uBar[1] = (-alpha0tilde * ncdf - npdf) * pvBar;
      uBar[2] = ((1 + alpha[0] * alpha[0]) / 2.0 * ncdf - (-2.0 * alpha0tilde + kappatilde) / 2.0 * npdf) * pvBar;
      uBar[3] = (-(alpha0tilde * alpha0tilde * alpha0tilde + 3.0 * alpha0tilde) / 6.0 * ncdf + (-3 * alpha0tilde * alpha0tilde + 3.0 * kappatilde * alpha0tilde - kappatilde * kappatilde - 2.0) / 6.0
          * npdf)
          * pvBar;
    } else {
      uBar[0] = -ncdf * pvBar;
      uBar[1] = (+alpha0tilde * ncdf - npdf) * pvBar;
      uBar[2] = (-(1 + alpha[0] * alpha[0]) / 2.0 * ncdf - (-2.0 * alpha0tilde + kappatilde) / 2.0 * npdf) * pvBar;
      uBar[3] = ((alpha0tilde * alpha0tilde * alpha0tilde + 3.0 * alpha0tilde) / 6.0 * ncdf + (-3 * alpha0tilde * alpha0tilde + 3.0 * kappatilde * alpha0tilde - kappatilde * kappatilde - 2.0) / 6.0
          * npdf)
          * pvBar;
    }
    final double annuityBar = (swaption.getStrike() - rate) * uBar[0] - derivativesRate[0] * uBar[1] + -derivativesRate[1] * uBar[2];
    final double[] derivativesAnnuityBar = new double[3];
    derivativesAnnuityBar[0] = (swaption.getStrike() - rate) * derivativesRate[0] * uBar[1] + ((swaption.getStrike() - rate) * derivativesRate[1] - 2.0 * derivativesRate[0] * derivativesRate[0])
        * uBar[2] + (-3 * derivativesRate[0] * derivativesRate[1] - 2 * derivativesRate[0] * derivativesRate[1] + (swaption.getStrike() - rate) * derivativesRate[2]) * uBar[3];
    derivativesAnnuityBar[1] = (swaption.getStrike() - rate) * derivativesRate[0] * derivativesRate[0] * uBar[2]
        + (-3 * derivativesRate[0] * derivativesRate[0] * derivativesRate[0] + (swaption.getStrike() - rate) * 3 * derivativesRate[0] * derivativesRate[1]) * uBar[3];
    derivativesAnnuityBar[2] = (swaption.getStrike() - rate) * derivativesRate[0] * derivativesRate[0] * derivativesRate[0] * uBar[3];
    final double rateBar = (derivativesAnnuity[1] * derivativesAnnuityBar[0])
        + (derivativesAnnuity[2] * derivativesAnnuityBar[1])
        + (derivativesAnnuity[0] * annuityBar)
        - (annuity * uBar[0])
        - (derivativesAnnuity[0] * derivativesRate[0] * uBar[1])
        - ((derivativesAnnuity[0] * derivativesRate[1] + derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[0]) * uBar[2])
        - (((derivativesAnnuity[0] * derivativesRate[2] + 3 * derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[1] + derivativesAnnuity[2] * derivativesRate[0] * derivativesRate[0]
            * derivativesRate[0]) + derivativesRate[2]) * uBar[3]);
    final double[] derivativesRateBar = new double[3];
    derivativesRateBar[0] = ((swaption.getStrike() - rate) * derivativesAnnuity[0] - annuity)
        * uBar[1]
        + ((swaption.getStrike() - rate) * (2.0 * derivativesAnnuity[1] * derivativesRate[0]) - 4 * derivativesAnnuity[0] * derivativesRate[0])
        * uBar[2]
        + (-3 * (derivativesAnnuity[0] * derivativesRate[1] + 3.0 * derivativesAnnuity[1] * derivativesRate[0] * derivativesRate[0]) - 2 * derivativesAnnuity[0] * derivativesRate[1] + (swaption
            .getStrike() - rate) * (3 * derivativesAnnuity[1] * derivativesRate[1] + derivativesAnnuity[2] * 3.0 * derivativesRate[0] * derivativesRate[0])) * uBar[3];
    derivativesRateBar[1] = ((swaption.getStrike() - rate) * (derivativesAnnuity[0]) - annuity) * uBar[2]
        + (-3 * derivativesRate[0] * (derivativesAnnuity[0]) - 2 * derivativesAnnuity[0] * derivativesRate[0] + (swaption.getStrike() - rate) * (3 * derivativesAnnuity[1] * derivativesRate[0]))
        * uBar[3];
    derivativesRateBar[2] = ((swaption.getStrike() - rate) * derivativesAnnuity[0] - rate) * uBar[3];
    //    double kappaBar = 0.0;
    final double[] discountedCashFlowFixedBar = new double[nbFixed];
    final double[] discountedCashFlowIborBar = new double[cfeIbor.getNumberOfPayments()];
    swapRateAdjointDiscountedCF(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor, rateBar, derivativesRateBar, derivativesRate, discountedCashFlowFixedBar,
        discountedCashFlowIborBar);
    final double[] dfFixedBar = new double[nbFixed];
    final List<DoublesPair> listDf = new ArrayList<>();
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      dfFixedBar[loopcf] = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction() * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getNotional()
          * discountedCashFlowFixedBar[loopcf];
      final DoublesPair dfSensi = DoublesPair.of(swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime(), -swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf)
          .getPaymentTime()
          * dfFixed[loopcf] * dfFixedBar[loopcf]);
      listDf.add(dfSensi);
    }
    final double[] dfIborBar = new double[cfeIbor.getNumberOfPayments()];
    final double[] cfeAmountIborBar = new double[cfeIbor.getNumberOfPayments()];
    dfIborBar[0] = pv * notional * (swaption.isLong() ? 1.0 : -1.0);
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      dfIborBar[loopcf] += cfeIbor.getNthPayment(loopcf).getAmount() * discountedCashFlowIborBar[loopcf];
      final DoublesPair dfSensi = DoublesPair.of(cfeIbor.getNthPayment(loopcf).getPaymentTime(), -cfeIbor.getNthPayment(loopcf).getPaymentTime() * dfIbor[loopcf] * dfIborBar[loopcf]);
      listDf.add(dfSensi);
      cfeAmountIborBar[loopcf] = dfIbor[loopcf] * discountedCashFlowIborBar[loopcf];
    }
    final Map<String, List<DoublesPair>> pvsDF = new HashMap<>();
    pvsDF.put(fundingCurveName, listDf);
    InterestRateCurveSensitivity sensitivity = new InterestRateCurveSensitivity(pvsDF);
    final Map<Double, InterestRateCurveSensitivity> cfeIborCurveSensi = swaption.getUnderlyingSwap().getSecondLeg().accept(CFECSC, hwData);
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      final InterestRateCurveSensitivity sensiCfe = cfeIborCurveSensi.get(cfeIbor.getNthPayment(loopcf).getPaymentTime());
      if (!(sensiCfe == null)) { // There is some sensitivity to that cfe.
        sensitivity = sensitivity.plus(sensiCfe.multipliedBy(cfeAmountIborBar[loopcf]));
      }
    }
    return sensitivity;
  }

  /**
   * Computation of the swap rate for a given random variable in the Hull-White one factor model.
   * @param x The random variable.
   * @param discountedCashFlowFixed The discounted cash flows.
   * @param alphaFixed The bond volatilities.
   * @param discountedCashFlowIbor The discounted cash flows.
   * @param alphaIbor The bond volatilities.
   * @param derivatives Array used to return the derivatives of the swap rate with respect to the random variable. The array is changed by the method.
   * The values are [0] the first order derivative and [1] the second order derivative.
   * @return The swap rate.
   */
  private double swapRate(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor,
      final double[] alphaIbor, final double[] derivatives) {
    final double[] f = new double[3];
    double y1;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      y1 = -discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf] * x - alphaIbor[loopcf] * alphaIbor[loopcf] / 2.0);
      f[0] += y1;
      f[1] += -alphaIbor[loopcf] * y1;
      f[2] += alphaIbor[loopcf] * alphaIbor[loopcf] * y1;
    }
    final double[] g = new double[3];
    double y2;
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      y2 = discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf] * x - alphaFixed[loopcf] * alphaFixed[loopcf] / 2.0);
      g[0] += y2;
      g[1] += -alphaFixed[loopcf] * y2;
      g[2] += alphaFixed[loopcf] * alphaFixed[loopcf] * y2;
    }
    final double swapRate = f[0] / g[0];
    derivatives[0] = (f[1] * g[0] - f[0] * g[1]) / (g[0] * g[0]);
    derivatives[1] = (f[2] * g[0] - f[0] * g[2]) / (g[0] * g[0]) - (f[1] * g[0] - f[0] * g[1]) * 2 * g[1] / (g[0] * g[0] * g[0]);
    return swapRate;
  }

  /**
   * Computation of the swap rate and its derivative with respect to the input parameters for a given random variable in the Hull-White one factor model.
   * @param x The random variable.
   * @param discountedCashFlowFixed The discounted cash flows.
   * @param alphaFixed The bond volatilities.
   * @param discountedCashFlowIbor The discounted cash flows.
   * @param alphaIbor The bond volatilities.
   * @param swapRateBar The sensitivity to the swap rate in the rest of the computation.
   * @param derivativesBar The sensitivity to the swap rate derivatives in the rest of the computation.
   * @param derivatives Array used to return the derivatives of the swap rate with respect to the random variable. The array is changed by the method.
   * The values are [0] the first order derivative and [1] the second order derivative.
   * @param alphaFixedBar Array used to return the derivatives of the result with respect to the alphaFixed variables.
   * @param alphaIborBar Array used to return the derivatives of the result with respect to the alphaIbor variables.
   * @return The swap rate.
   */
  private double swapRateAdjointAlpha(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor,
      final double swapRateBar,
      final double[] derivativesBar, final double[] derivatives, final double[] alphaFixedBar, final double[] alphaIborBar) {
    final double[] f = new double[3];
    final double[] y1 = new double[discountedCashFlowIbor.length];
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      y1[loopcf] = -discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf] * x - alphaIbor[loopcf] * alphaIbor[loopcf] / 2.0);
      f[0] += y1[loopcf];
      f[1] += -alphaIbor[loopcf] * y1[loopcf];
      f[2] += alphaIbor[loopcf] * alphaIbor[loopcf] * y1[loopcf];
    }
    final double[] g = new double[3];
    final double[] y2 = new double[discountedCashFlowFixed.length];
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      y2[loopcf] = discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf] * x - alphaFixed[loopcf] * alphaFixed[loopcf] / 2.0);
      g[0] += y2[loopcf];
      g[1] += -alphaFixed[loopcf] * y2[loopcf];
      g[2] += alphaFixed[loopcf] * alphaFixed[loopcf] * y2[loopcf];
    }
    final double swapRate = f[0] / g[0];
    derivatives[0] = (f[1] * g[0] - f[0] * g[1]) / (g[0] * g[0]);
    derivatives[1] = (f[2] * g[0] - f[0] * g[2]) / (g[0] * g[0]) - (f[1] * g[0] - f[0] * g[1]) * 2 * g[1] / (g[0] * g[0] * g[0]);
    // Backward sweep
    final double[] gBar = new double[3];
    gBar[0] = -f[0] / (g[0] * g[0]) * swapRateBar + (-f[1] / (g[0] * g[0]) + f[0] * g[1] / (g[0] * g[0] * g[0])) * derivativesBar[0]
        + (-f[2] / (g[0] * g[0]) + 2.0 * (f[0] * g[2] + 2 * g[1] * f[1]) / (g[0] * g[0] * g[0]) - 6.0 * f[0] * g[1] * g[1] / (g[0] * g[0] * g[0] * g[0])) * derivativesBar[1];
    gBar[1] = -f[0] / (g[0] * g[0]) * derivativesBar[0] + (-2 * f[1] / (g[0] * g[0]) + 4 * f[0] * g[1] / (g[0] * g[0] * g[0])) * derivativesBar[1];
    gBar[2] = -f[0] / (g[0] * g[0]) * derivativesBar[1];
    double y2p;
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      y2p = y2[loopcf] * (-x - alphaFixed[loopcf]);
      alphaFixedBar[loopcf] = y2p * gBar[0] + (-y2[loopcf] - alphaFixed[loopcf] * y2p) * gBar[1] + (2.0 * alphaFixed[loopcf] * y2[loopcf] + alphaFixed[loopcf] * alphaFixed[loopcf] * y2p) * gBar[2];
    }
    final double[] fBar = new double[3];
    fBar[0] = 1.0 / g[0] * swapRateBar - g[1] / (g[0] * g[0]) * derivativesBar[0] + (-g[2] / (g[0] * g[0]) + 2 * g[1] * g[1] / (g[0] * g[0] * g[0])) * derivativesBar[1];
    fBar[1] = 1.0 / g[0] * derivativesBar[0] + -2 * g[1] / (g[0] * g[0]) * derivativesBar[1];
    fBar[2] = 1.0 / g[0] * derivativesBar[1];
    double y1p;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      y1p = y1[loopcf] * (-x - alphaIbor[loopcf]);
      alphaIborBar[loopcf] = y1p * fBar[0] + (-y1[loopcf] - alphaIbor[loopcf] * y1p) * fBar[1] + (2 * alphaIbor[loopcf] * y1[loopcf] + alphaIbor[loopcf] * alphaIbor[loopcf] * y1p) * fBar[2];
    }
    return swapRate;
  }

  private double swapRateAdjointDiscountedCF(final double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor,
      final double swapRateBar, final double[] derivativesBar, final double[] derivatives, final double[] discountedCashFlowFixedBar, final double[] discountedCashFlowIborBar) {

    final double[] f = new double[3];
    final double[] y1 = new double[discountedCashFlowIbor.length];
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      y1[loopcf] = -Math.exp(-alphaIbor[loopcf] * x - alphaIbor[loopcf] * alphaIbor[loopcf] / 2.0);
      f[0] += discountedCashFlowIbor[loopcf] * y1[loopcf];
      f[1] += -alphaIbor[loopcf] * discountedCashFlowIbor[loopcf] * y1[loopcf];
      f[2] += alphaIbor[loopcf] * alphaIbor[loopcf] * discountedCashFlowIbor[loopcf] * y1[loopcf];
    }
    final double[] g = new double[3];
    final double[] y2 = new double[discountedCashFlowFixed.length];
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      y2[loopcf] = Math.exp(-alphaFixed[loopcf] * x - alphaFixed[loopcf] * alphaFixed[loopcf] / 2.0);
      g[0] += discountedCashFlowFixed[loopcf] * y2[loopcf];
      g[1] += -alphaFixed[loopcf] * discountedCashFlowFixed[loopcf] * y2[loopcf];
      g[2] += alphaFixed[loopcf] * alphaFixed[loopcf] * discountedCashFlowFixed[loopcf] * y2[loopcf];
    }
    final double swapRate = f[0] / g[0];
    derivatives[0] = (f[1] * g[0] - f[0] * g[1]) / (g[0] * g[0]);
    derivatives[1] = (f[2] * g[0] - f[0] * g[2]) / (g[0] * g[0]) - (f[1] * g[0] - f[0] * g[1]) * 2 * g[1] / (g[0] * g[0] * g[0]);
    // Backward sweep
    final double[] gBar = new double[3];
    gBar[0] = -f[0] / (g[0] * g[0]) * swapRateBar + (-f[1] / (g[0] * g[0]) + 2 * f[0] * g[1] / (g[0] * g[0] * g[0])) * derivativesBar[0]
        + (-f[2] / (g[0] * g[0]) + 2.0 * (f[0] * g[2] + 2 * g[1] * f[1]) / (g[0] * g[0] * g[0]) - 6.0 * f[0] * g[1] * g[1] / (g[0] * g[0] * g[0] * g[0])) * derivativesBar[1];
    gBar[1] = -f[0] / (g[0] * g[0]) * derivativesBar[0] + (-2 * f[1] / (g[0] * g[0]) + 4 * f[0] * g[1] / (g[0] * g[0] * g[0])) * derivativesBar[1];
    gBar[2] = -f[0] / (g[0] * g[0]) * derivativesBar[1];
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      discountedCashFlowFixedBar[loopcf] = y2[loopcf] * gBar[0] + -alphaFixed[loopcf] * y2[loopcf] * gBar[1] + alphaFixed[loopcf] * alphaFixed[loopcf] * y2[loopcf] * gBar[2];
    }
    final double[] fBar = new double[3];
    fBar[0] = 1.0 / g[0] * swapRateBar - g[1] / (g[0] * g[0]) * derivativesBar[0] + (-g[2] / (g[0] * g[0]) + 2 * g[1] * g[1] / (g[0] * g[0] * g[0])) * derivativesBar[1];
    fBar[1] = 1.0 / g[0] * derivativesBar[0] + -2 * g[1] / (g[0] * g[0]) * derivativesBar[1];
    fBar[2] = 1.0 / g[0] * derivativesBar[1];
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      discountedCashFlowIborBar[loopcf] = y1[loopcf] * fBar[0] + -alphaIbor[loopcf] * y1[loopcf] * fBar[1] + alphaIbor[loopcf] * alphaIbor[loopcf] * y1[loopcf] * fBar[2];
    }
    return swapRate;
  }

  /**
   * Computes the cash annuity from the swap rate and its derivatives.
   * @param swapRate The swap rate.
   * @param nbFixedPaymentYear The number of fixed payment per year.
   * @param nbFixedPeriod The total number of payments.
   * @param derivatives Array used to return the derivatives of the annuity with respect to the swap rate. The array is changed by the method.
   * The values are [0] the first order derivative, [1] the second order derivative and [2] the third order derivative.
   * @return The cash annuity
   */
  private double annuityCash(final double swapRate, final int nbFixedPaymentYear, final int nbFixedPeriod, final double[] derivatives) {
    final double invfact = 1 + swapRate / nbFixedPaymentYear;
    final double annuity = 1.0 / swapRate * (1.0 - 1.0 / Math.pow(invfact, nbFixedPeriod));
    derivatives[0] = 0.0;
    derivatives[1] = 0.0;
    derivatives[2] = 0.0;
    for (int looppay = 0; looppay < nbFixedPeriod; looppay++) {
      derivatives[0] += -(looppay + 1) * Math.pow(invfact, -looppay - 2) / (nbFixedPaymentYear * nbFixedPaymentYear);
      derivatives[1] += (looppay + 1) * (looppay + 2) * Math.pow(invfact, -looppay - 3) / (nbFixedPaymentYear * nbFixedPaymentYear * nbFixedPaymentYear);
      derivatives[2] += -(looppay + 1) * (looppay + 2) * (looppay + 3) * Math.pow(invfact, -looppay - 4) / (nbFixedPaymentYear * nbFixedPaymentYear * nbFixedPaymentYear * nbFixedPaymentYear);
    }
    return annuity;
  }

}
