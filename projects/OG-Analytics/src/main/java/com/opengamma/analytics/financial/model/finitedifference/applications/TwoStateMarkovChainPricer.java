/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoupledCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.CoupledFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.CoupledPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.local.AbsoluteLocalVolatilitySurface;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Solves a coupled forward PDE for the price of a call option when the process is CEV with vol levels determined by a two state Markov chain.  
 */
public class TwoStateMarkovChainPricer {
  private static final CoupledPDEDataBundleProvider BUNDLE_PROVIDER = new CoupledPDEDataBundleProvider();

  private final ConvectionDiffusionPDE1DCoupledCoefficients[] _data;

  private final ForwardCurve _forward;
  private final TwoStateMarkovChainDataBundle _chainDB;
  private final Function1D<Double, Double> _initalCond1;
  private final Function1D<Double, Double> _initalCond2;

  //  private final double _lambda12;
  //  private final double _lambda21;
  //  private final double _p0;

  public TwoStateMarkovChainPricer(final ForwardCurve forward, final TwoStateMarkovChainDataBundle chainDB) {
    Validate.notNull(forward, "null forward curve");
    Validate.notNull(chainDB, "null MC DB");

    _forward = forward;
    _chainDB = chainDB;
    _data = BUNDLE_PROVIDER.getCoupledForwardPair(forward, chainDB);
    _initalCond1 = getInitialCond(forward.getSpot(), chainDB.getP0());
    _initalCond2 = getInitialCond(forward.getSpot(), 1.0 - chainDB.getP0());
  }

  /**
   * Solves a coupled forward PDE for the price of a call option when the process is CEV with vol levels determined by a two state Markov chain
   * @param forward The forward curve of the underlying asset 
   * @param chainDB The chain data bundle
   * @param localVolOverlay The local volatility overlay
   */
  public TwoStateMarkovChainPricer(final ForwardCurve forward, final TwoStateMarkovChainDataBundle chainDB, final AbsoluteLocalVolatilitySurface localVolOverlay) {
    Validate.notNull(forward, "null forward curve");
    Validate.notNull(chainDB, "null MC DB");
    Validate.notNull(localVolOverlay, "null local vol");

    _forward = forward;
    _chainDB = chainDB;
    _data = BUNDLE_PROVIDER.getCoupledForwardPair(forward, chainDB, localVolOverlay);
    _initalCond1 = getInitialCond(forward.getSpot(), chainDB.getP0());
    _initalCond2 = getInitialCond(forward.getSpot(), 1.0 - chainDB.getP0());
  }

  PDEFullResults1D solve(final PDEGrid1D grid, final double theta) {
    Validate.notNull(grid, "null grid");
    Validate.isTrue(0 <= theta && theta <= 1.0, "theta must be in range 0 to 1");

    Validate.isTrue(grid.getSpaceNode(0) == 0.0, "space grid must start at zero");

    final Function1D<Double, Double> strikeZeroPrice1 = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double t) {
        return probState1(t) * _forward.getSpot();
      }
    };

    final Function1D<Double, Double> strikeZeroPrice2 = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double t) {
        return (1 - probState1(t)) * _forward.getSpot();
      }
    };

    final BoundaryCondition lower1 = new DirichletBoundaryCondition(strikeZeroPrice1, 0.0);
    final BoundaryCondition lower2 = new DirichletBoundaryCondition(strikeZeroPrice2, 0.0);

    final double kMax = grid.getSpaceNode(grid.getNumSpaceNodes() - 1);

    final BoundaryCondition upper = new NeumannBoundaryCondition(0, kMax, false);

    final CoupledPDEDataBundle d1 = new CoupledPDEDataBundle(_data[0], _initalCond1, lower1, upper, grid);
    final CoupledPDEDataBundle d2 = new CoupledPDEDataBundle(_data[1], _initalCond2, lower2, upper, grid);

    final CoupledFiniteDifference solver = new CoupledFiniteDifference(theta, true);
    final PDEResults1D[] res = solver.solve(d1, d2);
    final PDEFullResults1D res1 = (PDEFullResults1D) res[0];
    final PDEFullResults1D res2 = (PDEFullResults1D) res[1];

    final double[][] prices = new double[grid.getNumTimeNodes()][grid.getNumSpaceNodes()];
    for (int i = 0; i < grid.getNumTimeNodes(); i++) {
      for (int j = 0; j < grid.getNumSpaceNodes(); j++) {
        prices[i][j] = res1.getFunctionValue(j, i) + res2.getFunctionValue(j, i);
      }
    }
    return new PDEFullResults1D(grid, prices);

  }

  private double probState1(final double t) {
    final double sum = _chainDB.getLambda12() + _chainDB.getLambda21();
    return _chainDB.getSteadyStateProb() + (_chainDB.getP0() - _chainDB.getSteadyStateProb()) * Math.exp(-sum * t);
  }

  private Function1D<Double, Double> getInitialCond(final double s0, final double p0) {
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        return p0 * Math.max(0.0, s0 - k);
      }
    };
  }

}
