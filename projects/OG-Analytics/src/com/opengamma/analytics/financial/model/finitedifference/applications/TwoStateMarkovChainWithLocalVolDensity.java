/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.CoupledPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExtendedCoupledFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.ExtendedCoupledPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.surface.AbsoluteLocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
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
    return new PDEFullResults1D[] {res1, res2};
  }

  @SuppressWarnings("unused")
  private CoupledPDEDataBundle getCoupledPDEDataBundle(final ForwardCurve forward, final double vol, final double lambda1, final double lambda2, final double initialProb, final double beta,
      final LocalVolatilitySurfaceStrike localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double sigma = localVol.getVolatility(t, s) * vol * Math.pow(s, beta);
        return -sigma * sigma / 2;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double lvDiv = getLocalVolFirstDiv(localVol, t, s);
        final double lv = localVol.getVolatility(t, s);
        return s * (forward.getDrift(t) - 2 * vol * vol * lv * Math.pow(s, 2 * beta - 2) * (s * lvDiv + lv * beta));
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double lv1Div = getLocalVolFirstDiv(localVol, t, s);
        final double lv2Div = getLocalVolSecondDiv(localVol, t, s);
        final double lv = localVol.getVolatility(t, s);
        final double temp1 = vol * Math.pow(s, beta - 1) * (beta * lv + s * lv1Div);
        final double temp2 = vol * vol * lv * Math.pow(s, 2 * (beta - 1)) * (s * s * lv2Div + 2 * beta * s * lv1Div + beta * (beta - 1) * lv);

        return lambda1 + forward.getDrift(t) - temp1 * temp1 - temp2;
      }
    };

    //using a log-normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {
      private final double _volRootTOffset = 0.01;

      @Override
      public Double evaluate(final Double s) {
        if (s == 0 || initialProb == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward.getSpot());
        final NormalDistribution dist = new NormalDistribution(0, _volRootTOffset);
        return initialProb * dist.getPDF(x) / s;
      }
    };

    return new CoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), -lambda2, initialCondition);
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

  //TODO handle with a central calculator
  private double getLocalVolFirstDiv(final LocalVolatilitySurfaceStrike localVol, final double t, final double s) {
    final double eps = 1e-4;
    final double up = localVol.getVolatility(t, s + eps);
    final double down = localVol.getVolatility(t, s - eps);
    return (up - down) / 2 / eps;
  }

  private double getLocalVolSecondDiv(final LocalVolatilitySurfaceStrike localVol, final double t, final double s) {
    final double eps = 1e-4;
    final double up = localVol.getVolatility(t, s + eps);
    final double mid = localVol.getVolatility(t, s);
    final double down = localVol.getVolatility(t, s - eps);
    return (up + down - 2 * mid) / eps / eps;
  }

}
