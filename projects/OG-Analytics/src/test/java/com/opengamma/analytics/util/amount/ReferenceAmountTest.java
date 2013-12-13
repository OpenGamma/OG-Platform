/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.amount;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ReferenceAmountTest {

  private static final double TOLERANCE = 1.0E-10;

  private static final String STR1 = "Name 1";
  private static final String STR2 = "Name 2";
  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final Pair<String, Currency> STR1_USD = Pairs.of(STR1, USD);
  private static final Pair<String, Currency> STR1_EUR = Pairs.of(STR1, EUR);
  private static final Pair<String, Currency> STR2_USD = Pairs.of(STR2, USD);
  private static final Pair<String, Currency> STR2_EUR = Pairs.of(STR2, EUR);

  @Test
  public void constructor() {
    final ReferenceAmount<Pair<String, Currency>> surf0 = new ReferenceAmount<>();
    assertEquals("ReferenceAmount - constructor", 0, surf0.getMap().size());
  }

  @Test
  public void plusAdd() {
    final double value1 = 2345.678;
    final ReferenceAmount<Pair<String, Currency>> surf1 = new ReferenceAmount<>();
    surf1.add(STR1_USD, value1);
    assertEquals("ReferenceAmount - add", 1, surf1.getMap().size());
    final double value2 = 10 * Math.E;
    final ReferenceAmount<Pair<String, Currency>> surf2 = new ReferenceAmount<>();
    surf2.add(STR2_EUR, value2);
    final ReferenceAmount<Pair<String, Currency>> surf3 = surf1.plus(surf2);
    assertEquals("ReferenceAmount - plus", 2, surf3.getMap().size());
    assertTrue("ReferenceAmount - plus", surf3.getMap().containsKey(STR1_USD));
    assertTrue("ReferenceAmount - plus", surf3.getMap().containsKey(STR2_EUR));
    assertEquals("ReferenceAmount - plus", value1, surf3.getMap().get(STR1_USD), TOLERANCE);
    assertEquals("ReferenceAmount - plus", value2, surf3.getMap().get(STR2_EUR), TOLERANCE);
    final ReferenceAmount<Pair<String, Currency>> surf4 = surf2.plus(surf1);
    assertEquals("ReferenceAmount - plus", surf3, surf4);
    final ReferenceAmount<Pair<String, Currency>> surf5 = new ReferenceAmount<>();
    final double value3 = 10.01;
    surf5.add(STR2_EUR, value3);
    assertEquals("ReferenceAmount - plus", value2 + value3, surf3.plus(surf5).getMap().get(STR2_EUR), TOLERANCE);
  }

  @Test
  public void multipliedBy() {
    final double value1 = 2345.678;
    final ReferenceAmount<Pair<String, Currency>> surf1 = new ReferenceAmount<>();
    surf1.add(STR1_EUR, value1);
    surf1.add(STR2_USD, value1);
    final double factor = 3;
    final ReferenceAmount<Pair<String, Currency>> surf2 = surf1.multiplyBy(factor);
    final ReferenceAmount<Pair<String, Currency>> surf3 = surf1.plus(surf1).plus(surf1);
    assertEquals("ReferenceAmount - multipliedBy", surf2, surf3);
  }

}
