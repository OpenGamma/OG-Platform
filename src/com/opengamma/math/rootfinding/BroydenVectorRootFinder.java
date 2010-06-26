/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;

/**
 *  Uses Broyden's Jacobian update formula
 */
public class BroydenVectorRootFinder extends NewtonRootFinderImpl {

  private static final double DEF_TOL = 1e-7;
  private static final int MAX_STEPS = 100;

  // private DoubleMatrix2D _jacobianEstimate; //TODO change to Matrix<?>

  //private Decomposition<?> _decomp;

  public BroydenVectorRootFinder() {
    this(DEF_TOL, DEF_TOL, MAX_STEPS);
  }

  public BroydenVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps) {
    super(absoluteTol, relativeTol, maxSteps, new JacobianDirectionFunction(new LUDecompositionCommons()), new JacobianEstimateInitializationFunction(
        new FiniteDifferenceJacobianCalculator()), new BroydenMatrixUpdateFunction());
    // _decomp = new LUDecompositionCommons();
  }

  public BroydenVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, final Decomposition<?> decomp) {
    super(absoluteTol, relativeTol, maxSteps, new JacobianDirectionFunction(decomp), new JacobianEstimateInitializationFunction(
        new FiniteDifferenceJacobianCalculator()), new BroydenMatrixUpdateFunction());
    //Validate.notNull(decomp);
    //_decomp = decomp;
  }

  /* public void setDecompositionMethod(final Decomposition<?> decompMethod) {
     Validate.notNull(decompMethod);
     _decomp = decompMethod;
   }*/

  /*@Override
  protected DoubleMatrix1D getDirection(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    final DecompositionResult res = _decomp.evaluate(_jacobianEstimate);
    return res.solve(_y);
  }*/

  /*@Override
  protected DoubleMatrix2D getEstimate(final Function1D<DoubleMatrix1D, DoubleMatrix1D> f) {
    return _jacobianEstimate;
  }

  @Override
  protected void setEstimate(final DoubleMatrix2D estimate) {
    _jacobianEstimate = estimate;
  }*/

  /*  @SuppressWarnings("unchecked")
    @Override
    protected void initializeMatrices(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
      _jacobianEstimate = _jacobianCalculator.evaluate(_x, function);
    }*/

  /*@Override
  protected void updateMatrices(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    final double length2 = OG_ALGEBRA.getInnerProduct(_deltaX, _deltaX);
    Matrix<?> temp = OG_ALGEBRA.subtract(_deltaY, OG_ALGEBRA.multiply(_jacobianEstimate, _deltaX));
    temp = OG_ALGEBRA.scale(temp, 1.0 / length2);
    _jacobianEstimate = (DoubleMatrix2D) OG_ALGEBRA.add(_jacobianEstimate, OG_ALGEBRA.getOuterProduct(temp, _deltaX));
  }*/

}
