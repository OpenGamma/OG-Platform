/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.SeasonalCurve;

/**
 * Tests related to the seasonal curve. Usually used in a multiplicative way for PriceIndexCurve construction.
 */
public class SeasonalCurveTest {

  final static double REFERENCE_TIME = 0.75;
  final static double[] MONTHLY_FACTORS = new double[] {1.0010, 1.0010, 1.0020, 0.9990, 0.9990, 0.9990, 0.9990, 1.0000, 1.0010, 1.0010, 1.0010};
  final static SeasonalCurve SEASONAL_CURVE = new SeasonalCurve(REFERENCE_TIME, MONTHLY_FACTORS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFactor() {
    new SeasonalCurve(REFERENCE_TIME, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIncorrectNbFactor() {
    new SeasonalCurve(REFERENCE_TIME, new double[10]);
  }

  @Test
  public void valuesXYearAppart() {
    double[] time = new double[] {2.25, 3.45, 0.12};
    double[] shift = new double[] {1.0, 2.0, 5.0}; // Shift in years 
    for (int looptime = 0; looptime < time.length; looptime++) {
      for (int loopshift = 0; loopshift < shift.length; loopshift++) {
        assertEquals("Values x years appart", SEASONAL_CURVE.getYValue(time[looptime]), SEASONAL_CURVE.getYValue(time[looptime] + shift[loopshift]), 1.0E-10);
      }
    }
  }

  @Test
  public void valuesMonthly() {
    int nbTime = 3;
    int[] shift = new int[] {1, 2, 5}; // Shifts in months
    double[] monthlyCumulativeFactors = new double[12];
    monthlyCumulativeFactors[0] = 1.0;
    for (int loopmonth = 1; loopmonth < 12; loopmonth++) {
      monthlyCumulativeFactors[loopmonth] = monthlyCumulativeFactors[loopmonth - 1] * MONTHLY_FACTORS[loopmonth - 1];
    }
    for (int looptime = 0; looptime < nbTime; looptime++) {
      for (int loopshift = 0; loopshift < shift.length; loopshift++) {
        assertEquals("Values x month appart " + looptime + "-" + loopshift, SEASONAL_CURVE.getYValue(REFERENCE_TIME + looptime) * monthlyCumulativeFactors[shift[loopshift]],
            SEASONAL_CURVE.getYValue(REFERENCE_TIME + looptime + shift[loopshift] / 12.0), 1.0E-10);
      }
    }
  }

}
