/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunction;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * The result of a root-finder based caplet stripping 
 */
public class CapletStrippingResultRootFind extends CapletStrippingResult {

  /**
   * set up the results 
   * @param fitParms The calibrated model parameters 
   * @param func the function that maps model parameters into caplet volatilities 
   * @param pricer The pricer (which contained the details of the market values of the caps/floors) used in the calibrate
   */
  public CapletStrippingResultRootFind(final DoubleMatrix1D fitParms, final DiscreteVolatilityFunction func, final MultiCapFloorPricer pricer) {
    super(fitParms, func, pricer);
  }

  @Override
  public double getChiSq() {
    return 0;
  }

}
