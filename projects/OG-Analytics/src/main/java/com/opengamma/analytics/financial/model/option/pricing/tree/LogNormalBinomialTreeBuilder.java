/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.GeneralLogNormalOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Builds a binomial tree where the nodes are set to locally match a log-normal process. The process that the tree is emulating is of the form  df/f = mu(f,t)dt + sigma(f,t)dw.
 * From a node at (f,t) the two daughter nodes f+ and f- (at time t + dt) are set such that p*(1-p)*(ln(f+/f-))^2 = dt*sigma(f,t)^2, where p is the probability of reaching f+ from f. 
 * The forwarding condition is p*f+ + (1-p)*f- = f*exp(mu(f,t)*dt). This is adapted from the paper Derman and Kani, The Volatility Smile and Its Implied Tree
 * @param <T> A GeneralLogNormalOptionDataBundle or anything that extends it 
 */
public class LogNormalBinomialTreeBuilder<T extends GeneralLogNormalOptionDataBundle> extends BinomialTreeBuilder<T> {

  private static final double EPS = 1e-8;
  private static final RealSingleRootFinder s_root = new BrentSingleRootFinder();
  private static BracketRoot s_bracketRoot = new BracketRoot();

  @Override
  protected double[] getForwards(double[] spots, T data, double t, double dt) {
    int n = spots.length;
    double[] forwards = new double[n];
    for (int i = 0; i < n; i++) {
      double drift = data.getLocalDrift(spots[i], t);
      forwards[i] = spots[i] * Math.exp(drift * dt);
    }
    return forwards;
  }

  @Override
  protected DoublesPair getCentralNodePair(double dt, double sigma, double forward, double centreLevel) {

    Function1D<Double, Double> func = new CentreNode(dt, sigma, forward, centreLevel);
    double[] limits = s_bracketRoot.getBracketedPoints(func, forward, forward * Math.exp(sigma * Math.sqrt(dt)));

    double upper = s_root.getRoot(func, limits[0], limits[1]);
    double lower = centreLevel * centreLevel / upper;
    return DoublesPair.of(lower, upper);
  }

  @Override
  protected double getNextHigherNode(double dt, double sigma, double forward, double lowerNode) {
    Function1D<Double, Double> func = new UpperNodes(dt, sigma, forward, lowerNode);
    double fTry = forward * Math.exp(sigma * Math.sqrt(dt));
    //ensure we do not get p = 1 and thus a divide by zero
    double[] limits = s_bracketRoot.getBracketedPoints(func, (forward - lowerNode) / 0.6 + lowerNode, (forward - lowerNode) / 0.4 + lowerNode, forward * (1 + EPS), 10 * fTry);
    return s_root.getRoot(func, limits[0], limits[1]);
  }

  @Override
  protected double getNextLowerNode(double dt, double sigma, double forward, double higherNode) {
    if (forward == 0.0) {
      return 0.0;
    }
    Function1D<Double, Double> func = new LowerNodes(dt, sigma, forward, higherNode);
    double[] limits = s_bracketRoot.getBracketedPoints(func, forward * Math.exp(-sigma * Math.sqrt(dt)), forward);
    return s_root.getRoot(func, limits[0], limits[1]);
  }

  /**
   * The root of this function gives the next node above the currently know one 
   */
  private class UpperNodes extends Function1D<Double, Double> {

    private double _rootdt;
    private double _sigma;
    private double _f;
    private double _s;

    public UpperNodes(final double dt, final double sigma, final double forward, final double s) {
      _rootdt = Math.sqrt(dt);
      _sigma = sigma;
      _f = forward;
      _s = s;
    }

    @Override
    public Double evaluate(Double x) {
      double p = (_f - _s) / (x - _s);

      double res = _s * Math.exp(_rootdt * _sigma / Math.sqrt(p * (1 - p))) - x;
      return res;
    }
  }

  private class LowerNodes extends Function1D<Double, Double> {

    private double _rootdt;
    private double _sigma;
    private double _f;
    private double _s;

    public LowerNodes(final double dt, final double sigma, final double forward, final double s) {
      _rootdt = Math.sqrt(dt);
      _sigma = sigma;
      _f = forward;
      _s = s;
    }

    @Override
    public Double evaluate(Double x) {
      double p = (_f - x) / (_s - x);
      double res = _s * Math.exp(-_rootdt * _sigma / Math.sqrt(p * (1 - p))) - x;
      return res;
    }
  }

  private class CentreNode extends Function1D<Double, Double> {

    private double _rootdt;
    private double _sigma;
    private double _f;
    private double _spot;

    public CentreNode(final double dt, final double sigma, final double forward, final double spot) {
      _rootdt = Math.sqrt(dt);
      _sigma = sigma;
      _f = forward;
      _spot = spot;
    }

    @Override
    public Double evaluate(Double x) {
      double p;
      if (_f == _spot) {
        p = _f / (x + _f);
      } else {
        Validate.isTrue(x != _spot, "invalide x");
        p = (x * _f - _spot * _spot) / (x * x - _spot * _spot);
      }
      double res = _spot * _spot * Math.exp(_rootdt * _sigma / Math.sqrt(p * (1 - p))) - x * x;
      return res;
    }
  }

}
