package com.opengamma.financial.montecarlo;

import com.opengamma.financial.model.stochastic.StochasticProcess;

public interface MonteCarlo<U extends StochasticProcess> {

  public double[] getPath(U process, int n);
}
