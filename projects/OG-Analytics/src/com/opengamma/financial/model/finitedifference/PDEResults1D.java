/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

/**
 * 
 */
public interface PDEResults1D {

  int getNumberSpaceNodes();

  double getSpaceValue(int spaceIndex);

  double getFunctionValue(int spaceIndex);

  // spatial derivative
  double getFirstSpatialDerivative(int spaceIndex);

  double getSecondSpatialDerivative(int spaceIndex);

}
