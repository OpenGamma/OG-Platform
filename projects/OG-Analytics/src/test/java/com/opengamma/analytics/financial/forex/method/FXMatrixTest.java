/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the FXMatrix.
 */
@Test(groups = TestGroup.UNIT)
public class FXMatrixTest {

  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final Currency GBP = Currency.GBP;
  private static final Currency KRW = Currency.of("KRW");

  private static final double EUR_USD = 1.40;
  private static final double GBP_EUR = 1.20;
  private static final double USD_KRW = 1123.45;

  private static final double TOLERANCE_RATE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrencyConstructor1() {
    new FXMatrix((Currency) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency1() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(null, USD, EUR_USD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency2() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, null, EUR_USD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sameCurrency() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, EUR, EUR_USD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void currency1NotPresent() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.getFxRate(GBP, USD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void currency2NotPresent() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.getFxRate(USD, GBP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void currencyAlreadyPresent() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.addCurrency(GBP, EUR, GBP_EUR);
    fxMatrix.addCurrency(GBP, USD, 1.234);
  }

  @Test
  public void onePair() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    assertEquals("FXMatrix - first pair", EUR_USD, fxMatrix.getFxRate(EUR, USD), TOLERANCE_RATE);
    assertEquals("FXMatrix - first pair", 1.0 / EUR_USD, fxMatrix.getFxRate(USD, EUR), TOLERANCE_RATE);
    assertEquals("FXMatrix - first pair", 1.0, fxMatrix.getFxRate(USD, USD), TOLERANCE_RATE); // Is this useful? At least it is correct!
    assertEquals("FXMatrix - first pair", 1.0, fxMatrix.getFxRate(EUR, EUR), TOLERANCE_RATE);
  }

  @Test
  public void onePairSecondConstructor() {
    final FXMatrix fxMatrix = new FXMatrix(EUR, USD, EUR_USD);
    assertEquals("FXMatrix - first pair", EUR_USD, fxMatrix.getFxRate(EUR, USD), TOLERANCE_RATE);
    assertEquals("FXMatrix - first pair", 1.0 / EUR_USD, fxMatrix.getFxRate(USD, EUR), TOLERANCE_RATE);
    assertEquals("FXMatrix - first pair", 1.0, fxMatrix.getFxRate(USD, USD), TOLERANCE_RATE); // Is this useful? At least it is correct!
    assertEquals("FXMatrix - first pair", 1.0, fxMatrix.getFxRate(EUR, EUR), TOLERANCE_RATE);
  }

  @Test
  public void onePairThirdConstructor() {
    final FXMatrix fxMatrix = new FXMatrix(USD);
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    assertEquals("FXMatrix - first pair", EUR_USD, fxMatrix.getFxRate(EUR, USD), TOLERANCE_RATE);
    assertEquals("FXMatrix - first pair", 1.0 / EUR_USD, fxMatrix.getFxRate(USD, EUR), TOLERANCE_RATE);
    assertEquals("FXMatrix - first pair", 1.0, fxMatrix.getFxRate(USD, USD), TOLERANCE_RATE);
    assertEquals("FXMatrix - first pair", 1.0, fxMatrix.getFxRate(EUR, EUR), TOLERANCE_RATE);
  }

  @Test
  public void twoPairs() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.addCurrency(GBP, EUR, GBP_EUR);
    assertEquals("FXMatrix - two pairs", EUR_USD, fxMatrix.getFxRate(EUR, USD), 1.0E-10);
    assertEquals("FXMatrix - two pairs", GBP_EUR, fxMatrix.getFxRate(GBP, EUR), 1.0E-10);
    assertEquals("FXMatrix - two pairs", GBP_EUR * EUR_USD, fxMatrix.getFxRate(GBP, USD), 1.0E-10);
    assertEquals("FXMatrix - two pairs", 1.0 / EUR_USD, fxMatrix.getFxRate(USD, EUR), 1.0E-10);
    assertEquals("FXMatrix - two pairs", 1.0 / GBP_EUR, fxMatrix.getFxRate(EUR, GBP), 1.0E-10);
    assertEquals("FXMatrix - two pairs", 1.0 / (GBP_EUR * EUR_USD), fxMatrix.getFxRate(USD, GBP), 1.0E-10);
    assertEquals("FXMatrix - two pairs", 1.0, fxMatrix.getFxRate(GBP, GBP), 1.0E-10); // Is this useful? At least it is correct!
  }

  @Test
  public void threePairs() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.addCurrency(GBP, EUR, GBP_EUR);
    fxMatrix.addCurrency(KRW, USD, 1.0 / USD_KRW);
    assertEquals("FXMatrix - three pairs", EUR_USD, fxMatrix.getFxRate(EUR, USD), 1.0E-10);
    assertEquals("FXMatrix - three pairs", GBP_EUR, fxMatrix.getFxRate(GBP, EUR), 1.0E-10);
    assertEquals("FXMatrix - three pairs", GBP_EUR * EUR_USD, fxMatrix.getFxRate(GBP, USD), 1.0E-10);
    assertEquals("FXMatrix - three pairs", USD_KRW, fxMatrix.getFxRate(USD, KRW), 1.0E-10);
    assertEquals("FXMatrix - three pairs", 1.0 / EUR_USD, fxMatrix.getFxRate(USD, EUR), 1.0E-10);
    assertEquals("FXMatrix - three pairs", 1.0 / GBP_EUR, fxMatrix.getFxRate(EUR, GBP), 1.0E-10);
    assertEquals("FXMatrix - three pairs", 1.0 / (GBP_EUR * EUR_USD), fxMatrix.getFxRate(USD, GBP), 1.0E-10);
    assertEquals("FXMatrix - three pairs", 1.0 / USD_KRW, fxMatrix.getFxRate(KRW, USD), 1.0E-10);
    assertEquals("FXMatrix - three pairs", GBP_EUR * EUR_USD * USD_KRW, fxMatrix.getFxRate(GBP, KRW), 1.0E-10);
  }

  @Test
  public void constructorFromExisitingFxMatrix() {
    final FXMatrix fxMatrix1 = new FXMatrix(EUR, USD, EUR_USD);
    final FXMatrix fxMatrix2 = new FXMatrix(fxMatrix1);
    assertEquals("FXMatrix - constructor", fxMatrix1, fxMatrix2);
    fxMatrix2.addCurrency(GBP, EUR, GBP_EUR);
    assertFalse("FXMatrix - constructor", fxMatrix1.equals(fxMatrix2));
  }

  @Test
  /**
   * Check the conversion of a multiple currency amount.
   */
  public void convert() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.addCurrency(GBP, EUR, GBP_EUR);
    final double amountGBP = 1.0;
    final double amountEUR = 2.0;
    final double amountUSD = 3.0;
    MultipleCurrencyAmount amount = MultipleCurrencyAmount.of(GBP, amountGBP);
    amount = amount.plus(EUR, amountEUR);
    amount = amount.plus(USD, amountUSD);
    final CurrencyAmount totalUSDCalculated = fxMatrix.convert(amount, USD);
    final double totalUSDExpected = amountUSD + amountEUR * EUR_USD + amountGBP * GBP_EUR * EUR_USD;
    assertEquals("FXMatrix - convert", totalUSDExpected, totalUSDCalculated.getAmount(), 1.0E-10);
    assertEquals("FXMatrix - convert", USD, totalUSDCalculated.getCurrency());
  }

  @Test
  /**
   * Check the update of one exchange rate in matrix.
   */
  public void update() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.addCurrency(GBP, EUR, GBP_EUR);
    fxMatrix.addCurrency(KRW, USD, 1.0 / USD_KRW);
    assertEquals("FXMatrix - update", EUR_USD, fxMatrix.getFxRate(EUR, USD), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", GBP_EUR, fxMatrix.getFxRate(GBP, EUR), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", GBP_EUR * EUR_USD, fxMatrix.getFxRate(GBP, USD), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", USD_KRW, fxMatrix.getFxRate(USD, KRW), TOLERANCE_RATE);
    final double newGBPEUR = 1.10;
    fxMatrix.updateRates(GBP, EUR, newGBPEUR);
    assertEquals("FXMatrix - update", EUR_USD, fxMatrix.getFxRate(EUR, USD), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", newGBPEUR, fxMatrix.getFxRate(GBP, EUR), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", newGBPEUR * EUR_USD, fxMatrix.getFxRate(GBP, USD), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", USD_KRW, fxMatrix.getFxRate(USD, KRW), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", 1.0 / EUR_USD, fxMatrix.getFxRate(USD, EUR), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", 1.0 / newGBPEUR, fxMatrix.getFxRate(EUR, GBP), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", 1.0 / (newGBPEUR * EUR_USD), fxMatrix.getFxRate(USD, GBP), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", 1.0 / USD_KRW, fxMatrix.getFxRate(KRW, USD), TOLERANCE_RATE);
    assertEquals("FXMatrix - update", newGBPEUR * EUR_USD * USD_KRW, fxMatrix.getFxRate(GBP, KRW), TOLERANCE_RATE);
  }

  @Test
  public void testHashCodeEquals() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.addCurrency(GBP, EUR, GBP_EUR);
    fxMatrix.addCurrency(KRW, USD, 1.0 / USD_KRW);
    FXMatrix other = new FXMatrix();
    other.addCurrency(EUR, USD, EUR_USD);
    other.addCurrency(GBP, EUR, GBP_EUR);
    other.addCurrency(KRW, USD, 1.0 / USD_KRW);
    assertEquals(fxMatrix, other);
    assertEquals(fxMatrix.hashCode(), other.hashCode());
    other = new FXMatrix();
    other.addCurrency(EUR, USD, EUR_USD);
    other.addCurrency(GBP, EUR, GBP_EUR);
    assertFalse(fxMatrix.equals(other));
  }

  @Test
  /**
   * Tests that a matrix merged with itself is equal with itself.
   */
  public void mergeWithItself() {
    final FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.addCurrency(GBP, EUR, GBP_EUR);
    final FXMatrix merged = FXMatrixUtils.merge(fxMatrix, fxMatrix);
    assertEquals("FXMatrixUtils - merge", fxMatrix, merged);
  }

  @Test
  /**
   * Tests that a matrix constructed by adding currencies one by one or by merging by blocks returns the same result.
   */
  public void merge1() {
    final FXMatrix fxMatrixEURUSD = new FXMatrix();
    fxMatrixEURUSD.addCurrency(EUR, USD, EUR_USD);
    //    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(fxMatrixEURUSD, FXMatrixUtils.merge(fxMatrixEURUSD, new FXMatrix()), TOLERANCE_RATE));
    //    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(fxMatrixEURUSD, FXMatrixUtils.merge(new FXMatrix(), fxMatrixEURUSD), TOLERANCE_RATE));
    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(fxMatrixEURUSD, FXMatrixUtils.merge(fxMatrixEURUSD, new FXMatrix(USD)), TOLERANCE_RATE));
    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(fxMatrixEURUSD, FXMatrixUtils.merge(fxMatrixEURUSD, new FXMatrix(EUR)), TOLERANCE_RATE));
    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(fxMatrixEURUSD, FXMatrixUtils.merge(new FXMatrix(USD), fxMatrixEURUSD), TOLERANCE_RATE));
    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(fxMatrixEURUSD, FXMatrixUtils.merge(new FXMatrix(EUR), fxMatrixEURUSD), TOLERANCE_RATE));
    final FXMatrix fxMatrixGBPEUR = new FXMatrix();
    fxMatrixGBPEUR.addCurrency(GBP, EUR, GBP_EUR);
    final FXMatrix fxMatrixEURUSDGBP = new FXMatrix();
    fxMatrixEURUSDGBP.addCurrency(EUR, USD, EUR_USD);
    fxMatrixEURUSDGBP.addCurrency(GBP, EUR, GBP_EUR);
    final FXMatrix merged = FXMatrixUtils.merge(fxMatrixEURUSD, fxMatrixGBPEUR);
    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(merged, fxMatrixEURUSDGBP, TOLERANCE_RATE));
  }

  @Test
  /**
   * Tests that a matrix constructed by adding currencies one by one or by merging by blocks returns the same result.
   */
  public void merge2() {
    final FXMatrix fxMatrix1 = new FXMatrix();
    fxMatrix1.addCurrency(EUR, USD, EUR_USD);
    fxMatrix1.addCurrency(KRW, USD, 1.0 / USD_KRW);
    final FXMatrix fxMatrix2 = new FXMatrix();
    fxMatrix2.addCurrency(EUR, USD, EUR_USD);
    fxMatrix2.addCurrency(GBP, EUR, GBP_EUR);
    final FXMatrix fxMatrixMergeExpected = new FXMatrix();
    fxMatrixMergeExpected.addCurrency(EUR, USD, EUR_USD);
    fxMatrixMergeExpected.addCurrency(GBP, EUR, GBP_EUR);
    fxMatrixMergeExpected.addCurrency(KRW, USD, 1.0 / USD_KRW);
    final FXMatrix merged = FXMatrixUtils.merge(fxMatrix1, fxMatrix2);
    assertEquals("FXMatrixUtils - merge", merged.getFxRate(USD, EUR), fxMatrixMergeExpected.getFxRate(USD, EUR), TOLERANCE_RATE);
    assertEquals("FXMatrixUtils - merge", merged.getFxRate(USD, GBP), fxMatrixMergeExpected.getFxRate(USD, GBP), TOLERANCE_RATE);
    assertEquals("FXMatrixUtils - merge", merged.getFxRate(USD, KRW), fxMatrixMergeExpected.getFxRate(USD, KRW), TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the merge with an empty matrix.
   */
  public void merge3() {
    final FXMatrix fxMatrix1 = new FXMatrix();
    fxMatrix1.addCurrency(EUR, USD, EUR_USD);
    fxMatrix1.addCurrency(KRW, USD, 1.0 / USD_KRW);
    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(fxMatrix1, FXMatrixUtils.merge(fxMatrix1, new FXMatrix()), TOLERANCE_RATE));
    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(fxMatrix1, FXMatrixUtils.merge(new FXMatrix(), fxMatrix1), TOLERANCE_RATE));
    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(fxMatrix1, FXMatrixUtils.merge(new FXMatrix(USD), fxMatrix1), TOLERANCE_RATE));
    assertTrue("FXMatrixUtils - merge", FXMatrixUtils.compare(new FXMatrix(), FXMatrixUtils.merge(new FXMatrix(), new FXMatrix()), TOLERANCE_RATE));
  }

  @Test
  /**
   * Tests the comparison tool.
   */
  public void compare() {
    // Matrix with itself
    final FXMatrix fxMatrix1 = new FXMatrix();
    fxMatrix1.addCurrency(GBP, EUR, GBP_EUR);
    fxMatrix1.addCurrency(USD, EUR, 1.0d / EUR_USD);
    assertTrue("FXMatrixUtils - compare", FXMatrixUtils.compare(fxMatrix1, fxMatrix1, TOLERANCE_RATE));
    // Matrix in a different order
    final FXMatrix fxMatrix2 = new FXMatrix();
    fxMatrix2.addCurrency(EUR, USD, EUR_USD);
    fxMatrix2.addCurrency(GBP, EUR, GBP_EUR);
    assertTrue("FXMatrixUtils - compare", FXMatrixUtils.compare(fxMatrix1, fxMatrix2, TOLERANCE_RATE));
    // Matrix with different order rate
    final FXMatrix fxMatrix3 = new FXMatrix();
    fxMatrix3.addCurrency(EUR, USD, EUR_USD + 1.0E-5);
    fxMatrix3.addCurrency(GBP, EUR, GBP_EUR);
    assertFalse("FXMatrixUtils - compare", FXMatrixUtils.compare(fxMatrix1, fxMatrix3, TOLERANCE_RATE));
    // Matrix with different currencies
    final FXMatrix fxMatrix4 = new FXMatrix();
    fxMatrix4.addCurrency(GBP, EUR, GBP_EUR);
    fxMatrix4.addCurrency(USD, EUR, 1.0d / EUR_USD);
    fxMatrix4.addCurrency(KRW, USD, 1.0 / USD_KRW);
    assertFalse("FXMatrixUtils - compare", FXMatrixUtils.compare(fxMatrix1, fxMatrix4, TOLERANCE_RATE));
  }

}
