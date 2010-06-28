/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */

public interface InterestRateDerivative {

  double getRate(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve);

  double getLastUsedTime();
}
