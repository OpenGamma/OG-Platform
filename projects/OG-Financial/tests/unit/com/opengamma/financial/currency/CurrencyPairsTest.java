/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.money.Currency;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class CurrencyPairsTest {

  private CurrencyPairs _pairs;
  private CurrencyPairsSource _currencyPairsSource;

  @BeforeMethod
  protected void setUp() throws Exception {
    _pairs = new CurrencyPairs(ImmutableSet.of(CurrencyPair.of(Currency.EUR, Currency.USD), CurrencyPair.of(Currency.GBP, Currency.USD)));
    _currencyPairsSource = new CurrencyPairsSource() {
      
      @Override
      public CurrencyPairs getCurrencyPairs(String name) {
        return _pairs;
      }

      @Override
      public CurrencyPair getCurrencyPair(Currency currency1, Currency currency2, String name) {
        return _pairs.getCurrencyPair(currency1, currency2);
      }
    };
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
    assertEquals(160 / 100d, CurrencyUtils.getRate(Currency.GBP, Currency.USD, 100, -160, _currencyPairsSource, null));
    assertEquals(160 / 100d, CurrencyUtils.getRate(Currency.USD, Currency.GBP, 160, -100, _currencyPairsSource, null));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void zeroAmount1() {
    CurrencyUtils.getRate(Currency.GBP, Currency.USD, 0, 100, _currencyPairsSource, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void zeroAmount2() {
    CurrencyUtils.getRate(Currency.GBP, Currency.USD, 100, 0, _currencyPairsSource, null);
  }
}
