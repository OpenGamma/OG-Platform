package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.AsymmetricPowerOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
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
public class AsymmetricPowerOptionModel extends AnalyticOptionModel<AsymmetricPowerOptionDefinition, StandardOptionDataBundle> {
  private final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double, OptionPricingException> getPricingFunction(final AsymmetricPowerOptionDefinition definition) {
    Function1D<StandardOptionDataBundle, Double, OptionPricingException> pricingFunction = new Function1D<StandardOptionDataBundle, Double, OptionPricingException>() {

      @Override
      public Double evaluate(StandardOptionDataBundle data) throws OptionPricingException {
        try {
          double s = data.getSpot();
          double k = definition.getStrike();
          double t = definition.getTimeToExpiry(data.getDate());
          double sigma = data.getVolatility(t, k);
          double r = data.getInterestRate(t);
          double b = data.getCostOfCarry();
          return getPrice(s, k, sigma, t, r, b, definition.getPower(), definition.isCall());
        } catch (InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }

    };
    return pricingFunction;
  }

  double getPrice(double s, double k, double sigma, double t, double r, double b, double power, boolean isCall) {
    double denom = sigma * Math.sqrt(t);
    double d1 = (Math.log(s * Math.pow(k, power)) + t * (b + sigma * sigma * (power - 0.5))) / denom;
    double d2 = d1 - power * denom;
    int sign = isCall ? 1 : -1;
    double df1 = Math.exp(((power - 1) * (r + power * sigma * sigma * 0.5) - power * (r - b)) * t);
    double df2 = Math.exp(-r * t);
    return sign * (Math.pow(s, power) * df1 * _normalProbabilityDistribution.getCDF(sign * d1) - df2 * k * _normalProbabilityDistribution.getCDF(sign * d2));
  }
}
