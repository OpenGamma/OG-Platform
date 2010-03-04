/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import java.util.Collections;
import java.util.Set;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 */
public class JuZhongModel extends AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> {
  protected final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);
  protected final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  protected final Set<Greek> PRICE = Collections.singleton(Greek.PRICE);
  protected final RealSingleRootFinder FINDER = new BisectionSingleRootFinder();

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final AmericanVanillaOptionDefinition definition) {
    if (definition == null)
      throw new IllegalArgumentException("Definition was null");
    final double phi = definition.isCall() ? 1 : -1;
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        if (data == null)
          throw new IllegalArgumentException("Data bundle was null");
        final GreekResultCollection bsmResult = BSM.getGreeks(definition, data, PRICE);
        final double bsmPrice = ((SingleGreekResult) bsmResult.get(Greek.PRICE)).getResult();
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
        if (phi * (sEstimate - s) <= 0)
          return phi * (s - k);
        final double estimatePrice = ((SingleGreekResult) BSM.getGreeks(definition, data.withSpot(sEstimate), PRICE).get(Greek.PRICE)).getResult();
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
    return sEstimate * _normal.getPDF(d1) * sigma * df / (2 * r * Math.sqrt(t)) - phi * b * sEstimate * _normal.getCDF(phi * d1) * df / r + phi * k * _normal.getCDF(phi * d2);
  }

  protected Function1D<Double, Double> getFunction(final double phi, final double df, final double k, final double t, final double sigma, final double b, final double lambda,
      final OptionDefinition definition, final StandardOptionDataBundle data) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        final GreekResultCollection bsmPrice = BSM.getGreeks(definition, data.withSpot(x), PRICE);
        final double price = ((SingleGreekResult) bsmPrice.get(Greek.PRICE)).getResult();
        return phi * (df * _normal.getCDF(phi * getD1(x, k, t, sigma, b)) + lambda * (phi * (x - k) - price) / x - 1);
      }

    };
  }
}
