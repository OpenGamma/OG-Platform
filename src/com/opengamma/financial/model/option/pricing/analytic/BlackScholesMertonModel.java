package com.opengamma.financial.model.option.pricing.analytic;

import java.util.Date;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
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
 * 
 *         Generalised Black-Scholes-Merton option pricing. Inputs will give:<br>
 *         <em>b = r</em> Black-Scholes stock option pricing model<br>
 *         <em>b = r - q</em> Merton stock option model with continuous dividend
 *         yield <em>q</em><br>
 *         <em>b = 0</em> Black future option model<br>
 *         <em>b = 0, r = 0</em> Asay margined future option model<br>
 *         <em>b = r - r<sub>f</sub></em> Garman-Kohlhagen FX option model, with
 *         foreign risk-free rate <em>r<sub>f</sub></em>
 */
public class BlackScholesMertonModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);

  @Override
  protected Function1D<StandardOptionDataBundle, Double, OptionPricingException> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
    Function1D<StandardOptionDataBundle, Double, OptionPricingException> pricingFunction = new Function1D<StandardOptionDataBundle, Double, OptionPricingException>() {

      @Override
      public Double evaluate(StandardOptionDataBundle data) throws OptionPricingException {
        try {
          Date date = data.getDate();
          double s = data.getSpot();
          double k = definition.getStrike();
          double t = definition.getTimeToExpiry(date);
          double r = data.getInterestRate(t);
          double b = data.getCostOfCarry();
          double sigma = data.getVolatility(t, k);
          double d1 = getD1(s, k, t, sigma, b);
          double d2 = getD2(d1, sigma, t);
          int sign = definition.isCall() ? 1 : -1;
          return sign * Math.exp(-r * t) * (s * Math.exp(b * t) * _normalProbabilityDistribution.getCDF(sign * d1) - k * _normalProbabilityDistribution.getCDF(sign * d2));
        } catch (InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }
    };
    return pricingFunction;
  }

  /*
   * @Override protected double getDelta(EuropeanVanillaOptionDefinition
   * definition, Function<Double, Double> pricingFunction, Double[]
   * functionVariables) { double df = getDF(functionVariables); double d1 =
   * getD1(functionVariables); return df * (definition.isCall() ?
   * _normalProbabilityDistribution.getCDF(d1) :
   * _normalProbabilityDistribution.getCDF(d1) - 1); }
   * 
   * @Override protected double getGamma(EuropeanVanillaOptionDefinition
   * definition, Function<Double, Double> pricingFunction, Double[]
   * functionVariables) { double df = getDF(functionVariables); double d1 =
   * getD1(functionVariables); return _normalProbabilityDistribution.getPDF(d1)
   * * df / (functionVariables[0] * functionVariables[2] *
   * Math.sqrt(functionVariables[3])); }
   */
}
