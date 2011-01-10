/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import org.junit.Test;

import com.opengamma.math.linearalgebra.SVDecompositionCommons;

/**
 * 
 */
public class BroydenVectorRootFinderTest extends VectorRootFinderTest {
  private static final NewtonVectorRootFinder DEFAULT = new BroydenVectorRootFinder(TOLERANCE, TOLERANCE, MAXSTEPS);
  private static final NewtonVectorRootFinder SV = new BroydenVectorRootFinder(TOLERANCE, TOLERANCE, MAXSTEPS, new SVDecompositionCommons());
  private static final NewtonVectorRootFinder DEFAULT_JACOBIAN_2D = new BroydenVectorRootFinder(TOLERANCE, TOLERANCE, MAXSTEPS);
  private static final NewtonVectorRootFinder SV_JACOBIAN_2D = new BroydenVectorRootFinder(TOLERANCE, TOLERANCE, MAXSTEPS, new SVDecompositionCommons());
  private static final NewtonVectorRootFinder DEFAULT_JACOBIAN_3D = new BroydenVectorRootFinder(TOLERANCE, TOLERANCE, MAXSTEPS);
  private static final NewtonVectorRootFinder SV_JACOBIAN_3D = new BroydenVectorRootFinder(TOLERANCE, TOLERANCE, MAXSTEPS, new SVDecompositionCommons());

  @Test(expected = IllegalArgumentException.class)
  public void testSingular1() {
    testFunction2D(DEFAULT, EPS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSingular2() {
    testFunction2D(DEFAULT_JACOBIAN_2D, EPS);
  }

  @Test
  public void test() {
    testLinear(DEFAULT, EPS);
    testLinear(SV, EPS);
    testFunction2D(SV, EPS);
    testFunction2D(SV_JACOBIAN_2D, EPS);
    testFunction3D(DEFAULT, EPS);
    testFunction3D(DEFAULT_JACOBIAN_3D, EPS);
    testFunction3D(SV, EPS);
    testFunction3D(SV_JACOBIAN_3D, EPS);
    testYieldCurveBootstrap(DEFAULT, EPS);
  }
}
