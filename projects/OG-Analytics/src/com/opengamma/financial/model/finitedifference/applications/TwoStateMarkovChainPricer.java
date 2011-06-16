/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.financial.model.finitedifference.CoupledFiniteDifference;
import com.opengamma.financial.model.finitedifference.CoupledPDEDataBundle;
import com.opengamma.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.model.finitedifference.PDEResults1D;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * Solves a coupled forward PDE for the price of a call option when the process is CEV with vol levels determined by a two state Markov chain.  
 */
public class TwoStateMarkovChainPricer {
  private final CoupledPDEDataBundle _data1;
  private final CoupledPDEDataBundle _data2;
  private final ForwardCurve _forward;
  private final double _lambda12;
  private final double _lambda21;
  private final double _p0;

  /**
   * Solves a coupled forward PDE for the price of a call option when the process is CEV with vol levels determined by a two state Markov chain
   * @param forward The forward curve of the underlying asset 
   * @param vol1 The volatility in the 'normal' state
   * @param vol2 The volatility in the 'excited' state
   * @param lambda12 Rate (probably per unit time (years)) to go from 'normal' to 'excited' state
   * @param lambda21 Rate (probably per unit time (years)) to go from 'excited' to 'normal' state
   * @param probS1 Probability of stating in the 'normal' state 
   * @param beta1 CEV parameter in 'normal' state
   * @param beta2 CEV parameter in 'excited' state
   */
  public TwoStateMarkovChainPricer(final ForwardCurve forward, final double vol1, final double vol2,
      final double lambda12, final double lambda21, final double probS1, final double beta1, final double beta2) {
    Validate.notNull(forward, "null forward curve");

    Validate.isTrue(vol1 >= 0.0, "vol1 < 0");
    Validate.isTrue(vol2 >= 0.0, "vol2 < 0");
    Validate.isTrue(lambda12 >= 0.0, "lambda12 < 0");
    Validate.isTrue(lambda21 >= 0.0, "lambda21 < 0");
    Validate.isTrue(probS1 >= 0.0 && probS1 <= 1.0, "Need 0 <= probS1 <= 1.0");

    _forward = forward;
    _lambda12 = lambda12;
    _lambda21 = lambda21;
    _p0 = probS1;
    //TODO treat rates
    _data1 = getCoupledPDEDataBundle(forward, vol1, lambda12, lambda21, probS1, beta1);
    _data2 = getCoupledPDEDataBundle(forward, vol2, lambda21, lambda12, 1.0 - probS1, beta2);
  }

  PDEFullResults1D solve(PDEGrid1D grid) {

    Validate.isTrue(grid.getSpaceNode(0) == 0.0, "space grid must start at zero");

    Function1D<Double, Double> strikeZeroPrice1 = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        return probState1(t) * _forward.getSpot();
      }
    };

    Function1D<Double, Double> strikeZeroPrice2 = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        return (1 - probState1(t)) * _forward.getSpot();
      }
    };

    BoundaryCondition lower1 = new DirichletBoundaryCondition(strikeZeroPrice1, 0.0);
    BoundaryCondition lower2 = new DirichletBoundaryCondition(strikeZeroPrice2, 0.0);

    double kMax = grid.getSpaceNode(grid.getNumSpaceNodes() - 1);

    BoundaryCondition upper = new NeumannBoundaryCondition(0, kMax, false);
    CoupledFiniteDifference solver = new CoupledFiniteDifference(0.55, true);
    PDEResults1D[] res = solver.solve(_data1, _data2, grid, lower1, upper, lower2, upper, null);
    PDEFullResults1D res1 = (PDEFullResults1D) res[0];
    PDEFullResults1D res2 = (PDEFullResults1D) res[1];

    double[][] prices = new double[grid.getNumTimeNodes()][grid.getNumSpaceNodes()];
    for (int i = 0; i < grid.getNumTimeNodes(); i++) {
      for (int j = 0; j < grid.getNumSpaceNodes(); j++) {
        prices[i][j] = res1.getFunctionValue(j, i) + res2.getFunctionValue(j, i);
      }
    }
    return new PDEFullResults1D(grid, prices);

  }

  private double probState1(final double t) {
    double sum = _lambda12 + _lambda21;
    if (sum == 0) {
      return _p0;
    }
    double pi1 = _lambda21 / sum;
    return pi1 + (_p0 - pi1) * Math.exp(-sum * t);
  }

  private CoupledPDEDataBundle getCoupledPDEDataBundle(final ForwardCurve forward, final double vol, final double lambda1, final double lambda2,
      final double initialProb, final double beta) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        double k = tk[1];
        return -Math.pow(k, 2 * beta) * vol * vol / 2;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        double t = tk[0];
        double k = tk[1];
        return k * forward.getDrift(t);
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return lambda1;
      }
    };

    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double k) {
        return initialProb * Math.max(0, forward.getSpot() - k);
      }
    };

    return new CoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), -lambda2, initialCondition);
  }

}
