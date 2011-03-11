package com.opengamma.financial.model.finiteDifference;

public interface BoundaryCondition {
  
  double[] getLeftMatrixCondition(final PDEDataBundle data, final double t);
  
  double[] getRightMatrixCondition(final PDEDataBundle data, final double t);

  double getConstant(final PDEDataBundle data, final double t);
  
  double getLevel();
  
}
