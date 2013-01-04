/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.recoveryratemodel;

import org.apache.commons.math.distribution.BetaDistributionImpl;

/**
 * Class to specify a stochastic recovery rate model to tag to a given obligor/trade
 */
public class RecoveryRateModelStochastic {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to add the arg checkers for the input arguments

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The parameters of the beta distribution
  private final double _a;
  private final double _b;

  private final double _x;

  private final double _recoveryRate;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the stochastic recovery rate model

  public RecoveryRateModelStochastic(final double a, final double b, final double x) {

    _a = a;
    _b = b;

    _x = x;

    BetaDistributionImpl betaDistribution = new BetaDistributionImpl(_a, _b);

    _recoveryRate = betaDistribution.density(_x);

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double geta() {
    return _a;
  }

  public double getb() {
    return _b;
  }

  public double getx() {
    return _x;
  }

  public double getRecoveryRate() {
    return _recoveryRate;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to allow the stochastic recovery rate to be sampled at different values of x

  public RecoveryRateModelStochastic sampleRecoveryRate(final double x) {

    final RecoveryRateModelStochastic modifiedRecoveryRateModel = new RecoveryRateModelStochastic(geta(), getb(), x);

    return modifiedRecoveryRateModel;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
