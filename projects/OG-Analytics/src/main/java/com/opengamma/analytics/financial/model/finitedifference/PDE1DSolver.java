/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

/**
 * @param <T> The type of the PDE coefficients
 */
public interface PDE1DSolver<T extends PDE1DCoefficients> {

  PDEResults1D solve(PDE1DDataBundle<T> pdeData);

  //void visit(T coeff);

}
