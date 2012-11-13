/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool.definition;

import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to specify the composition and characteristics of a 'pool' of obligors
 * In the credit index context the underlying pool is the set of obligors that constitute the index
 */
public class UnderlyingPool {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work-in-Progress

  // TODO : Will want to include calculations such as e.g. average T year spread of constituents and other descriptive statistics
  // TODO : The standard index pools will be classes that derive from this one
  // TODO : Will want to include the recovery rates and weightings for the obligors in the pool

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // A vector of obligors constituting the underlying pool
  private final Obligor[] _obligors;

  // The number of obligors in the underlying pool (usually 125 for CDX and iTraxx - although defaults can reduce this)
  private final int _numberOfObligors;

  // Vector holding the coupons to apply to the obligors in the underlying pool
  private final double[] _coupons;

  // Vector holding the recovery rates of the obligors in the underlying pool
  private final double[] _recoveryRates;

  // Vector holding the weights of the obligor in the underlying pool
  private final double[] _obligorWeights;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the pool of obligor objects

  public UnderlyingPool(Obligor[] obligors, double[] coupons, double[] recoveryRates, double[] obligorWeights) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(obligors, "Obligors");
    ArgumentChecker.notNull(coupons, "Coupons");
    ArgumentChecker.notNull(recoveryRates, "Recovery Rates");
    ArgumentChecker.notNull(obligorWeights, "Obligor Weights");

    ArgumentChecker.isTrue(obligors.length == coupons.length, "Number of obligors and number of coupons should be equal");
    ArgumentChecker.isTrue(obligors.length == recoveryRates.length, "Number of obligors and number of recovery rates should be equal");
    ArgumentChecker.isTrue(obligors.length == obligorWeights.length, "Number of obligors and number of obligor weights should be equal");

    double totalObligorWeightings = 0.0;

    for (int i = 0; i < coupons.length; i++) {
      ArgumentChecker.notNegative(coupons[i], "Coupons for obligor " + i);

      ArgumentChecker.notNegative(recoveryRates[i], "Recovery Rate for obligor " + i);
      ArgumentChecker.isTrue(Double.doubleToLongBits(recoveryRates[i]) <= 1.0, "Recovery rate for obligor " + i + " should be less than or equal to 100%");

      ArgumentChecker.notNegative(obligorWeights[i], "Index weighting for obligor " + i);
      ArgumentChecker.isTrue(Double.doubleToLongBits(obligorWeights[i]) <= 1.0, "Index weighting for obligor " + i + " should be less than or equal to 100%");

      totalObligorWeightings += obligorWeights[i];
    }

    ArgumentChecker.isTrue(Double.doubleToLongBits(totalObligorWeightings) == 1.0, "Index constituent weights must sum to unity");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _obligors = obligors;

    _numberOfObligors = obligors.length;

    _coupons = coupons;
    _recoveryRates = recoveryRates;
    _obligorWeights = obligorWeights;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public Obligor[] getObligors() {
    return _obligors;
  }

  public int getNumberOfObligors() {
    return _numberOfObligors;
  }

  public double[] getCoupons() {
    return _coupons;
  }

  public double[] getRecoveryRates() {
    return _recoveryRates;
  }

  public double[] getObligorWeights() {
    return _obligorWeights;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
