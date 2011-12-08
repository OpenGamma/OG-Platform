package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

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
    assertEquals(sensi.getAlpha().getMap(), ALPHA);
    assertEquals(sensi.getRho().getMap(), RHO);
    assertEquals(sensi.getNu().getMap(), NU);
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
    assertEquals(sensi.getAlpha().getMap(), alpha);
    rho.put(new DoublesPair(0.5, 5.0), 21.0);
    rho.put(new DoublesPair(1.5, 5.0), 22.0);
    sensi.addRho(new DoublesPair(0.5, 5.0), 21.0);
    sensi.addRho(new DoublesPair(1.5, 5.0), 22.0);
    assertEquals(sensi.getRho().getMap(), rho);
    nu.put(new DoublesPair(0.5, 5.0), 31.0);
    nu.put(new DoublesPair(1.5, 5.0), 32.0);
    sensi.addNu(new DoublesPair(0.5, 5.0), 31.0);
    sensi.addNu(new DoublesPair(1.5, 5.0), 32.0);
    assertEquals(sensi.getNu().getMap(), nu);
  }

  @Test
  public void testMultiply() {
    final Map<DoublesPair, Double> alpha = new HashMap<DoublesPair, Double>();
    final Map<DoublesPair, Double> rho = new HashMap<DoublesPair, Double>();
    final Map<DoublesPair, Double> nu = new HashMap<DoublesPair, Double>();
    PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    sensi.addAlpha(new DoublesPair(0.5, 5.0), 11.0);
    sensi.addAlpha(new DoublesPair(1.5, 5.0), 12.0);
    sensi.addRho(new DoublesPair(0.5, 5.0), 21.0);
    sensi.addRho(new DoublesPair(1.5, 5.0), 22.0);
    sensi.addNu(new DoublesPair(0.5, 5.0), 31.0);
    sensi.addNu(new DoublesPair(1.5, 5.0), 32.0);
    sensi = PresentValueSABRSensitivityDataBundle.multiplyBy(sensi, 10.0);
    alpha.put(new DoublesPair(0.5, 5.0), 110.0);
    alpha.put(new DoublesPair(1.5, 5.0), 120.0);
    assertEquals(sensi.getAlpha().getMap(), alpha);
    rho.put(new DoublesPair(0.5, 5.0), 210.0);
    rho.put(new DoublesPair(1.5, 5.0), 220.0);
    assertEquals(sensi.getRho().getMap(), rho);
    nu.put(new DoublesPair(0.5, 5.0), 310.0);
    nu.put(new DoublesPair(1.5, 5.0), 320.0);
    assertEquals(sensi.getNu().getMap(), nu);
  }

  @Test
  /**
   * Tests related to the plus method.
   */
  public void plus() {
    Map<DoublesPair, Double> alpha1 = new HashMap<DoublesPair, Double>();
    Map<DoublesPair, Double> rho1 = new HashMap<DoublesPair, Double>();
    Map<DoublesPair, Double> nu1 = new HashMap<DoublesPair, Double>();
    alpha1.put(new DoublesPair(0.5, 5.0), 11.0);
    alpha1.put(new DoublesPair(1.5, 5.0), 12.0);
    rho1.put(new DoublesPair(0.5, 5.0), 21.0);
    rho1.put(new DoublesPair(1.5, 5.0), 22.0);
    nu1.put(new DoublesPair(0.5, 5.0), 31.0);
    nu1.put(new DoublesPair(2.5, 5.0), 32.0);
    Map<DoublesPair, Double> alpha2 = new HashMap<DoublesPair, Double>();
    Map<DoublesPair, Double> rho2 = new HashMap<DoublesPair, Double>();
    Map<DoublesPair, Double> nu2 = new HashMap<DoublesPair, Double>();
    alpha2.put(new DoublesPair(0.5, 5.0), 11.0);
    alpha2.put(new DoublesPair(1.5, 5.0), 12.0);
    rho2.put(new DoublesPair(0.5, 5.0), 21.0);
    rho2.put(new DoublesPair(1.5, 5.0), 22.0);
    nu2.put(new DoublesPair(0.5, 5.0), 31.0);
    nu2.put(new DoublesPair(2.5, 5.0), 32.0);
    PresentValueSABRSensitivityDataBundle sensi1 = new PresentValueSABRSensitivityDataBundle(alpha1, rho1, nu1);
    PresentValueSABRSensitivityDataBundle sensi2 = new PresentValueSABRSensitivityDataBundle(alpha2, rho2, nu2);
    PresentValueSABRSensitivityDataBundle sensi3 = PresentValueSABRSensitivityDataBundle.plus(sensi1, sensi1);
    sensi2 = PresentValueSABRSensitivityDataBundle.multiplyBy(sensi2, 2.0);
    assertTrue("Adding twice the same sensi", sensi3.equals(sensi2));
    Map<DoublesPair, Double> alpha3 = new HashMap<DoublesPair, Double>();
    Map<DoublesPair, Double> rho3 = new HashMap<DoublesPair, Double>();
    Map<DoublesPair, Double> nu3 = new HashMap<DoublesPair, Double>();
    alpha3.put(new DoublesPair(2.5, 5.0), 11.0);
    PresentValueSABRSensitivityDataBundle sensi4 = new PresentValueSABRSensitivityDataBundle(alpha3, rho3, nu3);
    Map<DoublesPair, Double> alpha4 = new HashMap<DoublesPair, Double>();
    Map<DoublesPair, Double> rho4 = new HashMap<DoublesPair, Double>();
    Map<DoublesPair, Double> nu4 = new HashMap<DoublesPair, Double>();
    alpha4.put(new DoublesPair(0.5, 5.0), 11.0);
    alpha4.put(new DoublesPair(1.5, 5.0), 12.0);
    alpha4.put(new DoublesPair(2.5, 5.0), 11.0);
    rho4.put(new DoublesPair(0.5, 5.0), 21.0);
    rho4.put(new DoublesPair(1.5, 5.0), 22.0);
    nu4.put(new DoublesPair(0.5, 5.0), 31.0);
    nu4.put(new DoublesPair(2.5, 5.0), 32.0);
    PresentValueSABRSensitivityDataBundle sensi5 = new PresentValueSABRSensitivityDataBundle(alpha4, rho4, nu4);
    assertTrue("Adding a single alpha risk", sensi5.equals(PresentValueSABRSensitivityDataBundle.plus(sensi1, sensi4)));
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
