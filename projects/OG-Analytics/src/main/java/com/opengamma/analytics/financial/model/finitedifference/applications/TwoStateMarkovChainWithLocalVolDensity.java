/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExtendedCoupledFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.ExtendedCoupledPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.local.AbsoluteLocalVolatilitySurface;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 *
 */
@SuppressWarnings("deprecation")
public class TwoStateMarkovChainWithLocalVolDensity {

  private static final double THETA = 1.0;

  private final ExtendedCoupledPDEDataBundle _data1;
  private final ExtendedCoupledPDEDataBundle _data2;

  public TwoStateMarkovChainWithLocalVolDensity(final ForwardCurve forward, final TwoStateMarkovChainDataBundle data, final AbsoluteLocalVolatilitySurface localVolOverlay) {
    Validate.notNull(forward, "null forward");
    Validate.notNull(data, "null data");
    Validate.notNull(localVolOverlay, "null localVolOverlay");
    //    _data1 = getCoupledPDEDataBundle(forward, data.getVol1(), data.getLambda12(), data.getLambda21(), data.getP0(), data.getBeta1(), localVol);
    //    _data2 = getCoupledPDEDataBundle(forward, data.getVol2(), data.getLambda21(), data.getLambda12(), 1.0 - data.getP0(), data.getBeta2(), localVol);
    _data1 = getExtendedCoupledPDEDataBundle(forward, data.getVol1(), data.getLambda12(), data.getLambda21(), data.getP0(), data.getBeta1(), localVolOverlay);
    _data2 = getExtendedCoupledPDEDataBundle(forward, data.getVol2(), data.getLambda21(), data.getLambda12(), 1.0 - data.getP0(), data.getBeta2(), localVolOverlay);
  }

  PDEFullResults1D[] solve(final PDEGrid1D grid) {

    final BoundaryCondition lower = new NeumannBoundaryCondition(0.0, grid.getSpaceNode(0), true);
    //BoundaryCondition lower = new DirichletBoundaryCondition(0.0, 0.0);//TODO for beta < 0.5 zero is accessible and thus there will be non-zero
    //density there
    final BoundaryCondition upper = new DirichletBoundaryCondition(0.0, grid.getSpaceNode(grid.getNumSpaceNodes() - 1));

    final ExtendedCoupledFiniteDifference solver = new ExtendedCoupledFiniteDifference(THETA);
    final PDEResults1D[] res = solver.solve(_data1, _data2, grid, lower, upper, lower, upper, null);
    //handle this with generics
    final PDEFullResults1D res1 = (PDEFullResults1D) res[0];
    final PDEFullResults1D res2 = (PDEFullResults1D) res[1];
    return new PDEFullResults1D[] {res1, res2 };
  }

  private ExtendedCoupledPDEDataBundle getExtendedCoupledPDEDataBundle(final ForwardCurve forward, final double vol, final double lambda1, final double lambda2, final double initialProb,
      final double beta, final AbsoluteLocalVolatilitySurface localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return -1.0;
      }
    };

    final Function<Double, Double> aStar = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double temp = localVol.getVolatility(t, s) * vol * Math.pow(s, beta);

        return 0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        return s * forward.getDrift(t);
      }
    };

    final Function<Double, Double> bStar = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return 1.0;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];

        return forward.getDrift(t) + lambda1;
      }
    };

    //using a log-normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {
      private final double _volRootTOffset = 0.01;

      @Override
      public Double evaluate(final Double s) {
        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward.getSpot());
        final NormalDistribution dist = new NormalDistribution(0, _volRootTOffset);
        return initialProb * dist.getPDF(x) / s;
      }
    };

    return new ExtendedCoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), FunctionalDoublesSurface.from(aStar),
        FunctionalDoublesSurface.from(bStar), -lambda2, initialCondition);

  }

}
