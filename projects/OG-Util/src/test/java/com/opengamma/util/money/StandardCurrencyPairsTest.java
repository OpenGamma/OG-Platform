/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * Test Currency.
 */
@Test
public class StandardCurrencyPairsTest {

  public void testCases() {
    assertTrue(StandardCurrencyPairs.isStandardPair(Currency.EUR, Currency.USD));
    assertFalse(StandardCurrencyPairs.isStandardPair(Currency.USD, Currency.EUR));
    assertTrue(StandardCurrencyPairs.isSingleCurrencyNumerator(Currency.EUR));
    assertFalse(StandardCurrencyPairs.isSingleCurrencyNumerator(Currency.CAD));
  }

}
