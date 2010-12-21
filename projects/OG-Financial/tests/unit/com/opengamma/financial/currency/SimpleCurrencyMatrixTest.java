/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixUniqueIdentifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests the SimpleCurrencyMatrix class
 */
public class SimpleCurrencyMatrixTest {
  
  private final Currency currencyUSD = Currency.getInstance ("USD");
  private final Currency currencyGBP = Currency.getInstance ("GBP");
  private final Currency currencyEUR = Currency.getInstance ("EUR");
  private final Currency currencyCHF = Currency.getInstance ("CHF");
  
  @Test
  public void testFixedConversion () {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix ();
    matrix.setFixedConversion (currencyUSD, currencyGBP, 1.6);
    assertEquals (CurrencyMatrixValue.of (1.6), matrix.getConversion(currencyUSD, currencyGBP));
    assertEquals(CurrencyMatrixValue.of(1.0 / 1.6), matrix.getConversion(currencyGBP, currencyUSD));
    assertNull(matrix.getConversion(currencyUSD, currencyEUR));
    assertNull(matrix.getConversion(currencyEUR, currencyUSD));
    assertEquals(2, matrix.getSourceCurrencies().size());
    assertEquals(2, matrix.getTargetCurrencies().size());
  }
  
  @Test
  public void testIdentityInsert() {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    assertEquals(CurrencyMatrixValue.of(1.0), matrix.getConversion(currencyUSD, currencyUSD));
    matrix.setFixedConversion(currencyUSD, currencyUSD, 1.0);
    assertEquals(CurrencyMatrixValue.of(1.0), matrix.getConversion(currencyUSD, currencyUSD));
    assertTrue(matrix.getSourceCurrencies().isEmpty());
    assertTrue(matrix.getTargetCurrencies().isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalIdentityInsert() {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(currencyUSD, currencyUSD, 2.0);
  }

  @Test
  public void testCrossConversion() {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(currencyUSD, currencyEUR, 1.4);
    matrix.setFixedConversion(currencyCHF, currencyEUR, 10.0);
    assertNull(matrix.getConversion(currencyUSD, currencyCHF));
    matrix.setCrossConversion(currencyUSD, currencyCHF, currencyEUR);
    assertEquals(CurrencyMatrixValue.of(currencyEUR), matrix.getConversion(currencyUSD, currencyCHF));
    assertEquals(CurrencyMatrixValue.of(currencyEUR), matrix.getConversion(currencyCHF, currencyUSD));
    assertEquals(3, matrix.getSourceCurrencies().size());
    assertEquals(3, matrix.getTargetCurrencies().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalCrossConversion1() {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(currencyUSD, currencyEUR, 1.4);
    matrix.setFixedConversion(currencyGBP, currencyEUR, 10.0);
    matrix.setCrossConversion(currencyUSD, currencyCHF, currencyEUR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalCrossConversion2() {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(currencyUSD, currencyGBP, 1.6);
    matrix.setFixedConversion(currencyCHF, currencyEUR, 10.0);
    matrix.setCrossConversion(currencyUSD, currencyCHF, currencyEUR);
  }

  @Test
  public void testLiveData () {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix ();
    matrix.setLiveData(currencyUSD, currencyGBP, UniqueIdentifier.of("Test", "USD_GBP"));
    CurrencyMatrixValue val = matrix.getConversion(currencyUSD, currencyGBP);
    assertTrue(val instanceof CurrencyMatrixUniqueIdentifier);
    assertFalse(((CurrencyMatrixUniqueIdentifier) val).isReciprocal());
    val = matrix.getConversion(currencyGBP, currencyUSD);
    assertTrue(val instanceof CurrencyMatrixUniqueIdentifier);
    assertTrue(((CurrencyMatrixUniqueIdentifier) val).isReciprocal());
    assertEquals(2, matrix.getSourceCurrencies().size());
    assertEquals(2, matrix.getTargetCurrencies().size());
  }

}
