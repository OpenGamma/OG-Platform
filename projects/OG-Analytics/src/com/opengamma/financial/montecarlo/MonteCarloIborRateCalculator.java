/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.montecarlo;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * Computes the instrument price as the average over different paths. The data bundle contains the different Ibor rates paths and the instrument reference amounts.
 * The numeraire is the last time in the LMM description.
 */
public class MonteCarloIborRateCalculator extends AbstractInstrumentDerivativeVisitor<MonteCarloIborRateDataBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final MonteCarloIborRateCalculator INSTANCE = new MonteCarloIborRateCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static MonteCarloIborRateCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  MonteCarloIborRateCalculator() {
  }

  @Override
  public Double visit(final InstrumentDerivative derivative, final MonteCarloIborRateDataBundle mcResults) {
    Validate.notNull(derivative);
    return derivative.accept(this, mcResults);
  }

  @Override
  public Double visitCapFloorIbor(final CapFloorIbor payment, final MonteCarloIborRateDataBundle mcResults) {
    Validate.isTrue(mcResults.getPathIborRate().length == 1, "Only one decision date for cap/floor.");
    double[][] pathIborRate = mcResults.getPathIborRate()[0];
    double[] impactAmount = mcResults.getImpactAmount()[0];
    int[] impactIndex = mcResults.getImpactIndex()[0];
    double price = 0;
    int nbPath = pathIborRate[0].length;
    int nbPeriod = pathIborRate.length;
    double[] ibor = new double[nbPath];
    double payoff;
    double[] discounting = new double[nbPath];
    double omega = (payment.isCap() ? 1.0 : -1.0);
    for (int looppath = 0; looppath < nbPath; looppath++) {
      ibor[looppath] = impactAmount[0] * pathIborRate[impactIndex[0]][looppath] + (impactAmount[0] - 1.0) / payment.getFixingYearFraction();
      // Ibor in the right convention; path in Dsc curve
      payoff = Math.max(omega * (ibor[looppath] - payment.getStrike()), 0);
      discounting[looppath] = 1.0;
      for (int loopdsc = impactIndex[2]; loopdsc < nbPeriod; loopdsc++) {
        discounting[looppath] *= (1.0 + pathIborRate[loopdsc][looppath] * mcResults.getDelta()[loopdsc]);
      }
      price += payoff * discounting[looppath];
    }
    price *= payment.getNotional() * payment.getPaymentYearFraction();
    return price;
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final MonteCarloIborRateDataBundle mcResults) {
    double[][] pathIborRate = mcResults.getPathIborRate()[0];
    double[] impactAmount = mcResults.getImpactAmount()[0];
    int[] impactIndex = mcResults.getImpactIndex()[0];
    int nbImpact = impactIndex.length;
    int nbPath = pathIborRate[0].length;
    int nbPeriod = pathIborRate.length;
    double[][] discounting = new double[nbPath][nbPeriod + 1];
    double price = 0.0;
    double[] pricePath = new double[nbPath];
    for (int looppath = 0; looppath < nbPath; looppath++) {
      discounting[looppath][nbPeriod] = 1.0;
      for (int loopdsc = nbPeriod - 1; loopdsc >= 0; loopdsc--) {
        discounting[looppath][loopdsc] = discounting[looppath][loopdsc + 1] * (1.0 + pathIborRate[loopdsc][looppath] * mcResults.getDelta()[loopdsc]);
      }
      for (int loopimpact = 0; loopimpact < nbImpact; loopimpact++) {
        pricePath[looppath] += impactAmount[impactIndex[loopimpact]] * discounting[looppath][impactIndex[loopimpact]];
      }
      price += Math.max(pricePath[looppath], 0);
    }
    return price * (swaption.isLong() ? 1.0 : -1.0);
  }

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final MonteCarloIborRateDataBundle mcResults) {
    double strike = swaption.getStrike();
    double[][] pathIborRate = mcResults.getPathIborRate()[0];
    double[] impactAmount = mcResults.getImpactAmount()[0];
    int[] impactIndex = mcResults.getImpactIndex()[0];
    int nbFixed = 0; // The number of fixed coupons.
    while (impactIndex[nbFixed] < impactIndex[nbFixed + 1]) {
      nbFixed++;
    }
    nbFixed++;
    int nbImpact = impactIndex.length;
    int nbPath = pathIborRate[0].length;
    int nbPeriod = pathIborRate.length;
    double[][] discounting = new double[nbPath][nbPeriod + 1];
    double price = 0.0;
    double omega = (swaption.getUnderlyingSwap().getFixedLeg().isPayer() ? 1.0 : -1.0);
    for (int looppath = 0; looppath < nbPath; looppath++) {
      double fixedPath = 0.0;
      double floatPath = 0.0;
      double swapRatePath = 0.0;
      discounting[looppath][nbPeriod] = 1.0;
      for (int loopdsc = nbPeriod - 1; loopdsc >= 0; loopdsc--) {
        discounting[looppath][loopdsc] = discounting[looppath][loopdsc + 1] * (1.0 + pathIborRate[loopdsc][looppath] * mcResults.getDelta()[loopdsc]);
      }
      for (int loopfixed = 0; loopfixed < nbFixed; loopfixed++) {
        fixedPath += impactAmount[loopfixed] * discounting[looppath][impactIndex[loopfixed]];
      }
      for (int loopfloat = nbFixed; loopfloat < nbImpact; loopfloat++) {
        floatPath += impactAmount[loopfloat] * discounting[looppath][impactIndex[loopfloat]];
      }
      swapRatePath = -floatPath / fixedPath;
      double annuityCashPath = SwapFixedIborMethod.getAnnuityCash(swaption.getUnderlyingSwap(), swapRatePath);
      price += annuityCashPath * Math.max(omega * (swapRatePath - strike), 0.0) * discounting[looppath][impactIndex[nbFixed]];
    }
    return price * (swaption.isLong() ? 1.0 : -1.0);
  }

}
