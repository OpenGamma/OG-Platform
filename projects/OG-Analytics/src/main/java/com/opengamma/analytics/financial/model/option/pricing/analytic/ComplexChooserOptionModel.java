/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.ComplexChooserOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class ComplexChooserOptionModel extends AnalyticOptionModel<ComplexChooserOptionDefinition, StandardOptionDataBundle> {
  private static final RealSingleRootFinder ROOT_FINDER = new BisectionSingleRootFinder();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final ProbabilityDistribution<double[]> BIVARIATE_NORMAL = new BivariateNormalDistribution();

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final ComplexChooserOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s = data.getSpot();
        final double kCall = definition.getCallStrike();
        final double kPut = definition.getPutStrike();
        final ZonedDateTime date = data.getDate();
        final double tCall = definition.getTimeToCallExpiry(date);
        final double tPut = definition.getTimeToPutExpiry(date);
        final double tChoose = definition.getTimeToExpiry(date);
        final double sigma = data.getVolatility(tChoose, kCall);
        final double r = data.getInterestRate(tChoose);
        final double b = data.getCostOfCarry();
        final double deltaTCall = tCall - tChoose;
        final double deltaTPut = tPut - tChoose;
        final double criticalValue = getCriticalValue(Math.exp(deltaTCall * (b - r)), Math.exp(-deltaTCall * r), Math.exp(deltaTPut * (b - r)), Math.exp(-deltaTPut * r), sigma, b, kCall, kPut,
            deltaTCall, deltaTPut);
        final double d1 = getD1(s, criticalValue, tChoose, sigma, b);
        final double d2 = getD2(d1, sigma, tChoose);
        final double d3 = getD1(s, kCall, tCall, sigma, b);
        final double d4 = getD2(d3, sigma, tCall);
        final double d5 = getD1(s, kPut, tPut, sigma, b);
        final double d6 = getD2(d5, sigma, tPut);
        final double rho1 = Math.sqrt(tChoose / tCall);
        final double rho2 = Math.sqrt(tChoose / tPut);
        return s * Math.exp(tCall * (b - r)) * BIVARIATE_NORMAL.getCDF(new double[] {d1, d3, rho1}) - kCall * Math.exp(-r * tCall) * BIVARIATE_NORMAL.getCDF(new double[] {d2, d4, rho1}) - s
            * Math.exp(tPut * (b - r)) * BIVARIATE_NORMAL.getCDF(new double[] {-d1, -d5, rho2}) + kPut * Math.exp(-r * tPut) * BIVARIATE_NORMAL.getCDF(new double[] {-d2, -d6, rho2});
      }

    };
  }

  private Double getCriticalValue(final double dfCall1, final double dfCall2, final double dfPut1, final double dfPut2, final double sigma, final double b, final double kCall, final double kPut,
      final double deltaTCall, final double deltaTPut) {
    final Function1D<Double, Double> function = new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double criticalValue) {
        final double d1Call = getD1(criticalValue, kCall, deltaTCall, sigma, b);
        final double d2Call = getD2(d1Call, sigma, deltaTCall);
        final double d1Put = getD1(criticalValue, kPut, deltaTPut, sigma, b);
        final double d2Put = getD2(d1Put, sigma, deltaTPut);
        return criticalValue * dfCall1 * NORMAL.getCDF(d1Call) - kCall * dfCall2 * NORMAL.getCDF(d2Call) + criticalValue * dfPut1 * NORMAL.getCDF(-d1Put) - kPut * dfPut2 * NORMAL.getCDF(-d2Put);
      }

    };
    return ROOT_FINDER.getRoot(function, 1e-9, 1000.);
  }
}
