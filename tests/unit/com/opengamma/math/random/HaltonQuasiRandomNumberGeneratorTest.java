/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import java.util.List;

import org.junit.Test;

import util.PrintToFile;

/**
 * 
 * @author emcleod
 */
public class HaltonQuasiRandomNumberGeneratorTest {

  @Test
  public void test() {
    final QuasiRandomNumberGenerator generator = new HaltonQuasiRandomNumberGenerator();
    final List<Double[]> result = generator.getVectors(50, 1000);
    PrintToFile.printToFile("test.txt", result);
  }
}
