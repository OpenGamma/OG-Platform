/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class QuasiNewtonVectorMinimizer implements MinimizerWithGradient<Function1D<DoubleMatrix1D, Double>, Function1D<DoubleMatrix1D, DoubleMatrix1D>, DoubleMatrix1D> {

  private static final int RESET_FREQ = 200;
  private static final double ALPHA = 1e-4;
  private static final double BETA = 1.5;
  private static final double EPS = 1e-8;
  private static final int DEF_MAX_STEPS = 200;
  private static final MatrixAlgebra MA = MatrixAlgebraFactory.getMatrixAlgebra("OG");
  private static final QuasiNewtonInverseHessianUpdate DEF_UPDATER = new BroydenFletcherGoldfarbShannoInverseHessianUpdate();

  private final double _absoluteTol, _relativeTol;
  private final int _maxSteps;
  private final QuasiNewtonInverseHessianUpdate _hessainUpdater;

  public QuasiNewtonVectorMinimizer() {
    this(EPS, EPS, DEF_MAX_STEPS);
  }

  public QuasiNewtonVectorMinimizer(final double absTolerance, final double relTolerance, final int maxInterations) {
    this(absTolerance, relTolerance, maxInterations, DEF_UPDATER);
  }

  public QuasiNewtonVectorMinimizer(final double absoluteTol, final double relativeTol, final int maxInterations, final QuasiNewtonInverseHessianUpdate hessianUpdater) {
    ArgumentChecker.notNull(hessianUpdater, "null updater");
    ArgumentChecker.notNegative(absoluteTol, "absolute tolerance");
    ArgumentChecker.notNegative(relativeTol, "relative tolerance");
    ArgumentChecker.notNegative(maxInterations, "maxSteps");
    _absoluteTol = absoluteTol;
    _relativeTol = relativeTol;
    _maxSteps = maxInterations;
    _hessainUpdater = hessianUpdater;
  }

  /**
   * Disabled because not working properly (see JIRA issue)
   * @param function The function
   * @param startPosition The start position
   * @return The minimum
   */
  @Override
  public DoubleMatrix1D minimize(final Function1D<DoubleMatrix1D, Double> function, final DoubleMatrix1D startPosition) {
    throw new NotImplementedException("Please supply gradient function or use ConjugateGradient");
  }

  @Override
  public DoubleMatrix1D minimize(final Function1D<DoubleMatrix1D, Double> function, final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad, final DoubleMatrix1D startPosition) {
    final DataBundle data = new DataBundle();
    final double y = function.evaluate(startPosition);
    data.setX(startPosition);
    data.setG0(y * y);
    data.setGrad(grad.evaluate(startPosition));
    data.setInverseHessianEsimate(getInitializedMatrix(startPosition));

    if (!getNextPosition(function, grad, data)) {
      throw new MathException("Cannot work with this starting position. Please choose another point");
    }

    int count = 0;
    int resetCount = 1;

    while (!isConverged(data)) {
      if ((resetCount) % RESET_FREQ == 0) {
        data.setInverseHessianEsimate(getInitializedMatrix(startPosition));
        resetCount = 1;
      } else {
        _hessainUpdater.update(data);
      }
      if (!getNextPosition(function, grad, data)) {
        data.setInverseHessianEsimate(getInitializedMatrix(startPosition));
        resetCount = 1;
        if (!getNextPosition(function, grad, data)) {
          throw new MathException("Failed to converge in backtracking");
        }
      }
      count++;
      resetCount++;
      if (count > _maxSteps) {
        throw new MathException("Failed to converge after " + _maxSteps + " iterations. Final point reached: " + data.getX().toString());
      }
    }
    return data.getX();
  }

  private DoubleMatrix2D getInitializedMatrix(final DoubleMatrix1D startPosition) {
    return DoubleMatrixUtils.getIdentityMatrix2D(startPosition.getNumberOfElements());
  }

  private DoubleMatrix1D getDirection(final DataBundle data) {
    return (DoubleMatrix1D) MA.multiply(data.getInverseHessianEsimate(), MA.scale(data.getGrad(), -1.0));
  }

  private boolean getNextPosition(final Function1D<DoubleMatrix1D, Double> function, final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad, final DataBundle data) {
    final DoubleMatrix1D p = getDirection(data);
    if (data.getLambda0() < 1.0) {
      data.setLambda0(1.0);
    } else {
      data.setLambda0(data.getLambda0() * BETA);
    }
    updatePosition(p, function, data);
    final double g1 = data.getG1();
    // the function is invalid at the new position, try to recover
    if (Double.isInfinite(g1) || Double.isNaN(g1)) {
      bisectBacktrack(p, function, data);
    }
    if (data.getG1() > data.getG0() / (1 + ALPHA * data.getLambda0())) {
      quadraticBacktrack(p, function, data);
      int count = 0;
      while (data.getG1() > data.getG0() / (1 + ALPHA * data.getLambda0())) {
        if (count > 5) {
          return false;
        }
        cubicBacktrack(p, function, data);
        count++;
      }
    }
    final DoubleMatrix1D deltaX = data.getDeltaX();
    data.setX((DoubleMatrix1D) MA.add(data.getX(), deltaX));
    data.setG0(data.getG1());
    final DoubleMatrix1D gradNew = grad.evaluate(data.getX());
    data.setDeltaGrad((DoubleMatrix1D) MA.subtract(gradNew, data.getGrad()));
    data.setGrad(gradNew);
    return true;
  }

  protected void updatePosition(final DoubleMatrix1D p, final Function1D<DoubleMatrix1D, Double> function, final DataBundle data) {
    final double lambda0 = data.getLambda0();
    final DoubleMatrix1D deltaX = (DoubleMatrix1D) MA.scale(p, lambda0);
    final DoubleMatrix1D xNew = (DoubleMatrix1D) MA.add(data.getX(), deltaX);
    data.setDeltaX(deltaX);
    data.setG2(data.getG1());
    final double y = function.evaluate(xNew);
    data.setG1(y * y);
  }

  private void bisectBacktrack(final DoubleMatrix1D p, final Function1D<DoubleMatrix1D, Double> function, final DataBundle data) {
    do {
      data.setLambda0(data.getLambda0() * 0.1);
      updatePosition(p, function, data);
    } while (Double.isNaN(data.getG1()) || Double.isInfinite(data.getG1()) || Double.isNaN(data.getG2()) || Double.isInfinite(data.getG2()));
  }

  private void quadraticBacktrack(final DoubleMatrix1D p, final Function1D<DoubleMatrix1D, Double> function, final DataBundle data) {
    final double lambda0 = data.getLambda0();
    final double g0 = data.getG0();
    final double lambda = Math.max(0.01 * lambda0, g0 * lambda0 * lambda0 / (data.getG1() + g0 * (2 * lambda0 - 1)));
    data.swapLambdaAndReplace(lambda);
    updatePosition(p, function, data);
  }

  private void cubicBacktrack(final DoubleMatrix1D p, final Function1D<DoubleMatrix1D, Double> function, final DataBundle data) {
    double temp1, temp2, temp3, temp4, temp5;
    final double lambda0 = data.getLambda0();
    final double lambda1 = data.getLambda1();
    final double g0 = data.getG0();
    temp1 = 1.0 / lambda0 / lambda0;
    temp2 = 1.0 / lambda1 / lambda1;
    temp3 = data.getG1() + g0 * (2 * lambda0 - 1.0);
    temp4 = data.getG2() + g0 * (2 * lambda1 - 1.0);
    temp5 = 1.0 / (lambda0 - lambda1);
    final double a = temp5 * (temp1 * temp3 - temp2 * temp4);
    final double b = temp5 * (-lambda1 * temp1 * temp3 + lambda0 * temp2 * temp4);
    double lambda = (-b + Math.sqrt(b * b + 6 * a * g0)) / 3 / a;
    lambda = Math.min(Math.max(lambda, 0.01 * lambda0), 0.75 * lambda1); // make sure new lambda is between 1% & 75% of old value
    data.swapLambdaAndReplace(lambda);
    updatePosition(p, function, data);
  }

  private boolean isConverged(final DataBundle data) {
    final DoubleMatrix1D deltaX = data.getDeltaX();
    final DoubleMatrix1D x = data.getX();
    final int n = deltaX.getNumberOfElements();
    double diff, scale;
    for (int i = 0; i < n; i++) {
      diff = Math.abs(deltaX.getEntry(i));
      scale = Math.abs(x.getEntry(i));
      if (diff > _absoluteTol + scale * _relativeTol) {
        return false;
      }
    }
    return (MA.getNorm2(data.getGrad()) < _absoluteTol);
  }

  /**
   * Data bundle for intermediate data
   */
  public static class DataBundle {
    private double _g0;
    private double _g1;
    private double _g2;
    private double _lambda0;
    private double _lambda1;
    private DoubleMatrix1D _deltaGrad;
    private DoubleMatrix1D _grad;
    private DoubleMatrix1D _deltaX;
    private DoubleMatrix1D _x;
    private DoubleMatrix2D _h;

    public double getG0() {
      return _g0;
    }

    public double getG1() {
      return _g1;
    }

    public double getG2() {
      return _g2;
    }

    public double getLambda0() {
      return _lambda0;
    }

    public double getLambda1() {
      return _lambda1;
    }

    public DoubleMatrix1D getDeltaGrad() {
      return _deltaGrad;
    }

    public DoubleMatrix1D getGrad() {
      return _grad;
    }

    public DoubleMatrix1D getDeltaX() {
      return _deltaX;
    }

    public DoubleMatrix1D getX() {
      return _x;
    }

    public void setG0(final double g0) {
      _g0 = g0;
    }

    public void setG1(final double g1) {
      _g1 = g1;
    }

    public void setG2(final double g2) {
      _g2 = g2;
    }

    public void setLambda0(final double lambda0) {
      _lambda0 = lambda0;
    }

    public void setDeltaGrad(final DoubleMatrix1D deltaGrad) {
      _deltaGrad = deltaGrad;
    }

    public void setGrad(final DoubleMatrix1D grad) {
      _grad = grad;
    }

    public void setDeltaX(final DoubleMatrix1D deltaX) {
      _deltaX = deltaX;
    }

    public void setX(final DoubleMatrix1D x) {
      _x = x;
    }

    /**
     * Inverse Hessian matrix 
     * @return The inverse Hessian Matrix
     */
    public DoubleMatrix2D getInverseHessianEsimate() {
      return _h;
    }

    public void setInverseHessianEsimate(final DoubleMatrix2D estimate) {
      _h = estimate;
    }

    public void swapLambdaAndReplace(final double lambda0) {
      _lambda1 = _lambda0;
      _lambda0 = lambda0;
    }
  }

}
