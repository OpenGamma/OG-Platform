package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class PresentValueSABRSensitivityDataBundleTest {

  private static Map<DoublesPair, Double> ALPHA = new HashMap<>();
  private static Map<DoublesPair, Double> BETA = new HashMap<>();
  private static Map<DoublesPair, Double> RHO = new HashMap<>();
  private static Map<DoublesPair, Double> NU = new HashMap<>();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullAlpha() {
    new PresentValueSABRSensitivityDataBundle(null, BETA, RHO, NU);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBeta() {
    new PresentValueSABRSensitivityDataBundle(ALPHA, null, RHO, NU);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullRho() {
    new PresentValueSABRSensitivityDataBundle(ALPHA, BETA, null, NU);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullNu() {
    new PresentValueSABRSensitivityDataBundle(ALPHA, BETA, RHO, null);
  }

  @Test
  public void getter() {
    ALPHA.put(DoublesPair.of(0.5, 5.0), 11.0);
    ALPHA.put(DoublesPair.of(1.5, 5.0), 12.0);
    BETA.put(DoublesPair.of(1.5, 10.0), 1.0);
    RHO.put(DoublesPair.of(0.5, 5.0), 21.0);
    RHO.put(DoublesPair.of(1.5, 5.0), 22.0);
    NU.put(DoublesPair.of(0.5, 5.0), 31.0);
    NU.put(DoublesPair.of(1.5, 5.0), 32.0);
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle(ALPHA, BETA, RHO, NU);
    assertEquals(sensi.getAlpha().getMap(), ALPHA);
    assertEquals(sensi.getBeta().getMap(), BETA);
    assertEquals(sensi.getRho().getMap(), RHO);
    assertEquals(sensi.getNu().getMap(), NU);
  }

  @Test
  public void testAdd() {
    final Map<DoublesPair, Double> alpha = new HashMap<>();
    final Map<DoublesPair, Double> beta = new HashMap<>();
    final Map<DoublesPair, Double> rho = new HashMap<>();
    final Map<DoublesPair, Double> nu = new HashMap<>();
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    alpha.put(DoublesPair.of(0.5, 5.0), 11.0);
    alpha.put(DoublesPair.of(1.5, 5.0), 12.0);
    sensi.addAlpha(DoublesPair.of(0.5, 5.0), 11.0);
    sensi.addAlpha(DoublesPair.of(1.5, 5.0), 12.0);
    assertEquals(sensi.getAlpha().getMap(), alpha);
    beta.put(DoublesPair.of(0.5, 6.0), 27.0);
    sensi.addBeta(DoublesPair.of(0.5, 6.0), 27.0);
    assertEquals(sensi.getBeta().getMap(), beta);
    rho.put(DoublesPair.of(0.5, 5.0), 21.0);
    rho.put(DoublesPair.of(1.5, 5.0), 22.0);
    sensi.addRho(DoublesPair.of(0.5, 5.0), 21.0);
    sensi.addRho(DoublesPair.of(1.5, 5.0), 22.0);
    assertEquals(sensi.getRho().getMap(), rho);
    nu.put(DoublesPair.of(0.5, 5.0), 31.0);
    nu.put(DoublesPair.of(1.5, 5.0), 32.0);
    sensi.addNu(DoublesPair.of(0.5, 5.0), 31.0);
    sensi.addNu(DoublesPair.of(1.5, 5.0), 32.0);
    assertEquals(sensi.getNu().getMap(), nu);
  }

  @Test
  public void testMultiply() {
    final Map<DoublesPair, Double> alpha = new HashMap<>();
    final Map<DoublesPair, Double> rho = new HashMap<>();
    final Map<DoublesPair, Double> nu = new HashMap<>();
    PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    sensi.addAlpha(DoublesPair.of(0.5, 5.0), 11.0);
    sensi.addAlpha(DoublesPair.of(1.5, 5.0), 12.0);
    sensi.addRho(DoublesPair.of(0.5, 5.0), 21.0);
    sensi.addRho(DoublesPair.of(1.5, 5.0), 22.0);
    sensi.addNu(DoublesPair.of(0.5, 5.0), 31.0);
    sensi.addNu(DoublesPair.of(1.5, 5.0), 32.0);
    sensi = sensi.multiplyBy(10.0);
    alpha.put(DoublesPair.of(0.5, 5.0), 110.0);
    alpha.put(DoublesPair.of(1.5, 5.0), 120.0);
    assertEquals(sensi.getAlpha().getMap(), alpha);
    rho.put(DoublesPair.of(0.5, 5.0), 210.0);
    rho.put(DoublesPair.of(1.5, 5.0), 220.0);
    assertEquals(sensi.getRho().getMap(), rho);
    nu.put(DoublesPair.of(0.5, 5.0), 310.0);
    nu.put(DoublesPair.of(1.5, 5.0), 320.0);
    assertEquals(sensi.getNu().getMap(), nu);
  }

  @Test
  /**
   * Tests related to the plus method.
   */
  public void plus() {
    final Map<DoublesPair, Double> alpha1 = new HashMap<>();
    final Map<DoublesPair, Double> beta1 = new HashMap<>();
    final Map<DoublesPair, Double> rho1 = new HashMap<>();
    final Map<DoublesPair, Double> nu1 = new HashMap<>();
    alpha1.put(DoublesPair.of(0.5, 5.0), 11.0);
    alpha1.put(DoublesPair.of(1.5, 5.0), 12.0);
    rho1.put(DoublesPair.of(0.5, 5.0), 21.0);
    rho1.put(DoublesPair.of(1.5, 5.0), 22.0);
    nu1.put(DoublesPair.of(0.5, 5.0), 31.0);
    nu1.put(DoublesPair.of(2.5, 5.0), 32.0);
    final Map<DoublesPair, Double> alpha2 = new HashMap<>();
    final Map<DoublesPair, Double> rho2 = new HashMap<>();
    final Map<DoublesPair, Double> nu2 = new HashMap<>();
    alpha2.put(DoublesPair.of(0.5, 5.0), 11.0);
    alpha2.put(DoublesPair.of(1.5, 5.0), 12.0);
    rho2.put(DoublesPair.of(0.5, 5.0), 21.0);
    rho2.put(DoublesPair.of(1.5, 5.0), 22.0);
    nu2.put(DoublesPair.of(0.5, 5.0), 31.0);
    nu2.put(DoublesPair.of(2.5, 5.0), 32.0);
    final PresentValueSABRSensitivityDataBundle sensi1 = new PresentValueSABRSensitivityDataBundle(alpha1, beta1, rho1, nu1);
    PresentValueSABRSensitivityDataBundle sensi2 = new PresentValueSABRSensitivityDataBundle(alpha2, beta1, rho2, nu2);
    final PresentValueSABRSensitivityDataBundle sensi3 = sensi1.plus(sensi1);
    sensi2 = sensi2.multiplyBy(2.0);
    assertTrue("Adding twice the same sensi", sensi3.equals(sensi2));
    final Map<DoublesPair, Double> alpha3 = new HashMap<>();
    final Map<DoublesPair, Double> rho3 = new HashMap<>();
    final Map<DoublesPair, Double> nu3 = new HashMap<>();
    alpha3.put(DoublesPair.of(2.5, 5.0), 11.0);
    final PresentValueSABRSensitivityDataBundle sensi4 = new PresentValueSABRSensitivityDataBundle(alpha3, beta1, rho3, nu3);
    final Map<DoublesPair, Double> alpha4 = new HashMap<>();
    final Map<DoublesPair, Double> rho4 = new HashMap<>();
    final Map<DoublesPair, Double> nu4 = new HashMap<>();
    alpha4.put(DoublesPair.of(0.5, 5.0), 11.0);
    alpha4.put(DoublesPair.of(1.5, 5.0), 12.0);
    alpha4.put(DoublesPair.of(2.5, 5.0), 11.0);
    rho4.put(DoublesPair.of(0.5, 5.0), 21.0);
    rho4.put(DoublesPair.of(1.5, 5.0), 22.0);
    nu4.put(DoublesPair.of(0.5, 5.0), 31.0);
    nu4.put(DoublesPair.of(2.5, 5.0), 32.0);
    final PresentValueSABRSensitivityDataBundle sensi5 = new PresentValueSABRSensitivityDataBundle(alpha4, beta1, rho4, nu4);
    assertTrue("Adding a single alpha risk", sensi5.equals(sensi1.plus(sensi4)));
  }

  @Test
  public void testHashCodeAndEquals() {
    final PresentValueSABRSensitivityDataBundle data = new PresentValueSABRSensitivityDataBundle(ALPHA, BETA, RHO, NU);
    PresentValueSABRSensitivityDataBundle other = new PresentValueSABRSensitivityDataBundle(ALPHA, BETA, RHO, NU);
    assertEquals(data, other);
    assertEquals(data.hashCode(), other.hashCode());
    other.addNu(DoublesPair.of(1., 2.), 10.);
    assertFalse(data.equals(other));
    final Map<DoublesPair, Double> differentMap = new HashMap<>();
    differentMap.put(DoublesPair.of(123.0, 456), Double.valueOf(12));
    other = new PresentValueSABRSensitivityDataBundle(differentMap, BETA, RHO, NU);
    assertFalse(data.equals(other));
    other = new PresentValueSABRSensitivityDataBundle(ALPHA, differentMap, RHO, NU);
    assertFalse(data.equals(other));
    other = new PresentValueSABRSensitivityDataBundle(ALPHA, BETA, RHO, differentMap);
    assertFalse(data.equals(other));
    other = new PresentValueSABRSensitivityDataBundle(ALPHA, BETA, differentMap, NU);
    assertFalse(data.equals(other));
  }
}
