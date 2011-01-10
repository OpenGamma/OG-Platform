/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.junit.Test;

/**
 * 
 */
public class BrentMinimizer1DTest extends Minimizer1DTestCase {
  private static final ScalarMinimizer MINIMIZER = new BrentMinimizer1D();

  @Test
  public void test() {
    super.testInputs(MINIMIZER);
    super.test(MINIMIZER);
  }
}
