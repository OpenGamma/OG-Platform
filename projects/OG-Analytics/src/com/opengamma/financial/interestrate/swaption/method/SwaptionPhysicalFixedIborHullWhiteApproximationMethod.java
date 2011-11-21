/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to computes the present value and sensitivities of physical delivery European swaptions with the Hull-White one factor model through efficient approximation. 
 * The method does not require the solution of a non-linear equation.
 * Reference: Henrard, M. Efficient swaptions price in Hull-White one factor model. arXiv, 2009. {@linktourl http://arxiv.org/abs/0901.1776}
 */
public class SwaptionPhysicalFixedIborHullWhiteApproximationMethod implements PricingMethod {

  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();

  /**
   * Computes the present value of the Physical delivery swaption through approximation..
   * @param swaption The swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    YieldAndDiscountCurve dsc = hwData.getCurve(swaption.getUnderlyingSwap().getFixedLeg().getDiscountCurve());
    double expiry = swaption.getTimeToExpiry();
    AnnuityPaymentFixed cfe = CFEC.visit(swaption.getUnderlyingSwap(), hwData);
    int nbCf = cfe.getNumberOfPayments();
    double a = hwData.getHullWhiteParameter().getMeanReversion();
    double[] sigma = hwData.getHullWhiteParameter().getVolatility();
    double[] s = hwData.getHullWhiteParameter().getVolatilityTime();
    double[] cfa = new double[nbCf];
    double[] t = new double[nbCf + 1];
    double[] expt = new double[nbCf + 1];
    double[] dfswap = new double[nbCf];
    double[] p0 = new double[nbCf];
    double[] cP = new double[nbCf];
    t[0] = expiry;
    expt[0] = Math.exp(-a * t[0]);
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      t[loopcf + 1] = cfe.getNthPayment(loopcf).getPaymentTime();
      cfa[loopcf] = -Math.signum(cfe.getNthPayment(0).getAmount()) * cfe.getNthPayment(loopcf).getAmount();
      expt[loopcf + 1] = Math.exp(-a * t[loopcf + 1]);
      dfswap[loopcf] = dsc.getDiscountFactor(t[loopcf + 1]);
      p0[loopcf] = dfswap[loopcf] / dfswap[0];
      cP[loopcf] = cfa[loopcf] * p0[loopcf];
    }
    double k = -cfa[0];
    double b0 = 0.0;
    double[] alpha0 = new double[nbCf - 1];
    for (int loopcf = 1; loopcf < nbCf; loopcf++) {
      b0 += cP[loopcf];
    }
    for (int loopcf = 1; loopcf < nbCf; loopcf++) {
      alpha0[loopcf - 1] = cfa[loopcf] * p0[loopcf] / b0;
    }
    double eta2 = 0.0;
    int j = 0;
    while (expiry > s[j + 1]) {
      eta2 += sigma[j] * sigma[j] * (Math.exp(2.0 * a * s[j + 1]) - Math.exp(2.0 * a * s[j]));
      j++;
    }
    eta2 += sigma[j] * sigma[j] * (Math.exp(2.0 * a * expiry) - Math.exp(2.0 * a * s[j]));
    eta2 /= 2.0 * a;
    double eta = Math.sqrt(eta2);
    double[] factor2 = new double[nbCf];
    double[] tau = new double[nbCf];
    double xbarnum = 0.0;
    double xbarde = 0.0;
    double[] pK = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      factor2[loopcf] = (expt[1] - expt[loopcf + 1]) / a;
      tau[loopcf] = factor2[loopcf] * eta;
      xbarnum += cP[loopcf] - cP[loopcf] * tau[loopcf] * tau[loopcf] / 2.0;
      xbarde += cP[loopcf] * tau[loopcf];
    }
    double xbar = xbarnum / xbarde;
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      pK[loopcf] = p0[loopcf] * (1.0 - tau[loopcf] * xbar - tau[loopcf] * tau[loopcf] / 2.0);
    }
    double[] alphaK = new double[nbCf - 1];
    double sigmaK = 0.0;
    for (int loopcf = 0; loopcf < nbCf - 1; loopcf++) {
      alphaK[loopcf] = cfa[loopcf + 1] * pK[loopcf + 1] / k;
      sigmaK += eta * (alpha0[loopcf] + alphaK[loopcf]) * factor2[loopcf + 1] / 2.0;
    }
    EuropeanVanillaOption option = new EuropeanVanillaOption(k, 1, !swaption.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(b0, dfswap[0], sigmaK);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = func.evaluate(dataBlack);
    return CurrencyAmount.of(swaption.getUnderlyingSwap().getFirstLeg().getCurrency(), pv * (swaption.isLong() ? 1.0 : -1.0));
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    return null;
  }

}
