/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TreeMap;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.cashflow.FixedPaymentMatrix;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class FixedPaymentMatrixTest extends AnalyticsTestBase {

  @Test
  public void cycleObject() {
    final TreeMap<LocalDate, MultipleCurrencyAmount> map = new TreeMap<LocalDate, MultipleCurrencyAmount>();
    map.put(LocalDate.of(2000, 1, 1), MultipleCurrencyAmount.of(CurrencyAmount.of(Currency.USD, 1000)));
    map.put(LocalDate.of(2000, 2, 5), MultipleCurrencyAmount.of(CurrencyAmount.of(Currency.USD, 2000), CurrencyAmount.of(Currency.EUR, 4000)));
    map.put(LocalDate.of(2010, 1, 1), MultipleCurrencyAmount.of(CurrencyAmount.of(Currency.USD, -3000), CurrencyAmount.of(Currency.GBP, 700)));
    final FixedPaymentMatrix matrix = new FixedPaymentMatrix(map);
    assertEquals(matrix, cycleObject(FixedPaymentMatrix.class, matrix));
  }
}
