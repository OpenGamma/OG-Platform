/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.CreditSpreadTenors;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.analytics.financial.credit.underlyingpool.pricing.UnderlyingPoolDescriptiveStatistics;

/**
 * Tests to verify the calculation of descriptive statistics for an UnderlyingPool object
 */
public class UnderlyingPoolDescriptiveStatisticsTest {

  //--------------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Make sure that all the input arguments have been error checked

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean outputResults = false;

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  private static final CreditSpreadTenors dummyCreditSpreadTenor = CreditSpreadTenors._3Y;
  private static final double q = 0.99;

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Create a pool construction object
  private static final UnderlyingPoolDummyPool pool = new UnderlyingPoolDummyPool();

  // Build the underlying pool
  private static final UnderlyingPool dummyPool = pool.constructPool();

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Create a pool statistics calculator object
  private static final UnderlyingPoolDescriptiveStatistics underlyingPoolStatistics = new UnderlyingPoolDescriptiveStatistics();

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolTotalNotionalPoolField() {

    underlyingPoolStatistics.getUnderlyingPoolTotalNotional(null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolNotionalMeanPoolField() {

    underlyingPoolStatistics.getUnderlyingPoolNotionalMean(null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolRecoveryRateMeanPoolField() {

    underlyingPoolStatistics.getUnderlyingPoolRecoveryRateMean(null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMinPoolField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMinimum(null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMinTenorField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMinimum(dummyPool, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMaxPoolField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMaximum(null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMaxTenorField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMaximum(dummyPool, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMeanPoolField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMean(null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMeanTenorField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMean(dummyPool, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMedianPoolField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMedian(null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMedianTenorField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMedian(dummyPool, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadModePoolField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMode(null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadModeTenorField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMode(dummyPool, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadStandardDeviationPoolField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadStandardDeviation(null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadStandardDeviationTenorField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadStandardDeviation(dummyPool, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadVariancePoolField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadVariance(null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadVarianceTenorField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadVariance(dummyPool, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadSkewnessPoolField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadSkewness(null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadSkewnessTenorField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadSkewness(dummyPool, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadKurtosisPoolField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadKurtosis(null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadKurtosisTenorField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadKurtosis(dummyPool, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadPercentilePoolField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadPercentile(null, dummyCreditSpreadTenor, q);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadPercentileTenorField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadPercentile(dummyPool, null, q);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadPercentileLessThanZeroField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadPercentile(dummyPool, dummyCreditSpreadTenor, -q);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadPercentileGreaterThanOneField() {

    underlyingPoolStatistics.getUnderlyingPoolCreditSpreadPercentile(dummyPool, dummyCreditSpreadTenor, q + 1.0);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test
  public void testUnderlyingPoolDescriptiveStatisticsCalculations() {

    double underlyingPoolTotalNotional = underlyingPoolStatistics.getUnderlyingPoolTotalNotional(dummyPool);
    double underlyingPoolNotionalMean = underlyingPoolStatistics.getUnderlyingPoolNotionalMean(dummyPool);

    double underlyingPoolRecoveryRateMean = underlyingPoolStatistics.getUnderlyingPoolRecoveryRateMean(dummyPool);

    double underlyingPoolCreditSpreadMinimum = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMinimum(dummyPool, dummyCreditSpreadTenor);
    double underlyingPoolCreditSpreadMaximum = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMaximum(dummyPool, dummyCreditSpreadTenor);

    double underlyingPoolCreditSpreadMean = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMean(dummyPool, dummyCreditSpreadTenor);
    double underlyingPoolCreditSpreadMedian = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMedian(dummyPool, dummyCreditSpreadTenor);
    //double underlyingPoolCreditSpreadMode = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMode(dummyPool, dummyCreditSpreadTenor);

    double underlyingPoolCreditSpreadVariance = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadVariance(dummyPool, dummyCreditSpreadTenor);
    double underlyingPoolCreditSpreadStandardDeviation = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadStandardDeviation(dummyPool, dummyCreditSpreadTenor);

    double underlyingPoolCreditSpreadSkewness = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadSkewness(dummyPool, dummyCreditSpreadTenor);
    double underlyingPoolCreditSpreadKurtosis = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadKurtosis(dummyPool, dummyCreditSpreadTenor);

    double underlyingPoolCreditSpreadqPercentile = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadPercentile(dummyPool, dummyCreditSpreadTenor, q);

    if (outputResults) {
      System.out.println("Pool statistics report");

      System.out.println("Number of obligors in pool = " + dummyPool.getNumberOfObligors());

      System.out.println("Total notional of all obligors in pool = " + underlyingPoolTotalNotional);
      System.out.println("Mean notional of all obligors in pool = " + underlyingPoolNotionalMean);

      System.out.println("Average recovery rate of all obligors in pool = " + underlyingPoolRecoveryRateMean * 100.0 + "%");

      System.out.println("Minimum " + dummyCreditSpreadTenor + " credit spread = " + underlyingPoolCreditSpreadMinimum + "bps");
      System.out.println("Maximum " + dummyCreditSpreadTenor + " credit spread = " + underlyingPoolCreditSpreadMaximum + "bps");

      System.out.println("Mean " + dummyCreditSpreadTenor + " credit spread = " + underlyingPoolCreditSpreadMean + "bps");
      System.out.println("Median " + dummyCreditSpreadTenor + " credit spread = " + underlyingPoolCreditSpreadMedian + "bps");
      //System.out.println("Modal " + dummyCreditSpreadTenor + " credit spread = " + underlyingPoolCreditSpreadMode + "bps");

      System.out.println(dummyCreditSpreadTenor + " Credit spread variance = " + underlyingPoolCreditSpreadVariance + "bps");
      System.out.println(dummyCreditSpreadTenor + " Credit spread standard deviation = " + underlyingPoolCreditSpreadStandardDeviation + "bps");

      System.out.println(dummyCreditSpreadTenor + " Credit spread skewness = " + underlyingPoolCreditSpreadSkewness + "bps");
      System.out.println(dummyCreditSpreadTenor + " Credit spread kurtosis = " + underlyingPoolCreditSpreadKurtosis + "bps");

      System.out.println(dummyCreditSpreadTenor + " Credit spread " + q * 100.0 + "% percentile = " + underlyingPoolCreditSpreadqPercentile + "bps");
      System.out.println();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------
}
