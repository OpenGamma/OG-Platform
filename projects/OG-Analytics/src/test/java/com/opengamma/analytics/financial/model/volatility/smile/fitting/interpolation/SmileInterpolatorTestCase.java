/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class SmileInterpolatorTestCase {

  private static final double FORWARD = 1172.011012;
  private static final double EXPIRY = 1.5;
  private static final double[] STRIKES = new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534};
  private static final double[] VOLS = new double[] {0.311, 0.288, 0.267, 0.271, 0.276};

  public abstract GeneralSmileInterpolator getSmileInterpolator();

  protected double[] getStrikes() {
    return STRIKES;
  }

  protected double[] getVols() {
    return VOLS;
  }

  protected double getForward() {
    return FORWARD;
  }

  protected double getExpiry() {
    return EXPIRY;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes() {

    getSmileInterpolator().getVolatilityFunction(FORWARD, null, EXPIRY, VOLS);

  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVols() {

    getSmileInterpolator().getVolatilityFunction(FORWARD, STRIKES, EXPIRY, null);

  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthVols() {

    getSmileInterpolator().getVolatilityFunction(FORWARD, STRIKES, EXPIRY, Arrays.copyOfRange(VOLS, 0, VOLS.length - 1));

  }

  @Test
  public void smileTest() {
    //    final long startTime = System.nanoTime();
    final GeneralSmileInterpolator interpolator = getSmileInterpolator();
    final Function1D<Double, Double> smile = interpolator.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, VOLS);
    final int n = STRIKES.length;
    for (int i = 0; i < n; i++) {
      final double k = STRIKES[i];
      final double vol = smile.evaluate(k);
      assertEquals(VOLS[i], vol, 1e-6);
    }
    //    final long endTime = System.nanoTime();
    //    System.out.println("smileTest time: " + (endTime - startTime) / 1e6 + "ms");
  }

  @Test
  public void flatTest() {
    final int n = STRIKES.length;
    final double flatVol = 0.11;
    final double[] vols = new double[n];
    Arrays.fill(vols, flatVol);
    //    final long startTime = System.nanoTime();
    final GeneralSmileInterpolator interpolator = getSmileInterpolator();
    final Function1D<Double, Double> smile = interpolator.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      assertEquals(flatVol, vol, 1e-8);
    }
    //    final long endTime = System.nanoTime();
    //    System.out.println("flat time: " + (endTime - startTime) / 1e6 + "ms");
  }

  @Test
  public void smallBumpTest() {
    final GeneralSmileInterpolator interpolator = getSmileInterpolator();
    final double bump = 1e-3;
    final int n = STRIKES.length;
    for (int index = 0; index < n; index++) {
      final double[] vols = Arrays.copyOf(VOLS, VOLS.length);
      vols[index] += bump;
      final Function1D<Double, Double> smile = interpolator.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
      for (int i = 0; i < n; i++) {
        final double k = STRIKES[i];
        final double vol = smile.evaluate(k);
        assertEquals(vols[i], vol, 1e-6);
      }
    }
  }

  //**********************************************************************************************
  // The following are debug tests that print output. Ensure enabled = false before committing
  //**********************************************************************************************

  @Test(enabled = false)
  public void printSmileTest() {

    final GeneralSmileInterpolator interpolator = getSmileInterpolator();
    final Function1D<Double, Double> smile = interpolator.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, VOLS);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test(enabled = false)
  public void bumpTest() {
    final GeneralSmileInterpolator interpolator = getSmileInterpolator();
    final double bump = 1e-3;
    final int index = 1;
    final double[] vols = Arrays.copyOf(VOLS, VOLS.length);
    vols[index] += bump;
    final Function1D<Double, Double> smile = interpolator.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test(enabled = false)
  public void flatBumpTest() {
    final GeneralSmileInterpolator interpolator = getSmileInterpolator();
    final int n = STRIKES.length;
    final double[] vols = new double[n];
    Arrays.fill(vols, 0.2);
    final double bump = 1e-3;
    final int index = 0;
    vols[index] += bump;

    final Function1D<Double, Double> smile = interpolator.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test(enabled = false)
  public void badFitTest() {
    final GeneralSmileInterpolator interpolator = getSmileInterpolator();
    final double forward = 1.30276013603506;
    final double[] strikes = new double[] {1.080256504787705, 1.161299691076151, 1.329077636516407, 1.5210230159922162, 1.635211041136184};
    final double expiry = 1.0;
    final double[] impVols = new double[] {0.2, 0.2, 0.2, 0.2, 0.2};
    final double bump = 1e-3;
    final int index = 2;
    impVols[index] += bump;
    final Function1D<Double, Double> smile = interpolator.getVolatilityFunction(forward, strikes, expiry, impVols);

    for (int i = 0; i < 200; i++) {
      final double k = 0.8 + 1.2 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

}
