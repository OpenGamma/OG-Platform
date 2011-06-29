/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;

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
