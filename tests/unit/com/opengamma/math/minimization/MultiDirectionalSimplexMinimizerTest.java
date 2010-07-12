/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.junit.Test;

/**
 * 
 */
public class MultiDirectionalSimplexMinimizerTest extends MultidimensionalMinimizerTestCase {

  @Test
  public void test() {
    final MultidimensionalMinimizer MINIMIZER = new MultiDirectionalSimplexMinimizer();
    super.testInputs(MINIMIZER);
    super.test(MINIMIZER);
  }
}
