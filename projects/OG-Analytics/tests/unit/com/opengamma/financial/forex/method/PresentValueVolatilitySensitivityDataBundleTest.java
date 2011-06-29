/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests related to the construction of volatility sensitivity data bundle.
 */
public class PresentValueVolatilitySensitivityDataBundleTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final Currency CUR_3 = Currency.GBP;

  @Test
  /**
   * Tests the currency pair and map getters.
   */
  public void getter() {
    final PresentValueVolatilitySensitivityDataBundle sensi = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2);
    Pair<Currency, Currency> pair = ObjectsPair.of(CUR_1, CUR_2);
    Map<DoublesPair, Double> vega = new HashMap<DoublesPair, Double>();
    assertEquals("Currency pair", pair, sensi.getCurrencyPair());
    assertEquals("Vega empty", vega, sensi.getVega());
    double exp = 1.0;
    double strike = 0.05;
    double value = 10000;
    DoublesPair point = new DoublesPair(exp, strike);
    vega.put(point, value);
    sensi.add(point, value);
    assertEquals("Vega", vega, sensi.getVega());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    double exp = 1.0;
    double strike = 0.05;
    double value = 10000;
    DoublesPair point = new DoublesPair(exp, strike);
    final PresentValueVolatilitySensitivityDataBundle sensi = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2);
    sensi.add(point, value);
    assertTrue(sensi.equals(sensi));
    final PresentValueVolatilitySensitivityDataBundle other = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2);
    other.add(point, value / 2.0);
    other.add(point, value / 2.0);
    assertTrue(other.equals(sensi));
    assertEquals(other.hashCode(), sensi.hashCode());
    PresentValueVolatilitySensitivityDataBundle modified;
    modified = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_3);
    modified.add(point, value);
    assertFalse(modified.equals(sensi));
    modified = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2);
    modified.add(point, value + 1.0);
    assertFalse(modified.equals(sensi));
    assertFalse(modified.equals(CUR_1));
    assertFalse(modified.equals(null));
  }

  @Test
  /**
   * The sensitivity is added twice to the same point and it is checked that the value is twice the initial value.
   */
  public void addSamePoint() {
    final PresentValueVolatilitySensitivityDataBundle sensi = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2);
    Map<DoublesPair, Double> vega = new HashMap<DoublesPair, Double>();
    double exp = 1.0;
    double strike = 0.05;
    double value = 10000;
    DoublesPair point = new DoublesPair(exp, strike);
    vega.put(point, 2 * value);
    sensi.add(point, value);
    sensi.add(point, value);
    assertEquals("Vega", vega, sensi.getVega());
  }

}
