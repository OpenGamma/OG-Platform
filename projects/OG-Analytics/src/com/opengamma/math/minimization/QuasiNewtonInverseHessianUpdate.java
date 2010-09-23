/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;


/**
 * 
 */
public interface QuasiNewtonInverseHessianUpdate {

  void update(QuasiNewtonVectorMinimizer.DataBundle data);

}
