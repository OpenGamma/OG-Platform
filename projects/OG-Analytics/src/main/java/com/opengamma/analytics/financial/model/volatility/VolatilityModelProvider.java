/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * This abstracts a mapping from a set of model parameters (given as a DoubleMatrix1D) to a volatility surface (given as
 * a VolatilityModel1D)
 */
public abstract class VolatilityModelProvider extends Function1D<DoubleMatrix1D, VolatilityModel1D> {
}
