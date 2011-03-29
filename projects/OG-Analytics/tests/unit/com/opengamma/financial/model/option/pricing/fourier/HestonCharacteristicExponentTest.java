/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

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

}
