/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PresentValueVolatilityNodeSensitivityDataBundleTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.EUR;
  private static final Currency CUR_3 = Currency.GBP;
  private static final int NB_EXPIRY = 4;
  private static final int NB_STRIKE = 5;
  private static final double[] EXPIRIES = new double[NB_EXPIRY];
  private static final double[] STRIKES = new double[NB_STRIKE];
  private static final double[][] VEGA = new double[NB_EXPIRY][NB_STRIKE];

  static {
    for (int i = 0; i < NB_EXPIRY; i++) {
      EXPIRIES[i] = i;
      for (int j = 0; j < NB_STRIKE; j++) {
        if (j == 0) {
          STRIKES[j] = 10 * j;
        }
        VEGA[i][j] = Math.random();
      }
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency1() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(null, CUR_2, NB_EXPIRY, NB_STRIKE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency2() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, null, NB_EXPIRY, NB_STRIKE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeExpiries() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, -NB_STRIKE, NB_EXPIRY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeStrikes() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, NB_EXPIRY, -NB_STRIKE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency3() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(null, CUR_2, new DoubleMatrix1D(EXPIRIES), new DoubleMatrix1D(STRIKES), new DoubleMatrix2D(VEGA));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency4() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, null, new DoubleMatrix1D(EXPIRIES), new DoubleMatrix1D(STRIKES), new DoubleMatrix2D(VEGA));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiries() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, null, new DoubleMatrix1D(STRIKES), new DoubleMatrix2D(VEGA));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, new DoubleMatrix1D(EXPIRIES), null, new DoubleMatrix2D(VEGA));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVega() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, new DoubleMatrix1D(EXPIRIES), new DoubleMatrix1D(STRIKES), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongExpiriesNumber() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, new DoubleMatrix1D(new double[] {1, 2}), new DoubleMatrix1D(STRIKES), new DoubleMatrix2D(VEGA));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStrikesNumber() {
    new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, new DoubleMatrix1D(EXPIRIES), new DoubleMatrix1D(new double[] {1, 2}), new DoubleMatrix2D(VEGA));
  }

  @Test
  /**
   * Tests the currency pair and matrix getters.
   */
  public void getter() {
    final Pair<Currency, Currency> pair = ObjectsPair.of(CUR_1, CUR_2);
    final DoubleMatrix1D expiries = new DoubleMatrix1D(EXPIRIES);
    final DoubleMatrix1D strikes = new DoubleMatrix1D(STRIKES);
    final DoubleMatrix2D vega = new DoubleMatrix2D(VEGA);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle nodeSensi = new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, expiries, strikes, vega);
    assertEquals("Currency pair", pair, nodeSensi.getCurrencyPair());
    assertEquals("Expiries", expiries, nodeSensi.getExpiries());
    assertEquals("Strikes", strikes, nodeSensi.getDelta());
    assertEquals("Vega", vega, nodeSensi.getVega());
  }

  @Test
  /**
   * Tests the constructors.
   */
  public void constructor() {
    final DoubleMatrix2D vega = new DoubleMatrix2D(NB_EXPIRY, NB_STRIKE);
    final DoubleMatrix1D expiries = new DoubleMatrix1D(new double[NB_EXPIRY]);
    final DoubleMatrix1D strikes = new DoubleMatrix1D(new double[NB_STRIKE]);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensi1 = new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, expiries, strikes, vega);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensi2 = new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, NB_EXPIRY, NB_STRIKE);
    assertTrue(sensi1.equals(sensi2));
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    final DoubleMatrix1D expiries = new DoubleMatrix1D(EXPIRIES);
    final DoubleMatrix1D strikes = new DoubleMatrix1D(STRIKES);
    final DoubleMatrix2D vega = new DoubleMatrix2D(VEGA);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensi = new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, expiries, strikes, vega);
    assertTrue(sensi.equals(sensi));
    PresentValueForexBlackVolatilityNodeSensitivityDataBundle other = new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, expiries, strikes, vega);
    assertTrue(other.equals(sensi));
    assertEquals(other.hashCode(), sensi.hashCode());
    final DoubleMatrix2D vegaModified = new DoubleMatrix2D(VEGA);
    vegaModified.getData()[0][1] = vegaModified.getData()[0][1] * 1000;
    other = new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, expiries, strikes, vegaModified);
    assertFalse(other.equals(sensi));
    final DoubleMatrix1D expiriesModified = new DoubleMatrix1D(new double[] {5, 6, 7, 8});
    other = new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, expiriesModified, strikes, vega);
    assertFalse(other.equals(sensi));
    final DoubleMatrix1D strikesModified = new DoubleMatrix1D(new double[] {5, 6, 7, 8, 9});
    other = new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, expiries, strikesModified, vega);
    assertFalse(other.equals(sensi));
    other = new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(CUR_1, CUR_3, expiries, strikes, vega);
    assertFalse(other.equals(sensi));
    assertFalse(other.equals(CUR_1));
    assertFalse(other.equals(null));
  }

}
