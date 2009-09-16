package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
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

public class CorradoSuSkewnessKurtosisModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, SkewKurtosisOptionDataBundle> {
  protected BlackScholesMertonModel _bsm;
  protected ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  public Function1D<SkewKurtosisOptionDataBundle, Double, OptionPricingException> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
    Function1D<SkewKurtosisOptionDataBundle, Double, OptionPricingException> pricingFunction = new Function1D<SkewKurtosisOptionDataBundle, Double, OptionPricingException>() {

      @Override
      public Double evaluate(SkewKurtosisOptionDataBundle data) throws OptionPricingException {
        try {
          double s = data.getSpot();
          double k = definition.getStrike();
          double t = definition.getTimeToExpiry(data.getDate());
          double sigma = data.getVolatility(t, k);
          double r = data.getInterestRate(t);
          double b = data.getCostOfCarry();
          double skew = data.getSkew();
          double kurtosis = data.getKurtosis();
          EuropeanVanillaOptionDefinition callDefinition = definition;
          if (!definition.isCall()) {
            callDefinition = new EuropeanVanillaOptionDefinition(callDefinition.getStrike(), callDefinition.getExpiry(), true);
          }
          Function1D<StandardOptionDataBundle, Double, OptionPricingException> bsm = _bsm.getPricingFunction(callDefinition);
          double bsmCall = bsm.evaluate(data);
          double w = getW(sigma, t, skew, kurtosis);
          double d = getD(s, k, sigma, t, b, w);
          double call = bsmCall + skew * getQ3(s, sigma, t, d, w) + (kurtosis - 3) * getQ4(s, sigma, t, d, w);
          if (!definition.isCall()) {
            return call - s * Math.exp((b - r) * t) + k * Math.exp(-r * t);
          }
          return call;
        } catch (InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }
    };
    return pricingFunction;
  }

  double getW(double sigma, double t, double skew, double kurtosis) {
    double sigma3 = sigma * sigma * sigma;
    return skew * sigma3 * Math.pow(t, 1.5) / 6. + kurtosis * sigma * sigma3 * t * t / 24.;
  }

  double getD(double s, double k, double sigma, double t, double b, double w) {
    return (Math.log(s / k) + t * (b + sigma * sigma / 2.) - Math.log(1 + w)) / (sigma * Math.sqrt(t));
  }

  double getQ3(double s, double sigma, double t, double d, double w) {
    double sigmaT = sigma * Math.sqrt(t);
    return s * sigmaT * (2 * sigmaT - d) * _normalProbabilityDistribution.getPDF(d) / (24 * (1 + w));
  }

  double getQ4(double s, double sigma, double t, double d, double w) {
    double sigmaT = sigma * Math.sqrt(t);
    return s * sigmaT * (d * d - 3 * d * sigmaT + 3 * sigmaT * sigmaT - 1) * _normalProbabilityDistribution.getPDF(d) / (24 * (1 + w));
  }
}
