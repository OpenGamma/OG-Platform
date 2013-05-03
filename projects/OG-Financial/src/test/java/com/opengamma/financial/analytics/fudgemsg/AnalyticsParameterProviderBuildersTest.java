/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class AnalyticsParameterProviderBuildersTest extends AnalyticsTestBase {

  @Test
  public void testFXMatrix() {
    final Map<Currency, Integer> map = new LinkedHashMap<>();
    final Currency[] currencies = new Currency[] {Currency.AUD, Currency.CAD, Currency.CHF, Currency.FRF, Currency.DEM, Currency.USD, Currency.GBP, Currency.EUR, Currency.HKD, Currency.DKK};
    final double[][] fxRates = new double[10][10];
    for (int i = 0; i < 10; i++) {
      map.put(currencies[i], i);
      for (int j = 0; j < 10; j++) {
        fxRates[i][j] = Math.random();
      }
    }
    final FXMatrix matrix = new FXMatrix(map, fxRates);
    assertEquals(matrix, cycleObject(FXMatrix.class, matrix));
  }
}
