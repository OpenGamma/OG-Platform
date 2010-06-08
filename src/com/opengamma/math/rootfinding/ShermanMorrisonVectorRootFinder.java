/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static com.opengamma.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import org.apache.commons.lang.Validate;

import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.DoubleMatrixUtils;

/**
 * Uses the Sherman-Morrison formula to invert Broyden's Jacobian update formula, thus providing a direct update formula for the inverse Jacobian 
 */
public class ShermanMorrisonVectorRootFinder extends NewtonRootFinderImpl {

  private static final double DEF_TOL = 1e-7;
  private static final int MAX_STEPS = 100;
  private DoubleMatrix2D _inverseJacobianEstimate;
  private final Decomposition<?> _decomp;

  public ShermanMorrisonVectorRootFinder() {
    this(DEF_TOL, DEF_TOL, MAX_STEPS);
  }

  /**
   * @param absoluteTol Absolute tolerance
   * @param relativeTol Relative tolerance
   * @param maxSteps Maximum number of steps to be used
   */
  public ShermanMorrisonVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps) {
    super(absoluteTol, relativeTol, maxSteps);
    _decomp = new LUDecompositionCommons();
  }

  public ShermanMorrisonVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, final Decomposition<?> decomp) {
    super(absoluteTol, relativeTol, maxSteps);
    Validate.notNull(decomp);
    _decomp = decomp;
  }

  @Override
  protected DoubleMatrix1D getDirection() {
    return (DoubleMatrix1D) OG_ALGEBRA.multiply(_inverseJacobianEstimate, _y);
  }

  @Override
  protected void initializeMatrices() {
    final DoubleMatrix2D jacobianEst = _jacobian.evaluate(_x);
    final DecompositionResult deconResult = _decomp.evaluate(jacobianEst);
    _inverseJacobianEstimate = deconResult.solve(DoubleMatrixUtils.getIdentityMatrix2D(_x.getNumberOfElements()));
  }

  @Override
  protected void updateMatrices() {
    DoubleMatrix1D vtemp1 = (DoubleMatrix1D) OG_ALGEBRA.multiply(_deltaX, _inverseJacobianEstimate);
    final double length2 = OG_ALGEBRA.getInnerProduct(vtemp1, _deltaY);
    vtemp1 = (DoubleMatrix1D) OG_ALGEBRA.scale(vtemp1, 1.0 / length2);
    final DoubleMatrix1D vtemp2 = (DoubleMatrix1D) OG_ALGEBRA.subtract(_deltaX, OG_ALGEBRA.multiply(_inverseJacobianEstimate, _deltaY));
    final DoubleMatrix2D mtemp = OG_ALGEBRA.getOuterProduct(vtemp2, vtemp1);
    _inverseJacobianEstimate = (DoubleMatrix2D) OG_ALGEBRA.add(_inverseJacobianEstimate, mtemp);
  }

}
