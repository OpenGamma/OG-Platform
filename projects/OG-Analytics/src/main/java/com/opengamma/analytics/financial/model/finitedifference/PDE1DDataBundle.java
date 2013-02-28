/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import java.util.Arrays;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * This class contains all the relevant information to solve a 1D PDE on a finite difference grid - the (functional) coefficients that
 * describe the PDE; the initial condition; the boundary conditions; the free boundary (if any); and the grid.
 * @param <T> The type of PDE
 */
public class PDE1DDataBundle<T extends PDE1DCoefficients> {

  private final T _coefficients;
  private final double[] _initialCondition;
  private final BoundaryCondition _lowerBoundary;
  private final BoundaryCondition _upperBoundary;
  private final Surface<Double, Double, Double> _freeBoundary;
  private final PDEGrid1D _grid;

  /**
   * All the relevant information to solve a 1D PDE on a finite difference grid
   * @param coefficients The description of the PDE $\mathcal{D}[V(t,x)]=0$
   * @param initialCondition The function V(0,x)
   * @param lowerBoundary Boundary condition at the lowest value of x
   * @param upperBoundary Boundary condition at the highest value of x
   * @param grid 2D grid (t & x) on which the PDE will be solved
   */
  public PDE1DDataBundle(final T coefficients,
      final Function1D<Double, Double> initialCondition,
      final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary,
      final PDEGrid1D grid) {

    _initialCondition = getInitialConditions(coefficients, initialCondition, lowerBoundary, upperBoundary, grid);
    _coefficients = coefficients;
    _lowerBoundary = lowerBoundary;
    _upperBoundary = upperBoundary;
    _grid = grid;
    _freeBoundary = null;
  }

  /**
   * All the relevant information to solve a 1D PDE on a finite difference grid
   * @param coefficients The description of the PDE $\mathcal{D}[V(t,x)]=0$
   * @param initialCondition The function V(0,x)
   * @param lowerBoundary Boundary condition at the lowest value of x
   * @param upperBoundary Boundary condition at the highest value of x
   * @param freeBoundary for a free-boundary function, $H(t,x)$, the solution to the PDE at $(t,x)$ is $max(V^*(t,x),H(t,x))$
   * where $max(V^*(t,x)$ is the value calculated before the free-boundary condition is applied
   * @param grid 2D grid (t & x) on which the PDE will be solved
   */
  public PDE1DDataBundle(final T coefficients,
      final Function1D<Double, Double> initialCondition,
      final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary,
      final Surface<Double, Double, Double> freeBoundary,
      final PDEGrid1D grid) {

    ArgumentChecker.notNull(freeBoundary, "null freeBoundary");
    _initialCondition = getInitialConditions(coefficients, initialCondition, lowerBoundary, upperBoundary, grid);
    _coefficients = coefficients;
    _lowerBoundary = lowerBoundary;
    _upperBoundary = upperBoundary;
    _grid = grid;
    _freeBoundary = freeBoundary;
  }

  /**
   * All the relevant information to solve a 1D PDE on a finite difference grid
   * @param coefficients The description of the PDE $\mathcal{D}[V(t,x)]=0$
   * @param initialCondition The values $V(0,x_i)$ where $x_i$ are the spacial grid points
   * @param lowerBoundary Boundary condition at the lowest value of x
   * @param upperBoundary Boundary condition at the highest value of x
   * @param grid 2D grid (t & x) on which the PDE will be solved
   */
  public PDE1DDataBundle(final T coefficients,
      final double[] initialCondition,
      final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary,
      final PDEGrid1D grid) {
    checkData(coefficients, initialCondition, lowerBoundary, upperBoundary, grid);

    _coefficients = coefficients;
    _initialCondition = initialCondition;
    _lowerBoundary = lowerBoundary;
    _upperBoundary = upperBoundary;
    _grid = grid;
    _freeBoundary = null;
  }

  /**
   * All the relevant information to solve a 1D PDE on a finite difference grid
   * @param coefficients The description of the PDE $\mathcal{D}[V(t,x)]=0$
   * @param initialCondition The values $V(0,x_i)$ where $x_i$ are the spacial grid points
   * @param lowerBoundary Boundary condition at the lowest value of x
   * @param upperBoundary Boundary condition at the highest value of x
   * @param freeBoundary for a free-boundary function, $H(t,x)$, the solution to the PDE at $(t,x)$ is $max(V^*(t,x),H(t,x))$
   * where $max(V^*(t,x)$ is the value calculated before the free-boundary condition is applied
   * @param grid 2D grid (t & x) on which the PDE will be solved
   */
  public PDE1DDataBundle(final T coefficients,
      final double[] initialCondition,
      final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary,
      final Surface<Double, Double, Double> freeBoundary,
      final PDEGrid1D grid) {

    ArgumentChecker.notNull(freeBoundary, "null freeBoundary");
    checkData(coefficients, initialCondition, lowerBoundary, upperBoundary, grid);

    _coefficients = coefficients;
    _initialCondition = initialCondition;
    _lowerBoundary = lowerBoundary;
    _upperBoundary = upperBoundary;
    _freeBoundary = freeBoundary;
    _grid = grid;
  }

  /**
   * Gets the coefficients.
   * @return the coefficients
   */
  public T getCoefficients() {
    return _coefficients;
  }

  /**
   * Gets the initialCondition.
   * @return the initialCondition
   */
  public double[] getInitialCondition() {
    return _initialCondition;
  }

  /**
   * Gets the lowerBoundary.
   * @return the lowerBoundary
   */
  public BoundaryCondition getLowerBoundary() {
    return _lowerBoundary;
  }

  /**
   * Gets the upperBoundary.
   * @return the upperBoundary
   */
  public BoundaryCondition getUpperBoundary() {
    return _upperBoundary;
  }

  /**
   * Gets the freeBoundary.
   * @return the freeBoundary
   */
  public Surface<Double, Double, Double> getFreeBoundary() {
    return _freeBoundary;
  }

  /**
   * Gets the grid.
   * @return the grid
   */
  public PDEGrid1D getGrid() {
    return _grid;
  }

  public PDE1DDataBundle<T> withInitialConditions(final Function1D<Double, Double> initialCondition) {
    if (_freeBoundary == null) {
      return new PDE1DDataBundle<>(_coefficients, initialCondition, _lowerBoundary, _upperBoundary, _grid);
    }
    return new PDE1DDataBundle<>(_coefficients, initialCondition, _lowerBoundary, _upperBoundary, _freeBoundary, _grid);
  }

  public PDE1DDataBundle<T> withInitialConditions(final double[] initialCondition) {
    if (_freeBoundary == null) {
      return new PDE1DDataBundle<>(_coefficients, initialCondition, _lowerBoundary, _upperBoundary, _grid);
    }
    return new PDE1DDataBundle<>(_coefficients, initialCondition, _lowerBoundary, _upperBoundary, _freeBoundary, _grid);
  }

  public PDE1DDataBundle<T> withGrid(final PDEGrid1D grid) {
    if (_freeBoundary == null) {
      return new PDE1DDataBundle<>(_coefficients, _initialCondition, _lowerBoundary, _upperBoundary, grid);
    }
    return new PDE1DDataBundle<>(_coefficients, _initialCondition, _lowerBoundary, _upperBoundary, _freeBoundary, grid);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_coefficients == null) ? 0 : _coefficients.hashCode());
    result = prime * result + ((_freeBoundary == null) ? 0 : _freeBoundary.hashCode());
    result = prime * result + ((_grid == null) ? 0 : _grid.hashCode());
    result = prime * result + Arrays.hashCode(_initialCondition);
    result = prime * result + ((_lowerBoundary == null) ? 0 : _lowerBoundary.hashCode());
    result = prime * result + ((_upperBoundary == null) ? 0 : _upperBoundary.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    @SuppressWarnings("unchecked")
    final
    PDE1DDataBundle<T> other = (PDE1DDataBundle<T>) obj;
    if (_coefficients == null) {
      if (other._coefficients != null) {
        return false;
      }
    } else if (!_coefficients.equals(other._coefficients)) {
      return false;
    }
    if (_freeBoundary == null) {
      if (other._freeBoundary != null) {
        return false;
      }
    } else if (!_freeBoundary.equals(other._freeBoundary)) {
      return false;
    }
    if (_grid == null) {
      if (other._grid != null) {
        return false;
      }
    } else if (!_grid.equals(other._grid)) {
      return false;
    }
    if (!Arrays.equals(_initialCondition, other._initialCondition)) {
      return false;
    }
    if (_lowerBoundary == null) {
      if (other._lowerBoundary != null) {
        return false;
      }
    } else if (!_lowerBoundary.equals(other._lowerBoundary)) {
      return false;
    }
    if (_upperBoundary == null) {
      if (other._upperBoundary != null) {
        return false;
      }
    } else if (!_upperBoundary.equals(other._upperBoundary)) {
      return false;
    }
    return true;
  }

  private static double[] getInitialConditions(final PDE1DCoefficients coefficients, final Function1D<Double, Double> initialCondition,
      final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary, final PDEGrid1D grid) {
    ArgumentChecker.notNull(coefficients, "null coefficients");
    ArgumentChecker.notNull(initialCondition, "null initialCondition");
    ArgumentChecker.notNull(lowerBoundary, "null lowerBoundary");
    ArgumentChecker.notNull(upperBoundary, "null upperBoundary");
    ArgumentChecker.notNull(grid, "null grid");

    final int n = grid.getNumSpaceNodes();

    ArgumentChecker.isTrue(Math.abs((grid.getSpaceNode(0) - lowerBoundary.getLevel()) / (1.0 + Math.abs(lowerBoundary.getLevel()))) < 1e-12,
        "space grid not consistent with lower boundary level. Lowerst grid point at " + grid.getSpaceNode(0) + " but lower boundary at "
            + lowerBoundary.getLevel());
    ArgumentChecker.isTrue(Math.abs((grid.getSpaceNode(n - 1) - upperBoundary.getLevel()) / (1.0 + Math.abs(upperBoundary.getLevel()))) < 1e-12,
        "space grid not consistent with upper boundary level. Highest grid point at " + grid.getSpaceNode(n - 1) + " but upper boundary at "
            + upperBoundary.getLevel());

    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = initialCondition.evaluate(grid.getSpaceNode(i));
    }
    return res;
  }

  private static void checkData(final PDE1DCoefficients coefficients, final double[] initialCondition, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary, final PDEGrid1D grid) {
    ArgumentChecker.notNull(coefficients, "null coefficients");
    ArgumentChecker.notNull(initialCondition, "null initialCondition");
    ArgumentChecker.notNull(lowerBoundary, "null lowerBoundary");
    ArgumentChecker.notNull(upperBoundary, "null upperBoundary");
    ArgumentChecker.notNull(grid, "null grid");

    final int n = grid.getNumSpaceNodes();
    ArgumentChecker.isTrue(initialCondition.length == n, n + " space nodes, but " + initialCondition.length + " values specified for initial condition");
    ArgumentChecker.isTrue(Math.abs((grid.getSpaceNode(0) - lowerBoundary.getLevel()) / (1.0 + lowerBoundary.getLevel())) < 1e-12,
        "space grid not consistent with lower boundary level. Lowerst grid point at " + grid.getSpaceNode(0) + " but lower boundary at "
            + lowerBoundary.getLevel());
    ArgumentChecker.isTrue(Math.abs((grid.getSpaceNode(n - 1) - upperBoundary.getLevel()) / (1.0 + upperBoundary.getLevel())) < 1e-12,
        "space grid not consistent with upper boundary level. Highest grid point at " + grid.getSpaceNode(n - 1) + " but upper boundary at "
            + upperBoundary.getLevel());

    //TODO check that boundary condition is consistent with initial condition
  }
}
