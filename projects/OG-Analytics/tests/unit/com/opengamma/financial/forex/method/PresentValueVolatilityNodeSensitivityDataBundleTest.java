/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

public class PresentValueVolatilityNodeSensitivityDataBundleTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final Currency CUR_3 = Currency.GBP;
  private static final int NB_EXPIRY = 4;
  private static final int NB_STRIKE = 5;

  @Test
  /**
   * Tests the currency pair and matrix getters.
   */
  public void getter() {
    Pair<Currency, Currency> pair = ObjectsPair.of(CUR_1, CUR_2);
    DoubleMatrix2D vega = new DoubleMatrix2D(NB_EXPIRY, NB_STRIKE);
    vega.getData()[2][3] = 12345.67;
    PresentValueVolatilityNodeSensitivityDataBundle nodeSensi = new PresentValueVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, vega);
    assertEquals("Currency pair", pair, nodeSensi.getCurrencyPair());
    assertEquals("Vega", vega, nodeSensi.getVega());
  }

  @Test
  /**
   * Tests the constructors.
   */
  public void constructor() {
    DoubleMatrix2D vega = new DoubleMatrix2D(NB_EXPIRY, NB_STRIKE);
    PresentValueVolatilityNodeSensitivityDataBundle sensi1 = new PresentValueVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, vega);
    PresentValueVolatilityNodeSensitivityDataBundle sensi2 = new PresentValueVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, NB_EXPIRY, NB_STRIKE);
    assertTrue(sensi1.equals(sensi2));
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    DoubleMatrix2D vega = new DoubleMatrix2D(NB_EXPIRY, NB_STRIKE);
    vega.getData()[2][3] = 12345.67;
    PresentValueVolatilityNodeSensitivityDataBundle sensi = new PresentValueVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, vega);
    assertTrue(sensi.equals(sensi));
    PresentValueVolatilityNodeSensitivityDataBundle other = new PresentValueVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, vega);
    assertTrue(other.equals(sensi));
    assertEquals(other.hashCode(), sensi.hashCode());
    PresentValueVolatilityNodeSensitivityDataBundle modified;
    DoubleMatrix2D vegaModified = new DoubleMatrix2D(NB_EXPIRY, NB_STRIKE);
    vegaModified.getData()[2][3] = 76543.21;
    modified = new PresentValueVolatilityNodeSensitivityDataBundle(CUR_1, CUR_2, vegaModified);
    assertFalse(modified.equals(sensi));
    modified = new PresentValueVolatilityNodeSensitivityDataBundle(CUR_1, CUR_3, vega);
    assertFalse(modified.equals(sensi));
    assertFalse(modified.equals(CUR_1));
    assertFalse(modified.equals(null));
  }

}
