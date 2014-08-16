/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunction;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.util.ArgumentChecker;

/**
 * The result of a least-squares based caplet stripping 
 */
public class CapletStrippingResultLeastSquare extends CapletStrippingResult {

  private final LeastSquareResults _results;

  /**
   * set up the results 
   * @param results The results from the least-squares fit ({@link LeastSquareResults}
  * @param func the function that maps model parameters into caplet volatilities 
   * @param pricer The pricer (which contained the details of the market values of the caps/floors) used in the calibrate
   */
  public CapletStrippingResultLeastSquare(final LeastSquareResults results, final DiscreteVolatilityFunction func, final MultiCapFloorPricer pricer) {
    super(check(results), func, pricer);
    _results = results;
  }

  /**
   * The (weighted) sum of squares between the market and (calibrated) model values 
   * @return the chi-squared
   */
  @Override
  public double getChiSq() {
    return _results.getChiSq();
  }

  private static DoubleMatrix1D check(final LeastSquareResults results) {
    ArgumentChecker.notNull(results, "results");
    return results.getFitParameters();
  }

}
