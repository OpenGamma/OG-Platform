/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static com.opengamma.analytics.math.linearalgebra.TridiagonalSolver.solvTriDag;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.TridiagonalMatrix;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * A theta (i.e. weighted between explicit and implicit time stepping) scheme using SOR algorithm to solve the matrix system at each time step
 * This uses the exponentially fitted scheme of duffy
 */
public class ThetaMethodFiniteDifference implements ConvectionDiffusionPDESolver {
  private static final Decomposition<?> DCOMP = new LUDecompositionCommons();
  // private static final DEFAULT
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
    ArgumentChecker.isTrue(theta >= 0 && theta <= 1.0, "theta must be in the range 0 to 1");
    _theta = theta;
    _showFullResults = showFullResults;
  }

  public double getTheta() {
    return _theta;
  }

  @Override
  //TODO This is so ugly
  public PDEResults1D solve(final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> pdeData) {
    ArgumentChecker.notNull(pdeData, "pde data");
    final ConvectionDiffusionPDE1DCoefficients coeff = pdeData.getCoefficients();
    if (coeff instanceof ConvectionDiffusionPDE1DStandardCoefficients) {
      final PDE1DDataBundle<ConvectionDiffusionPDE1DStandardCoefficients> temp = convertPDE1DDataBundle(pdeData);
      final SolverImpl solver = new SolverImpl(temp);
      return solver.solve();
    } else if (coeff instanceof ConvectionDiffusionPDE1DFullCoefficients) {
      final ConvectionDiffusionPDE1DFullCoefficients temp = (ConvectionDiffusionPDE1DFullCoefficients) coeff;
      final ExtendedSolverImpl solver = new ExtendedSolverImpl(temp, pdeData.getInitialCondition(), pdeData.getLowerBoundary(), pdeData.getUpperBoundary(),
          pdeData.getFreeBoundary(), pdeData.getGrid());
      return solver.solve();
    }
    throw new IllegalArgumentException(coeff.getClass() + " not handled");
  }

  private static PDE1DDataBundle<ConvectionDiffusionPDE1DStandardCoefficients> convertPDE1DDataBundle(final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> pdeData) {
    if (pdeData.getFreeBoundary() == null) {
      return new PDE1DDataBundle<>(
          (ConvectionDiffusionPDE1DStandardCoefficients) pdeData.getCoefficients(), pdeData.getInitialCondition(), pdeData.getLowerBoundary(),
          pdeData.getUpperBoundary(), pdeData.getGrid());
    }
    return new PDE1DDataBundle<>(
        (ConvectionDiffusionPDE1DStandardCoefficients) pdeData.getCoefficients(), pdeData.getInitialCondition(), pdeData.getLowerBoundary(),
        pdeData.getUpperBoundary(), pdeData.getFreeBoundary(), pdeData.getGrid());
  }

  private enum SolverMode {
    tridiagonal,
    luDecomp,
    psor;
  }

  class SolverImpl {

    // grid
    private final int _nNodesX;
    private final int _nNodesT;
    private final double[] _dt;
    private final PDEGrid1D _grid;
    private final double[][] _x1st;
    private final double[][] _x2nd;
    private final double[] _dx;
    //initial and boundary conditions
    private final double[] _initial;
    private final BoundaryCondition _lower;
    private final BoundaryCondition _upper;
    //PDE coefficients
    private final ConvectionDiffusionPDE1DStandardCoefficients _coeff;
    //free boundary problems
    private final SolverMode _mode;
    private final Surface<Double, Double, Double> _freeB;

    public SolverImpl(final PDE1DDataBundle<ConvectionDiffusionPDE1DStandardCoefficients> pdeData) {

      //unpack pdeData
      _grid = pdeData.getGrid();
      _coeff = pdeData.getCoefficients();
      _lower = pdeData.getLowerBoundary();
      _upper = pdeData.getUpperBoundary();

      _nNodesX = _grid.getNumSpaceNodes();
      _nNodesT = _grid.getNumTimeNodes();

      _x1st = new double[_nNodesX - 2][];
      _x2nd = new double[_nNodesX - 2][];
      for (int ii = 0; ii < _nNodesX - 2; ii++) {
        _x1st[ii] = _grid.getFirstDerivativeCoefficients(ii + 1);
        _x2nd[ii] = _grid.getSecondDerivativeCoefficients(ii + 1);
      }
      _dx = new double[_nNodesX - 1];
      for (int ii = 0; ii < _nNodesX - 1; ii++) {
        _dx[ii] = _grid.getSpaceStep(ii);
      }

      _initial = pdeData.getInitialCondition();
      _dt = new double[_nNodesT - 1];
      for (int jj = 0; jj < _nNodesT - 1; jj++) {
        _dt[jj] = _grid.getTimeStep(jj);
      }

      //free boundary
      _freeB = pdeData.getFreeBoundary();
      if (_freeB == null) {
        _mode = SolverMode.tridiagonal;
      } else {
        _mode = SolverMode.psor;
      }
    }

    @SuppressWarnings({"synthetic-access" })
    public PDEResults1D solve() {

      double[][] full = null;
      if (_showFullResults) {
        full = new double[_nNodesT][_nNodesX];
        full[0] = _initial;
      }
      double[] h = _initial;

      double t = _grid.getTimeNode(0);

      double[] topRow = _lower.getLeftMatrixCondition(_coeff, _grid, t);
      double[] bottomRow = _upper.getLeftMatrixCondition(_coeff, _grid, t);
      final double[] cDag = new double[_nNodesX - 2];
      final double[] lDag = new double[_nNodesX - 2];
      final double[] uDag = new double[_nNodesX - 2];
      for (int ii = 0; ii < _nNodesX - 2; ii++) { //tri-diagonal form
        final double x = _grid.getSpaceNode(ii + 1);
        final double a = _coeff.getA(t, x);
        final double b = _coeff.getB(t, x);
        final double c = _coeff.getC(t, x);
        cDag[ii] = _x2nd[ii][1] * a + _x1st[ii][1] * b + c;
        lDag[ii] = _x2nd[ii][0] * a + _x1st[ii][0] * b;
        uDag[ii] = _x2nd[ii][2] * a + _x1st[ii][2] * b;
      }

      for (int jj = 0; jj < _nNodesT - 1; jj++) {
        final double dt = _dt[jj];

        //RHS of system
        final double[] y = new double[_nNodesX];
        //main part of RHS
        for (int ii = 1; ii < _nNodesX - 1; ii++) { //tri-diagonal form
          y[ii] = (1 - (1 - _theta) * dt * cDag[ii - 1]) * h[ii] - (1 - _theta) * dt * (lDag[ii - 1] * h[ii - 1] + +uDag[ii - 1] * h[ii + 1]);
        }

        t = _grid.getTimeNode(jj + 1);
        //lower & upper boundaries
        y[0] = _lower.getConstant(_coeff, t);
        y[_nNodesX - 1] = _upper.getConstant(_coeff, t);

        //put the LHS of system in tri-diagonal form
        final double[] d = new double[_nNodesX]; //main diag
        final double[] u = new double[_nNodesX - 1]; //upper
        final double[] l = new double[_nNodesX - 1]; //lower
        //lower boundary conditions
        topRow = _lower.getLeftMatrixCondition(_coeff, _grid, t);
        final int p2 = topRow.length;
        d[0] = topRow[0];
        if (p2 > 1) {
          u[0] = topRow[1];
          //Review do we need this?
          ArgumentChecker.isFalse(p2 > 2, "Boundary condition means that system is not tri-diagonal");
        }
        bottomRow = _upper.getLeftMatrixCondition(_coeff, _grid, t);
        final int q2 = bottomRow.length;
        d[_nNodesX - 1] = bottomRow[q2 - 1];
        if (q2 > 1) {
          l[_nNodesX - 2] = bottomRow[q2 - 2];
          ArgumentChecker.isFalse(q2 > 2, "Boundary condition means that system is not tri-diagonal");
        }

        for (int ii = 0; ii < _nNodesX - 2; ii++) { //tri-diagonal form
          final double x = _grid.getSpaceNode(ii + 1);
          final double a = _coeff.getA(t, x);
          final double b = _coeff.getB(t, x);
          final double c = _coeff.getC(t, x);
          //debug - fitting par
          //a = getFittingParameter(a, b, ii);
          cDag[ii] = _x2nd[ii][1] * a + _x1st[ii][1] * b + c;
          lDag[ii] = _x2nd[ii][0] * a + _x1st[ii][0] * b;
          uDag[ii] = _x2nd[ii][2] * a + _x1st[ii][2] * b;
        }


        for (int ii = 1; ii < _nNodesX - 1; ii++) {
          d[ii] = 1 + _theta * dt * cDag[ii - 1];
          u[ii] = _theta * dt * uDag[ii - 1];
          l[ii - 1] = _theta * dt * lDag[ii - 1];
        }
        final TridiagonalMatrix lhs = new TridiagonalMatrix(d, u, l);

        //solve the system (update h)
        switch (_mode) {
          case tridiagonal:
            h = solvTriDag(lhs, y);
            break;
          case luDecomp:
            h = solveLU(lhs, y);
            break;
          case psor:
            h = solvTriDag(lhs, y);
            final double[] free = new double[_nNodesX];
            for (int ii = 0; ii < _nNodesX; ii++) {
              final double x = _grid.getSpaceNode(ii);
              free[ii] = _freeB.getZValue(t, x);
            }
            h = solvePSOR(lhs, y, h, free);
            break;
          default:
            throw new NotImplementedException("SolverMode " + _mode.toString() + " not implemented");
        }

        if (_showFullResults && full != null) {
          full[jj + 1] = Arrays.copyOf(h, _nNodesX);
        }
      }
      PDEResults1D res;
      if (_showFullResults) {
        res = new PDEFullResults1D(_grid, full);
      } else {
        res = new PDETerminalResults1D(_grid, h);
      }
      return res;
    }

    @SuppressWarnings("synthetic-access")
    private double[] solveLU(final TridiagonalMatrix lM, final double[] y) {
      final DecompositionResult res = DCOMP.evaluate(lM.toDoubleMatrix2D());
      return res.solve(y);
    }

    private double[] solvePSOR(final TridiagonalMatrix lM, final double[] b, final double[] x, final double[] minVal) {

      final double[] d = lM.getDiagonalData();
      final double[] u = lM.getUpperSubDiagonalData();
      final double[] l = lM.getLowerSubDiagonalData();

      final int maxInt = 100000;
      final double omega = 1.0;
      final double[] invD = new double[_nNodesX];
      for (int ii = 0; ii < _nNodesX; ii++) {
        if (d[ii] == 0.0) {
          throw new MathException("Cannot solve by PSOR - zero on diagonal");
        }
        invD[ii] = 1.0 / d[ii];
      }

      final int n = b.length;
      double maxErr = 1.0;
      int count = 0;
      double temp;

      for (int ii = 0; ii < n; ii++) {
        x[ii] = Math.max(minVal[ii], x[ii]);
      }
      final double small = 1e-15;

      while (count < maxInt && maxErr > 1e-8) {
        temp = Math.max(minVal[0], (1 - omega) * x[0] + omega * invD[0] * (b[0] - u[0] * x[1]));
        // errSqr = (temp - x[0]) * (temp - x[0]);
        maxErr = Math.abs(temp - x[0]) / (Math.abs(x[0]) + small);
        x[0] = temp;
        for (int ii = 1; ii < n - 1; ii++) {
          temp = Math.max(minVal[ii], (1 - omega) * x[ii] + omega * invD[ii] * (b[ii] - l[ii - 1] * x[ii - 1] - u[ii] * x[ii + 1]));
          // errSqr += (temp - x[ii]) * (temp - x[ii]);
          maxErr = Math.max(Math.abs(temp - x[ii]) / (Math.abs(x[ii]) + small), maxErr);
          x[ii] = temp;
        }
        temp = Math.max(minVal[n - 1], (1 - omega) * x[n - 1] + omega * invD[n - 1] * (b[n - 1] - l[n - 2] * x[n - 2]));
        maxErr = Math.max(Math.abs(temp - x[n - 1]) / (Math.abs(x[n - 1]) + small), maxErr);
        //errSqr += (temp - x[n - 1]) * (temp - x[n - 1]);
        x[n - 1] = temp;

        //   errSqr = Math.sqrt(errSqr);
        count++;
      }

      if (count == maxInt) {
        throw new MathException("PSOR failed to converge");
      }
      return x;
    }

    /**
     * This is modified from the Exponential Fitting of Duffy. We find no effect on the accuracy from using this adjustment, but leave it as a stub for further investigation
     * @param a The diffusion coefficient
     * @param b The convection coefficient
     * @param i The index of the (internal) space node
     * @return The adjusted diffusion coefficient (rho)
     */
    @SuppressWarnings("unused")
    private double getFittingParameter(final double a, final double b, final int i) {
      double rho;
      if (a == 0 && b == 0) {
        return 0.0;
      }

      final double[] x1st = _x1st[i];
      final double[] x2nd = _x2nd[i];
      final double bdx1 = (b * _dx[i]);
      final double bdx2 = (b * _dx[i + 1]);

      // convection dominated
      if (Math.abs(bdx1) > 10 * Math.abs(a) || Math.abs(bdx2) > 10 * Math.abs(a)) {
        // a > 0 is unphysical as it corresponds to a negative diffusion
        final double sign = (a > 0.0 ? -1.0 : 1.0);
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
  }

  /**
   * @deprecated Use SolverImpl
   */
  @Deprecated
  class SolverImplDeprecated {
    // private final ConvectionDiffusionPDEDataBundle _pdeData;
    private final ConvectionDiffusionPDE1DStandardCoefficients _coefficients;
    private final double[] _initialCondition;
    private final PDEGrid1D _grid;
    private final BoundaryCondition _lowerBoundary;
    private final BoundaryCondition _upperBoundary;
    private final Surface<Double, Double, Double> _freeBoundary;
    private double[] _f;
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
    public SolverImplDeprecated(final ConvectionDiffusionPDE1DStandardCoefficients coeff, final double[] initialCondition, final BoundaryCondition lowerBoundary,
        final BoundaryCondition upperBoundary, final Surface<Double, Double, Double> freeBoundary, final PDEGrid1D grid) {
      _coefficients = coeff;
      _initialCondition = initialCondition;
      _lowerBoundary = lowerBoundary;
      _upperBoundary = upperBoundary;
      _freeBoundary = freeBoundary;
      _grid = grid;

      final int tNodes = _grid.getNumTimeNodes();
      final int xNodes = _grid.getNumSpaceNodes();

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

        setT2(getGrid().getTimeNode(n));
        updateRHSVector();
        updateRHSBoundary();
        updateCoefficents();
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
      final double t0 = getGrid().getTimeNode(0);
      setT1(t0);
      _f = Arrays.copyOf(_initialCondition, getGrid().getNumSpaceNodes());
      if (_showFullResults) {
        _full[0] = _initialCondition;
      }

      double x;
      for (int i = 0; i < getGrid().getNumSpaceNodes() - 2; i++) {
        x = getGrid().getSpaceNode(i + 1);
        setA(i, _coefficients.getA(t0, x));
        setB(i, _coefficients.getB(t0, x));
        setC(i, _coefficients.getC(t0, x));
        setRho(i, getFittingParameter(getGrid(), getA(i), getB(i), i + 1));
      }
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
      double[] temp = _lowerBoundary.getRightMatrixCondition(_coefficients, getGrid(), getT1());
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * getF(k);
      }
      setQ(0, sum + _lowerBoundary.getConstant(_coefficients, getT2()));

      temp = _upperBoundary.getRightMatrixCondition(_coefficients, getGrid(), getT1());
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * getF(getGrid().getNumSpaceNodes() - 1 - k);
      }

      setQ(getGrid().getNumSpaceNodes() - 1, sum + _upperBoundary.getConstant(_coefficients, getT2()));
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
      double[] temp = _lowerBoundary.getLeftMatrixCondition(_coefficients, getGrid(), getT2());
      for (int k = 0; k < temp.length; k++) {
        setM(0, k, temp[k]);
      }

      temp = _upperBoundary.getLeftMatrixCondition(_coefficients, getGrid(), getT2());
      for (int k = 0; k < temp.length; k++) {
        setM(getGrid().getNumSpaceNodes() - 1, getGrid().getNumSpaceNodes() - temp.length + k, temp[k]);
      }
    }

    void updateCoefficents() {
      double x;
      for (int i = 1; i < getGrid().getNumSpaceNodes() - 1; i++) {
        x = getGrid().getSpaceNode(i);
        setA(i - 1, _coefficients.getA(getT2(), x));
        setB(i - 1, _coefficients.getB(getT2(), x));
        setC(i - 1, _coefficients.getC(getT2(), x));
        //TODO R White 19/09/2012 change this back debug
        //setRho(i - 1, getA(i - 1));
        setRho(i - 1, getFittingParameter(getGrid(), getA(i - 1), getB(i - 1), i));
      }
    }

    private void solveMatrixSystem() {
      //   @SuppressWarnings("unused")
      //NOTE get this working again with dynamic omega
      //final int count = solveBySOR(omega);
      solveByLU();
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

    @SuppressWarnings({"synthetic-access" })
    private void solveByLU() {
      final DoubleMatrix2D temp = new DoubleMatrix2D(_m);
      final DecompositionResult res = DCOMP.evaluate(temp);
      final double[] f = res.solve(_q);
      for (int i = 0; i < f.length; i++) {
        _f[i] = f[i];
      }
    }

    @SuppressWarnings("unused")
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
        final double sign = (a > 0.0 ? -1.0 : 1.0);
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

  private class ExtendedSolverImpl extends SolverImplDeprecated {

    //private final ExtendedConvectionDiffusionPDEDataBundle _pdeData;
    private final ConvectionDiffusionPDE1DFullCoefficients _coeff;
    private final double[] _alpha;
    private final double[] _beta;

    public ExtendedSolverImpl(final ConvectionDiffusionPDE1DFullCoefficients coeff, final double[] initialCondition,
        final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary, final Surface<Double, Double, Double> freeBoundary, final PDEGrid1D grid) {
      super(coeff.getStandardCoefficients(), initialCondition, lowerBoundary, upperBoundary, freeBoundary, grid);
      _coeff = coeff;
      final int xNodes = grid.getNumSpaceNodes();
      _alpha = new double[xNodes];
      _beta = new double[xNodes];
    }

    /**
     * @param pdeData
     * @param grid
     * @param lowerBoundary
     * @param upperBoundary
     * @param freeBoundary
     */
    //    public ExtendedSolverImpl(ExtendedConvectionDiffusionPDEDataBundle pdeData, PDEGrid1D grid, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary,
    //        Surface<Double, Double, Double> freeBoundary) {
    //      super(pdeData, grid, lowerBoundary, upperBoundary, freeBoundary);
    //      _pdeData = pdeData;
    //      final int xNodes = grid.getNumSpaceNodes();
    //      _alpha = new double[xNodes];
    //      _beta = new double[xNodes];
    //    }

    @Override
    void initialise() {
      super.initialise();
      double x;
      final double t = getT1();
      for (int i = 0; i < getGrid().getNumSpaceNodes(); i++) {
        x = getGrid().getSpaceNode(i);
        _alpha[i] = _coeff.getAlpha(t, x);
        _beta[i] = _coeff.getBeta(t, x);
      }
    }

    @Override
    void updateRHSVector() {
      final double dt = getT2() - getT1();
      double[] x1st, x2nd;
      double temp;
      for (int i = 1; i < getGrid().getNumSpaceNodes() - 1; i++) {
        x1st = getGrid().getFirstDerivativeCoefficients(i);
        x2nd = getGrid().getSecondDerivativeCoefficients(i);
        //TODO Work out a fitting scheme
        temp = getF(i);
        temp -= (1 - getTheta()) * dt * (x2nd[0] * getA(i - 1) * _alpha[i - 1] + x1st[0] * getB(i - 1) * _beta[i - 1]) * getF(i - 1);
        temp -= (1 - getTheta()) * dt * (x2nd[1] * getA(i - 1) * _alpha[i] + x1st[1] * getB(i - 1) * _beta[i] + getC(i - 1)) * getF(i);
        temp -= (1 - getTheta()) * dt * (x2nd[2] * getA(i - 1) * _alpha[i + 1] + x1st[2] * getB(i - 1) * _beta[i + 1]) * getF(i + 1);
        setQ(i, temp);
      }
    }

    @Override
    void updateLHSMatrix() {
      final double dt = getT2() - getT1();
      double[] x1st, x2nd;
      for (int i = 1; i < getGrid().getNumSpaceNodes() - 1; i++) {
        x1st = getGrid().getFirstDerivativeCoefficients(i);
        x2nd = getGrid().getSecondDerivativeCoefficients(i);

        setM(i, i - 1, getTheta() * dt * (x2nd[0] * getA(i - 1) * _alpha[i - 1] + x1st[0] * getB(i - 1) * _beta[i - 1]));
        setM(i, i, 1 + getTheta() * dt * (x2nd[1] * getA(i - 1) * _alpha[i] + x1st[1] * getB(i - 1) * _beta[i] + getC(i - 1)));
        setM(i, i + 1, getTheta() * dt * (x2nd[2] * getA(i - 1) * _alpha[i + 1] + x1st[2] * getB(i - 1) * _beta[i + 1]));
      }
    }

    @Override
    void updateCoefficents() {
      super.updateCoefficents();
      double x;
      for (int i = 0; i < getGrid().getNumSpaceNodes(); i++) {
        x = getGrid().getSpaceNode(i);
        _alpha[i] = _coeff.getAlpha(getT2(), x);
        _beta[i] = _coeff.getBeta(getT2(), x);
      }
    }

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
    return count;
  }

}
