/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.swaption.SwaptionCashFixedIbor;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value of cash-settled European swaptions with the Hull-White one factor model by a third order approximation.
 * Reference: Henrard, M., Cash-Settled Swaptions: How Wrong are We? (November 2010). Available at SSRN: http://ssrn.com/abstract=1703846
 */
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
   * The normal distribution implementation.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Present value calculator using a third order approximation.
   * @param swaption The swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    double expiryTime = swaption.getTimeToExpiry();
    int nbFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    double[] alphaFixed = new double[nbFixed];
    double[] dfFixed = new double[nbFixed];
    double[] discountedCashFlowFixed = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      alphaFixed[loopcf] = MODEL.alpha(0.0, expiryTime, expiryTime, swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime(), hwData.getHullWhiteParameter());
      dfFixed[loopcf] = hwData.getCurve(swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getFundingCurveName()).getDiscountFactor(
          swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction()
          * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getNotional();
    }
    AnnuityPaymentFixed cfeIbor = CFEC.visit(swaption.getUnderlyingSwap().getSecondLeg(), hwData);
    double[] alphaIbor = new double[cfeIbor.getNumberOfPayments()];
    double[] dfIbor = new double[cfeIbor.getNumberOfPayments()];
    double[] discountedCashFlowIbor = new double[cfeIbor.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      alphaIbor[loopcf] = MODEL.alpha(0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime(), hwData.getHullWhiteParameter());
      dfIbor[loopcf] = hwData.getCurve(cfeIbor.getDiscountCurve()).getDiscountFactor(cfeIbor.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount();
    }
    AnnuityPaymentFixed cfe = CFEC.visit(swaption.getUnderlyingSwap(), hwData);
    double[] alpha = new double[cfe.getNumberOfPayments()];
    double[] df = new double[cfe.getNumberOfPayments()];
    double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alpha[loopcf] = MODEL.alpha(0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime(), hwData.getHullWhiteParameter());
      df[loopcf] = hwData.getCurve(cfe.getDiscountCurve()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }
    double kappa = MODEL.kappa(discountedCashFlow, alpha);
    final int nbFixedPaymentYear = (int) Math.round(1.0 / swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    double[] derivativesRate = new double[3];
    double[] derivativesAnnuity = new double[3];
    double x0 = 0.0; //    (swaption.getUnderlyingSwap().getFixedLeg().isPayer()) ? Math.max(kappa, 0) : Math.min(kappa, 0);
    double rate = swapRate(x0, discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor, derivativesRate);
    double annuity = annuityCash(rate, nbFixedPaymentYear, swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments(), derivativesAnnuity);
    double[] u = new double[4];
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
    double kappatilde = kappa + alpha[0];
    double alpha0tilde = alpha[0] + x0;
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
    return CurrencyAmount.of(swaption.getCurrency(), pv * notional * df[0] * (swaption.isLong() ? 1.0 : -1.0));
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionCashFixedIbor, "Cash delivery swaption");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue(instrument, curves);
  }

  /**
   * Computation of the swap rate for a given random variable.
   * @param x The random variabale.
   * @param discountedCashFlowFixed The discounted cash flows.
   * @param alphaFixed The bond volatilities.
   * @param discountedCashFlowIbor The discounted cash flows.
   * @param alphaIbor The bond volatilities.
   * @param derivatives Array used to return the derivatives of the swap rate with respect to the random variable. The array is changed by the method. 
   * The values are [0] the first order derivative and [1] the second order derivative.
   * @return The swap rate.
   */
  private double swapRate(double x, final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor, double[] derivatives) {
    double[] f = new double[3];
    double y;
    for (int loopcf = 0; loopcf < discountedCashFlowIbor.length; loopcf++) {
      y = -discountedCashFlowIbor[loopcf] * Math.exp(-alphaIbor[loopcf] * x - alphaIbor[loopcf] * alphaIbor[loopcf] / 2.0);
      f[0] += y;
      f[1] += -alphaIbor[loopcf] * y;
      f[2] += alphaIbor[loopcf] * alphaIbor[loopcf] * y;
    }
    double[] g = new double[3];
    for (int loopcf = 0; loopcf < discountedCashFlowFixed.length; loopcf++) {
      y = discountedCashFlowFixed[loopcf] * Math.exp(-alphaFixed[loopcf] * x - alphaFixed[loopcf] * alphaFixed[loopcf] / 2.0);
      g[0] += y;
      g[1] += -alphaFixed[loopcf] * y;
      g[2] += alphaFixed[loopcf] * alphaFixed[loopcf] * y;
    }
    double swapRate = f[0] / g[0];
    derivatives[0] = (f[1] * g[0] - f[0] * g[1]) / (g[0] * g[0]);
    derivatives[1] = (f[2] * g[0] - f[0] * g[2]) / (g[0] * g[0]) - (f[1] * g[0] - f[0] * g[1]) * 2 * g[1] / (g[0] * g[0] * g[0]);
    return swapRate;
  }

  /**
   * Computes the cash annuity from the swap rate.
   * @param swapRate The swap rate.
   * @param nbFixedPaymentYear The number of fixed payment per year.
   * @param nbFixedPeriod The total number of payments.
   * @param derivatives Array used to return the derivatives of the annuity with respect to the swap rate. The array is changed by the method. 
   * The values are [0] the first order derivative, [1] the second order derivative and [2] the third order derivative.
   * @return
   */
  private double annuityCash(double swapRate, int nbFixedPaymentYear, int nbFixedPeriod, double[] derivatives) {
    double invfact = 1 + swapRate / nbFixedPaymentYear;
    double annuity = 1.0 / swapRate * (1.0 - 1.0 / Math.pow(invfact, nbFixedPeriod));
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
