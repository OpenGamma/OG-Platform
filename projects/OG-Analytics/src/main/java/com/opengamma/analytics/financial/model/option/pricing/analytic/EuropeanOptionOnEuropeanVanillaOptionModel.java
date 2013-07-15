/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.EuropeanOptionOnEuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class EuropeanOptionOnEuropeanVanillaOptionModel extends AnalyticOptionModel<EuropeanOptionOnEuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final ProbabilityDistribution<double[]> BIVARIATE = new BivariateNormalDistribution();
  private static final RealSingleRootFinder ROOT_FINDER = new BisectionSingleRootFinder();

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final EuropeanOptionOnEuropeanVanillaOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data, "data");
        final double s = data.getSpot();
        final OptionDefinition underlying = definition.getUnderlyingOption();
        final double k1 = definition.getStrike();
        final double k2 = underlying.getStrike();
        final ZonedDateTime date = data.getDate();
        final double t1 = definition.getTimeToExpiry(date);
        final double t2 = definition.getUnderlyingOption().getTimeToExpiry(date);
        final double deltaT = t2 - t1;
        final double sigma = data.getVolatility(t1, k1); //REVIEW emcleod 20-7-10 This will work with a flat volatility surface but otherwise will give odd results
        final double r = data.getInterestRate(t1);
        final double b = data.getCostOfCarry();
        final double criticalValue = getCriticalValue(new EuropeanVanillaOptionDefinition(k2, new Expiry(DateUtils.getDateOffsetWithYearFraction(date, deltaT)), underlying.isCall()), data, k1);
        final double d1 = getD1(s, criticalValue, t1, sigma, b);
        final double d2 = getD2(d1, sigma, t1);
        final double d3 = getD1(s, k2, t2, sigma, b);
        final double d4 = getD2(d3, sigma, t2);
        if (definition.isCall()) {
          final double rho = Math.sqrt(t1 / t2);
          if (underlying.isCall()) {
            return s * Math.exp(t2 * (b - r)) * BIVARIATE.getCDF(new double[] {d3, d1, rho}) - k2 * Math.exp(-r * t2) * BIVARIATE.getCDF(new double[] {d4, d2, rho}) - k1 * Math.exp(-r * t1)
                * NORMAL.getCDF(d2);
          }
          return k2 * Math.exp(-r * t2) * BIVARIATE.getCDF(new double[] {-d4, -d2, rho}) - s * Math.exp(t2 * (b - r)) * BIVARIATE.getCDF(new double[] {-d3, -d1, rho}) - k1 * Math.exp(-r * t1)
              * NORMAL.getCDF(-d2);
        }
        final double rho = -Math.sqrt(t1 / t2);
        if (underlying.isCall()) {
          return k2 * Math.exp(-r * t2) * BIVARIATE.getCDF(new double[] {d4, -d2, rho}) - s * Math.exp(t2 * (b - r)) * BIVARIATE.getCDF(new double[] {d3, -d1, rho}) + k1 * Math.exp(-r * t1)
              * NORMAL.getCDF(-d2);
        }
        return s * Math.exp(t2 * (b - r)) * BIVARIATE.getCDF(new double[] {-d3, d1, rho}) - k2 * Math.exp(-r * t2) * BIVARIATE.getCDF(new double[] {-d4, d2, rho}) + k1 * Math.exp(-r * t1)
            * NORMAL.getCDF(d2);
      }

    };
  }

  private double getCriticalValue(final OptionDefinition definition, final StandardOptionDataBundle data, final double k) {
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double x) {
        return k - BSM.getPricingFunction(definition).evaluate(data.withSpot(x));
      }

    };
    return ROOT_FINDER.getRoot(f, 0., 10000.);
  }
}
