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
public class DownhillSimplexMinimizerTest extends MultidimensionalMinimizerTestCase {
  private static final MultidimensionalMinimizer MINIMIZER = new DownhillSimplexMinimizer();

  @Test
  public void test() {
    super.testInputs(MINIMIZER);
    super.test(MINIMIZER);
  }
}
