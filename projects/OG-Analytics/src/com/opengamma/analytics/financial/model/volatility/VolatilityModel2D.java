/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;


/**
 *  Interface for any model where the option's (Black) volatility is a function of the forward, the strike, timeToExpiry and an additional 
 *  period (i.e. swap length for swaption). The model itself will most likely have a set of parameters (e.g. the alpha, beta, nu & rho of SABR), 
 *  but they are nothing to do with this interface. The 2D refers to the number of time dimensions. 
 */
public interface VolatilityModel2D extends VolatilityModel<double[]> {

  double getVolatility(final double forward, final double strike, final double timeToExpiry, final double tenor);
}
