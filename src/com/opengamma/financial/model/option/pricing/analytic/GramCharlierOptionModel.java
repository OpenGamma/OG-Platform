package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 */
public class GramCharlierOptionModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, SkewKurtosisOptionDataBundle> {
  ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<SkewKurtosisOptionDataBundle, Double> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
    Function1D<SkewKurtosisOptionDataBundle, Double> pricingFunction = new Function1D<SkewKurtosisOptionDataBundle, Double>() {

      @Override
      public Double evaluate(SkewKurtosisOptionDataBundle data) {
        try {
          double s = data.getSpot();
          double k = definition.getStrike();
          double t = definition.getTimeToExpiry(data.getDate());
          double sigma = data.getVolatility(t, k);
          double r = data.getInterestRate(t);
          double b = data.getCostOfCarry();
          double sqrtT = Math.sqrt(t);
          double skew = data.getSkew() / sqrtT;
          double kurtosis = data.getKurtosis() / t;
          double d1 = getD1(s, k, t, sigma, b);
          double sigmaT = sigma * sqrtT;
          double df1 = Math.exp(-r * t);
          double df2 = getDF(r, b, t);
          double callPrice = s * df2 * _normalProbabilityDistribution.getCDF(d1) - k * df1 * _normalProbabilityDistribution.getCDF(d1 - sigmaT) + s
              * _normalProbabilityDistribution.getPDF(d1) * sigmaT * (skew * (2 * sigmaT - d1) / 6. - kurtosis * (1 - d1 * d1 + 3 * d1 * sigmaT - 3 * sigmaT * sigmaT) / 24.);
          if (!definition.isCall()) {
            return callPrice + k * df1 - s * df2;
          }
          return callPrice;
        } catch (InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }
    };
    return pricingFunction;
  }
}
