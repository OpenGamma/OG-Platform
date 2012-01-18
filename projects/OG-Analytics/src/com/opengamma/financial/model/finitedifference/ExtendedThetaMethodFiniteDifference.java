/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.math.surface.Surface;

/**
 * PDE solver for the type {@latex.inline $\\frac{\\partial f}{\\partial t} + a(t,x)\\frac{\\partial^2}{\\partial x^2}\\left[ \\alpha(t,x) f \\right] +
 * b(t,x)\\frac{\\partial}{\\partial x}\\left[\\beta(t,x) f \\right] + c(t,x)f = 0$}, which includes the Fokker-Planck PDE.
 * If the terms {@latex.inline $\\alpha$} and {@latex.inline $\\beta$} are constant or can be simply differentiated, this can be written in the form
 * {@latex.inline $\\frac{\\partial f}{\\partial t} + a^*(t,x)\\frac{\\partial^2f}{\\partial x^2} +  b^*(t,x)\\frac{\\partial f}{\\partial x} + c^*(t,x)f = 0$}
 * and solved by ThetaMethodFiniteDifference
 */
public class ExtendedThetaMethodFiniteDifference extends ThetaMethodFiniteDifference {

  public ExtendedThetaMethodFiniteDifference(final double theta, final boolean showFullResults) {
    super(theta, showFullResults);
  }

  //TODO this should @Override
  public PDEResults1D solve(final ExtendedConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary) {

    Validate.notNull(pdeData, "pde data");
    Validate.notNull(grid, "need a grid");
    validateSetup(grid, lowerBoundary, upperBoundary);
    SolverImpl solver = new ExtendedSolverImpl(pdeData, grid, lowerBoundary, upperBoundary, null);
    return solver.solve();

  }

  private class ExtendedSolverImpl extends SolverImpl {

    private final ExtendedConvectionDiffusionPDEDataBundle _pdeData;
    private final double[] _alpha;
    private final double[] _beta;

    /**
     * @param pdeData
     * @param grid
     * @param lowerBoundary
     * @param upperBoundary
     * @param freeBoundary
     */
    public ExtendedSolverImpl(ExtendedConvectionDiffusionPDEDataBundle pdeData, PDEGrid1D grid, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary,
        Surface<Double, Double, Double> freeBoundary) {
      super(pdeData, grid, lowerBoundary, upperBoundary, freeBoundary);
      _pdeData = pdeData;
      final int xNodes = grid.getNumSpaceNodes();
      _alpha = new double[xNodes];
      _beta = new double[xNodes];
    }

    @Override
    void initialise() {
      super.initialise();
      double x;
      for (int i = 0; i < getGrid().getNumSpaceNodes(); i++) {
        x = getGrid().getSpaceNode(i);
        _alpha[i] = _pdeData.getAlpha(0, x);
        _beta[i] = _pdeData.getBeta(0, x);
      }
    }

    @Override
    void updateRHSVector() {
      double dt = getT2() - getT1();
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
      double dt = getT2() - getT1();
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
        _alpha[i] = _pdeData.getAlpha(getT2(), x);
        _beta[i] = _pdeData.getBeta(getT2(), x);
      }
    }

  }

}
