/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.montecarlo;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * Computes the instrument price as the average over different paths. The data bundle contains the different discount factor paths and the instrument reference amounts.
 */
public class MonteCarloDiscountFactorDerivativeCalculator extends AbstractInterestRateDerivativeVisitor<MonteCarloDiscountFactorDerivativeDataBundle, Double> {

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
  public Double visit(final InterestRateDerivative derivative, final MonteCarloDiscountFactorDerivativeDataBundle mcResults) {
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

}
