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
public class VanDerCorputQuasiRandomNumberGeneratorTest {

  @Test
  public void test() {
    new VanDerCorputQuasiRandomNumberGenerator(3).getQuasiRandomVectors(1, 24);
  }
}
