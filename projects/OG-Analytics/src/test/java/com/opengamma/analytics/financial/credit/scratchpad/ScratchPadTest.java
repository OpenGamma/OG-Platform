/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.scratchpad;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ScratchPadTest {

  @Test(enabled = false)
  public void test() {

    double startTime = tic(); //System.nanoTime();

    int n = 1000000;

    double[] x = new double[n];

    for (int i = 0; i < n; i++) {
      x[i] = n - i * i;
    }

    for (int i = 0; i < n; i++) {
      //System.out.println(i + "\t" + x[i]);
    }

    //final int num = x.length;

    final double[] copy = Arrays.copyOf(x, n);

    Arrays.sort(copy);

    for (int i = 0; i < n; i++) {
      //System.out.println(i + "\t" + copy[i]);
    }

    double endTime = toc(); //System.nanoTime();

    double duration = (endTime - startTime) / 1e9;

    //System.out.println(duration);

  }

  double tic() {
    return System.nanoTime();
  }

  double toc() {
    return System.nanoTime();
  }

}
