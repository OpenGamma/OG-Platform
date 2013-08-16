/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to computes the present value and sensitivities of physical delivery European swaptions with the Hull-White one factor model through efficient approximation.
 * The method does not require the solution of a non-linear equation.
 * Reference: Henrard, M. Efficient swaptions price in Hull-White one factor model. arXiv, 2009. http://arxiv.org/abs/0901.1776
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborHullWhiteApproximationMethod}
 */
@Deprecated
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
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(hwData, "Hull-White data");
    final YieldAndDiscountCurve dsc = hwData.getCurve(swaption.getUnderlyingSwap().getFixedLeg().getDiscountCurve());
    final double expiry = swaption.getTimeToExpiry();
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, hwData);
    final int nbCf = cfe.getNumberOfPayments();
    final double a = hwData.getHullWhiteParameter().getMeanReversion();
    final double[] sigma = hwData.getHullWhiteParameter().getVolatility();
    final double[] s = hwData.getHullWhiteParameter().getVolatilityTime();
    final double[] cfa = new double[nbCf];
    final double[] t = new double[nbCf + 1];
    final double[] expt = new double[nbCf + 1];
    final double[] dfswap = new double[nbCf];
    final double[] p0 = new double[nbCf];
    final double[] cP = new double[nbCf];
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
    final double k = -cfa[0];
    double b0 = 0.0;
    final double[] alpha0 = new double[nbCf - 1];
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
    final double eta = Math.sqrt(eta2);
    final double[] factor2 = new double[nbCf];
    final double[] tau = new double[nbCf];
    double xbarnum = 0.0;
    double xbarde = 0.0;
    final double[] pK = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      factor2[loopcf] = (expt[1] - expt[loopcf + 1]) / a;
      tau[loopcf] = factor2[loopcf] * eta;
      xbarnum += cP[loopcf] - cP[loopcf] * tau[loopcf] * tau[loopcf] / 2.0;
      xbarde += cP[loopcf] * tau[loopcf];
    }
    final double xbar = xbarnum / xbarde;
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      pK[loopcf] = p0[loopcf] * (1.0 - tau[loopcf] * xbar - tau[loopcf] * tau[loopcf] / 2.0);
    }
    final double[] alphaK = new double[nbCf - 1];
    double sigmaK = 0.0;
    for (int loopcf = 0; loopcf < nbCf - 1; loopcf++) {
      alphaK[loopcf] = cfa[loopcf + 1] * pK[loopcf + 1] / k;
      sigmaK += eta * (alpha0[loopcf] + alphaK[loopcf]) * factor2[loopcf + 1] / 2.0;
    }
    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, 1, !swaption.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(b0, dfswap[0], sigmaK);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = func.evaluate(dataBlack);
    return CurrencyAmount.of(swaption.getUnderlyingSwap().getFirstLeg().getCurrency(), pv * (swaption.isLong() ? 1.0 : -1.0));
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    return null;
  }

}
