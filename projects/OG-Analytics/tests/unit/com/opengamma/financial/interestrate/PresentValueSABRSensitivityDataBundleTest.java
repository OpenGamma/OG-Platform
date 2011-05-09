package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.tuple.DoublesPair;

public class PresentValueSABRSensitivityDataBundleTest {

  private static Map<DoublesPair, Double> ALPHA = new HashMap<DoublesPair, Double>();
  private static Map<DoublesPair, Double> RHO = new HashMap<DoublesPair, Double>();
  private static Map<DoublesPair, Double> NU = new HashMap<DoublesPair, Double>();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAlpha() {
    new PresentValueSABRSensitivityDataBundle(null, RHO, NU);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRho() {
    new PresentValueSABRSensitivityDataBundle(ALPHA, null, NU);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNu() {
    new PresentValueSABRSensitivityDataBundle(ALPHA, RHO, null);
  }

  @Test
  public void testGetter() {
    ALPHA.put(new DoublesPair(0.5, 5.0), 11.0);
    ALPHA.put(new DoublesPair(1.5, 5.0), 12.0);
    RHO.put(new DoublesPair(0.5, 5.0), 21.0);
    RHO.put(new DoublesPair(1.5, 5.0), 22.0);
    NU.put(new DoublesPair(0.5, 5.0), 31.0);
    NU.put(new DoublesPair(1.5, 5.0), 32.0);
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle(ALPHA, RHO, NU);
    assertEquals(sensi.getAlpha(), ALPHA);
    assertEquals(sensi.getRho(), RHO);
    assertEquals(sensi.getNu(), NU);
  }

  @Test
  public void testAdd() {
    final Map<DoublesPair, Double> alpha = new HashMap<DoublesPair, Double>();
    final Map<DoublesPair, Double> rho = new HashMap<DoublesPair, Double>();
    final Map<DoublesPair, Double> nu = new HashMap<DoublesPair, Double>();
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    alpha.put(new DoublesPair(0.5, 5.0), 11.0);
    alpha.put(new DoublesPair(1.5, 5.0), 12.0);
    sensi.addAlpha(new DoublesPair(0.5, 5.0), 11.0);
    sensi.addAlpha(new DoublesPair(1.5, 5.0), 12.0);
    assertEquals(sensi.getAlpha(), alpha);
    rho.put(new DoublesPair(0.5, 5.0), 21.0);
    rho.put(new DoublesPair(1.5, 5.0), 22.0);
    sensi.addRho(new DoublesPair(0.5, 5.0), 21.0);
    sensi.addRho(new DoublesPair(1.5, 5.0), 22.0);
    assertEquals(sensi.getRho(), rho);
    nu.put(new DoublesPair(0.5, 5.0), 31.0);
    nu.put(new DoublesPair(1.5, 5.0), 32.0);
    sensi.addNu(new DoublesPair(0.5, 5.0), 31.0);
    sensi.addNu(new DoublesPair(1.5, 5.0), 32.0);
    assertEquals(sensi.getNu(), nu);
  }

  @Test
  public void testMultiply() {
    final Map<DoublesPair, Double> alpha = new HashMap<DoublesPair, Double>();
    final Map<DoublesPair, Double> rho = new HashMap<DoublesPair, Double>();
    final Map<DoublesPair, Double> nu = new HashMap<DoublesPair, Double>();
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    sensi.addAlpha(new DoublesPair(0.5, 5.0), 11.0);
    sensi.addAlpha(new DoublesPair(1.5, 5.0), 12.0);
    sensi.addRho(new DoublesPair(0.5, 5.0), 21.0);
    sensi.addRho(new DoublesPair(1.5, 5.0), 22.0);
    sensi.addNu(new DoublesPair(0.5, 5.0), 31.0);
    sensi.addNu(new DoublesPair(1.5, 5.0), 32.0);
    sensi.multiply(10.0);
    alpha.put(new DoublesPair(0.5, 5.0), 110.0);
    alpha.put(new DoublesPair(1.5, 5.0), 120.0);
    assertEquals(sensi.getAlpha(), alpha);
    rho.put(new DoublesPair(0.5, 5.0), 210.0);
    rho.put(new DoublesPair(1.5, 5.0), 220.0);
    assertEquals(sensi.getRho(), rho);
    nu.put(new DoublesPair(0.5, 5.0), 310.0);
    nu.put(new DoublesPair(1.5, 5.0), 320.0);
    assertEquals(sensi.getNu(), nu);
  }

  @Test
  public void testHashCodeAndEquals() {
    final PresentValueSABRSensitivityDataBundle data = new PresentValueSABRSensitivityDataBundle(ALPHA, NU, RHO);
    PresentValueSABRSensitivityDataBundle other = new PresentValueSABRSensitivityDataBundle(ALPHA, NU, RHO);
    assertEquals(data, other);
    assertEquals(data.hashCode(), other.hashCode());
    other.addNu(DoublesPair.of(1., 2.), 10.);
    assertFalse(data.equals(other));
    other = new PresentValueSABRSensitivityDataBundle(NU, NU, RHO);
    assertFalse(data.equals(other));
    other = new PresentValueSABRSensitivityDataBundle(ALPHA, ALPHA, RHO);
    assertFalse(data.equals(other));
    other = new PresentValueSABRSensitivityDataBundle(ALPHA, NU, NU);
    assertFalse(data.equals(other));
  }
}
