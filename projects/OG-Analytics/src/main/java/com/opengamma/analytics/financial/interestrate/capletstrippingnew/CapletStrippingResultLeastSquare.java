/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CapletStrippingResultLeastSquare extends CapletStrippingResult {

  private final LeastSquareResults _results;

  public CapletStrippingResultLeastSquare(final LeastSquareResults results, final DiscreteVolatilityFunction func, final MultiCapFloorPricer pricer) {
    super(check(results), func, pricer);
    _results = results;
  }

  @Override
  public double getChiSq() {
    return _results.getChiSq();
  }

  private static DoubleMatrix1D check(final LeastSquareResults results) {
    ArgumentChecker.notNull(results, "results");
    return results.getFitParameters();
  }

}
