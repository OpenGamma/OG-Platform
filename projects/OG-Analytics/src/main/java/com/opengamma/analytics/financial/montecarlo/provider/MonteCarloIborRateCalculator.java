/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo.provider;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.montecarlo.MonteCarloIborRateDataBundle;

/**
 * Computes the total instrument price over different paths (the sum of prices over the different paths, not its average).
 * The data bundle contains the different Ibor rates paths and the instrument reference amounts. The numeraire is the last time in the LMM description.
 */
public class MonteCarloIborRateCalculator extends InstrumentDerivativeVisitorAdapter<MonteCarloIborRateDataBundle, Double> {

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

  /**
   * The swap method.
   */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  @Override
  public Double visitCapFloorIbor(final CapFloorIbor payment, final MonteCarloIborRateDataBundle mcResults) {
    Validate.isTrue(mcResults.getPathIborRate().length == 1, "Only one decision date for cap/floor.");
    final double[][] pathIborRate = mcResults.getPathIborRate()[0];
    final double[] impactAmount = mcResults.getImpactAmount()[0];
    final int[] impactIndex = mcResults.getImpactIndex()[0];
    double price = 0;
    final int nbPath = pathIborRate[0].length;
    final int nbPeriod = pathIborRate.length;
    final double[] ibor = new double[nbPath];
    double payoff;
    final double[] discounting = new double[nbPath];
    final double omega = (payment.isCap() ? 1.0 : -1.0);
    for (int looppath = 0; looppath < nbPath; looppath++) {
      ibor[looppath] = impactAmount[0] * pathIborRate[impactIndex[0]][looppath] + (impactAmount[0] - 1.0) / payment.getFixingAccrualFactor();
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
    final double[][] pathIborRate = mcResults.getPathIborRate()[0];
    final double[] impactAmount = mcResults.getImpactAmount()[0];
    final int[] impactIndex = mcResults.getImpactIndex()[0];
    final int nbImpact = impactIndex.length;
    final int nbPath = pathIborRate[0].length;
    final int nbPeriod = pathIborRate.length;
    final double[][] discounting = new double[nbPath][nbPeriod + 1];
    double price = 0.0;
    final double[] pricePath = new double[nbPath];
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
    final double strike = swaption.getStrike();
    final double[][] pathIborRate = mcResults.getPathIborRate()[0];
    final double[] impactAmount = mcResults.getImpactAmount()[0];
    final int[] impactIndex = mcResults.getImpactIndex()[0];
    int nbFixed = 0; // The number of fixed coupons.
    while (impactIndex[nbFixed] < impactIndex[nbFixed + 1]) {
      nbFixed++;
    }
    nbFixed++;
    final int nbImpact = impactIndex.length;
    final int nbPath = pathIborRate[0].length;
    final int nbPeriod = pathIborRate.length;
    final double[][] discounting = new double[nbPath][nbPeriod + 1];
    double price = 0.0;
    final double omega = (swaption.getUnderlyingSwap().getFixedLeg().isPayer() ? 1.0 : -1.0);
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
      final double annuityCashPath = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), swapRatePath);
      price += annuityCashPath * Math.max(omega * (swapRatePath - strike), 0.0) * discounting[looppath][impactIndex[nbFixed]];
    }
    return price * (swaption.isLong() ? 1.0 : -1.0);
  }

  @Override
  public Double visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final MonteCarloIborRateDataBundle mcResults) {
    final int nbCpn = annuity.getNumberOfPayments();
    final double[][][] pathIborRate = mcResults.getPathIborRate(); //    Size: nbJump x nbPeriodLMM x nbPath
    final int nbPath = pathIborRate[0][0].length;
    final int nbPeriod = pathIborRate[0].length;
    final double[][] impactAmount = mcResults.getImpactAmount(); // impact - amount
    final int[][] impactIndex = mcResults.getImpactIndex(); // impact - index
    final double[] delta = mcResults.getDelta();
    // Discount factors
    final double[][][] discounting = new double[nbCpn][nbPeriod + 1][nbPath];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) { //nbCpn
      for (int looppath = 0; looppath < nbPath; looppath++) {
        discounting[loopcpn][nbPeriod][looppath] = 1.0;
        for (int loopdsc = nbPeriod - 1; loopdsc >= 0; loopdsc--) {
          discounting[loopcpn][loopdsc][looppath] = discounting[loopcpn][loopdsc + 1][looppath] * (1.0 + pathIborRate[loopcpn][loopdsc][looppath] * delta[loopdsc]);
        }
      }
    }
    // Coupons and annuity value
    final double[][] cpnRate = new double[nbCpn][nbPath];
    final double[] annuityPathValue = new double[nbPath];
    double ibor;
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) { //nbCpn
      if (annuity.isFixed()[loopcpn]) { // Coupon already fixed: only one cash flow
        final CouponFixed cpn = (CouponFixed) annuity.getNthPayment(loopcpn);
        for (int looppath = 0; looppath < nbPath; looppath++) {
          cpnRate[loopcpn][looppath] = cpn.getFixedRate();
          annuityPathValue[looppath] += impactAmount[loopcpn][0] * discounting[loopcpn][impactIndex[loopcpn][0]][looppath];
        }
      } else {
        if (annuity.getNthPayment(loopcpn) instanceof CouponIborRatchet) {
          final CouponIborRatchet cpn = (CouponIborRatchet) annuity.getNthPayment(loopcpn);
          for (int looppath = 0; looppath < nbPath; looppath++) {
            ibor = (-impactAmount[loopcpn][0] * discounting[loopcpn][impactIndex[loopcpn][0]][looppath] / (impactAmount[loopcpn][1] * discounting[loopcpn][impactIndex[loopcpn][1]][looppath]) - 1.0)
                / cpn.getFixingAccrualFactor();
            final double cpnMain = cpn.getMainCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getMainCoefficients()[1] * ibor + cpn.getMainCoefficients()[2];
            final double cpnFloor = cpn.getFloorCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getFloorCoefficients()[1] * ibor + cpn.getFloorCoefficients()[2];
            final double cpnCap = cpn.getCapCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getCapCoefficients()[1] * ibor + cpn.getCapCoefficients()[2];
            cpnRate[loopcpn][looppath] = Math.min(Math.max(cpnFloor, cpnMain), cpnCap);
            annuityPathValue[looppath] += cpnRate[loopcpn][looppath] * cpn.getPaymentYearFraction() * cpn.getNotional() * discounting[loopcpn][impactIndex[loopcpn][1]][looppath];
          }
        } else {
          final CouponIborGearing cpn = (CouponIborGearing) annuity.getNthPayment(loopcpn); // Only possible for the first coupon
          for (int looppath = 0; looppath < nbPath; looppath++) {
            ibor = (-impactAmount[0][0] * discounting[loopcpn][impactIndex[loopcpn][0]][looppath] / (impactAmount[0][1] * discounting[loopcpn][impactIndex[loopcpn][1]][looppath]) - 1.0)
                / cpn.getFixingAccrualFactor();
            cpnRate[loopcpn][looppath] = cpn.getFactor() * ibor + cpn.getSpread();
            annuityPathValue[looppath] += cpnRate[loopcpn][looppath] * cpn.getPaymentYearFraction() * cpn.getNotional() * discounting[loopcpn][impactIndex[loopcpn][1]][looppath];
          }
        }
      }
    }
    double price = 0.0;
    for (int looppath = 0; looppath < nbPath; looppath++) {
      price += annuityPathValue[looppath];
    }
    return price;
  }

}
