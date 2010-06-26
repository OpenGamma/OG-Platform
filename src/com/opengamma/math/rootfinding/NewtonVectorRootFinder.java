/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;

/**
 * Attempts to find the multi-dimensional root of a series of N equations with N variables, i.e. a square problem. 
 * If the analytic Jacobian is not known, it will be calculated using central difference 
 */
public class NewtonVectorRootFinder extends NewtonRootFinderImpl {
  private static final double DEF_TOL = 1e-7;
  private static final int MAX_STEPS = 100;

  // private DoubleMatrix2D _jacobianEstimate;

  //private Decomposition<?> _decomp;

  public NewtonVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, final Decomposition<?> decomp) {
    super(absoluteTol, relativeTol, maxSteps, new JacobianDirectionFunction(decomp), new JacobianEstimateInitializationFunction(
        new FiniteDifferenceJacobianCalculator()), new DefaultUpdateFunction());
    //Validate.notNull(decomp);
    //_decomp = decomp;
  }

  public NewtonVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps) {
    super(absoluteTol, relativeTol, maxSteps, new JacobianDirectionFunction(new LUDecompositionCommons()), new JacobianEstimateInitializationFunction(
        new FiniteDifferenceJacobianCalculator()), new DefaultUpdateFunction());
    //_decomp = new LUDecompositionCommons();
  }

  public NewtonVectorRootFinder() {
    this(DEF_TOL, DEF_TOL, MAX_STEPS);
  }

  /**
   * By default this uses {@link LUDecompositionCommons} to solve for the Newton step. This can be a problem if the Jacobian
   * is singular near the solution, in which case SVD is preferred, i.e {@link SVDecompositionCommons}
   * @param decompMethod The method used to solve for the Newton step
   * @throws IllegalArgumentException If the Decomposition is null 
   */
  //public void setDecompositionMethod(final Decomposition<?> decompMethod) {
  //  Validate.notNull(decompMethod);
  //  _decomp = decompMethod;
  //}

  /*@SuppressWarnings("unchecked")
  @Override
  protected DoubleMatrix1D getDirection(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    final DoubleMatrix2D jacobianEst = _jacobianCalculator.evaluate(_x, function);
    final DecompositionResult res = _decomp.evaluate(jacobianEst);
    return res.solve(_y);
  }*/

  /*@Override
  protected DoubleMatrix2D getEstimate(final Function1D<DoubleMatrix1D, DoubleMatrix1D> f) {
    if (_jacobianEstimate == null) {
      _jacobianEstimate = _jacobianCalculator.evaluate(_x, f);
    }
    return _jacobianEstimate;
  }

  @Override
  protected void setEstimate(final DoubleMatrix2D estimate) {
    _jacobianEstimate = estimate;
  }

  /*@Override
  protected void initializeMatrices(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    // no need to do anything
  }*/

  /*@Override
  protected void updateMatrices(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    // no need to do anything
  }*/

}
