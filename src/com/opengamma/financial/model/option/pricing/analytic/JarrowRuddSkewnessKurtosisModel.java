package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;

/**
 * 
 * @author emcleod
 * 
 */

public class JarrowRuddSkewnessKurtosisModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, SkewKurtosisOptionDataBundle> {
  protected BlackScholesMertonModel _bsm;

  @Override
  protected Function1D<SkewKurtosisOptionDataBundle, Double> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
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
          double skew = data.getSkew();
          double kurtosis = data.getKurtosis();
          EuropeanVanillaOptionDefinition callDefinition = definition;
          if (!definition.isCall()) {
            callDefinition = new EuropeanVanillaOptionDefinition(callDefinition.getStrike(), callDefinition.getExpiry(), true);
          }
          Function1D<StandardOptionDataBundle, Double> bsm = _bsm.getPricingFunction(callDefinition);
          double bsmCall = bsm.evaluate(data);
          double d2 = getD2(getD1(s, k, t, sigma, b), sigma, t);
          double a = getA(d2, k, sigma, t);
          double call = bsmCall + getLambda1(sigma, t, skew) * getQ3(s, k, sigma, t, r, a, d2) + getLambda2(sigma, t, kurtosis) * getQ4(s, k, sigma, t, r, a, d2);
          if (!definition.isCall()) {
            return call - s * Math.exp((b - r) * t) + k * Math.exp(-r * t);
          }
          return call;
        } catch (InterpolationException e) {
          return null;
        }
      }

    };
    return pricingFunction;
  }

  double getA(double d2, double k, double sigma, double t) {
    return Math.exp(-d2 * d2 / 2.) / (k * sigma * 2 * Math.PI * Math.sqrt(t));
  }

  double getLambda1(double sigma, double t, double skew) {
    double q = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    double skewDistribution = q * (3 + q * q);
    return skew - skewDistribution;
  }

  double getLambda2(double sigma, double t, double kurtosis) {
    double q = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    double q2 = q * q;
    double q4 = q2 * q2;
    double q6 = q4 * q2;
    double q8 = q6 * q2;
    double kurtosisDistribution = 16 * q2 + 15 * q4 + 6 * q6 + q8 + 3;
    return kurtosis - kurtosisDistribution;
  }

  double getQ3(double s, double k, double sigma, double t, double r, double a, double d2) {
    double sigmaT = sigma * Math.sqrt(t);
    double da = a * (d2 - sigmaT) / (k * sigmaT);
    double df = Math.exp(-r * t);
    return -Math.pow(s * df, 3) * Math.pow(Math.exp(sigmaT * sigmaT - 1), 1.5) * df * da / 6.;
  }

  double getQ4(double s, double k, double sigma, double t, double r, double a, double d2) {
    double sigmaT = sigma * Math.sqrt(t);
    double sub = d2 - sigmaT;
    double da2 = a * (sub * sub - sigmaT * sub - 1) / (k * k * sigmaT * sigmaT);
    double df = Math.exp(-r * t);
    return Math.pow(s * df, 4) * Math.pow(Math.exp(sigmaT * sigmaT) - 1, 2) * df * da2 / 24.;
  }
}
