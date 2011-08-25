/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method.montecarlo;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * Computes the instrument price as the average over different paths. The data bundle contains the different discount factor paths and the instrument reference amounts.
 */
public class MonteCarloDiscountFactorCalculator extends AbstractInterestRateDerivativeVisitor<MonteCarloDiscountFactorDataBundle, Double> {

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
  public Double visit(final InterestRateDerivative derivative, final MonteCarloDiscountFactorDataBundle mcResults) {
    Validate.notNull(derivative);
    return derivative.accept(this, mcResults);
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final MonteCarloDiscountFactorDataBundle mcResults) {
    Double[][][] pathDiscountFactors = mcResults.getPathDiscountingFactors();
    double[][] impactAmount = mcResults.getImpactAmount();
    Validate.isTrue(pathDiscountFactors[0].length == 1, "Only one decision date for swaptions.");
    double price = 0;
    int nbPath = pathDiscountFactors.length;
    double swapPathValue;
    for (int looppath = 0; looppath < nbPath; looppath++) {
      swapPathValue = 0;
      for (int loopcf = 0; loopcf < impactAmount[0].length; loopcf++) {
        swapPathValue += impactAmount[0][loopcf] * pathDiscountFactors[looppath][0][loopcf];
      }
      price += Math.max(swapPathValue, 0);
    }
    price = price / nbPath;
    return price;
  }

}
