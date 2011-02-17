/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Random;

import org.junit.Test;

import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.currency.AbstractCurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixValue;
import com.opengamma.financial.currency.SimpleCurrencyMatrix;
import com.opengamma.id.UniqueIdentifier;

public class CurrencyMatrixTest extends FinancialTestBase {

  private static void assertMatrixEqual(final CurrencyMatrix expected, final CurrencyMatrix actual) {
    final Collection<CurrencyUnit> expectedSourceCurrencies = expected.getSourceCurrencies();
    final Collection<CurrencyUnit> expectedTargetCurrencies = expected.getTargetCurrencies();
    final Collection<CurrencyUnit> actualSourceCurrencies = actual.getSourceCurrencies();
    final Collection<CurrencyUnit> actualTargetCurrencies = actual.getTargetCurrencies();
    assertEquals(expectedSourceCurrencies.size(), actualSourceCurrencies.size());
    for (CurrencyUnit source : actualSourceCurrencies) {
      assertEquals(true, expectedSourceCurrencies.contains(source));
    }
    assertEquals(expectedTargetCurrencies.size(), actualTargetCurrencies.size());
    for (CurrencyUnit target : actualTargetCurrencies) {
      assertEquals(true, expectedTargetCurrencies.contains(target));
    }
    for (CurrencyUnit source : expectedSourceCurrencies) {
      for (CurrencyUnit target : expectedTargetCurrencies) {
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
    simple.setLiveData(CurrencyUnit.USD, CurrencyUnit.GBP, UniqueIdentifier.of(SecurityUtils.BLOOMBERG_TICKER.getName(), "GBP Curncy"));
    simple.setFixedConversion(CurrencyUnit.GBP, CurrencyUnit.EUR, 0.9);
    simple.setCrossConversion(CurrencyUnit.USD, CurrencyUnit.EUR, CurrencyUnit.GBP);
    simple.setFixedConversion(CurrencyUnit.EUR, CurrencyUnit.CHF, 10.0);
    assertMatrixEqual(simple, cycleGenericObject(CurrencyMatrix.class, simple));
  }

  private static class RandomMatrix extends AbstractCurrencyMatrix {

    private static CurrencyMatrixValue randomValue(final Random r) {
      switch (r.nextInt(4)) {
        case 1:
          return CurrencyMatrixValue.of(r.nextDouble());
        case 2:
          return CurrencyMatrixValue.of(CurrencyUnit.of("AA" + (char) ('A' + r.nextInt(('Z' - 'A') + 1))));
        case 3:
          return CurrencyMatrixValue.of(UniqueIdentifier.of("Test", "" + r.nextLong()));
      }
      return null;
    }

    public RandomMatrix() {
      final Random r = new Random();
      for (char source = 'A'; source < 'Z'; source++) {
        final CurrencyUnit sourceCurrency = CurrencyUnit.of("AA" + source);
        for (char target = (char) (source + 1); target <= 'Z'; target++) {
          final CurrencyUnit targetCurrency = CurrencyUnit.of("AA" + target);
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
