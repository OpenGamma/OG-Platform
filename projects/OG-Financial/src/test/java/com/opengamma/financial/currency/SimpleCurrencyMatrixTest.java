/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleCurrencyMatrixTest {
  
  private final Currency currencyUSD = Currency.of ("USD");
  private final Currency currencyGBP = Currency.of ("GBP");
  private final Currency currencyEUR = Currency.of ("EUR");
  private final Currency currencyCHF = Currency.of ("CHF");
  
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

  @Test(expectedExceptions = IllegalArgumentException.class)
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIllegalCrossConversion1() {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(currencyUSD, currencyEUR, 1.4);
    matrix.setFixedConversion(currencyGBP, currencyEUR, 10.0);
    matrix.setCrossConversion(currencyUSD, currencyCHF, currencyEUR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIllegalCrossConversion2() {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(currencyUSD, currencyGBP, 1.6);
    matrix.setFixedConversion(currencyCHF, currencyEUR, 10.0);
    matrix.setCrossConversion(currencyUSD, currencyCHF, currencyEUR);
  }

  @Test
  public void testLiveData () {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix ();
    matrix.setLiveData(currencyUSD, currencyGBP, new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalId.of("Test", "USD_GBP")));
    CurrencyMatrixValue val = matrix.getConversion(currencyUSD, currencyGBP);
    assertTrue(val instanceof CurrencyMatrixValueRequirement);
    assertFalse(((CurrencyMatrixValueRequirement) val).isReciprocal());
    val = matrix.getConversion(currencyGBP, currencyUSD);
    assertTrue(val instanceof CurrencyMatrixValueRequirement);
    assertTrue(((CurrencyMatrixValueRequirement) val).isReciprocal());
    assertEquals(2, matrix.getSourceCurrencies().size());
    assertEquals(2, matrix.getTargetCurrencies().size());
  }

}
