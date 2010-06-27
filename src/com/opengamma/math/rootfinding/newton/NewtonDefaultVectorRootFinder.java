/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;

/**
 * Attempts to find the multi-dimensional root of a series of N equations with N variables, i.e. a square problem. 
 * If the analytic Jacobian is not known, it will be calculated using central difference 
 */
public class NewtonDefaultVectorRootFinder extends NewtonVectorRootFinder {
  private static final double DEF_TOL = 1e-7;
  private static final int MAX_STEPS = 100;

  public NewtonDefaultVectorRootFinder() {
    this(DEF_TOL, DEF_TOL, MAX_STEPS);
  }

  public NewtonDefaultVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps) {
    this(absoluteTol, relativeTol, maxSteps, new LUDecompositionCommons());
  }

  public NewtonDefaultVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, final Decomposition<?> decomp) {
    this(absoluteTol, relativeTol, maxSteps, new FiniteDifferenceJacobianCalculator(), decomp);
  }

  public NewtonDefaultVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, JacobianCalculator calculator) {
    this(absoluteTol, relativeTol, maxSteps, calculator, new LUDecompositionCommons());
  }

  public NewtonDefaultVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, JacobianCalculator calculator,
      final Decomposition<?> decomp) {
    super(absoluteTol, relativeTol, maxSteps, new JacobianDirectionFunction(decomp), new JacobianEstimateInitializationFunction(calculator),
        new NewtonDefaultUpdateFunction());
  }

}
