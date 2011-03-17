/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

/**
 * 
 */
public interface QuadratureWeightAndAbscissaFunction {

  GaussianQuadratureFunction generate(int n, Double... parameters);

}
