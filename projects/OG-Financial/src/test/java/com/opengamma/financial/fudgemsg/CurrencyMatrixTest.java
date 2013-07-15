/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;
import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.currency.AbstractCurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixValue;
import com.opengamma.financial.currency.SimpleCurrencyMatrix;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyMatrixTest extends FinancialTestBase {

  private static void assertMatrixEqual(final CurrencyMatrix expected, final CurrencyMatrix actual) {
    final Collection<Currency> expectedSourceCurrencies = expected.getSourceCurrencies();
    final Collection<Currency> expectedTargetCurrencies = expected.getTargetCurrencies();
    final Collection<Currency> actualSourceCurrencies = actual.getSourceCurrencies();
    final Collection<Currency> actualTargetCurrencies = actual.getTargetCurrencies();
    assertEquals(expectedSourceCurrencies.size(), actualSourceCurrencies.size());
    for (Currency source : actualSourceCurrencies) {
      assertEquals(true, expectedSourceCurrencies.contains(source));
    }
    assertEquals(expectedTargetCurrencies.size(), actualTargetCurrencies.size());
    for (Currency target : actualTargetCurrencies) {
      assertEquals(true, expectedTargetCurrencies.contains(target));
    }
    for (Currency source : expectedSourceCurrencies) {
      for (Currency target : expectedTargetCurrencies) {
        assertEquals(expected.getConversion(source, target), actual.getConversion(source, target));
      }
    }
  }

  @Test
  public void testEmptyMatrix() {
    final SimpleCurrencyMatrix empty = new SimpleCurrencyMatrix ();
    assertMatrixEqual(empty, cycleGenericObject(CurrencyMatrix.class, empty));
  }

  @Test
  public void testSymmetricalMatrix() {
    final SimpleCurrencyMatrix simple = new SimpleCurrencyMatrix();
    simple.setLiveData(Currency.USD, Currency.GBP,
        new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER.getName(), "GBP Curncy")));
    simple.setFixedConversion(Currency.GBP, Currency.EUR, 0.9);
    simple.setCrossConversion(Currency.USD, Currency.EUR, Currency.GBP);
    simple.setFixedConversion(Currency.EUR, Currency.CHF, 10.0);
    assertMatrixEqual(simple, cycleGenericObject(CurrencyMatrix.class, simple));
  }

  private static class RandomMatrix extends AbstractCurrencyMatrix {

    private static CurrencyMatrixValue randomValue(final Random r) {
      switch (r.nextInt(4)) {
        case 1:
          return CurrencyMatrixValue.of(r.nextDouble());
        case 2:
          return CurrencyMatrixValue.of(Currency.of("AA" + (char) ('A' + r.nextInt(('Z' - 'A') + 1))));
        case 3:
          return CurrencyMatrixValue.of(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalId.of("Test", "" + r.nextLong())));
      }
      return null;
    }

    public RandomMatrix() {
      final Random r = new Random();
      for (char source = 'A'; source < 'Z'; source++) {
        final Currency sourceCurrency = Currency.of("AA" + source);
        for (char target = (char) (source + 1); target <= 'Z'; target++) {
          final Currency targetCurrency = Currency.of("AA" + target);
          CurrencyMatrixValue value = randomValue(r);
          if (value != null) {
            addConversion(sourceCurrency, targetCurrency, value);
            if (r.nextBoolean()) {
              addConversion(targetCurrency, sourceCurrency, value.getReciprocal());
            } else {
              value = randomValue(r);
              if (value != null) {
                addConversion(targetCurrency, sourceCurrency, value);
              }
            }
          } else {
            value = randomValue(r);
            if (value != null) {
              addConversion(targetCurrency, sourceCurrency, value);
            }
          }
        }
      }
    }

  }

  @Test
  public void testRandomMatrix() {
    final RandomMatrix random = new RandomMatrix();
    assertMatrixEqual(random, cycleGenericObject(CurrencyMatrix.class, random));
  }

}
