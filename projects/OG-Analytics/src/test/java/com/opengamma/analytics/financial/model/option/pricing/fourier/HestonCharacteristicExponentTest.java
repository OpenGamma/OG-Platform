/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static com.opengamma.analytics.math.ComplexMathUtils.add;
import static com.opengamma.analytics.math.ComplexMathUtils.conjugate;
import static com.opengamma.analytics.math.ComplexMathUtils.divide;
import static com.opengamma.analytics.math.ComplexMathUtils.multiply;
import static com.opengamma.analytics.math.ComplexMathUtils.subtract;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.number.ComplexNumber;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HestonCharacteristicExponentTest {
  private static final double KAPPA = 0.5;
  private static final double THETA = 0.4;
  private static final double VOL0 = 0.8;
  private static final double OMEGA = 0.66;
  private static final double RHO = -0.45;
  private static final HestonCharacteristicExponent EXPONENT = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RHO);
  private RandomEngine RANDOM = new cern.jet.random.engine.MersenneTwister(123);

  @Test
  public void test() {
    assertEquals(EXPONENT.getKappa(), KAPPA, 0);
    assertEquals(EXPONENT.getOmega(), OMEGA, 0);
    assertEquals(EXPONENT.getRho(), RHO, 0);
    assertEquals(EXPONENT.getTheta(), THETA, 0);
    assertEquals(EXPONENT.getVol0(), VOL0, 0);
    HestonCharacteristicExponent other = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RHO);
    assertEquals(other, EXPONENT);
    assertEquals(other.hashCode(), EXPONENT.hashCode());
    other = new HestonCharacteristicExponent(KAPPA + 1, THETA, VOL0, OMEGA, RHO);
    assertFalse(other.equals(EXPONENT));
    other = new HestonCharacteristicExponent(KAPPA, THETA + 1, VOL0, OMEGA, RHO);
    assertFalse(other.equals(EXPONENT));
    other = new HestonCharacteristicExponent(KAPPA, THETA, VOL0 + 1, OMEGA, RHO);
    assertFalse(other.equals(EXPONENT));
    other = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA + 1, RHO);
    assertFalse(other.equals(EXPONENT));
    other = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RHO * 0.5);
    assertFalse(other.equals(EXPONENT));
  }

  @Test
  public void testSensitivities() {
    testSensitivitiesAtRandomPoints(EXPONENT);
  }

  @Test
  public void testKappaZero() {
    testSensitivitiesAtRandomPoints(EXPONENT.withKappa(0));
  }

  @Test
  public void testThetaZero() {
    testSensitivitiesAtRandomPoints(EXPONENT.withTheta(0.0));
  }

  @Test
  public void testVol0Zero() {
    testSensitivitiesAtRandomPoints(EXPONENT.withVol0(0.0));
  }

  @Test(enabled = false)
  //TODO make this test work
  public void testOmegaZero() {
    testSensitivitiesAtRandomPoints(EXPONENT.withOmega(0));
  }

  @Test
  public void testRhoMinusOne() {
    testSensitivitiesAtRandomPoints(EXPONENT.withRho(-1));
  }

  @Test
  public void testRhoZero() {
    testSensitivitiesAtRandomPoints(EXPONENT.withRho(0));
  }

  @Test
  public void testRhoPlusOne() {
    testSensitivitiesAtRandomPoints(EXPONENT.withRho(1));
  }

  private void testSensitivitiesAtRandomPoints(final HestonCharacteristicExponent ce) {
    double t;
    ComplexNumber z;
    for (int i = 0; i < 100; i++) {
      t = 10 * RANDOM.nextDouble();
      z = new ComplexNumber(8 * RANDOM.nextDouble() - 4, 4 * RANDOM.nextDouble() - 2);
      testSensitivities(ce, z, t);
    }
  }

  private void testSensitivities(final HestonCharacteristicExponent ce, final ComplexNumber z, final double t) {
    ComplexNumber[] fdSense = finiteDifferenceSensitivity(ce, z, t);
    ComplexNumber[] sense = ce.getCharacteristicExponentAdjoint(z, t);
    ComplexNumber[] senseConj = ce.getCharacteristicExponentAdjoint(multiply(-1.0, conjugate(z)), t);
    for (int i = 0; i < 6; i++) {
      assertEquals(i + " real z:" + z.toString() + ", t:" + t, fdSense[i].getReal(), sense[i].getReal(), 1e-5);
      assertEquals(i + " img z:" + z.toString() + ", t:" + t, fdSense[i].getImaginary(), sense[i].getImaginary(), 1e-5);
      //check symmetry property
      assertEquals("symmetry property (real)", sense[i].getReal(), senseConj[i].getReal(), 1e-9);
      assertEquals("symmetry property (img)", sense[i].getImaginary(), -1.0 * senseConj[i].getImaginary(), 1e-9);

    }
  }

  @Test
  public void testSensitivities2() {
    double t = 2.5;
    ComplexNumber z = new ComplexNumber(2.3, -0.6);
    ComplexNumber[] fdSense = finiteDifferenceSensitivity(EXPONENT, z, t);
    ComplexNumber[] sense = EXPONENT.getCharacteristicExponentAdjointDebug(z, t);
    ComplexNumber[] senseConj = EXPONENT.getCharacteristicExponentAdjointDebug(multiply(-1.0, conjugate(z)), t);
    for (int i = 0; i < 6; i++) {
      //      System.out.println(fdSense[i] + "\t" + sense[i] + "\t"+senseConj[i]);
      assertEquals("real: " + i, fdSense[i].getReal(), sense[i].getReal(), 1e-9);
      assertEquals("im: " + i, fdSense[i].getImaginary(), sense[i].getImaginary(), 1e-9);
      //check symmetry property
      assertEquals("symmetry property (real)", sense[i].getReal(), senseConj[i].getReal(), 1e-9);
      assertEquals("symmetry property (img)", sense[i].getImaginary(), -1.0 * senseConj[i].getImaginary(), 1e-9);

    }
  }

  private ComplexNumber[] finiteDifferenceSensitivity(final HestonCharacteristicExponent heston, ComplexNumber z, double t) {
    double eps = 1e-5;
    ComplexNumber[] res = new ComplexNumber[6];
    res[0] = heston.getFunction(t).evaluate(z);
    //bump kappa
    final double kappa = heston.getKappa();
    HestonCharacteristicExponent hestonT = heston.withKappa(kappa + eps);
    ComplexNumber up = hestonT.getFunction(t).evaluate(z);
    if (kappa > eps) {
      hestonT = heston.withKappa(kappa - eps);
      ComplexNumber down = hestonT.getFunction(t).evaluate(z);
      res[1] = divide(subtract(up, down), 2 * eps);
    } else {
      hestonT = heston.withKappa(kappa + 2 * eps);
      ComplexNumber up2 = hestonT.getFunction(t).evaluate(z);
      res[1] = add(multiply(-1.5 / eps, res[0]), multiply(2 / eps, up), multiply(-0.5 / eps, up2));
    }
    //bump theta
    final double theta = heston.getTheta();
    hestonT = heston.withTheta(theta + eps);
    up = hestonT.getFunction(t).evaluate(z);
    if (theta > eps) {
      hestonT = heston.withTheta(theta - eps);
      ComplexNumber down = hestonT.getFunction(t).evaluate(z);
      res[2] = divide(subtract(up, down), 2 * eps);
    } else {
      hestonT = heston.withTheta(theta + 2 * eps);
      ComplexNumber up2 = hestonT.getFunction(t).evaluate(z);
      res[2] = add(multiply(-1.5 / eps, res[0]), multiply(2 / eps, up), multiply(-0.5 / eps, up2));
    }
    //bump vol0
    final double vol0 = heston.getVol0();
    hestonT = heston.withVol0(vol0 + eps);
    up = hestonT.getFunction(t).evaluate(z);
    if (vol0 > eps) {
      hestonT = heston.withVol0(vol0 - eps);
      ComplexNumber down = hestonT.getFunction(t).evaluate(z);
      res[3] = divide(subtract(up, down), 2 * eps);
    } else {
      hestonT = heston.withVol0(vol0 + 2 * eps);
      ComplexNumber up2 = hestonT.getFunction(t).evaluate(z);
      res[3] = add(multiply(-1.5 / eps, res[0]), multiply(2 / eps, up), multiply(-0.5 / eps, up2));
    }
    //bump omega
    final double omega = heston.getOmega();
    hestonT = heston.withOmega(omega + eps);
    up = hestonT.getFunction(t).evaluate(z);
    if (omega > eps) {
      hestonT = heston.withOmega(omega - eps);
      ComplexNumber down = hestonT.getFunction(t).evaluate(z);
      res[4] = divide(subtract(up, down), 2 * eps);
    } else {
      hestonT = heston.withOmega(omega + 2 * eps);
      ComplexNumber up2 = hestonT.getFunction(t).evaluate(z);
      res[4] = add(multiply(-1.5 / eps, res[0]), multiply(2 / eps, up), multiply(-0.5 / eps, up2));
    }
    //bump rho
    final double rho = heston.getRho();
    if (rho + 1 < eps) {
      hestonT = heston.withRho(rho + eps);
      up = hestonT.getFunction(t).evaluate(z);
      hestonT = heston.withRho(rho + 2 * eps);
      ComplexNumber up2 = hestonT.getFunction(t).evaluate(z);
      res[5] = add(multiply(-1.5 / eps, res[0]), multiply(2 / eps, up), multiply(-0.5 / eps, up2));
    } else if (1 - rho < eps) {
      hestonT = heston.withRho(rho - eps);
      ComplexNumber down = hestonT.getFunction(t).evaluate(z);
      hestonT = heston.withRho(rho - 2 * eps);
      ComplexNumber down2 = hestonT.getFunction(t).evaluate(z);
      res[5] = add(multiply(0.5 / eps, down2), multiply(-2 / eps, down), multiply(1.5 / eps, res[0]));
    } else {
      hestonT = heston.withRho(rho + eps);
      up = hestonT.getFunction(t).evaluate(z);
      hestonT = heston.withRho(rho - eps);
      ComplexNumber down = hestonT.getFunction(t).evaluate(z);
      res[5] = divide(subtract(up, down), 2 * eps);
    }
    return res;
  }

}
