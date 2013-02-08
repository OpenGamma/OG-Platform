/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.scratchpad;

import java.util.Arrays;

import org.testng.annotations.Test;

/**
 * 
 */
public class ScratchPadTest {

  @Test
  public void test() {

    double[] x = new double[10];

    for (int i = 0; i < 10; i++) {
      x[i] = 100 - i * i;
    }

    for (int i = 0; i < 10; i++) {
      //System.out.println(i + "\t" + x[i]);
    }

    final int n = x.length;

    final double[] copy = Arrays.copyOf(x, n);

    Arrays.sort(copy);

    for (int i = 0; i < 10; i++) {
      //System.out.println(i + "\t" + copy[i]);
    }
  }

}
