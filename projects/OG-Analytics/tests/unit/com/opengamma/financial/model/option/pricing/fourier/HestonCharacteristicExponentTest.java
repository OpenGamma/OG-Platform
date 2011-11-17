/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.conjugate;
import static com.opengamma.math.ComplexMathUtils.divide;
import static com.opengamma.math.ComplexMathUtils.multiply;
import static com.opengamma.math.ComplexMathUtils.subtract;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class HestonCharacteristicExponentTest {
  private static final double KAPPA = 0.5;
  private static final double THETA = 0.4;
  private static final double VOL0 = 0.8;
  private static final double OMEGA = 0.66;
  private static final double RHO = -0.45;
  private static final HestonCharacteristicExponent EXPONENT = new HestonCharacteristicExponent(KAPPA, THETA, VOL0, OMEGA, RHO);

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
    double t = 2.5;
    ComplexNumber z = new ComplexNumber(2.3, -0.6);
    ComplexNumber[] fdSense = finiteDifferenceSensitivity(EXPONENT, z, t);
    ComplexNumber[] sense = EXPONENT.getCharacteristicExponentAdjoint(z, t);
    ComplexNumber[] senseConj = EXPONENT.getCharacteristicExponentAdjoint( multiply(-1.0,conjugate(z)), t);
    for (int i = 0; i < 6; i++) {
//      System.out.println(fdSense[i] + "\t" + sense[i] + "\t"+senseConj[i]);
      assertEquals(fdSense[i].getReal(),sense[i].getReal(),1e-9);
      assertEquals(fdSense[i].getImaginary(),sense[i].getImaginary(),1e-9);
      //check symmetry property 
      assertEquals(sense[i].getReal(),senseConj[i].getReal(),1e-9);
      assertEquals(sense[i].getImaginary(),-1.0*senseConj[i].getImaginary(),1e-9);
      
    }
  }

  private ComplexNumber[] finiteDifferenceSensitivity(final HestonCharacteristicExponent heston, ComplexNumber z, double t) {
    double eps = 1e-5;
    ComplexNumber[] res = new ComplexNumber[6];
    res[0] = heston.getFunction(t).evaluate(z);
    //bump kappa
    HestonCharacteristicExponent hestonT = new HestonCharacteristicExponent(heston.getKappa() + eps, heston.getTheta(), heston.getVol0(), heston.getOmega(), heston.getRho());
    ComplexNumber up = hestonT.getFunction(t).evaluate(z);
    hestonT = new HestonCharacteristicExponent(heston.getKappa() - eps, heston.getTheta(), heston.getVol0(), heston.getOmega(), heston.getRho());
    ComplexNumber down = hestonT.getFunction(t).evaluate(z);
    res[1] = divide(subtract(up, down), 2 * eps);
    //bump theta
    hestonT = new HestonCharacteristicExponent(heston.getKappa(), heston.getTheta() + eps, heston.getVol0(), heston.getOmega(), heston.getRho());
    up = hestonT.getFunction(t).evaluate(z);
    hestonT = new HestonCharacteristicExponent(heston.getKappa(), heston.getTheta() - eps, heston.getVol0(), heston.getOmega(), heston.getRho());
    down = hestonT.getFunction(t).evaluate(z);
    res[2] = divide(subtract(up, down), 2 * eps);
    //bump vol0
    hestonT = new HestonCharacteristicExponent(heston.getKappa(), heston.getTheta(), heston.getVol0() + eps, heston.getOmega(), heston.getRho());
    up = hestonT.getFunction(t).evaluate(z);
    hestonT = new HestonCharacteristicExponent(heston.getKappa(), heston.getTheta(), heston.getVol0() - eps, heston.getOmega(), heston.getRho());
    down = hestonT.getFunction(t).evaluate(z);
    res[3] = divide(subtract(up, down), 2 * eps);
    //bump omega
    hestonT = new HestonCharacteristicExponent(heston.getKappa(), heston.getTheta(), heston.getVol0(), heston.getOmega() + eps, heston.getRho());
    up = hestonT.getFunction(t).evaluate(z);
    hestonT = new HestonCharacteristicExponent(heston.getKappa(), heston.getTheta(), heston.getVol0(), heston.getOmega() - eps, heston.getRho());
    down = hestonT.getFunction(t).evaluate(z);
    res[4] = divide(subtract(up, down), 2 * eps);
    //bump rho
    hestonT = new HestonCharacteristicExponent(heston.getKappa(), heston.getTheta(), heston.getVol0(), heston.getOmega(), heston.getRho() + eps);
    up = hestonT.getFunction(t).evaluate(z);
    hestonT = new HestonCharacteristicExponent(heston.getKappa(), heston.getTheta(), heston.getVol0(), heston.getOmega(), heston.getRho() - eps);
    down = hestonT.getFunction(t).evaluate(z);
    res[5] = divide(subtract(up, down), 2 * eps);

    return res;
  }

}
