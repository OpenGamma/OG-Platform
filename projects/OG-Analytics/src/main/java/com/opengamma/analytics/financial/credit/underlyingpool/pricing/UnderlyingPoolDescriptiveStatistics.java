/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool.pricing;

import java.util.Arrays;

import com.opengamma.analytics.financial.credit.CreditSpreadTenors;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.MedianCalculator;
import com.opengamma.analytics.math.statistics.descriptive.ModeCalculator;
import com.opengamma.analytics.math.statistics.descriptive.PercentileCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleFisherKurtosisCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleSkewnessCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleVarianceCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to compute basic statistics (e.g. average 5Y spread) for an underlying pool
 */
public class UnderlyingPoolDescriptiveStatistics {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add ability to identify the obligor with the max/min spread etc?
  // TODO : Add method to return the n tightest/widest spreads (and the associated obligors)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate how many of the names in the underlying pool are marked as already defaulted
  public int getUnderlyingPoolNumberOfDefaultedNames(final UnderlyingPool underlyingPool) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");

    int numberOfDefaultedNames = 0;

    int numberOfObligorsInPool = underlyingPool.getNumberOfObligors();

    for (int i = 0; i < numberOfObligorsInPool; i++) {

      if (underlyingPool.getObligors()[i].isHasDefaulted() == true) {
        numberOfDefaultedNames++;
      }
    }

    return numberOfDefaultedNames;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate how many of the names in the underlying pool are marked as not already defaulted
  public int getUnderlyingPoolNumberOfNonDefaultedNames(final UnderlyingPool underlyingPool) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");

    int numberOfObligorsInPool = underlyingPool.getNumberOfObligors();

    int numberOfDefaultedNames = getUnderlyingPoolNumberOfDefaultedNames(underlyingPool);

    int numberOfNonDefaultedNames = numberOfObligorsInPool - numberOfDefaultedNames;

    return numberOfNonDefaultedNames;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the total notional amount of the underlying pool (sum of individual obligor notionals)
  public double getUnderlyingPoolTotalNotional(final UnderlyingPool underlyingPool) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");

    double totalNotional = 0.0;

    for (int i = 0; i < underlyingPool.getNumberOfObligors(); i++) {
      totalNotional += underlyingPool.getObligorNotionals()[i];
    }

    return totalNotional;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the average notional of the obligors in the underlying pool
  public double getUnderlyingPoolNotionalMean(final UnderlyingPool underlyingPool) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");

    MeanCalculator mean = new MeanCalculator();

    return mean.evaluate(underlyingPool.getObligorNotionals());
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the average recovery rate of the obligors in the underlying pool
  public double getUnderlyingPoolRecoveryRateMean(final UnderlyingPool underlyingPool) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");

    MeanCalculator mean = new MeanCalculator();

    double[] recoveryRates = underlyingPool.getRecoveryRates();

    return mean.evaluate(recoveryRates);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Get the minimum spread of the obligors in the underlying pool for the given tenor
  public double getUnderlyingPoolCreditSpreadMinimum(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    double[] spreads = getSpreads(underlyingPool, creditSpreadTenors, creditSpreadTermStructures, creditSpreadTenor);

    Arrays.sort(spreads);

    return spreads[0];
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Get the maximum spread of the obligors in the underlying pool for the given tenor
  public double getUnderlyingPoolCreditSpreadMaximum(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    double[] spreads = getSpreads(underlyingPool, creditSpreadTenors, creditSpreadTermStructures, creditSpreadTenor);

    Arrays.sort(spreads);

    return spreads[underlyingPool.getNumberOfObligors() - 1];
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the average (mean) spread of the obligors in the underlying pool for a given tenor
  public double getUnderlyingPoolCreditSpreadMean(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    MeanCalculator mean = new MeanCalculator();

    double[] spreads = getSpreads(underlyingPool, creditSpreadTenors, creditSpreadTermStructures, creditSpreadTenor);

    return mean.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the median spread of the obligors in the underlying pool for a given tenor
  public double getUnderlyingPoolCreditSpreadMedian(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    MedianCalculator median = new MedianCalculator();

    double[] spreads = getSpreads(underlyingPool, creditSpreadTenors, creditSpreadTermStructures, creditSpreadTenor);

    return median.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the modal spread of the obligors in the underlying pool for a given tenor
  public double getUnderlyingPoolCreditSpreadMode(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    ModeCalculator mode = new ModeCalculator();

    double[] spreads = getSpreads(underlyingPool, creditSpreadTenors, creditSpreadTermStructures, creditSpreadTenor);

    return mode.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the variance of the spread of the obligors in the underlying pool for a given tenor 
  public double getUnderlyingPoolCreditSpreadVariance(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    SampleVarianceCalculator variance = new SampleVarianceCalculator();

    double[] spreads = getSpreads(underlyingPool, creditSpreadTenors, creditSpreadTermStructures, creditSpreadTenor);

    return variance.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the standard deviation of the spread of the obligors in the underlying pool for a given tenor
  public double getUnderlyingPoolCreditSpreadStandardDeviation(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    SampleStandardDeviationCalculator standardDeviation = new SampleStandardDeviationCalculator();

    double[] spreads = getSpreads(underlyingPool, creditSpreadTenors, creditSpreadTermStructures, creditSpreadTenor);

    return standardDeviation.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the skewness of the spread of the obligors in the underlying pool for a given tenor
  public double getUnderlyingPoolCreditSpreadSkewness(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    SampleSkewnessCalculator skewness = new SampleSkewnessCalculator();

    double[] spreads = getSpreads(underlyingPool, creditSpreadTenors, creditSpreadTermStructures, creditSpreadTenor);

    return skewness.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the excess kurtosis of the spread of the obligors in the underlying pool for a given tenor
  public double getUnderlyingPoolCreditSpreadKurtosis(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    SampleFisherKurtosisCalculator excessKurtosis = new SampleFisherKurtosisCalculator();

    double[] spreads = getSpreads(underlyingPool, creditSpreadTenors, creditSpreadTermStructures, creditSpreadTenor);

    return excessKurtosis.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the q'th percentile of the spread distribution of the obligors in the underlying pool for a given tenor
  public double getUnderlyingPoolCreditSpreadPercentile(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor, final double q) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");
    ArgumentChecker.notNegative(q, "Percentile");
    ArgumentChecker.isTrue(q <= 1.0, "Percentile must be less than or equal to 100%");

    PercentileCalculator percentile = new PercentileCalculator(q);

    double[] spreads = getSpreads(underlyingPool, creditSpreadTenors, creditSpreadTermStructures, creditSpreadTenor);

    return percentile.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to extract out the creditSpreadTenor spreads for the names in the underlying pool
  private double[] getSpreads(final UnderlyingPool underlyingPool, final CreditSpreadTenors[] creditSpreadTenors, final double[][] creditSpreadTermStructures,
      final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(underlyingPool, "Underlying pool");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(creditSpreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    // Check that creditSpreadTenor is one of the tenors contained in the underlying pool 
    checkCreditSpreadTenor(creditSpreadTenors, creditSpreadTenor);

    int counter = 0;

    double[] spreads = new double[underlyingPool.getNumberOfObligors()];

    // TODO : This is assuming the tenors in creditSpreadTenors[] do not contain duplicate entries
    while (creditSpreadTenors[counter] != creditSpreadTenor) {
      counter++;
    }

    for (int i = 0; i < underlyingPool.getNumberOfObligors(); i++) {
      spreads[i] = creditSpreadTermStructures[i][counter];
    }

    return spreads;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Check that creditSpreadTenor is one of the tenors contained in the underlying pool (if not throw an exception)
  private void checkCreditSpreadTenor(final CreditSpreadTenors[] creditSpreadTenors, CreditSpreadTenors creditSpreadTenor) {

    boolean haveFoundCreditSpreadTenor = false;

    for (int m = 0; m < creditSpreadTenors.length; m++) {

      if (creditSpreadTenor.equals(creditSpreadTenors[m])) {
        haveFoundCreditSpreadTenor = true;

        return;
      }
    }

    if (haveFoundCreditSpreadTenor == false) {
      throw new IllegalArgumentException("The credit spread tenor " + creditSpreadTenor + " is not in the list of credit spread tenors in the underlying pool");
    }
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
