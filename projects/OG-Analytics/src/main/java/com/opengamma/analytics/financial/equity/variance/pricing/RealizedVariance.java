/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import java.util.Arrays;

import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 *  Model-independent realized variance result of the swap based upon observations already made.<p>
 *  Notes on market-standard form :<p>
 *  Computed as the average daily variance of log returns, scaled by an annualization factor, an estimate of the number of business days per year<p>
 *  In this calculation, the average is taken over the actual number of observations provided. 
 *  In variance instruments, the number of actual observations may be less than number expected,
 *  due to unforeseen market disruptions. To account for this, the sum is normalized by nObsExpected (>= nObsActual) 
 *  The realized variance calculated in this class do not perform this normalization. See {@link VarianceSwapStaticReplication#presentValue}
 *  for an example of this normalization.  
 */
public class RealizedVariance extends Function1D<VarianceSwap, Double> {

  @Override
  public Double evaluate(final VarianceSwap swap) {
    ArgumentChecker.notNull(swap, "swap");
    final double[] obs = swap.getObservations();
    final int nObs = obs.length;

    if (nObs < 2) {
      return 0.0;
    }

    final double[] weights = new double[obs.length - 1];
    if (swap.getObservationWeights().length == 0) {
      Arrays.fill(weights, 1.0);
    } else if (swap.getObservationWeights().length == 1) {
      Arrays.fill(weights, swap.getObservationWeights()[0]);
    } else {
      final int nWeights = swap.getObservationWeights().length;
      ArgumentChecker.isTrue(nWeights == nObs - 1,
          "If provided, observationWeights must be of length one less than observations, as they weight returns log(obs[i]/obs[i-1])."
              + " Found {} weights and {} observations.", nWeights, nObs);
    }
    ArgumentChecker.isTrue(obs[0] != 0.0, "In VarianceSwap, the first observation is zero so the estimate of RealizedVariance is undefined. Check time series.");
    double logReturns = 0;
    for (int i = 1; i < nObs; i++) {
      ArgumentChecker.isTrue(Double.compare(obs[i], 0.0) != 0, "Encountered an invalid observation of zero in VarianceSwap at {}'th observation. "
          + "The estimate of RealizedVariance is undefined. Check time series.", i);

      logReturns += weights[i - 1] * FunctionUtils.square(Math.log(obs[i] / obs[i - 1]));
    }

    return logReturns / (nObs - 1) * swap.getAnnualizationFactor();
  }

}
