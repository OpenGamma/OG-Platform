/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.minimization.GoldenSectionMinimizer1D;
import com.opengamma.analytics.math.minimization.ScalarMinimizer;

/**
 * 
 */
public class GoldenSectionMinimizer1DTest extends Minimizer1DTestCase {
  private static final ScalarMinimizer MINIMIZER = new GoldenSectionMinimizer1D();

  @Test
  public void test() {
    super.assertInputs(MINIMIZER);
    super.assertMinimizer(MINIMIZER);
  }
}
