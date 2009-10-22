/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class GoldenSectionMinimizer1DTest extends Minimizer1DTest {
  private static final Minimizer1D MINIMIZER = new GoldenSectionMinimizer1D();

  @Test
  public void test() {
    super.testInputs(MINIMIZER);
    super.test(MINIMIZER);
  }
}
