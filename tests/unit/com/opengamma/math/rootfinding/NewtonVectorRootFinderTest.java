/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.junit.Test;

import com.opengamma.math.linearalgebra.SVDecompositionCommons;

/**
 * 
 */
public class NewtonVectorRootFinderTest extends VectorRootFinderTest {
  private static final VectorRootFinder DEFAULT = new NewtonVectorRootFinder(EPS, EPS, MAXSTEPS);
  private static final VectorRootFinder SV = new NewtonVectorRootFinder(EPS, EPS, MAXSTEPS, new SVDecompositionCommons());

  @Test
  public void test() {
    testLinear(DEFAULT, EPS);
    testFunction2D(SV, EPS);
    testFunction3D(DEFAULT, EPS);
    testJacobian3D(DEFAULT, EPS);
    testYieldCurveBootstrap(DEFAULT, EPS);
  }
}
