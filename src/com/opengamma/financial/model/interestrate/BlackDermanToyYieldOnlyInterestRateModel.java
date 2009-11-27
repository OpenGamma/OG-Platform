/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.definition.BlackDermanToyDataBundle;
import com.opengamma.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;
import com.opengamma.util.Triple;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * @author emcleod
 */
public class BlackDermanToyYieldOnlyInterestRateModel {
  protected RealSingleRootFinder _rootFinder = new VanWijngaardenDekkerBrentSingleRootFinder();
  protected final int _n;
  protected final int _j;

  public BlackDermanToyYieldOnlyInterestRateModel(final int n) {
    _n = n;
    _j = RecombiningBinomialTree.NODES.evaluate(_n);
  }

  public Function1D<BlackDermanToyDataBundle, RecombiningBinomialTree<Triple<Double, Double, Double>>> getTrees(final ZonedDateTime time) {
    return new Function1D<BlackDermanToyDataBundle, RecombiningBinomialTree<Triple<Double, Double, Double>>>() {

      @SuppressWarnings("unchecked")
      @Override
      public RecombiningBinomialTree<Triple<Double, Double, Double>> evaluate(final BlackDermanToyDataBundle data) {
        final Double[][] r = new Double[_n][_j];
        final Double[][] q = new Double[_n][_j];
        final Double[][] d = new Double[_n][_j];
        final Double[] u = new Double[_n];
        final Double[] p = new Double[_n + 1];
        final double t = DateUtil.getDifferenceInYears(data.getDate(), time);
        final double dt = t / _n;
        final double dtSqrt = Math.sqrt(dt);
        final double r1 = data.getInterestRate(dt);
        final double sigma = data.getVolatility(dt);
        for (int i = 0; i <= _n; i++) {
          p[i] = Math.pow(1. / (1 + data.getInterestRate(dt * i)), dt * i);
        }
        q[0][0] = 1.;
        u[0] = r1;
        r[0][0] = r1;
        d[0][0] = 1. / (1 + r1 * dt);
        for (int i = 1; i < _n; i++) {
          q[i][0] = 0.5 * q[i - 1][0] * d[i - 1][0];
          q[i][i] = 0.5 * q[i - 1][i - 1] * d[i - 1][i - 1];
          for (int j = -i + 2, k = 1; j <= i - 2; j += 2, k++) {
            q[i][k] = 0.5 * (q[i - 1][k - 1] * d[i - 1][k - 1] + q[i - 1][k] * d[i - 1][k]);
          }
          u[i] = _rootFinder.getRoot(getMedian(sigma, i, dt, q, p[i + 1]), 0., 1.);
          for (int j = -i, k = 0; j <= i; j += 2, k++) {
            r[i][k] = u[i] * Math.exp(sigma * j * dtSqrt);
            d[i][k] = 1. / (1 + r[i][k] * dt);
          }
        }
        final Triple<Double, Double, Double>[][] result = new Triple[_n][_j];
        for (int i = 0; i < _n; i++) {
          for (int j = 0; j < _j; j++) {
            result[i][j] = new Triple<Double, Double, Double>(r[i][j], d[i][j], q[i][j]);
          }
        }
        return new RecombiningBinomialTree<Triple<Double, Double, Double>>(result);
      }

    };
  }

  protected Function1D<Double, Double> getMedian(final Double sigma, final int i, final Double dt, final Double[][] q, final Double p) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double u) {
        Double sum = 0.;
        for (int j = -i, k = 0; j <= i; j += 2, k++) {
          sum += q[i][k] / (1 + u * Math.exp(sigma * j * dt) * dt);
        }
        return sum - p;
      }
    };
  }
}
