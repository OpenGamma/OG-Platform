/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class CapletStrippingResultRootFind extends CapletStrippingResult {

  public CapletStrippingResultRootFind(final DoubleMatrix1D fitParms, final DiscreteVolatilityFunction func, final MultiCapFloorPricer pricer) {
    super(fitParms, func, pricer);
  }

  @Override
  public double getChiSq() {
    return 0;
  }

}
