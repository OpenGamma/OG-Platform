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
 * 
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
    Double[][][] pD = mcResults.getPathDiscountingFactors();
    double[][] impactAmount = mcResults.getImpactAmount();
    Validate.isTrue(pD[0].length == 1, "Only one decision date for swaptions.");
    double p = 0;
    int nbPath = pD.length;
    double v;
    for (int looppath = 0; looppath < nbPath; looppath++) {
      v = 0;
      for (int loopcf = 0; loopcf < impactAmount[0].length; loopcf++) {
        v += impactAmount[0][loopcf] * pD[looppath][0][loopcf];
      }
      p += Math.max(v, 0);
    }
    p = p / nbPath;
    return p;
  }

}
