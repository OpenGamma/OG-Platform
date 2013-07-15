/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.G2ppPiecewiseConstantModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to computes the present value of physical delivery European swaptions with the G2++ model through efficient approximation.
 * <p>Reference: Henrard, M. Swaptions in Libor Market Model with local volatility. Wilmott Journal, 2010, 2, 135-154.
 * Preprint available at http://ssrn.com/abstract=1098420
 */
public final class SwaptionPhysicalFixedIborG2ppApproximationMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionPhysicalFixedIborG2ppApproximationMethod INSTANCE = new SwaptionPhysicalFixedIborG2ppApproximationMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionPhysicalFixedIborG2ppApproximationMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionPhysicalFixedIborG2ppApproximationMethod() {
  }

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
  public MultipleCurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final G2ppProviderInterface g2Data) {
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, g2Data.getMulticurveProvider());
    return presentValue(swaption, cfe, g2Data);
  }

  /**
   * Computes the present value of the Physical delivery swaption through approximation..
   * @param swaption The swaption.
   * @param cfe The swaption cash flow equiovalent.
   * @param g2Data The G2++ parameters and the curves.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final AnnuityPaymentFixed cfe, final G2ppProviderInterface g2Data) {
    final Currency ccy = swaption.getCurrency();
    final MulticurveProviderInterface multicurves = g2Data.getMulticurveProvider();
    final int nbCf = cfe.getNumberOfPayments();
    final double[] cfa = new double[nbCf];
    final double[] t = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      cfa[loopcf] = -Math.signum(cfe.getNthPayment(0).getAmount()) * cfe.getNthPayment(loopcf).getAmount();
      t[loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
    }
    final double rhog2pp = g2Data.getG2ppParameters().getCorrelation();
    final double[][] ht0 = MODEL_G2PP.volatilityMaturityPart(g2Data.getG2ppParameters(), t[0], t);
    final double[] dfswap = new double[nbCf];
    final double[] p0 = new double[nbCf];
    final double[] cP = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      dfswap[loopcf] = multicurves.getDiscountFactor(ccy, t[loopcf]);
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
    final double[][] gamma = MODEL_G2PP.gamma(g2Data.getG2ppParameters(), 0, swaption.getTimeToExpiry());
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
    final double[] betaBar = new double[] {(beta0[0] + betaK[0]) / 2.0, (beta0[1] + betaK[1]) / 2.0};
    final double sigmaBar2 = gamma[0][0] * betaBar[0] * betaBar[0] + gamma[1][1] * betaBar[1] * betaBar[1] + 2 * rhog2pp * gamma[0][1] * betaBar[0] * betaBar[1];
    final double sigmaBar = Math.sqrt(sigmaBar2);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, 1, !swaption.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(b0, dfswap[0], sigmaBar);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return MultipleCurrencyAmount.of(swaption.getCurrency(), price);
  }

}
