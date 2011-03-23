/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

/**
 * Neumann boundary condition, i.e. du/dx(A,t) = f(t), where A is the boundary level, and f(t) is some specified function of time
 */
public class NeumannBoundaryCondition implements BoundaryCondition {

  @Override
  public double getConstant(PDEDataBundle data, double t) {
    return 0;
  }

  @Override
  public double[] getLeftMatrixCondition(PDEDataBundle data, double t) {
    return null;
  }

  @Override
  public double getLevel() {
    return 0;
  }

  @Override
  public double[] getRightMatrixCondition(PDEDataBundle data, double t) {
    return null;
  }

}
