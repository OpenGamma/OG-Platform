package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.CappedPowerOptionDefinition;
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
public class CappedPowerOptionModel extends AnalyticOptionModel<CappedPowerOptionDefinition, StandardOptionDataBundle> {
  final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final CappedPowerOptionDefinition definition) {
    Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(StandardOptionDataBundle data) {
        try {
          double s = data.getSpot();
          double k = definition.getStrike();
          double t = definition.getTimeToExpiry(data.getDate());
          double sigma = data.getVolatility(t, k);
          double r = data.getInterestRate(t);
          double b = data.getCostOfCarry();
          double power = definition.getPower();
          double cap = definition.getCap();
          boolean isCall = definition.isCall();
          double sigmaT = t * Math.sqrt(sigma);
          double x = t * (b + sigma * sigma * (power - 0.5));
          double d1 = getD(s / Math.pow(k, 1. / power), x, sigmaT);
          double d2 = d1 - power * sigmaT;
          double d3 = getD(isCall ? s / Math.pow(k + cap, 1. / power) : s / Math.pow(k - cap, 1. / power), x, sigmaT);
          double d4 = d3 - power * sigmaT;
          int sign = isCall ? 1 : -1;
          double df1 = Math.exp(-r * t);
          double df2 = Math.exp(t * ((power - 1) * (r + power * sigma * sigma * 0.5) - power * (r - b)));
          return sign
              * (Math.pow(s, power) * df2 * (_normalProbabilityDistribution.getCDF(sign * d1) - _normalProbabilityDistribution.getCDF(sign * d3)) + sign * df1
                  * (k * _normalProbabilityDistribution.getCDF(sign * d2) - (k + sign * cap) * _normalProbabilityDistribution.getCDF(sign * d4)));
        } catch (InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }

    };
    return pricingFunction;
  }

  double getD(double x, double y, double z) {
    return (Math.log(x) + y) / z;
  }
}
