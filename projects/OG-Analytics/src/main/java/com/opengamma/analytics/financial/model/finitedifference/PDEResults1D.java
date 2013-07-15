/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

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

  PDEGrid1D getGrid();

  double[] getTerminalResults();

}
