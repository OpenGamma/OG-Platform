/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

/**
 *  Interface for any model where the option's (Black) volatility is a function of the forward, the strike and the timeToExpiry only. 
 *  The model itself will most likely have a set of parameters (e.g. the alpha, beta, nu & rho of SABR), but they are nothing to do with this 
 *  interface. The 1D refers to the number of time dimensions. 
 */
public interface VolatilityModel1D extends VolatilityModel<double[]> {

  double getVolatility(final double forward, final double strike, final double timeToExpiry);

  double getVolatility(final SimpleOptionData option);
}
