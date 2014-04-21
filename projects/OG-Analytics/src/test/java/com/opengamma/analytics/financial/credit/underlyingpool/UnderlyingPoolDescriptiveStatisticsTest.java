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
import com.opengamma.util.test.TestGroup;

/**
 * Tests to verify the calculation of descriptive statistics for an UnderlyingPool object
 */
@Test(groups = TestGroup.UNIT)
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

  // Get the credit spread tenors of the obligors in the underlying pool
  private static final CreditSpreadTenors[] dummyCreditSpreadTenors = pool.assignCreditSpreadTenors();

  // Get the term structures of credit spreads for each obligor in the underlying pool
  private static final double[][] dummyCreditSpreadTermStructures = pool.assignCreditSpreadTermStructures();

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Create a pool statistics calculator object
  private static final UnderlyingPoolDescriptiveStatistics underlyingPoolDescriptiveStatistics = new UnderlyingPoolDescriptiveStatistics();

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolTotalNotionalPoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolTotalNotional(null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolNotionalMeanPoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolNotionalMean(null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolRecoveryRateMeanPoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolRecoveryRateMean(null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMinPoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMinimum(null, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMinTenorsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMinimum(dummyPool, null, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMinSpreadsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMinimum(dummyPool, dummyCreditSpreadTenors, null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMinTenorField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMinimum(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMaxPoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMaximum(null, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMaxTenorsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMaximum(dummyPool, null, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMaxSpreadsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMaximum(dummyPool, dummyCreditSpreadTenors, null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMaxTenorField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMaximum(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMeanPoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMean(null, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMeanTenorsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMean(dummyPool, null, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMeanSpreadsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMean(dummyPool, dummyCreditSpreadTenors, null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMeanTenorField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMean(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMedianPoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMedian(null, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMedianTenorsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMedian(dummyPool, null, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMedianSpreadsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMedian(dummyPool, dummyCreditSpreadTenors, null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadMedianTenorField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMedian(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadModePoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMode(null, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadModeTenorsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMode(dummyPool, null, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadModeSpreadsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMode(dummyPool, dummyCreditSpreadTenors, null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadModeTenorField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMode(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadStandardDeviationPoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadStandardDeviation(null, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadStandardDeviationTenorsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadStandardDeviation(dummyPool, null, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadStandardDeviationSpreadsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadStandardDeviation(dummyPool, dummyCreditSpreadTenors, null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadStandardDeviationTenorField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadStandardDeviation(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadVariancePoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadVariance(null, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadVarianceTenorsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadVariance(dummyPool, null, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadVarianceSpreadsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadVariance(dummyPool, dummyCreditSpreadTenors, null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadVarianceTenorField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadVariance(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadSkewnessPoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadSkewness(null, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadSkewnessTenorsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadSkewness(dummyPool, null, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadSkewnessSpreadsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadSkewness(dummyPool, dummyCreditSpreadTenors, null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadSkewnessTenorField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadSkewness(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadKurtosisPoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadKurtosis(null, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadKurtosisTenorsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadKurtosis(dummyPool, null, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadKurtosisSpreadsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadKurtosis(dummyPool, dummyCreditSpreadTenors, null, dummyCreditSpreadTenor);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadKurtosisTenorField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadKurtosis(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, null);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadPercentilePoolField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadPercentile(null, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor, q);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadPercentileTenorsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadPercentile(dummyPool, null, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor, q);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadPercentileSpreadsField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadPercentile(dummyPool, dummyCreditSpreadTenors, null, dummyCreditSpreadTenor, q);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadPercentileTenorField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadPercentile(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, null, q);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadPercentileLessThanZeroField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadPercentile(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor, -q);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingPoolCreditSpreadPercentileGreaterThanOneField() {

    underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadPercentile(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor, q + 1.0);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test
  public void testUnderlyingPoolDescriptiveStatisticsCalculations() {

    double underlyingPoolTotalNotional = underlyingPoolDescriptiveStatistics.getUnderlyingPoolTotalNotional(dummyPool);
    double underlyingPoolNotionalMean = underlyingPoolDescriptiveStatistics.getUnderlyingPoolNotionalMean(dummyPool);

    double underlyingPoolRecoveryRateMean = underlyingPoolDescriptiveStatistics.getUnderlyingPoolRecoveryRateMean(dummyPool);

    double underlyingPoolCreditSpreadMinimum = underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMinimum(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures,
        dummyCreditSpreadTenor);
    double underlyingPoolCreditSpreadMaximum = underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMaximum(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures,
        dummyCreditSpreadTenor);

    double underlyingPoolCreditSpreadMean = underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMean(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures,
        dummyCreditSpreadTenor);
    double underlyingPoolCreditSpreadMedian = underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadMedian(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures,
        dummyCreditSpreadTenor);
    //double underlyingPoolCreditSpreadMode = underlyingPoolStatistics.getUnderlyingPoolCreditSpreadMode(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);

    double underlyingPoolCreditSpreadVariance = underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadVariance(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures,
        dummyCreditSpreadTenor);
    double underlyingPoolCreditSpreadStandardDeviation = underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadStandardDeviation(dummyPool, dummyCreditSpreadTenors,
        dummyCreditSpreadTermStructures, dummyCreditSpreadTenor);

    double underlyingPoolCreditSpreadSkewness = underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadSkewness(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures,
        dummyCreditSpreadTenor);
    double underlyingPoolCreditSpreadKurtosis = underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadKurtosis(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures,
        dummyCreditSpreadTenor);

    double underlyingPoolCreditSpreadqPercentile = underlyingPoolDescriptiveStatistics.getUnderlyingPoolCreditSpreadPercentile(dummyPool, dummyCreditSpreadTenors, dummyCreditSpreadTermStructures,
        dummyCreditSpreadTenor, q);

    if (outputResults) {
      System.out.println("Pool statistics report");

      System.out.println("Number of obligors in pool = " + dummyPool.getNumberOfObligors());

      System.out.println("Total notional of all obligors in pool = " + underlyingPoolTotalNotional);
      System.out.println("Mean notional of all obligors in pool = " + underlyingPoolNotionalMean);

      System.out.println("Average recovery rate of all obligors in pool = " + underlyingPoolRecoveryRateMean * 100.0 + "%");

      System.out.println("Minimum " + dummyCreditSpreadTenors + " credit spread = " + underlyingPoolCreditSpreadMinimum + "bps");
      System.out.println("Maximum " + dummyCreditSpreadTenors + " credit spread = " + underlyingPoolCreditSpreadMaximum + "bps");

      System.out.println("Mean " + dummyCreditSpreadTenors + " credit spread = " + underlyingPoolCreditSpreadMean + "bps");
      System.out.println("Median " + dummyCreditSpreadTenors + " credit spread = " + underlyingPoolCreditSpreadMedian + "bps");
      //System.out.println("Modal " + dummyCreditSpreadTenor + " credit spread = " + underlyingPoolCreditSpreadMode + "bps");

      System.out.println(dummyCreditSpreadTenors + " Credit spread variance = " + underlyingPoolCreditSpreadVariance + "bps");
      System.out.println(dummyCreditSpreadTenors + " Credit spread standard deviation = " + underlyingPoolCreditSpreadStandardDeviation + "bps");

      System.out.println(dummyCreditSpreadTenors + " Credit spread skewness = " + underlyingPoolCreditSpreadSkewness + "bps");
      System.out.println(dummyCreditSpreadTenors + " Credit spread kurtosis = " + underlyingPoolCreditSpreadKurtosis + "bps");

      System.out.println(dummyCreditSpreadTenors + " Credit spread " + q * 100.0 + "% percentile = " + underlyingPoolCreditSpreadqPercentile + "bps");
      System.out.println();
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------
}
