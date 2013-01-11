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
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 *  Solves a coupled forward PDE (i.e. coupled Fokker-Plank) for the density of an asset when the process is CEV with vol levels determined by a
 *  two state Markov chain. The densities, p(t,s,state1) & p(t,s,state2), are such that int_{0}^{\infty} p(t,s,stateX) ds gives the probability
 *  of being in state X at time t, and (p(t,s,state1)+p(t,s,state2))*ds is the probability that the asset with be between s and s + ds at time t.   
 */
public class TwoStateMarkovChainDensity {
  private static final double THETA = 1.0;

  private final ConvectionDiffusionPDE1DCoupledCoefficients _data1;
  private final ConvectionDiffusionPDE1DCoupledCoefficients _data2;
  private final Function1D<Double, Double> _initCon11;
  private final Function1D<Double, Double> _initCon12;

  public TwoStateMarkovChainDensity(final ForwardCurve forward, final double vol1, final double deltaVol, final double lambda12, final double lambda21, final double probS1, final double beta1,
      final double beta2) {
    this(forward, new TwoStateMarkovChainDataBundle(vol1, vol1 + deltaVol, lambda12, lambda21, probS1, beta1, beta2));
  }

  public TwoStateMarkovChainDensity(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data) {
    Validate.notNull(forward, "null forward");
    Validate.notNull(data, "null data");

    _data1 = getCoupledPDEDataBundle(forward, data.getVol1(), data.getLambda12(), data.getLambda21(), data.getBeta1());
    _data2 = getCoupledPDEDataBundle(forward, data.getVol2(), data.getLambda21(), data.getLambda12(), data.getBeta2());
    _initCon11 = getInitialCondition(forward, data.getP0());
    _initCon12 = getInitialCondition(forward, 1.0 - data.getP0());
  }

  PDEFullResults1D[] solve(final PDEGrid1D grid) {

    //BoundaryCondition lower = new FixedSecondDerivativeBoundaryCondition(0, grid.getSpaceNode(0), true);
    final BoundaryCondition lower = new NeumannBoundaryCondition(0.0, grid.getSpaceNode(0), true);
    //BoundaryCondition lower = new DirichletBoundaryCondition(0.0, grid.getSpaceNode(0));//TODO for beta < 0.5 zero is accessible and thus there will be non-zero 
    //density there
    final BoundaryCondition upper = new DirichletBoundaryCondition(0.0, grid.getSpaceNode(grid.getNumSpaceNodes() - 1));

    CoupledPDEDataBundle d1 = new CoupledPDEDataBundle(_data1, _initCon11, lower, upper, grid);
    CoupledPDEDataBundle d2 = new CoupledPDEDataBundle(_data2, _initCon12, lower, upper, grid);

    final CoupledFiniteDifference solver = new CoupledFiniteDifference(THETA, true);
    final PDEResults1D[] res = solver.solve(d1, d2);
    //handle this with generics  
    final PDEFullResults1D res1 = (PDEFullResults1D) res[0];
    final PDEFullResults1D res2 = (PDEFullResults1D) res[1];
    return new PDEFullResults1D[] {res1, res2 };
  }

  private Function1D<Double, Double> getInitialCondition(final ForwardCurve forward, final double initialProb) {
    //using a log-normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    return new Function1D<Double, Double>() {
      private final double _volRootTOffset = 0.01;

      @Override
      public Double evaluate(final Double s) {
        if (s <= 0 || initialProb == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward.getSpot());
        final NormalDistribution dist = new NormalDistribution(0, _volRootTOffset);
        return initialProb * dist.getPDF(x) / s;
      }
    };
  }

  private ConvectionDiffusionPDE1DCoupledCoefficients getCoupledPDEDataBundle(final ForwardCurve forward, final double vol, final double lambda1, final double lambda2, final double beta) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        double s = ts[1];
        if (s <= 0.0) { //TODO review how to handle absorption 
          s = -s;
        }
        return -Math.pow(s, 2 * beta) * vol * vol / 2;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        double s = ts[1];
        if (s < 0.0) {
          s = -s;
        }
        final double temp = (s < 0.0 ? 0.0 : 2 * vol * vol * beta * Math.pow(s, 2 * (beta - 1)));
        return s * (forward.getDrift(t) - temp);
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        double s = ts[1];

        if (s < 0.) {
          s = -s;
        }
        double temp = (beta == 1.0 ? 1.0 : Math.pow(s, 2 * (beta - 1)));
        if (s < 0) {
          temp = 0.0;
        }
        return lambda1 + forward.getDrift(t) - vol * vol * beta * (2 * beta - 1) * temp;
      }
    };

    return new ConvectionDiffusionPDE1DCoupledCoefficients(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), -lambda2);
  }
}
