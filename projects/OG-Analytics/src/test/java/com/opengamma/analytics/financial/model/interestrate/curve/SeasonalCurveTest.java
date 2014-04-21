/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the seasonal curve. Usually used in a multiplicative way for PriceIndexCurve construction.
 */
@Test(groups = TestGroup.UNIT)
public class SeasonalCurveTest {

  //TODO: seasonal curve have been modified, so this test have to be modified.
  final static double[] MONTHLY_FACTORS = new double[] {1.0010, 1.0010, 1.0020, 0.9990, 0.9990, 0.9990, 0.9990, 1.0000, 1.0010, 1.0010, 1.005 };
  static double[] STEPS = new double[] {0.0, 1.0 / 12, 2.0 / 12, 3.0 / 12, 4.0 / 12, 5.0 / 12, 6.0 / 12, 7.0 / 12, 8.0 / 12, 9.0 / 12, 10.0 / 12, 11.0 / 12, 12.0 / 12, 13.0 / 12,
      14.0 / 12, 15.0 / 12, 16.0 / 12, 17.0 / 12, 18.0 / 12, 19.0 / 12, 20.0 / 12, 21.0 / 12, 22.0 / 12, 23.0 / 12, 24.0 / 12, 25.0 / 12, 26.0 / 12, 27.0 / 12, 28.0 / 12,
      29.0 / 12, 30.0 / 12, 31.0 / 12, 32.0 / 12, 33.0 / 12, 34.0 / 12, 35.0 / 12, 36.0 / 12, 37.0 / 12, 38.0 / 12, 39.0 / 12, 40.0 / 12, 41.0 / 12, 42.0 / 12, 43.0 / 12,
      44.0 / 12, 45.0 / 12, 46.0 / 12, 47.0 / 12, 48.0 / 12, 49.0 / 12, 50.0 / 12, 51.0 / 12, 52.0 / 12, 53.0 / 12, 54.0 / 12, 55.0 / 12, 56.0 / 12, 57.0 / 12, 58.0 / 12,
      59.0 / 12, 60.0 / 12, 61.0 / 12, 62.0 / 12 };

  final static SeasonalCurve SEASONAL_CURVE = new SeasonalCurve(STEPS, MONTHLY_FACTORS, false);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSteps() {
    new SeasonalCurve(null, MONTHLY_FACTORS, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFactor() {
    new SeasonalCurve(STEPS, null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIncorrectNbFactor() {
    new SeasonalCurve(STEPS, new double[10], false);
  }

  @Test
  public void valuesXYearAppart() {
    double[] time = new double[] {0.25, 1.45 };
    double[] shift = new double[] {1.0, 2.0, 3.0 }; // Shift in years 
    for (int looptime = 0; looptime < time.length; looptime++) {
      for (int loopshift = 0; loopshift < shift.length; loopshift++) {
        assertEquals("Values x years appart", SEASONAL_CURVE.getFunction().evaluate(time[looptime]), SEASONAL_CURVE.getFunction().evaluate(time[looptime] + shift[loopshift]), 1.0E-10);
      }
    }
  }

  @Test
  public void values() {
    double[] factors = new double[12];
    double product = 1;
    for (int looptime = 0; looptime < 11; looptime++) {
      factors[looptime] = MONTHLY_FACTORS[looptime];
      product = product * MONTHLY_FACTORS[looptime];
    }
    factors[11] = 1 / product;
    double[] cumulativeFactors = new double[12];
    cumulativeFactors[0] = 1.0;
    for (int loopmonth = 1; loopmonth < 12; loopmonth++) {
      cumulativeFactors[loopmonth] = cumulativeFactors[loopmonth - 1] * factors[loopmonth - 1];
    }

    for (int looptime = 0; looptime < 12; looptime++) {

      assertEquals("Values x month appart " + looptime + "-" + 1, SEASONAL_CURVE.getFunction().evaluate(looptime / 12.0),
          cumulativeFactors[looptime], 1.0E-10);
    }
  }

  @Test
  public void valuesMonthly() {
    double[] factors = new double[12];
    double product = 1;
    for (int looptime = 0; looptime < 11; looptime++) {
      factors[looptime] = MONTHLY_FACTORS[looptime];
      product = product * MONTHLY_FACTORS[looptime];
    }
    factors[11] = 1 / product;
    for (int looptime = 0; looptime < STEPS.length - 1; looptime++) {
      double x = SEASONAL_CURVE.getFunction().evaluate(looptime / 12.0);
      assertEquals("Values x month appart " + looptime + "-" + 1, SEASONAL_CURVE.getFunction().evaluate(looptime / 12.0) * factors[looptime % 12],
          SEASONAL_CURVE.getFunction().evaluate((looptime + 1) / 12.0), 1.0E-10);

    }
  }

}
