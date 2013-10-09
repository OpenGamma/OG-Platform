/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.G2ppPiecewiseConstantModel;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to computes the present value of physical delivery European swaptions with the G2++ model through efficient approximation.
 * Reference: Henrard, M. Swaptions in Libor Market Model with local volatility. Wilmott Journal, 2010, 2, 135-154. Preprint available at http://ssrn.com/abstract=1098420
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborG2ppApproximationMethod}
 */
@Deprecated
public class SwaptionPhysicalFixedIborG2ppApproximationMethod implements PricingMethod {

  /**
   * The model used in computations.
   */
  private static final G2ppPiecewiseConstantModel MODEL_G2PP = new G2ppPiecewiseConstantModel();
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();

  /**
   * Computes the present value of the Physical delivery swaption through approximation..
   * @param swaption The swaption.
   * @param g2Data The G2++ parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final G2ppPiecewiseConstantDataBundle g2Data) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(g2Data, "G2++ data");
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, g2Data);
    return presentValue(swaption, cfe, g2Data);
  }

  /**
   * Computes the present value of the Physical delivery swaption through approximation..
   * @param swaption The swaption.
   * @param cfe The swaption cash flow equivalent.
   * @param g2Data The G2++ parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final AnnuityPaymentFixed cfe, final G2ppPiecewiseConstantDataBundle g2Data) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(cfe, "cash-flow equivalent");
    ArgumentChecker.notNull(g2Data, "G2++ data");
    final YieldAndDiscountCurve dsc = g2Data.getCurve(swaption.getUnderlyingSwap().getFixedLeg().getDiscountCurve());
    final int nbCf = cfe.getNumberOfPayments();
    final double[] cfa = new double[nbCf];
    final double[] t = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      cfa[loopcf] = -Math.signum(cfe.getNthPayment(0).getAmount()) * cfe.getNthPayment(loopcf).getAmount();
      t[loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
    }
    final double rhog2pp = g2Data.getG2ppParameter().getCorrelation();
    final double[][] ht0 = MODEL_G2PP.volatilityMaturityPart(g2Data.getG2ppParameter(), t[0], t);
    final double[] dfswap = new double[nbCf];
    final double[] p0 = new double[nbCf];
    final double[] cP = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      dfswap[loopcf] = dsc.getDiscountFactor(t[loopcf]);
      p0[loopcf] = dfswap[loopcf] / dfswap[0];
      cP[loopcf] = cfa[loopcf] * p0[loopcf];
    }
    final double k = -cfa[0];
    double b0 = 0.0;
    for (int loopcf = 1; loopcf < nbCf; loopcf++) {
      b0 += cP[loopcf];
    }
    final double[] alpha0 = new double[nbCf - 1];
    final double[] beta0 = new double[2];
    for (int loopcf = 0; loopcf < nbCf - 1; loopcf++) {
      alpha0[loopcf] = cfa[loopcf + 1] * p0[loopcf + 1] / b0;
      beta0[0] += alpha0[loopcf] * ht0[0][loopcf + 1];
      beta0[1] += alpha0[loopcf] * ht0[1][loopcf + 1];
    }
    final double[][] gamma = MODEL_G2PP.gamma(g2Data.getG2ppParameter(), 0, swaption.getTimeToExpiry());
    final double[] tau = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      tau[loopcf] = gamma[0][0] * ht0[0][loopcf] * ht0[0][loopcf] + gamma[1][1] * ht0[1][loopcf] * ht0[1][loopcf] + 2 * rhog2pp * gamma[0][1] * ht0[0][loopcf] * ht0[1][loopcf];
    }
    double xbarnum = 0.0;
    double xbarde = 0.0;
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      xbarnum += cP[loopcf] - cP[loopcf] * tau[loopcf] * tau[loopcf] / 2.0;
      xbarde += cP[loopcf] * tau[loopcf];
    }
    final double xbar = xbarnum / xbarde;
    final double[] pK = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      pK[loopcf] = p0[loopcf] * (1.0 - tau[loopcf] * xbar - tau[loopcf] * tau[loopcf] / 2.0);
    }
    final double[] alphaK = new double[nbCf - 1];
    final double[] betaK = new double[2];
    for (int loopcf = 0; loopcf < nbCf - 1; loopcf++) {
      alphaK[loopcf] = cfa[loopcf + 1] * pK[loopcf + 1] / k;
      betaK[0] += alphaK[loopcf] * ht0[0][loopcf + 1];
      betaK[1] += alphaK[loopcf] * ht0[1][loopcf + 1];
    }
    final double[] betaBar = new double[] {(beta0[0] + betaK[0]) / 2.0, (beta0[1] + betaK[1]) / 2.0 };
    final double sigmaBar2 = gamma[0][0] * betaBar[0] * betaBar[0] + gamma[1][1] * betaBar[1] * betaBar[1] + 2 * rhog2pp * gamma[0][1] * betaBar[0] * betaBar[1];
    final double sigmaBar = Math.sqrt(sigmaBar2);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, 1, !swaption.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(b0, dfswap[0], sigmaBar);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return CurrencyAmount.of(swaption.getCurrency(), price);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    Validate.isTrue(curves instanceof G2ppPiecewiseConstantDataBundle, "Bundle should contain G2++ data");
    return presentValue((SwaptionPhysicalFixedIbor) instrument, (G2ppPiecewiseConstantDataBundle) curves);
  }

}
