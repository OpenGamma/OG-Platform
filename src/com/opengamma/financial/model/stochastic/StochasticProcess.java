package com.opengamma.financial.model.stochastic;

public interface StochasticProcess {

  public double[] getPath(int n);
}
