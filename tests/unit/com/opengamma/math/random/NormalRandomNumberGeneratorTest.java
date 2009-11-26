/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class NormalRandomNumberGeneratorTest {
  private static final NormalRandomNumberGenerator GENERATOR = new NormalRandomNumberGenerator(0, 1);

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() {
    new NormalRandomNumberGenerator(0, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadDimension() {
    GENERATOR.getVectors(-1, 4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadN() {
    GENERATOR.getVectors(1, -5);
  }
}
