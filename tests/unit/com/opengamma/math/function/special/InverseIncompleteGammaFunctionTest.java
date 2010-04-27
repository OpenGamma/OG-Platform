/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.Function2D;

/**
 * 
 * @author emcleod
 */
public class InverseIncompleteGammaFunctionTest {
  private static final Function1D<Double, Double> INCOMPLETE_GAMMA = new IncompleteGammaFunction(2);
  private static final Function2D<Double, Double> INVERSE = new InverseIncompleteGammaFunction();
  private static final double EPS = 1e-12;

  @Test
  public void test() {

  }
}
