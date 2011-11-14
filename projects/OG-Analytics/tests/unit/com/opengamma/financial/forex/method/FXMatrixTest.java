/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;

/**
 * Tests related to the FXMatrix.
 */
public class FXMatrixTest {

  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final Currency GBP = Currency.GBP;
  private static final Currency KRW = Currency.of("KRW");

  private static final double EUR_USD = 1.40;
  private static final double GBP_EUR = 1.20;
  private static final double USD_KRW = 1123.45;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency1() {
    FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(null, USD, EUR_USD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency2() {
    FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, null, EUR_USD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sameCurrency() {
    FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, EUR, EUR_USD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void currency1NotPresent() {
    FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.getFxRate(GBP, USD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void currency2NotPresent() {
    FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.getFxRate(USD, GBP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void currencyAlreadyPresent() {
    FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.addCurrency(GBP, EUR, GBP_EUR);
    fxMatrix.addCurrency(GBP, USD, 1.234);
  }

  @Test
  public void onePair() {
    FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    assertEquals("FXMatrix - first pair", EUR_USD, fxMatrix.getFxRate(EUR, USD), 1.0E-10);
    assertEquals("FXMatrix - first pair", 1.0 / EUR_USD, fxMatrix.getFxRate(USD, EUR), 1.0E-10);
    assertEquals("FXMatrix - first pair", 1.0, fxMatrix.getFxRate(USD, USD), 1.0E-10); // Is this useful? At least it is correct!
    assertEquals("FXMatrix - first pair", 1.0, fxMatrix.getFxRate(EUR, EUR), 1.0E-10);
  }

  @Test
  public void onePairSecondConstructor() {
    FXMatrix fxMatrix = new FXMatrix(EUR, USD, EUR_USD);
    assertEquals("FXMatrix - first pair", EUR_USD, fxMatrix.getFxRate(EUR, USD), 1.0E-10);
    assertEquals("FXMatrix - first pair", 1.0 / EUR_USD, fxMatrix.getFxRate(USD, EUR), 1.0E-10);
    assertEquals("FXMatrix - first pair", 1.0, fxMatrix.getFxRate(USD, USD), 1.0E-10); // Is this useful? At least it is correct!
    assertEquals("FXMatrix - first pair", 1.0, fxMatrix.getFxRate(EUR, EUR), 1.0E-10);
  }

  @Test
  public void twoPairs() {
    FXMatrix fxMatrix = new FXMatrix();
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
    FXMatrix fxMatrix = new FXMatrix();
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
  /**
   * Check the update of one exchange rate in matrix.
   */
  public void update() {
    FXMatrix fxMatrix = new FXMatrix();
    fxMatrix.addCurrency(EUR, USD, EUR_USD);
    fxMatrix.addCurrency(GBP, EUR, GBP_EUR);
    fxMatrix.addCurrency(KRW, USD, 1.0 / USD_KRW);
    assertEquals("FXMatrix - update", EUR_USD, fxMatrix.getFxRate(EUR, USD), 1.0E-10);
    assertEquals("FXMatrix - update", GBP_EUR, fxMatrix.getFxRate(GBP, EUR), 1.0E-10);
    assertEquals("FXMatrix - update", GBP_EUR * EUR_USD, fxMatrix.getFxRate(GBP, USD), 1.0E-10);
    assertEquals("FXMatrix - update", USD_KRW, fxMatrix.getFxRate(USD, KRW), 1.0E-10);
    double newGBPEUR = 1.10;
    fxMatrix.updateRates(GBP, EUR, newGBPEUR);
    assertEquals("FXMatrix - update", EUR_USD, fxMatrix.getFxRate(EUR, USD), 1.0E-10);
    assertEquals("FXMatrix - update", newGBPEUR, fxMatrix.getFxRate(GBP, EUR), 1.0E-10);
    assertEquals("FXMatrix - update", newGBPEUR * EUR_USD, fxMatrix.getFxRate(GBP, USD), 1.0E-10);
    assertEquals("FXMatrix - update", USD_KRW, fxMatrix.getFxRate(USD, KRW), 1.0E-10);
    assertEquals("FXMatrix - update", 1.0 / EUR_USD, fxMatrix.getFxRate(USD, EUR), 1.0E-10);
    assertEquals("FXMatrix - update", 1.0 / newGBPEUR, fxMatrix.getFxRate(EUR, GBP), 1.0E-10);
    assertEquals("FXMatrix - update", 1.0 / (newGBPEUR * EUR_USD), fxMatrix.getFxRate(USD, GBP), 1.0E-10);
    assertEquals("FXMatrix - update", 1.0 / USD_KRW, fxMatrix.getFxRate(KRW, USD), 1.0E-10);
    assertEquals("FXMatrix - update", newGBPEUR * EUR_USD * USD_KRW, fxMatrix.getFxRate(GBP, KRW), 1.0E-10);
  }

}
