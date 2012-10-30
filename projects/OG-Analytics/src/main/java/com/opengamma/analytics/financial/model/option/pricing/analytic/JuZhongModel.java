/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class JuZhongModel extends AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final Set<Greek> PRICE = Collections.singleton(Greek.FAIR_PRICE);
  private static final RealSingleRootFinder FINDER = new BisectionSingleRootFinder();

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final AmericanVanillaOptionDefinition definition) {
    Validate.notNull(definition);
    final double phi = definition.isCall() ? 1 : -1;
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        final GreekResultCollection bsmResult = BSM.getGreeks(definition, data, PRICE);
        final double bsmPrice = bsmResult.get(Greek.FAIR_PRICE);
        final double s = data.getSpot();
        final double k = definition.getStrike();
        final double t = definition.getTimeToExpiry(data.getDate());
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        final double sigma = data.getVolatility(t, k);
        final double sigmaSq = sigma * sigma;
        final double h = getH(r, t);
        final double alpha = getAlpha(r, sigmaSq);
        final double beta = getBeta(r, b, sigmaSq);
        final double lambda = getLambda(phi, alpha, beta, h);
        final double lambdaDash = getLambdaDash(phi, alpha, beta, h);
        final Function1D<Double, Double> function = getFunction(phi, Math.exp(-b * t), k, t, sigmaSq, b, lambda, definition, data);
        final double sEstimate = FINDER.getRoot(function, 0., s * 2);
        if (phi * (sEstimate - s) <= 0) {
          return phi * (s - k);
        }
        final double estimatePrice = BSM.getGreeks(definition, data.withSpot(sEstimate), PRICE).get(Greek.FAIR_PRICE);
        final double hA = phi * (sEstimate - k) - estimatePrice;
        final double derivative = getDerivative(k, r, b, t, sigma, phi, sEstimate);
        final double c = getC(h, alpha, lambdaDash, lambda, beta);
        final double d = getD(h, alpha, derivative, hA, lambdaDash, lambda, beta);
        final double ratio = Math.log(s / sEstimate);
        final double chi = c * Math.pow(ratio, 2) + d * ratio;
        return bsmPrice + hA * Math.pow(s / sEstimate, lambda) / (1 - chi);
      }

    };
    return pricingFunction;
  }

  protected double getH(final double r, final double t) {
    return 1 - Math.exp(-r * t);
  }

  protected double getAlpha(final double r, final double sigmaSq) {
    return 2 * r / sigmaSq;
  }

  protected double getBeta(final double r, final double b, final double sigmaSq) {
    return 2 * (r - b) / sigmaSq;
  }

  protected double getLambda(final double phi, final double alpha, final double beta, final double h) {
    final double beta1 = beta - 1;
    return (-beta + phi * Math.sqrt(beta1 * beta1 + 4 * alpha / h)) / 2;
  }

  protected double getLambdaDash(final double phi, final double alpha, final double beta, final double h) {
    return phi * alpha / (h * h * Math.sqrt((beta - 1) * (beta - 1) + 4 * alpha / h));
  }

  protected double getC(final double h, final double alpha, final double lambdaDash, final double lambda, final double beta) {
    return (1 - h) * alpha * lambdaDash / (2 * (2 * lambda + beta - 1));
  }

  protected double getD(final double h, final double alpha, final double derivative, final double hA, final double lambdaDash, final double lambda, final double beta) {
    final double denom = 2 * lambda + beta - 1;
    return (1 - h) * alpha * (derivative / hA + 1 / h + lambdaDash / denom) / denom;
  }

  protected double getDerivative(final double k, final double r, final double b, final double t, final double sigma, final double phi, final double sEstimate) {
    final double df = Math.exp(t * (r - b));
    final double d1 = getD1(sEstimate, k, t, sigma, b);
    final double d2 = getD2(d1, sigma, t);
    return sEstimate * NORMAL.getPDF(d1) * sigma * df / (2 * r * Math.sqrt(t)) - phi * b * sEstimate * NORMAL.getCDF(phi * d1) * df / r + phi * k * NORMAL.getCDF(phi * d2);
  }

  protected Function1D<Double, Double> getFunction(final double phi, final double df, final double k, final double t, final double sigma, final double b, final double lambda,
      final OptionDefinition definition, final StandardOptionDataBundle data) {
    return new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double x) {
        final GreekResultCollection bsmPrice = BSM.getGreeks(definition, data.withSpot(x), PRICE);
        final double price = bsmPrice.get(Greek.FAIR_PRICE);
        return phi * (df * NORMAL.getCDF(phi * getD1(x, k, t, sigma, b)) + lambda * (phi * (x - k) - price) / x - 1);
      }

    };
  }
}
