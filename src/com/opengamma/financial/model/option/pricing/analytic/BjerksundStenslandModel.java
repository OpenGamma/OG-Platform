package com.opengamma.financial.model.option.pricing.analytic;

import java.util.Date;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurveTransformation;
import com.opengamma.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 */
public class BjerksundStenslandModel extends AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> {
  private final ProbabilityDistribution<Double[]> _bivariateNormal = new BivariateNormalDistribution();
  private final ProbabilityDistribution<Double> _normal = new NormalProbabilityDistribution(0, 1);
  protected BlackScholesMertonModel _bsm = new BlackScholesMertonModel();

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final AmericanVanillaOptionDefinition definition) {
    Function1D<StandardOptionDataBundle, Double> pricingFunction = new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(StandardOptionDataBundle data) {
        try {
          Date date = data.getDate();
          double s = data.getSpot();
          double k = definition.getStrike();
          double t = definition.getTimeToExpiry(date);
          double sigma = data.getVolatility(t, k);
          double r = data.getInterestRate(t);
          double b = data.getCostOfCarry();
          StandardOptionDataBundle newData = data;
          if (!definition.isCall()) {
            r -= b;
            b *= -1;
            DiscountCurve curve = DiscountCurveTransformation.getParallelShiftedCurve(data.getDiscountCurve(), -b);
            newData = new StandardOptionDataBundle(curve, b, data.getVolatilitySurface(), s, date);
          }
          if (b >= r) {
            EuropeanVanillaOptionDefinition european = new EuropeanVanillaOptionDefinition(definition.getStrike(), definition.getExpiry(), definition.isCall());
            Function1D<StandardOptionDataBundle, Double> bsm = _bsm.getPricingFunction(european);
            return bsm.evaluate(newData);
          }
          return getCallPrice(s, k, sigma, t, r, b);
        } catch (InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }
    };
    return pricingFunction;
  }

  double getCallPrice(double s, double k, double sigma, double t, double r, double b) {
    double a = 0.5 - b / (sigma * sigma);
    double beta = a + Math.sqrt(a * a + 2 * r / (sigma * sigma));
    double b0 = getB0(k, r, b);
    double bInfinity = getBInfinity(k, beta);
    double h1 = getH(k, b, getT1(t), sigma, b0, bInfinity);
    double h2 = getH(k, b, t, sigma, b0, bInfinity);
    double i2 = getI(b0, bInfinity, h2);
    if (s >= i2)
      return s - k;
    double i1 = getI(b0, bInfinity, h1);
    double alpha1 = Math.pow(i1, -beta) * (i1 - k);
    double alpha2 = Math.pow(i2, -beta) * (i2 - k);
    double t1 = getT1(t);
    return alpha2 * Math.pow(s, beta) - alpha2 * getPhi(s, t1, beta, i2, i2, b, sigma, r) + getPhi(s, t1, 1, i2, i2, b, sigma, r) - getPhi(s, t1, 1, i1, i1, b, sigma, r) - k
        * getPhi(s, t1, 0, i2, i2, b, sigma, r) + k * getPhi(s, t1, 0, i1, i2, b, sigma, r) + alpha1 * getPhi(s, t1, beta, i1, i2, b, sigma, r) - alpha1
        * getPsi(s, t, beta, i1, i2, i1, t1, b, sigma, r) + getPsi(s, t, 1, i1, i2, i1, t1, b, sigma, r) - getPsi(s, t, 1, k, i2, i1, t1, b, sigma, r) - k
        * getPsi(s, t, 0, i1, i2, i1, t1, b, sigma, r) + getPsi(s, t, 0, k, i2, i1, t1, b, sigma, r);
  }

  private double getB0(double k, double r, double b) {
    return k * Math.max(1, 1 * r / (r - b));
  }

  private double getBInfinity(double k, double beta) {
    return k * beta / (beta - 1);
  }

  private double getT1(double t) {
    return t * (Math.sqrt(5) - 1) / 2.;
  }

  private double getH(double k, double b, double t, double sigma, double b0, double bInfinity) {
    return -(b * t + 2 * sigma * Math.sqrt(t)) * (k * k / (b0 * (bInfinity - b0)));
  }

  private double getI(double b0, double bInfinity, double h) {
    return b0 + (bInfinity - b0) * (1 - Math.exp(h));
  }

  private double getLambda(double r, double b, double sigma, double gamma) {
    return -r + gamma * b + 0.5 * sigma * sigma * gamma * (gamma - 1);
  }

  private double getKappa(double b, double sigma, double gamma) {
    return 2 * b / (sigma * sigma) + 2 * gamma - 1;
  }

  private double getPhi(double s, double t, double gamma, double h, double i, double b, double sigma, double r) {
    double d = b + sigma * sigma * (gamma - 0.5);
    double denom = sigma * Math.sqrt(t);
    double d1 = getD((s / h), d, t, denom);
    double d2 = getD((i * i) / (s * h), d, t, denom);
    double lambda = getLambda(r, b, sigma, gamma);
    double kappa = getKappa(b, sigma, gamma);
    return Math.exp(lambda) * Math.pow(s, gamma) * (_normal.getCDF(-d1) - Math.pow(i / s, kappa) * _normal.getCDF(-d2));
  }

  private double getPsi(double s, double t, double gamma, double h, double i2, double i1, double t1, double b, double sigma, double r) {
    double denom = sigma * Math.sqrt(t);
    double denom1 = sigma * Math.sqrt(t1);
    double d = b + sigma * sigma * (gamma - 0.5);
    double x1 = s / i1;
    double x2 = i2 * i2 / (s * i1);
    double e1 = getD(x1, d, t1, denom1);
    double e2 = getD(x2, d, t1, denom1);
    double e3 = getD(x1, -d, t1, denom1);
    double e4 = getD(x2, -d, t1, denom1);
    double f1 = getD(s / h, d, t, denom);
    double f2 = getD((i1 * i1) / (s * h), d, t, denom);
    double f3 = getD((i2 * i2) / (s * h), d, t, denom);
    double f4 = getD((s * i1 * i1) / (h * i2 * i2), d, t, denom);
    double rho = Math.sqrt(t1 / t);
    double lambda = getLambda(r, b, sigma, gamma);
    double kappa = getKappa(b, sigma, gamma);
    return Math.exp(lambda * t)
        * Math.pow(s, gamma)
        * (_bivariateNormal.getCDF(new Double[] { -e1, -f1, rho }) - Math.pow(i2 / s, kappa) * _bivariateNormal.getCDF(new Double[] { -e2, -f2, rho }) - Math.pow(i1 / s, kappa)
            * _bivariateNormal.getCDF(new Double[] { -e3, -f3, -rho }) + Math.pow(i1 / i2, kappa) * _bivariateNormal.getCDF(new Double[] { -e4, -f4, -rho }));
  }

  private double getD(double x, double y, double t, double denom) {
    return (Math.log(x) + y * t) / denom;
  }
}
