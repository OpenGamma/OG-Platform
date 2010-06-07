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
import com.opengamma.math.matrix.Matrix;

/**
 *  Uses Broyden's Jacobian update formula
 */
public class BroydenVectorRootFinder extends NewtonRootFinderImpl {

  private static final double DEF_TOL = 1e-7;
  private static final int MAX_STEPS = 100;
  private DoubleMatrix2D _jacobianEst; //TODO change to Matrix<?>
  private Decomposition<?> _decomp;

  public BroydenVectorRootFinder() {
    this(DEF_TOL, DEF_TOL, MAX_STEPS);
  }

  public BroydenVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps) {
    super(absoluteTol, relativeTol, maxSteps);
    _decomp = new LUDecompositionCommons();
  }

  public BroydenVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, final Decomposition<?> decomp) {
    super(absoluteTol, relativeTol, maxSteps);
    Validate.notNull(decomp);
    _decomp = decomp;
  }

  public void setDecompositionMethod(final Decomposition<?> decompMethod) {
    Validate.notNull(decompMethod);
    _decomp = decompMethod;
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.rootfinding.VectorRootFinderImpl#getDirection()
   */
  @Override
  protected DoubleMatrix1D getDirection() {
    final DecompositionResult res = _decomp.evaluate(_jacobianEst);
    return res.solve(_y);
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.rootfinding.VectorRootFinderImpl#initializeMatrices()
   */
  @Override
  protected void initializeMatrices() {
    _jacobianEst = _jacobian.evaluate(_x);
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.rootfinding.VectorRootFinderImpl#updateMatrices()
   */
  @Override
  protected void updateMatrices() {
    final double length2 = OG_ALGEBRA.getInnerProduct(_deltax, _deltax);
    Matrix<?> temp = OG_ALGEBRA.subtract(_deltay, OG_ALGEBRA.multiply(_jacobianEst, _deltax));
    temp = OG_ALGEBRA.scale(temp, 1.0 / length2);
    _jacobianEst = (DoubleMatrix2D) OG_ALGEBRA.add(_jacobianEst, OG_ALGEBRA.getOuterProduct(temp, _deltax));
  }

}
