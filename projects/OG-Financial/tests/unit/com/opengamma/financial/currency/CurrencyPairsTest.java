/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.money.Currency;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class CurrencyPairsTest {

  private CurrencyPairs _pairs;

  @BeforeClass
  protected void setUp() throws Exception {
    _pairs = new CurrencyPairs(
        ImmutableSet.of(CurrencyPair.of(Currency.EUR, Currency.USD), CurrencyPair.of(Currency.GBP, Currency.USD)));
  }

  @Test
  public void getCurrencyPair() {
    CurrencyPair eurUsd = CurrencyPair.of(Currency.EUR, Currency.USD);
    CurrencyPair gbpUsd = CurrencyPair.of(Currency.GBP, Currency.USD);
    assertEquals(eurUsd, _pairs.getCurrencyPair(Currency.USD, Currency.EUR));
    assertEquals(eurUsd, _pairs.getCurrencyPair(Currency.EUR, Currency.USD));
    assertEquals(gbpUsd, _pairs.getCurrencyPair(Currency.GBP, Currency.USD));
    assertEquals(gbpUsd, _pairs.getCurrencyPair(Currency.USD, Currency.GBP));
  }

  @Test
  public void getRate() {
    assertEquals(160 / 100d, _pairs.getRate(Currency.GBP, 100, Currency.USD, -160));
    assertEquals(160 / 100d, _pairs.getRate(Currency.USD, 160, Currency.GBP, -100));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void zeroAmount1() {
    _pairs.getRate(Currency.GBP, 0, Currency.USD, 100);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void zeroAmount2() {
    _pairs.getRate(Currency.GBP, 100, Currency.USD, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void amountsHaveSameSign() {
    _pairs.getRate(Currency.GBP, 100, Currency.USD, 200);
  }
}
