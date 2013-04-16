/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.definition.StandardDiscountBondModelDataBundle;
import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class BlackDermanToyYieldOnlyInterestRateModel {
  private final RealSingleRootFinder _rootFinder = new BrentSingleRootFinder();
  private final int _n;
  private final int _j;

  public BlackDermanToyYieldOnlyInterestRateModel(final int n) {
    if (n < 2) {
      throw new IllegalArgumentException("Must have more than one node");
    }
    _n = n;
    _j = RecombiningBinomialTree.NODES.evaluate(_n);
  }

  public Function1D<StandardDiscountBondModelDataBundle, RecombiningBinomialTree<Triple<Double, Double, Double>>> getTrees(final ZonedDateTime time) {
    Validate.notNull(time, "time");
    return new Function1D<StandardDiscountBondModelDataBundle, RecombiningBinomialTree<Triple<Double, Double, Double>>>() {

      @SuppressWarnings({"unchecked", "synthetic-access" })
      @Override
      public RecombiningBinomialTree<Triple<Double, Double, Double>> evaluate(final StandardDiscountBondModelDataBundle data) {
        Validate.notNull(data, "data");
        final double[][] r = new double[_n + 1][_j];
        final double[][] q = new double[_n + 1][_j];
        final double[][] d = new double[_n + 1][_j];
        final double[] u = new double[_n + 1];
        final double[] p = new double[_n + 2];
        final double t = DateUtils.getDifferenceInYears(data.getDate(), time);
        final double dt = t / _n;
        final double dtSqrt = Math.sqrt(dt);
        final double r1 = data.getShortRate(dt);
        final double sigma = data.getShortRateVolatility(dt);
        p[0] = 1.0;
        for (int i = 1; i <= _n + 1; i++) {
          p[i] = 1. / Math.pow((1 + data.getShortRate(i) * dt), dt * i);
        }
        q[0][0] = 1.;
        u[0] = r1;
        r[0][0] = r1;
        d[0][0] = 1. / (1 + r1 * dt);
        for (int i = 1; i <= _n; i++) {
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
        final Triple<Double, Double, Double>[][] result = new Triple[_n + 1][_j];
        for (int i = 0; i <= _n; i++) {
          for (int j = 0; j < _j; j++) {
            result[i][j] = new Triple<>(r[i][j], d[i][j], q[i][j]);
          }
        }
        return new RecombiningBinomialTree<>(result);
      }

    };
  }

  protected Function1D<Double, Double> getMedian(final double sigma, final int i, final double dt, final double[][] q, final double p) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double u) {
        double sum = 0.;
        final double dtSqrt = Math.sqrt(dt);
        for (int j = -i, k = 0; j <= i; j += 2, k++) {
          sum += q[i][k] / (1 + u * Math.exp(sigma * j * dtSqrt) * dt);

        }
        return sum - p;
      }
    };
  }
}
