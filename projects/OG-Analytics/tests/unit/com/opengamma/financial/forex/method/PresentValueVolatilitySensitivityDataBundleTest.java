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
    final Map<DoublesPair, Double> vega = new HashMap<DoublesPair, Double>();
    final double exp = 1.0;
    final double strike = 0.05;
    final double value = 10000;
    final DoublesPair point = new DoublesPair(exp, strike);
    vega.put(point, value);
    final PresentValueVolatilitySensitivityDataBundle sensi = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2, vega);
    final Pair<Currency, Currency> pair = ObjectsPair.of(CUR_1, CUR_2);
    assertEquals("Currency pair", pair, sensi.getCurrencyPair());
    assertEquals("Vega", vega, sensi.getVega());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    final double exp = 1.0;
    final double strike = 0.05;
    final double value = 10000;
    final DoublesPair point = new DoublesPair(exp, strike);
    Map<DoublesPair, Double> vega = new HashMap<DoublesPair, Double>();
    vega.put(point, value);
    final PresentValueVolatilitySensitivityDataBundle sensitivities = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2, vega);
    final Map<DoublesPair, Double> copy = new HashMap<DoublesPair, Double>();
    for (final Map.Entry<DoublesPair, Double> entry : vega.entrySet()) {
      copy.put(entry.getKey(), entry.getValue());
    }
    assertTrue(sensitivities.equals(new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2, copy)));
    vega = new HashMap<DoublesPair, Double>();
    final Map<DoublesPair, Double> newVega = new HashMap<DoublesPair, Double>();
    newVega.put(point, value / 2.0);
    PresentValueVolatilitySensitivityDataBundle other = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2, newVega);
    other = other.add(point, value / 2.0);
    assertTrue(other.equals(sensitivities));
    assertEquals(other.hashCode(), sensitivities.hashCode());
    PresentValueVolatilitySensitivityDataBundle modified;
    modified = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_3, newVega);
    assertFalse(modified.equals(sensitivities));
    vega.put(point, value + 1);
    modified = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2, newVega);
    assertFalse(modified.equals(sensitivities));
    assertFalse(modified.equals(CUR_1));
    assertFalse(modified.equals(null));
  }

  @Test
  /**
   * The sensitivity is added twice to the same point and it is checked that the value is twice the initial value.
   */
  public void addSamePoint() {
    Map<DoublesPair, Double> vega = new HashMap<DoublesPair, Double>();
    final double exp = 1.0;
    final double strike = 0.05;
    final double value = 10000;
    final DoublesPair point = new DoublesPair(exp, strike);
    vega.put(point, 2 * value);
    final PresentValueVolatilitySensitivityDataBundle first = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2, vega);
    vega = new HashMap<DoublesPair, Double>();
    vega.put(point, value);
    PresentValueVolatilitySensitivityDataBundle second = new PresentValueVolatilitySensitivityDataBundle(CUR_1, CUR_2, vega);
    second = second.add(point, value);
    assertEquals(first, second);
    //assertEquals("Vega", vega, sensi.getVega());
  }

}
