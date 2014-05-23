/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests related to the construction of volatility sensitivity data bundle.
 */
@Test(groups = TestGroup.UNIT)
public class PresentValueVolatilitySensitivityDataBundleTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.EUR;
  private static final Currency CUR_3 = Currency.GBP;

  @Test
  /**
   * Tests the currency pair and map getters.
   */
  public void getter() {
    SurfaceValue vega = new SurfaceValue();
    final double exp = 1.0;
    final double strike = 0.05;
    final double value = 10000;
    final DoublesPair point = DoublesPair.of(exp, strike);
    vega.add(point, value);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(CUR_1, CUR_2, SurfaceValue.from(point, value));
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
    final DoublesPair point = DoublesPair.of(exp, strike);
    SurfaceValue vega = new SurfaceValue();
    vega.add(point, value);
    final PresentValueForexBlackVolatilitySensitivity sensitivities = new PresentValueForexBlackVolatilitySensitivity(CUR_1, CUR_2, vega);
    final SurfaceValue copy = new SurfaceValue();
    for (final Map.Entry<DoublesPair, Double> entry : vega.getMap().entrySet()) {
      copy.add(entry.getKey(), entry.getValue());
    }
    assertTrue(sensitivities.equals(new PresentValueForexBlackVolatilitySensitivity(CUR_1, CUR_2, copy)));
    vega = new SurfaceValue();
    final SurfaceValue newVega = new SurfaceValue();
    newVega.add(point, value / 2.0);
    PresentValueForexBlackVolatilitySensitivity other = new PresentValueForexBlackVolatilitySensitivity(CUR_1, CUR_2, newVega);
    other.add(point, value / 2.0);
    assertTrue(other.equals(sensitivities));
    assertEquals(other.hashCode(), sensitivities.hashCode());
    PresentValueForexBlackVolatilitySensitivity modified;
    modified = new PresentValueForexBlackVolatilitySensitivity(CUR_1, CUR_3, newVega);
    assertFalse(modified.equals(sensitivities));
    vega.add(point, value + 1);
    modified = new PresentValueForexBlackVolatilitySensitivity(CUR_1, CUR_2, newVega);
    assertFalse(modified.equals(sensitivities));
    assertFalse(modified.equals(CUR_1));
    assertFalse(modified.equals(null));
  }

  @Test
  /**
   * The sensitivity is added twice to the same point and it is checked that the value is twice the initial value.
   */
  public void addSamePoint() {
    SurfaceValue vega = new SurfaceValue();
    final double exp = 1.0;
    final double strike = 0.05;
    final double value = 10000;
    final DoublesPair point = DoublesPair.of(exp, strike);
    vega.add(point, 2 * value);
    final PresentValueForexBlackVolatilitySensitivity first = new PresentValueForexBlackVolatilitySensitivity(CUR_1, CUR_2, vega);
    vega = new SurfaceValue();
    vega.add(point, value);
    PresentValueForexBlackVolatilitySensitivity second = new PresentValueForexBlackVolatilitySensitivity(CUR_1, CUR_2, vega);
    second.add(point, value);
    assertEquals(first, second);
  }

}
