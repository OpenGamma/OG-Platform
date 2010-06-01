/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static com.opengamma.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * Base implementation for for all Newton-Raphson style multi-dimensional root finding (i.e. using the Jacobian matrix as a basis for some iterative process)
 */
abstract class NewtonRootFinderImpl extends VectorRootFinder {

  protected static final double ALPHA = 1e-4;
  protected static final double BETA = 1.5;
  protected static final int FULL_JACOBIAN_RECAL_FREQ = 20;
  protected final double _absoluteTol, _relativeTol;
  protected final int _maxSteps;
  protected Function1D<DoubleMatrix1D, DoubleMatrix1D> _function;
  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> _jacobian;
  protected DoubleMatrix1D _y, _x, _deltax, _deltay;
  protected double _lambda1, _lambda2;
  protected double _g0, _g1, _g2;

  public NewtonRootFinderImpl(final double absoluteTol, final double relativeTol, final int maxSteps) {

    _absoluteTol = absoluteTol;
    _relativeTol = relativeTol;
    _maxSteps = maxSteps;
  }

  /**
   * Use this if you do NOT have access to an analytic Jacobian
   * @param function a vector function (i.e. vector to vector) 
   * @param startPosition where to start the root finder for. Note if multiple roots exist which one if found (if at all) will depend on startPosition 
   * @return the vector root of the collection of functions 
   */
  public DoubleMatrix1D getRoot(Function1D<DoubleMatrix1D, DoubleMatrix1D> function, DoubleMatrix1D startPosition) {
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobian = new JacobianCalculator(function);
    return getRoot(function, jacobian, startPosition);
  }

  /**
   * Use this if you DO have access to an analytic Jacobian
   *@param function a vector function (i.e. vector to vector) 
  * @param startPosition where to start the root finder for. Note if multiple roots exist which one if found (if at all) will depend on startPosition 
  * @param jacobian A function that returns the Jacobian at a given position, i.e  J<sub>i,j,</sub> = dF<sub>i</sub>/dx<sub>j</sub>
  * @return the vector root of the collection of functions 
   */
  public DoubleMatrix1D getRoot(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function,
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobian, DoubleMatrix1D startPosition) {

    if (function == null || jacobian == null) {
      throw new IllegalArgumentException("function or jacobian is null");
    }
    _function = function;
    _jacobian = jacobian;

    _x = startPosition;
    _y = _function.evaluate(_x);
    _g0 = OG_ALGEBRA.getInnerProduct(_y, _y);

    initializeMatrices();

    if (!getNextPosition()) {
      throw new RootNotFoundException("Cannot work with this starting position. Please choose another point");
    }

    int count = 0;
    int jacReconCount = 1;
    while (!converged()) {
      //Want to reset the Jacobian every so often even if backtracking is working 
      if ((jacReconCount) % FULL_JACOBIAN_RECAL_FREQ == 0) {
        initializeMatrices();
        jacReconCount = 1;
      } else {
        updateMatrices();
        jacReconCount++;
      }
      //if backtracking fails, could be that Jacobian estimate has drifted too far  
      if (!getNextPosition()) {
        initializeMatrices();
        jacReconCount = 1;
        if (!getNextPosition()) {
          throw new RootNotFoundException("Failed to converge in backtracking, even after a Jacobian recalculation");
        }
      }
      count++;
      if (count > _maxSteps) {
        throw new RootNotFoundException("Failed to converge");
      }
    }

    return _x;
  }

  private Boolean getNextPosition() {

    DoubleMatrix1D p = getDirection();

    if (_lambda1 < 1.0) {
      _lambda1 = 1.0;
    } else {
      _lambda1 *= BETA;
    }
    updatePosition(p);

    if (_g1 > _g0 * (1 - ALPHA * _lambda1)) {

      if (Double.isInfinite(_g1)) {
        bisectBacktrack(p);
      }

      quadraticBacktrack(p);

      int count = 0;
      while (_g1 > _g0 * (1 - ALPHA * _lambda1)) {
        if (count > 5) {
          return false;
        }
        cubicBacktrack(p);
        count++;
      }
    }

    _g0 = _g1;
    _x = (DoubleMatrix1D) OG_ALGEBRA.add(_x, _deltax);
    _y = (DoubleMatrix1D) OG_ALGEBRA.add(_y, _deltay);
    return true;
  }

  protected abstract DoubleMatrix1D getDirection();

  protected abstract void initializeMatrices();

  protected abstract void updateMatrices();

  protected void updatePosition(final DoubleMatrix1D p) {
    _deltax = (DoubleMatrix1D) OG_ALGEBRA.scale(p, -_lambda1);
    DoubleMatrix1D xNew = (DoubleMatrix1D) OG_ALGEBRA.add(_x, _deltax);
    DoubleMatrix1D yNew = _function.evaluate(xNew);
    _deltay = (DoubleMatrix1D) OG_ALGEBRA.subtract(yNew, _y);

    _g2 = _g1;
    _g1 = OG_ALGEBRA.getInnerProduct(yNew, yNew);
  }

  private void bisectBacktrack(final DoubleMatrix1D p) {
    do {
      _lambda1 *= 0.1;
      updatePosition(p);
    } while (Double.isInfinite(_g1) || Double.isInfinite(_g2));
  }

  private void quadraticBacktrack(final DoubleMatrix1D p) {
    double lambda = _g0 * _lambda1 * _lambda1 / (_g1 + _g0 * (2 * _lambda1 - 1));
    _lambda2 = _lambda1;
    _lambda1 = Math.max(_lambda1 * 0.01, lambda); //don't make the linear guess too small
    updatePosition(p);
  }

  private void cubicBacktrack(final DoubleMatrix1D p) {

    double temp1, temp2, temp3, temp4, temp5;

    temp1 = 1.0 / _lambda1 / _lambda1;
    temp2 = 1.0 / _lambda2 / _lambda2;
    temp3 = _g1 + _g0 * (2 * _lambda1 - 1.0);
    temp4 = _g2 + _g0 * (2 * _lambda2 - 1.0);
    temp5 = 1.0 / (_lambda1 - _lambda2);
    double a = temp5 * (temp1 * temp3 - temp2 * temp4);
    double b = temp5 * (-_lambda2 * temp1 * temp3 + _lambda1 * temp2 * temp4);

    double lambda = (-b + Math.sqrt(b * b + 6 * a * _g0)) / 3 / a;
    lambda = Math.min(Math.max(lambda, 0.01 * _lambda1), 0.75 * _lambda2); //make sure new lambda is between 1% & 75% of old value 

    _lambda2 = _lambda1;
    _lambda1 = lambda;
    updatePosition(p);
  }

  private boolean converged() {

    final int n = _deltax.getNumberOfElements();

    double diff, scale;
    for (int i = 0; i < n; i++) {
      diff = Math.abs(_deltax.getEntry(i));
      scale = Math.abs(_x.getEntry(i));
      if (diff > _absoluteTol + scale * _relativeTol) {
        return false;
      }
    }
    return (Math.sqrt(_g0) < _absoluteTol);
  }

}
