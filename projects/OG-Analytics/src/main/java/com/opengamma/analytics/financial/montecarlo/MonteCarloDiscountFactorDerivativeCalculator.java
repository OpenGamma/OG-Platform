/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * Computes the instrument price as the average over different paths and the derivative of the output with respect to the inputs. 
 * The data bundle contains the different discount factor paths and the instrument reference amounts. The method set the derivatives to the inputs.
 */
public class MonteCarloDiscountFactorDerivativeCalculator extends InstrumentDerivativeVisitorAdapter<MonteCarloDiscountFactorDerivativeDataBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final MonteCarloDiscountFactorDerivativeCalculator INSTANCE = new MonteCarloDiscountFactorDerivativeCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static MonteCarloDiscountFactorDerivativeCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  MonteCarloDiscountFactorDerivativeCalculator() {
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final MonteCarloDiscountFactorDerivativeDataBundle mcResults) {
    // Forward sweep
    final Double[][][] pathDiscountFactors = mcResults.getPathDiscountingFactor();
    final double[][] impactAmount = mcResults.getImpactAmount();
    Validate.isTrue(pathDiscountFactors[0].length == 1, "Only one decision date for swaptions.");
    double price = 0;
    final int nbPath = pathDiscountFactors.length;
    final double[] swapPathValue = new double[nbPath];
    for (int looppath = 0; looppath < nbPath; looppath++) {
      for (int loopcf = 0; loopcf < impactAmount[0].length; loopcf++) {
        swapPathValue[looppath] += impactAmount[0][loopcf] * pathDiscountFactors[looppath][0][loopcf];
      }
      price += Math.max(swapPathValue[looppath], 0);
    }
    price = price / nbPath * (swaption.isLong() ? 1.0 : -1.0);
    // Backward sweep
    final double priceBar = 1.0;
    final double[] swapPathValueBar = new double[nbPath];
    for (int looppath = 0; looppath < nbPath; looppath++) {
      swapPathValueBar[looppath] = ((swapPathValue[looppath] > 0) ? 1.0 : 0.0) / nbPath * priceBar;
    }
    final double[][] impactAmountBar = new double[1][impactAmount[0].length];
    for (int looppath = 0; looppath < nbPath; looppath++) {
      for (int loopcf = 0; loopcf < impactAmount[0].length; loopcf++) {
        impactAmountBar[0][loopcf] += pathDiscountFactors[looppath][0][loopcf] * swapPathValueBar[looppath];
      }
    }
    final Double[][][] pathDiscountFactorsBar = new Double[nbPath][1][];
    for (int looppath = 0; looppath < nbPath; looppath++) {
      pathDiscountFactorsBar[looppath][0] = new Double[impactAmount[0].length];
      for (int loopcf = 0; loopcf < impactAmount[0].length; loopcf++) {
        pathDiscountFactorsBar[looppath][0][loopcf] = impactAmount[0][loopcf] * swapPathValueBar[looppath];
      }
    }
    mcResults.setImpactAmountDerivative(impactAmountBar);
    mcResults.setPathDiscountingFactorDerivative(pathDiscountFactorsBar);
    return price;
  }

  @Override
  public Double visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final MonteCarloDiscountFactorDerivativeDataBundle mcResults) {
    final Double[][][] pathDiscountFactors = mcResults.getPathDiscountingFactor();
    final double[][] impactAmount = mcResults.getImpactAmount();
    double price = 0.0;
    final int nbPath = pathDiscountFactors.length;
    final int nbCpn = annuity.getNumberOfPayments();
    final double[] annuityPathValue = new double[nbPath];
    final double[][] cpnRate = new double[nbCpn][nbPath];
    final double[][] ibor = new double[nbCpn][nbPath];
    final double[][] cpnMain = new double[nbCpn][nbPath];
    final double[][] cpnFloor = new double[nbCpn][nbPath];
    final double[][] cpnCap = new double[nbCpn][nbPath];
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
            ibor[loopcpn][looppath] = (-impactAmount[loopcpn][0] * pathDiscountFactors[looppath][loopcpn][0] / (impactAmount[loopcpn][1] * pathDiscountFactors[looppath][loopcpn][1]) - 1.0)
                / cpn.getFixingAccrualFactor();
            cpnMain[loopcpn][looppath] = cpn.getMainCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getMainCoefficients()[1] * ibor[loopcpn][looppath] + cpn.getMainCoefficients()[2];
            cpnFloor[loopcpn][looppath] = cpn.getFloorCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getFloorCoefficients()[1] * ibor[loopcpn][looppath] + cpn.getFloorCoefficients()[2];
            cpnCap[loopcpn][looppath] = cpn.getCapCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getCapCoefficients()[1] * ibor[loopcpn][looppath] + cpn.getCapCoefficients()[2];
            cpnRate[loopcpn][looppath] = Math.min(Math.max(cpnFloor[loopcpn][looppath], cpnMain[loopcpn][looppath]), cpnCap[loopcpn][looppath]);
            annuityPathValue[looppath] += cpnRate[loopcpn][looppath] * cpn.getPaymentYearFraction() * cpn.getNotional() * pathDiscountFactors[looppath][loopcpn][1];
          }
        } else {
          final CouponIborGearing cpn = (CouponIborGearing) annuity.getNthPayment(loopcpn);
          for (int looppath = 0; looppath < nbPath; looppath++) {
            ibor[loopcpn][looppath] = (-impactAmount[0][0] * pathDiscountFactors[looppath][0][0] / (impactAmount[0][1] * pathDiscountFactors[looppath][0][1]) - 1.0) / cpn.getFixingAccrualFactor();
            cpnRate[loopcpn][looppath] = cpn.getFactor() * ibor[loopcpn][looppath] + cpn.getSpread();
            annuityPathValue[looppath] += cpnRate[loopcpn][looppath] * cpn.getPaymentYearFraction() * cpn.getNotional() * pathDiscountFactors[looppath][loopcpn][1];
          }
        }
      }
    }
    for (int looppath = 0; looppath < nbPath; looppath++) {
      price += annuityPathValue[looppath];
    }
    price = price / nbPath;
    // Backward sweep
    final double priceBar = 1.0;
    final double[] annuityPathValueBar = new double[nbPath];
    for (int looppath = 0; looppath < nbPath; looppath++) {
      annuityPathValueBar[looppath] = 1.0 / nbPath * priceBar;
    }
    final double[][] impactAmountBar = new double[nbCpn][];
    final double[][] cpnRateBar = new double[nbCpn][nbPath];
    final double[][] iborBar = new double[nbCpn][nbPath];
    final double[][] cpnMainBar = new double[nbCpn][nbPath];
    final double[][] cpnFloorBar = new double[nbCpn][nbPath];
    final double[][] cpnCapBar = new double[nbCpn][nbPath];
    final Double[][][] pathDiscountFactorsBar = new Double[nbPath][nbCpn][];
    for (int loopcpn = nbCpn - 1; loopcpn >= 0; loopcpn--) {
      impactAmountBar[loopcpn] = new double[impactAmount[loopcpn].length];
      for (int looppath = 0; looppath < nbPath; looppath++) {
        pathDiscountFactorsBar[looppath][loopcpn] = new Double[impactAmount[loopcpn].length];
      }
      if (annuity.isFixed()[loopcpn]) { // Coupon already fixed: only one cash flow
        for (int looppath = 0; looppath < nbPath; looppath++) {
          impactAmountBar[loopcpn][0] += pathDiscountFactors[looppath][loopcpn][0] * annuityPathValueBar[looppath];
          pathDiscountFactorsBar[looppath][loopcpn][0] = impactAmount[loopcpn][0] * annuityPathValueBar[looppath];
        }
      } else {
        if (annuity.getNthPayment(loopcpn) instanceof CouponIborRatchet) {
          final CouponIborRatchet cpn = (CouponIborRatchet) annuity.getNthPayment(loopcpn);
          for (int looppath = 0; looppath < nbPath; looppath++) {
            cpnRateBar[loopcpn][looppath] += cpn.getPaymentYearFraction() * cpn.getNotional() * pathDiscountFactors[looppath][loopcpn][1] * annuityPathValueBar[looppath];
            cpnCapBar[loopcpn][looppath] = (cpnCap[loopcpn][looppath] < Math.max(cpnFloor[loopcpn][looppath], cpnMain[loopcpn][looppath]) ? 1.0 : 0.0) * cpnRateBar[loopcpn][looppath];
            cpnFloorBar[loopcpn][looppath] = (cpnCap[loopcpn][looppath] >= Math.max(cpnFloor[loopcpn][looppath], cpnMain[loopcpn][looppath]) ? 1.0 : 0.0)
                * (cpnFloor[loopcpn][looppath] > cpnMain[loopcpn][looppath] ? 1.0 : 0.0) * cpnRateBar[loopcpn][looppath];
            cpnMainBar[loopcpn][looppath] = (cpnCap[loopcpn][looppath] >= Math.max(cpnFloor[loopcpn][looppath], cpnMain[loopcpn][looppath]) ? 1.0 : 0.0)
                * (cpnFloor[loopcpn][looppath] <= cpnMain[loopcpn][looppath] ? 1.0 : 0.0) * cpnRateBar[loopcpn][looppath];
            cpnRateBar[loopcpn - 1][looppath] += cpn.getCapCoefficients()[0] * cpnCapBar[loopcpn][looppath] + cpn.getFloorCoefficients()[0] * cpnFloorBar[loopcpn][looppath]
                + cpn.getMainCoefficients()[0] * cpnMainBar[loopcpn][looppath];
            iborBar[loopcpn][looppath] = cpn.getMainCoefficients()[1] * cpnMainBar[loopcpn][looppath] + cpn.getFloorCoefficients()[1] * cpnFloorBar[loopcpn][looppath] + cpn.getCapCoefficients()[1]
                * cpnCapBar[loopcpn][looppath];
            impactAmountBar[loopcpn][0] += -pathDiscountFactors[looppath][loopcpn][0] / (impactAmount[loopcpn][1] * pathDiscountFactors[looppath][loopcpn][1]) / cpn.getFixingAccrualFactor()
                * iborBar[loopcpn][looppath];
            impactAmountBar[loopcpn][1] += impactAmount[loopcpn][0] * pathDiscountFactors[looppath][loopcpn][0] / pathDiscountFactors[looppath][loopcpn][1]
                / (impactAmount[loopcpn][1] * impactAmount[loopcpn][1]) / cpn.getFixingAccrualFactor() * iborBar[loopcpn][looppath];
            pathDiscountFactorsBar[looppath][loopcpn][1] = cpnRate[loopcpn][looppath] * cpn.getPaymentYearFraction() * cpn.getNotional() * annuityPathValueBar[looppath];
            pathDiscountFactorsBar[looppath][loopcpn][0] = -impactAmount[loopcpn][0] / (impactAmount[loopcpn][1] * pathDiscountFactors[looppath][loopcpn][1]) / cpn.getFixingAccrualFactor()
                * iborBar[loopcpn][looppath];
            pathDiscountFactorsBar[looppath][loopcpn][1] += impactAmount[loopcpn][0] * pathDiscountFactors[looppath][loopcpn][0] / impactAmount[loopcpn][1]
                / (pathDiscountFactors[looppath][loopcpn][1] * pathDiscountFactors[looppath][loopcpn][1]) / cpn.getFixingAccrualFactor() * iborBar[loopcpn][looppath];
          }
        } else {
          final CouponIborGearing cpn = (CouponIborGearing) annuity.getNthPayment(loopcpn);
          for (int looppath = 0; looppath < nbPath; looppath++) {
            cpnRateBar[loopcpn][looppath] += cpn.getPaymentYearFraction() * cpn.getNotional() * pathDiscountFactors[looppath][loopcpn][1] * annuityPathValueBar[looppath];
            iborBar[loopcpn][looppath] = cpn.getFactor() * cpnRateBar[loopcpn][looppath];
            impactAmountBar[0][0] += -pathDiscountFactors[looppath][0][0] / (impactAmount[0][1] * pathDiscountFactors[looppath][0][1]) / cpn.getFixingAccrualFactor() * iborBar[loopcpn][looppath];
            impactAmountBar[0][1] += impactAmount[0][0] * pathDiscountFactors[looppath][0][0] / pathDiscountFactors[looppath][0][1] / (impactAmount[0][1] * impactAmount[0][1])
                / cpn.getFixingAccrualFactor() * iborBar[loopcpn][looppath];
            pathDiscountFactorsBar[looppath][loopcpn][1] = cpnRate[loopcpn][looppath] * cpn.getPaymentYearFraction() * cpn.getNotional() * annuityPathValueBar[looppath];
            pathDiscountFactorsBar[looppath][0][0] = -impactAmount[0][0] / (impactAmount[0][1] * pathDiscountFactors[looppath][0][1]) / cpn.getFixingAccrualFactor() * iborBar[loopcpn][looppath];
            pathDiscountFactorsBar[looppath][0][1] += impactAmount[0][0] * pathDiscountFactors[looppath][0][0] / impactAmount[0][1]
                / (pathDiscountFactors[looppath][0][1] * pathDiscountFactors[looppath][0][1]) / cpn.getFixingAccrualFactor() * iborBar[loopcpn][looppath];
          }
        }
      }
    }
    mcResults.setImpactAmountDerivative(impactAmountBar);
    mcResults.setPathDiscountingFactorDerivative(pathDiscountFactorsBar);
    return price;
  }
}
