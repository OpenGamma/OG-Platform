/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.pricing;

import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.math.FunctionUtils;
import com.opengamma.math.function.Function1D;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 *  Model-independent Realized Variance result of the swap based upon observations already made.<p>
 *  Notes on market-standard form :<p>
 *  Computed as the average daily variance of log returns,then scaled by an annualizationFactor, an estimate of the number of business days per year<p>
 *  Number of actual observations may be less than number expected, due to unforeseen market disruptions, but sum is still normalized by nObsExpected
 *   
 */
public class RealizedVariance extends Function1D<VarianceSwap, Double> {
  @Override
  public Double evaluate(final VarianceSwap swap) {

    Validate.isTrue(swap.getObsExpected() > 0, "Encountered a VarianceSwap with 0 _nObsExpected! "
        + "If it is impractical to count, contact Quant to default this value in VarianceSwap constructor.");

    Double[] obs = swap.getObservations();

    if (obs == null) {
      return 0.0;
    }

    int nObs = obs.length;
    if (nObs < 2) {
      return 0.0;
    }

    Double[] weights = new Double[obs.length - 1];
    if (swap.getObservationWeights() == null) {
      Arrays.fill(weights, 1.0);
    } else {
      int nWeights = swap.getObservationWeights().length;
      Validate.isTrue(nWeights == nObs - 1,
          "If provided, observationWeights must be of length one less than observations, as they weight returns log(obs[i]/obs[i-1])."
              + " Found " + nWeights + " weights and " + nObs + " observations.");
    }
    Validate.isTrue(obs[0] != 0.0, "In VarianceSwap, the first observation is zero so the estimate of RealizedVariance is undefined. Check time series.");
    double logReturns = 0;
    for (int i = 1; i < nObs; i++) {
      Validate.isTrue(obs[i] != 0.0, "Encountered an invalid observation of zero in VarianceSwap at " + i + "'th observation. "
          + "The estimate of RealizedVariance is undefined. Check time series.");

      logReturns += weights[i - 1] * FunctionUtils.square(Math.log(obs[i] / obs[i - 1]));
    }

    return logReturns / swap.getObsExpected() * swap.getAnnualizationFactor();
  }

}
