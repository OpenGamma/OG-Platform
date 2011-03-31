/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

/**
 * 
 */
public interface BoundaryCondition2D {

  double[] getLeftMatrixCondition(final PDEDataBundle data, final double t, final double boundaryPosition);

  double[] getRightMatrixCondition(final PDEDataBundle data, final double t, final double boundaryPosition);

  double getConstant(final PDEDataBundle data, final double t, final double boundaryPosition, final double gridSpacing);

  double getLevel();

}
