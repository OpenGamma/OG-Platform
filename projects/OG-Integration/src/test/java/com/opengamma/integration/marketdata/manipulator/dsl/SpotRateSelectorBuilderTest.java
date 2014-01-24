/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SpotRateSelectorBuilderTest {

  @Test
  public void parseCurrencyPair() {
    assertEquals(CurrencyPair.of(Currency.AUD, Currency.CAD), SpotRateSelectorBuilder.parse("AUDCAD"));
    assertEquals(CurrencyPair.of(Currency.AUD, Currency.CAD), SpotRateSelectorBuilder.parse("AUD/CAD"));
  }
}
