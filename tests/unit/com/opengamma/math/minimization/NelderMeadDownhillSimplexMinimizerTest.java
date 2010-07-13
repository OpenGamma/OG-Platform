/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.junit.Test;

public class NelderMeadDownhillSimplexMinimizerTest extends MultidimensionalMinimizerTestCase {

  @Test
  public void test() {
    final SimplexMinimizer MINIMIZER = new NelderMeadDownhillSimplexMinimizer();
    super.testInputs(MINIMIZER);
    super.test(MINIMIZER);
  }

}
