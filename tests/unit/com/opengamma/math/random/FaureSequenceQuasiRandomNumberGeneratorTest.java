/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import java.util.List;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class FaureSequenceQuasiRandomNumberGeneratorTest {
  private static final QuasiRandomNumberGenerator GENERATOR = new FaureSequenceQuasiRandomNumberGenerator();

  @Test
  public void test() {
    final List<Double[]> vectors = GENERATOR.getVectors(3, 8);
  }
}
