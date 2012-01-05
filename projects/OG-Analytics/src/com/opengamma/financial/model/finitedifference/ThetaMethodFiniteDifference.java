/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.Surface;

/**
 * A theta (i.e. weighted between explicit and implicit time stepping) scheme using SOR algorithm to solve the matrix system at each time step
 * This uses the exponentially fitted scheme of duffy
 */
public class ThetaMethodFiniteDifference implements ConvectionDiffusionPDESolver {
  private static final Decomposition<?> DCOMP = new LUDecompositionCommons();
  private final double _theta;
  private final boolean _showFullResults;

  /**
   * Sets up a standard Crank-Nicolson scheme
   */
  public ThetaMethodFiniteDifference() {
    _theta = 0.5;
    _showFullResults = false;
  }

  /**
   * Sets up a scheme that is the weighted average of an explicit and an implicit scheme
   * @param theta The weight. theta = 0 - fully explicit, theta = 0.5 - Crank-Nicolson, theta = 1.0 - fully implicit
   * @param showFullResults Show the full results
   */
  public ThetaMethodFiniteDifference(final double theta, final boolean showFullResults) {
    Validate.isTrue(theta >= 0 && theta <= 1.0, "theta must be in the range 0 to 1");
    _theta = theta;
    _showFullResults = showFullResults;
  }

  public double getTheta() {
    return _theta;
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary) {
    return solve(pdeData, tSteps, xSteps, tMax, lowerBoundary, upperBoundary, null);
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary, final Surface<Double, Double, Double> freeBoundary) {
    final PDEGrid1D grid = new PDEGrid1D(tSteps + 1, xSteps + 1, tMax, lowerBoundary.getLevel(), upperBoundary.getLevel());
    return solve(pdeData, grid, lowerBoundary, upperBoundary, freeBoundary);
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary) {
    return solve(pdeData, grid, lowerBoundary, upperBoundary, null);
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary,
      final Surface<Double, Double, Double> freeBoundary) {
    Validate.notNull(pdeData, "pde data");
    Validate.notNull(grid, "need a grid");
    validateSetup(grid, lowerBoundary, upperBoundary);

    final SolverImpl solver = new SolverImpl(pdeData, grid, lowerBoundary, upperBoundary, freeBoundary);
    return solver.solve();
  }

  class SolverImpl {
    private final ConvectionDiffusionPDEDataBundle _pdeData;
    private final PDEGrid1D _grid;
    private final BoundaryCondition _lowerBoundary;
    private final BoundaryCondition _upperBoundary;
    private final Surface<Double, Double, Double> _freeBoundary;
    private final double[] _f;
    private double[][] _full;

    private final double[] _q;
    private final double[][] _m;

    private final double[] _rho;
    private final double[] _a;
    private final double[] _b;
    private final double[] _c;

    private double _t1;
    private double _t2;

    @SuppressWarnings("synthetic-access")
    public SolverImpl(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary,
        final Surface<Double, Double, Double> freeBoundary) {

      _pdeData = pdeData;
      _grid = grid;
      _lowerBoundary = lowerBoundary;
      _upperBoundary = upperBoundary;
      _freeBoundary = freeBoundary;

      final int tNodes = grid.getNumTimeNodes();
      final int xNodes = grid.getNumSpaceNodes();

      _f = new double[xNodes];
      if (_showFullResults) {
        _full = new double[tNodes][xNodes];
      }

      _q = new double[xNodes];
      _m = new double[xNodes][xNodes];
      _rho = new double[xNodes - 2];
      _a = new double[xNodes - 2];
      _b = new double[xNodes - 2];
      _c = new double[xNodes - 2];

    }

    @SuppressWarnings("synthetic-access")
    public PDEResults1D solve() {

      initialise();

      for (int n = 1; n < getGrid().getNumTimeNodes(); n++) {
        //debug
        if (_t1 == 4.4) {
          System.out.print("arrggg " + _t1 + " " + _t2);
        }

        setT2(getGrid().getTimeNode(n));
        updateRHSVector();
        updateRHSBoundary();
        updateCoefficents(n);
        updateLHSMatrix();
        updateLHSBoundary();
        solveMatrixSystem();
        if (_showFullResults) {
          _full[n] = Arrays.copyOf(_f, _f.length);
        }
        setT1(getT2());
      }

      PDEResults1D res;
      if (_showFullResults) {
        res = new PDEFullResults1D(getGrid(), _full);
      } else {
        res = new PDETerminalResults1D(getGrid(), _f);
      }
      return res;

    }

    @SuppressWarnings("synthetic-access")
    void initialise() {
      for (int i = 0; i < getGrid().getNumSpaceNodes(); i++) {
        _f[i] = _pdeData.getInitialValue(getGrid().getSpaceNode(i));
      }
      if (_showFullResults) {
        _full[0] = Arrays.copyOf(_f, _f.length);
      }

      double x;
      for (int i = 0; i < getGrid().getNumSpaceNodes() - 2; i++) {
        x = getGrid().getSpaceNode(i + 1);
        setA(i, _pdeData.getA(0, x));
        setB(i, _pdeData.getB(0, x));
        setC(i, _pdeData.getC(0, x));
        //TODO R White 4/08/2011 change this back debug
        //setRho(i, getA(i));
        setRho(i, getFittingParameter(getGrid(), getA(i), getB(i), i + 1));
      }
      setT1(0.0);
    }

    @SuppressWarnings("synthetic-access")
    void updateRHSVector() {
      final double dt = getT2() - getT1();
      double[] x1st, x2nd;
      double temp;
      for (int i = 1; i < getGrid().getNumSpaceNodes() - 1; i++) {
        x1st = getGrid().getFirstDerivativeCoefficients(i);
        x2nd = getGrid().getSecondDerivativeCoefficients(i);

        temp = getF(i);
        temp -= (1 - _theta) * dt * (x2nd[0] * getRho(i - 1) + x1st[0] * getB(i - 1)) * _f[i - 1];
        temp -= (1 - _theta) * dt * (x2nd[1] * getRho(i - 1) + x1st[1] * getB(i - 1) + getC(i - 1)) * _f[i];
        temp -= (1 - _theta) * dt * (x2nd[2] * getRho(i - 1) + x1st[2] * getB(i - 1)) * _f[i + 1];
        setQ(i, temp);
      }
    }

    /**
     * 
     */
    void updateRHSBoundary() {
      double[] temp = _lowerBoundary.getRightMatrixCondition(_pdeData, getGrid(), getT1());
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * getF(k);
      }
      setQ(0, sum + _lowerBoundary.getConstant(_pdeData, getT2()));

      temp = _upperBoundary.getRightMatrixCondition(_pdeData, getGrid(), getT1());
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * getF(getGrid().getNumSpaceNodes() - 1 - k);
      }

      setQ(getGrid().getNumSpaceNodes() - 1, sum + _upperBoundary.getConstant(_pdeData, getT2()));
    }

    @SuppressWarnings("synthetic-access")
    void updateLHSMatrix() {
      final double dt = getT2() - getT1();
      double[] x1st, x2nd;
      for (int i = 1; i < getGrid().getNumSpaceNodes() - 1; i++) {
        x1st = getGrid().getFirstDerivativeCoefficients(i);
        x2nd = getGrid().getSecondDerivativeCoefficients(i);

        setM(i, i - 1, _theta * dt * (x2nd[0] * getRho(i - 1) + x1st[0] * getB(i - 1)));
        setM(i, i, 1 + _theta * dt * (x2nd[1] * getRho(i - 1) + x1st[1] * getB(i - 1) + getC(i - 1)));
        setM(i, i + 1, _theta * dt * (x2nd[2] * getRho(i - 1) + x1st[2] * getB(i - 1)));
      }
    }

    void updateLHSBoundary() {
      double[] temp = _lowerBoundary.getLeftMatrixCondition(_pdeData, getGrid(), getT2());
      for (int k = 0; k < temp.length; k++) {
        setM(0, k, temp[k]);
      }

      temp = _upperBoundary.getLeftMatrixCondition(_pdeData, getGrid(), getT2());
      for (int k = 0; k < temp.length; k++) {
        setM(getGrid().getNumSpaceNodes() - 1, getGrid().getNumSpaceNodes() - temp.length + k, temp[k]);
      }
    }

    void updateCoefficents(final int n) {
      double x;
      for (int i = 1; i < getGrid().getNumSpaceNodes() - 1; i++) {
        x = getGrid().getSpaceNode(i);
        setA(i - 1, _pdeData.getA(getT2(), x));
        setB(i - 1, _pdeData.getB(getT2(), x));
        setC(i - 1, _pdeData.getC(getT2(), x));
        //TODO R White 4/08/2011 change this back debug
        //setRho(i - 1, getA(i - 1));
        setRho(i - 1, getFittingParameter(getGrid(), getA(i - 1), getB(i - 1), i));
      }
    }

    private void solveMatrixSystem() {
      final double omega = 1.0;
      @SuppressWarnings("unused")
      //NOTE get this working again with dynamic omega
      final int count = solveBySOR(omega);
      //   solveByLU();
      //      if (oldCount > 0) {
      //        if ((omegaIncrease && count > oldCount) || (!omegaIncrease && count < oldCount)) {
      //          omega = Math.max(1.0, omega * 0.9);
      //          omegaIncrease = false;
      //        } else {
      //          omega = 1.1 * omega;
      //          omegaIncrease = true;
      //        }
      //      }
      //      oldCount = count;

    }

    @SuppressWarnings({"unused", "synthetic-access" })
    private void solveByLU() {
      DoubleMatrix2D temp = new DoubleMatrix2D(_m);
      DecompositionResult res = DCOMP.evaluate(temp);
      double[] f = res.solve(_q);
      for (int i = 0; i < f.length; i++) {
        _f[i] = f[i];
      }
    }

    private int solveBySOR(final double omega) {

      double sum;
      int count = 0;
      double scale = 1.0;
      double errorSqr = Double.POSITIVE_INFINITY;
      while (errorSqr / (scale + 1e-10) > 1e-18) {
        errorSqr = 0.0;
        scale = 0.0;
        for (int j = 0; j < getGrid().getNumSpaceNodes(); j++) {
          sum = 0;
          for (int k = 0; k < getGrid().getNumSpaceNodes(); k++) {
            sum += getM(j, k) * getF(k);
          }
          double correction = omega / getM(j, j) * (getQ(j) - sum);
          if (_freeBoundary != null) {
            correction = Math.max(correction, _freeBoundary.getZValue(getT2(), getGrid().getSpaceNode(j)) - getF(j));
          }
          errorSqr += correction * correction;
          setF(j, getF(j) + correction); //TODO don't like this
          scale += getF(j) * getF(j);
        }
        count++;
      }
      return count;
    }

    /**
     * @param grid
     * @param a
     * @param b
     * @param i
     * @return
     */
    private double getFittingParameter(final PDEGrid1D grid, final double a, final double b, final int i) {
      double rho;
      if (a == 0 && b == 0) {
        return 0.0;
      }

      final double[] x1st = grid.getFirstDerivativeCoefficients(i);
      final double[] x2nd = grid.getSecondDerivativeCoefficients(i);
      final double bdx1 = (b * grid.getSpaceStep(i - 1));
      final double bdx2 = (b * grid.getSpaceStep(i));

      // convection dominated
      if (Math.abs(bdx1) > 10 * Math.abs(a) || Math.abs(bdx2) > 10 * Math.abs(a)) {
        // a > 0 is unphysical as it corresponds to a negative diffusion
        double sign = (a > 0.0 ? -1.0 : 1.0);
        if (b > 0) {
          rho = sign * b * x1st[0] / x2nd[0];
        } else {
          rho = -sign * b * x1st[2] / x2nd[2];
        }
      } else if (Math.abs(a) > 10 * Math.abs(bdx1) || Math.abs(a) > 10 * Math.abs(bdx2)) {
        rho = a; // diffusion dominated
      } else {
        final double expo1 = Math.exp(bdx1 / a);
        final double expo2 = Math.exp(-bdx2 / a);
        rho = -b * (x1st[0] * expo1 + x1st[1] + x1st[2] * expo2) / (x2nd[0] * expo1 + x2nd[1] + x2nd[2] * expo2);
      }
      return rho;
    }

    /**
     * Gets the grid.
     * @return the grid
     */
    public PDEGrid1D getGrid() {
      return _grid;
    }

    public void setT1(final double t1) {
      _t1 = t1;
    }

    public double getT1() {
      return _t1;
    }

    public void setT2(final double t2) {
      _t2 = t2;
    }

    public double getT2() {
      return _t2;
    }

    public double getQ(final int i) {
      return _q[i];
    }

    public void setQ(final int i, final double value) {
      _q[i] = value;
    }

    public double getM(final int i, final int j) {
      return _m[i][j];
    }

    public void setM(final int i, final int j, final double value) {
      _m[i][j] = value;
    }

    public double getF(final int i) {
      return _f[i];
    }

    public void setF(final int i, final double value) {
      _f[i] = value;
    }

    public double getRho(final int i) {
      return _rho[i];
    }

    public void setRho(final int i, final double value) {
      _rho[i] = value;
    }

    public double getA(final int i) {
      return _a[i];
    }

    public void setA(final int i, final double value) {
      _a[i] = value;
    }

    public double getB(final int i) {
      return _b[i];
    }

    public void setB(final int i, final double value) {
      _b[i] = value;
    }

    public double getC(final int i) {
      return _c[i];
    }

    public void setC(final int i, final double value) {
      _c[i] = value;
    }

  }

  /**
   * Checks that the lower and upper boundaries match up with the grid
   * @param grid The grid
   * @param lowerBoundary The lower boundary
   * @param upperBoundary The upper boundary
   */
  protected void validateSetup(final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary) {
    // TODO would like more sophistication that simply checking to the grid is consistent with the boundary level
    Validate.isTrue(Math.abs(grid.getSpaceNode(0) - lowerBoundary.getLevel()) < 1e-7, "space grid not consistent with boundary level");
    Validate.isTrue(Math.abs(grid.getSpaceNode(grid.getNumSpaceNodes() - 1) - upperBoundary.getLevel()) < 1e-7, "space grid not consistent with boundary level");
  }

  /**
   * @param omega omega
   * @param grid the grid
   * @param freeBoundary the free boundary
   * @param xNodes x nodes
   * @param f f
   * @param q q
   * @param m m
   * @param t2 t2
   * @return an int
   */
  protected int sor(final double omega, final PDEGrid1D grid, final Surface<Double, Double, Double> freeBoundary, final int xNodes, final double[] f, final double[] q, final double[][] m,
      final double t2) {
    double sum;
    int count = 0;
    double scale = 1.0;
    double errorSqr = Double.POSITIVE_INFINITY;
    while (errorSqr / (scale + 1e-10) > 1e-18) {
      errorSqr = 0.0;
      scale = 0.0;
      for (int j = 0; j < xNodes; j++) {
        sum = 0;
        for (int k = 0; k < xNodes; k++) {
          sum += m[j][k] * f[k];
        }
        double correction = omega / m[j][j] * (q[j] - sum);
        if (freeBoundary != null) {
          correction = Math.max(correction, freeBoundary.getZValue(t2, grid.getSpaceNode(j)) - f[j]);
        }
        errorSqr += correction * correction;
        f[j] += correction;
        scale += f[j] * f[j];
      }
      count++;
    }
    //debug
    //  System.out.println(count + " " + omega);
    return count;
  }

}
