/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * Computes the instrument price as the average over different paths. The data bundle contains the different discount factor paths and the instrument reference amounts.
 */
public class MonteCarloDiscountFactorCalculator extends InstrumentDerivativeVisitorAdapter<MonteCarloDiscountFactorDataBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final MonteCarloDiscountFactorCalculator INSTANCE = new MonteCarloDiscountFactorCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static MonteCarloDiscountFactorCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  MonteCarloDiscountFactorCalculator() {
  }

  @Override
  public Double visitCapFloorIbor(final CapFloorIbor payment, final MonteCarloDiscountFactorDataBundle mcResults) {
    final Double[][][] pathDiscountFactors = mcResults.getPathDiscountingFactor();
    final double[][] impactAmount = mcResults.getImpactAmount();
    Validate.isTrue(pathDiscountFactors[0].length == 1, "Only one decision date for cap/floor.");
    double price = 0;
    final int nbPath = pathDiscountFactors.length;
    double ibor;
    final double omega = (payment.isCap() ? 1.0 : -1.0);
    for (int looppath = 0; looppath < nbPath; looppath++) {
      ibor = (-impactAmount[0][0] * pathDiscountFactors[looppath][0][0] / (impactAmount[0][1] * pathDiscountFactors[looppath][0][1]) - 1.0) / payment.getFixingAccrualFactor();
      price += Math.max(omega * (ibor - payment.getStrike()), 0) * pathDiscountFactors[looppath][0][2];
    }
    price = price / nbPath * payment.getNotional() * payment.getPaymentYearFraction();
    return price;
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final MonteCarloDiscountFactorDataBundle mcResults) {
    final Double[][][] pathDiscountFactors = mcResults.getPathDiscountingFactor();
    final double[][] impactAmount = mcResults.getImpactAmount();
    Validate.isTrue(pathDiscountFactors[0].length == 1, "Only one decision date for swaptions.");
    double price = 0;
    final int nbPath = pathDiscountFactors.length;
    double swapPathValue;
    for (int looppath = 0; looppath < nbPath; looppath++) {
      swapPathValue = 0;
      for (int loopcf = 0; loopcf < impactAmount[0].length; loopcf++) {
        swapPathValue += impactAmount[0][loopcf] * pathDiscountFactors[looppath][0][loopcf];
      }
      price += Math.max(swapPathValue, 0);
    }
    price = price / nbPath * (swaption.isLong() ? 1.0 : -1.0);
    return price;
  }

  @Override
  public Double visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final MonteCarloDiscountFactorDataBundle mcResults) {
    final Double[][][] pathDiscountFactors = mcResults.getPathDiscountingFactor();
    final double[][] impactAmount = mcResults.getImpactAmount();
    double price = 0.0;
    final int nbPath = pathDiscountFactors.length;
    final int nbCpn = annuity.getNumberOfPayments();
    final double[] annuityPathValue = new double[nbPath];
    final double[][] cpnRate = new double[nbCpn][nbPath];
    double ibor;
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) { //nbCpn
      if (annuity.isFixed()[loopcpn]) { // Coupon already fixed: only one cash flow
        final CouponFixed cpn = (CouponFixed) annuity.getNthPayment(loopcpn);
        for (int looppath = 0; looppath < nbPath; looppath++) {
          cpnRate[loopcpn][looppath] = cpn.getFixedRate();
          annuityPathValue[looppath] += impactAmount[loopcpn][0] * pathDiscountFactors[looppath][loopcpn][0];
        }
      } else {
        if (annuity.getNthPayment(loopcpn) instanceof CouponIborRatchet) {
          final CouponIborRatchet cpn = (CouponIborRatchet) annuity.getNthPayment(loopcpn);
          for (int looppath = 0; looppath < nbPath; looppath++) {
            ibor = (-impactAmount[loopcpn][0] * pathDiscountFactors[looppath][loopcpn][0] / (impactAmount[loopcpn][1] *
                pathDiscountFactors[looppath][loopcpn][1]) - 1.0) / cpn.getFixingAccrualFactor();
            final double cpnMain = cpn.getMainCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getMainCoefficients()[1] * ibor + cpn.getMainCoefficients()[2];
            final double cpnFloor = cpn.getFloorCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getFloorCoefficients()[1] * ibor + cpn.getFloorCoefficients()[2];
            final double cpnCap = cpn.getCapCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getCapCoefficients()[1] * ibor + cpn.getCapCoefficients()[2];
            cpnRate[loopcpn][looppath] = Math.min(Math.max(cpnFloor, cpnMain), cpnCap);
            annuityPathValue[looppath] += cpnRate[loopcpn][looppath] * cpn.getPaymentYearFraction() * cpn.getNotional() * pathDiscountFactors[looppath][loopcpn][1];
          }
        } else {
          final CouponIborGearing cpn = (CouponIborGearing) annuity.getNthPayment(loopcpn);
          for (int looppath = 0; looppath < nbPath; looppath++) {
            ibor = (-impactAmount[0][0] * pathDiscountFactors[looppath][0][0] / (impactAmount[0][1] * pathDiscountFactors[looppath][0][1]) - 1.0) / cpn.getFixingAccrualFactor();
            cpnRate[loopcpn][looppath] = cpn.getFactor() * ibor + cpn.getSpread();
            annuityPathValue[looppath] += cpnRate[loopcpn][looppath] * cpn.getPaymentYearFraction() * cpn.getNotional() * pathDiscountFactors[looppath][loopcpn][1];
          }
        }
      }
    }
    for (int looppath = 0; looppath < nbPath; looppath++) {
      price += annuityPathValue[looppath];
    }
    price = price / nbPath;
    return price;
  }

}
