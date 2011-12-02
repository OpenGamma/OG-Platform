/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.montecarlo;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.derivative.CouponIborRatchet;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * Computes the instrument price as the average over different paths and the derivative of the output with respect to the inputs. 
 * The data bundle contains the different discount factor paths and the instrument reference amounts. The method set the derivatives to the inputs.
 */
public class MonteCarloDiscountFactorDerivativeCalculator extends AbstractInstrumentDerivativeVisitor<MonteCarloDiscountFactorDerivativeDataBundle, Double> {

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
  public Double visit(final InstrumentDerivative derivative, final MonteCarloDiscountFactorDerivativeDataBundle mcResults) {
    Validate.notNull(derivative);
    return derivative.accept(this, mcResults);
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final MonteCarloDiscountFactorDerivativeDataBundle mcResults) {
    // Forward sweep
    Double[][][] pathDiscountFactors = mcResults.getPathDiscountingFactor();
    double[][] impactAmount = mcResults.getImpactAmount();
    Validate.isTrue(pathDiscountFactors[0].length == 1, "Only one decision date for swaptions.");
    double price = 0;
    int nbPath = pathDiscountFactors.length;
    double[] swapPathValue = new double[nbPath];
    for (int looppath = 0; looppath < nbPath; looppath++) {
      for (int loopcf = 0; loopcf < impactAmount[0].length; loopcf++) {
        swapPathValue[looppath] += impactAmount[0][loopcf] * pathDiscountFactors[looppath][0][loopcf];
      }
      price += Math.max(swapPathValue[looppath], 0);
    }
    price = price / nbPath * (swaption.isLong() ? 1.0 : -1.0);
    // Backward sweep
    double priceBar = 1.0;
    double[] swapPathValueBar = new double[nbPath];
    for (int looppath = 0; looppath < nbPath; looppath++) {
      swapPathValueBar[looppath] = ((swapPathValue[looppath] > 0) ? 1.0 : 0.0) / nbPath * priceBar;
    }
    double[][] impactAmountBar = new double[1][impactAmount[0].length];
    for (int looppath = 0; looppath < nbPath; looppath++) {
      for (int loopcf = 0; loopcf < impactAmount[0].length; loopcf++) {
        impactAmountBar[0][loopcf] += pathDiscountFactors[looppath][0][loopcf] * swapPathValueBar[looppath];
      }
    }
    Double[][][] pathDiscountFactorsBar = new Double[nbPath][1][];
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
    Double[][][] pathDiscountFactors = mcResults.getPathDiscountingFactor();
    double[][] impactAmount = mcResults.getImpactAmount();
    double price = 0.0;
    int nbPath = pathDiscountFactors.length;
    int nbCpn = annuity.getNumberOfPayments();
    double[] annuityPathValue = new double[nbPath];
    double[][] cpnRate = new double[nbCpn][nbPath];
    double[][] ibor = new double[nbCpn][nbPath];
    double[][] cpnMain = new double[nbCpn][nbPath];
    double[][] cpnFloor = new double[nbCpn][nbPath];
    double[][] cpnCap = new double[nbCpn][nbPath];
    for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) { //nbCpn
      if (annuity.isFixed()[loopcpn]) { // Coupon already fixed: only one cash flow
        CouponFixed cpn = (CouponFixed) annuity.getNthPayment(loopcpn);
        for (int looppath = 0; looppath < nbPath; looppath++) {
          cpnRate[loopcpn][looppath] = cpn.getFixedRate();
          annuityPathValue[looppath] += impactAmount[loopcpn][0] * pathDiscountFactors[looppath][loopcpn][0];
        }
      } else {
        if (annuity.getNthPayment(loopcpn) instanceof CouponIborRatchet) {
          CouponIborRatchet cpn = (CouponIborRatchet) annuity.getNthPayment(loopcpn);
          for (int looppath = 0; looppath < nbPath; looppath++) {
            ibor[loopcpn][looppath] = (-impactAmount[loopcpn][0] * pathDiscountFactors[looppath][loopcpn][0] / (impactAmount[loopcpn][1] * pathDiscountFactors[looppath][loopcpn][1]) - 1.0)
                / cpn.getFixingYearFraction();
            cpnMain[loopcpn][looppath] = cpn.getMainCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getMainCoefficients()[1] * ibor[loopcpn][looppath] + cpn.getMainCoefficients()[2];
            cpnFloor[loopcpn][looppath] = cpn.getFloorCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getFloorCoefficients()[1] * ibor[loopcpn][looppath] + cpn.getFloorCoefficients()[2];
            cpnCap[loopcpn][looppath] = cpn.getCapCoefficients()[0] * cpnRate[loopcpn - 1][looppath] + cpn.getCapCoefficients()[1] * ibor[loopcpn][looppath] + cpn.getCapCoefficients()[2];
            cpnRate[loopcpn][looppath] = Math.min(Math.max(cpnFloor[loopcpn][looppath], cpnMain[loopcpn][looppath]), cpnCap[loopcpn][looppath]);
            annuityPathValue[looppath] += cpnRate[loopcpn][looppath] * cpn.getPaymentYearFraction() * cpn.getNotional() * pathDiscountFactors[looppath][loopcpn][1];
          }
        } else {
          CouponIborGearing cpn = (CouponIborGearing) annuity.getNthPayment(loopcpn);
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
    double priceBar = 1.0;
    double[] annuityPathValueBar = new double[nbPath];
    for (int looppath = 0; looppath < nbPath; looppath++) {
      annuityPathValueBar[looppath] = 1.0 / nbPath * priceBar;
    }
    double[][] impactAmountBar = new double[nbCpn][];
    double[][] cpnRateBar = new double[nbCpn][nbPath];
    double[][] iborBar = new double[nbCpn][nbPath];
    double[][] cpnMainBar = new double[nbCpn][nbPath];
    double[][] cpnFloorBar = new double[nbCpn][nbPath];
    double[][] cpnCapBar = new double[nbCpn][nbPath];
    Double[][][] pathDiscountFactorsBar = new Double[nbPath][nbCpn][];
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
          CouponIborRatchet cpn = (CouponIborRatchet) annuity.getNthPayment(loopcpn);
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
            impactAmountBar[loopcpn][0] += -pathDiscountFactors[looppath][loopcpn][0] / (impactAmount[loopcpn][1] * pathDiscountFactors[looppath][loopcpn][1]) / cpn.getFixingYearFraction()
                * iborBar[loopcpn][looppath];
            impactAmountBar[loopcpn][1] += impactAmount[loopcpn][0] * pathDiscountFactors[looppath][loopcpn][0] / pathDiscountFactors[looppath][loopcpn][1]
                / (impactAmount[loopcpn][1] * impactAmount[loopcpn][1]) / cpn.getFixingYearFraction() * iborBar[loopcpn][looppath];
            pathDiscountFactorsBar[looppath][loopcpn][1] = cpnRate[loopcpn][looppath] * cpn.getPaymentYearFraction() * cpn.getNotional() * annuityPathValueBar[looppath];
            pathDiscountFactorsBar[looppath][loopcpn][0] = -impactAmount[loopcpn][0] / (impactAmount[loopcpn][1] * pathDiscountFactors[looppath][loopcpn][1]) / cpn.getFixingYearFraction()
                * iborBar[loopcpn][looppath];
            pathDiscountFactorsBar[looppath][loopcpn][1] += impactAmount[loopcpn][0] * pathDiscountFactors[looppath][loopcpn][0] / impactAmount[loopcpn][1]
                / (pathDiscountFactors[looppath][loopcpn][1] * pathDiscountFactors[looppath][loopcpn][1]) / cpn.getFixingYearFraction() * iborBar[loopcpn][looppath];
          }
        } else {
          CouponIborGearing cpn = (CouponIborGearing) annuity.getNthPayment(loopcpn);
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
