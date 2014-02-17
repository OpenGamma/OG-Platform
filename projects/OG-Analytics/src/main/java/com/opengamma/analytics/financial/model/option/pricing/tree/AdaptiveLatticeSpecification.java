/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.model.option.pricing.tree.BarrierOptionFunctionProvider.BarrierTypes;
import com.opengamma.util.ArgumentChecker;

/**
 * Adaptive mesh method for single barrier options
 */
public class AdaptiveLatticeSpecification extends LatticeSpecification {
  private static final double SHIFT = 1.e-12;
  private final BarrierOptionFunctionProvider _provider;

  /**
   * Constructor specifying the barrier
   * @param provider BarrierOptionFunctionProvider of OptionFunctionProvider1D
   */
  public AdaptiveLatticeSpecification(final OptionFunctionProvider1D provider) {
    ArgumentChecker.notNull(provider, "provider");

    if (!(provider instanceof BarrierOptionFunctionProvider)) {
      throw new IllegalArgumentException();
    }
    if (provider instanceof DoubleBarrierOptionFunctionProvider) {
      throw new NotImplementedException();
    }
    _provider = (BarrierOptionFunctionProvider) provider;

  }

  @Override
  public double[] getParameters(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    throw new IllegalArgumentException("This lattice specification does not cover binomial tree");
  }

  @Override
  public double[] getParametersTrinomial(final double spot, final double strike, final double timeToExpiry, final double volatility, final double interestRate, final int nSteps, final double dt) {
    final double barrier = _provider.getBarrier();
    final BarrierTypes type = _provider.getBarrierType();
    final double sign = type == BarrierTypes.UpAndOut ? 1. : -1.;
    final double modBarrier = barrier + sign * SHIFT;

    final double volSq = volatility * volatility;
    final double mu = interestRate - 0.5 * volSq;
    final double mudt = mu * dt;
    final double mudtSq = mudt * mudt;

    final double rootDt = Math.sqrt(dt);
    final double dx0 = volatility * Math.sqrt(3.) * rootDt;

    final int position = (int) Math.round(Math.log(spot / barrier) / dx0);
    final double lambdaSqRoot = Math.abs(Math.log(modBarrier / spot) / (position * volatility * rootDt));
    final double dx = volatility * lambdaSqRoot * rootDt;

    final double upFactor = Math.exp(dx);
    final double downFactor = Math.exp(-dx);

    final double part = (volSq * dt + mudtSq) / dx / dx;
    final double upProbability = 0.5 * (part + mudt / dx);
    final double middleProbability = 1. - part;
    final double downProbability = 0.5 * (part - mudt / dx);

    return new double[] {upFactor, 1., downFactor, upProbability, middleProbability, downProbability };
  }
}
