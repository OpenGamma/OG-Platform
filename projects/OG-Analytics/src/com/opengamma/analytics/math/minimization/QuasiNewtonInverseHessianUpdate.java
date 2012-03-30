/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;


/**
 * 
 */
public interface QuasiNewtonInverseHessianUpdate {

  void update(QuasiNewtonVectorMinimizer.DataBundle data);

}
