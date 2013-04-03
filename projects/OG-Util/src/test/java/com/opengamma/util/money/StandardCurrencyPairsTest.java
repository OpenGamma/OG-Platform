/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test Currency.
 */
@Test(groups = TestGroup.UNIT)
public class StandardCurrencyPairsTest {

  public void testCases() {
    assertTrue(StandardCurrencyPairs.isStandardPair(Currency.EUR, Currency.USD));
    assertFalse(StandardCurrencyPairs.isStandardPair(Currency.USD, Currency.EUR));
    assertTrue(StandardCurrencyPairs.isSingleCurrencyNumerator(Currency.EUR));
    assertFalse(StandardCurrencyPairs.isSingleCurrencyNumerator(Currency.CAD));
  }

}
