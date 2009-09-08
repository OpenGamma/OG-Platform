package com.opengamma.financial.model.option.pricing.analytic;

import java.util.Date;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.HullWhiteStochasticVolatilityModelOptionDataBundle;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 */

public class HullWhiteStochasticVolatilityOptionModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, HullWhiteStochasticVolatilityModelOptionDataBundle> {
  // TODO see if case when rho = 0 gives same results for uncorrelated model -
  // if so, add to the pricing function

  static final double ZERO = 1e-4;
  private final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);
  protected BlackScholesMertonModel _bsm = new BlackScholesMertonModel();

  @Override
  protected Function1D<HullWhiteStochasticVolatilityModelOptionDataBundle, Double> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
    Function1D<HullWhiteStochasticVolatilityModelOptionDataBundle, Double> pricingFunction = new Function1D<HullWhiteStochasticVolatilityModelOptionDataBundle, Double>() {

      @Override
      public Double evaluate(HullWhiteStochasticVolatilityModelOptionDataBundle data) {
        try {
          Date date = data.getDate();
          double s = data.getSpot();
          double k = definition.getStrike();
          double t = definition.getTimeToExpiry(date);
          double sigma = data.getVolatility(t, k);
          double r = data.getInterestRate(t);
          double b = data.getCostOfCarry();
          double lambda = data.getHalfLife();
          double sigmaLR = data.getLongRunVolatility();
          double volOfSigma = data.getVolatilityOfVolatility();
          double rho = data.getCorrelation();
          StandardOptionDataBundle bsmData = new StandardOptionDataBundle(data.getDiscountCurve(), b, data.getVolatilitySurface(), data.getSpot(), date);
          double df = getDF(r, b, t);
          double beta = -Math.log(2) / lambda;
          double alpha = -beta * sigmaLR * sigmaLR;
          double delta = beta * t;
          double eDelta = Math.exp(delta);
          boolean betaIsZero = Math.abs(beta) < ZERO;
          double meanVariance = getMeanVariance(betaIsZero, sigma * sigma, t, alpha, beta, eDelta, delta);
          double meanStd = Math.sqrt(meanVariance);
          // TODO replace vol with Math.sqrt(meanVariance);
          double d1 = getD1(s, k, t, meanStd, b);
          double d2 = getD2(d1, meanStd, t);
          double f0 = _bsm.getPricingFunction(definition).evaluate(bsmData);
          double f1 = getF1(betaIsZero, s, t, df, d1, d2, meanVariance, alpha, beta, eDelta, delta, rho);
          double f2 = getF2(betaIsZero, s, t, df, d1, d2, meanVariance, alpha, beta, eDelta, delta, rho);
          double callPrice = f0 + f1 * volOfSigma + f2 * volOfSigma * volOfSigma;
          if (!definition.isCall()) {
            return callPrice - s * df + k * Math.exp(-r * t);
          }
          return callPrice;
        } catch (InterpolationException e) {
          return null;
          // TODO
        }
      }

    };
    return pricingFunction;
  }

  double getMeanVariance(boolean betaIsZero, double variance, double t, double alpha, double beta, double eDelta, double delta) {
    if (betaIsZero) {
      return variance + alpha * t / 2.;
    }
    double ratio = alpha / beta;
    return (variance + ratio) * (eDelta - 1) / delta - ratio;
  }

  double getPhi1(boolean betaIsZero, double variance, double t, double rho, double alpha, double beta, double eDelta, double delta) {
    if (betaIsZero) {
      return rho * rho * t * t * t * (variance + alpha * t / 4.) / 6.;
    }
    return rho * rho * ((alpha + beta * variance) * (eDelta * (delta * delta / 2. - delta + 1) - 1) + alpha * (eDelta * (2 - delta) - (2 + delta))) / (beta * beta * beta * beta);
  }

  double getPhi2(boolean betaIsZero, double variance, double rho, double alpha, double beta, double eDelta, double delta, double phi1) {
    if (betaIsZero) {
      return phi1 * (2 + 1. / (rho * rho));
    }
    double eDelta2 = eDelta * eDelta;
    return 2 * phi1 + ((alpha + beta * variance) * (eDelta2 - 2 * eDelta * delta - 1) - alpha * (eDelta2 - 4 * eDelta + 2 * delta + 3) / 2.) / (2 * beta * beta * beta * beta);
  }

  double getIota(double variance, double rho, double alpha, double beta, double eDelta, double delta) {
    return rho * ((alpha + beta * variance) * (eDelta - delta * eDelta - 1) - alpha * (1 + delta - eDelta)) / (beta * beta * beta);
  }

  double getPhi3(boolean betaIsZero, double variance, double t, double rho, double alpha, double beta, double eDelta, double delta) {
    if (betaIsZero) {
      double a = rho * t * (variance + alpha * t / 3.);
      return a * a / 8.;
    }
    double a = getIota(variance, rho, alpha, beta, eDelta, delta);
    return a * a / 2.;
  }

  double getdCdSdV(double s, double df, double nD1, double d2, double variance) {
    return -s * df * nD1 * d2 / (2 * variance);
  }

  double getdCdVdV(double s, double df, double nD1, double d1, double d2, double variance) {
    return s * df * nD1 * (d1 * d2 - 1) / (4 * Math.pow(variance, 1.5));
  }

  double getdCdSdVdV(double s, double df, double nD1, double d1, double d2, double variance) {
    return s * df * nD1 * (-d1 * d2 * d2 + d1 + 2 * d2) / (4 * variance * variance);
  }

  double getdCdVdVdV(double s, double df, double nD1, double d1, double d2, double variance) {
    // TODO multiply out brackets and simplify
    return s * df * nD1 * ((d1 * d2 - 1) * (d1 * d2 - 3) - (d1 * d1 + d2 * d2)) / (8 * Math.pow(variance, 2.5));
  }

  double getF1(boolean betaIsZero, double s, double t, double df, double d1, double d2, double variance, double alpha, double beta, double eDelta, double delta, double rho) {
    double nD1 = _normalProbabilityDistribution.getPDF(d1);
    double dCdSdV = getdCdSdV(s, df, nD1, d2, variance);
    if (betaIsZero) {
      return rho * t * dCdSdV * (variance + alpha * t / 3.) / 2.;
    }
    return getIota(variance, rho, alpha, beta, eDelta, delta) * dCdSdV / t;
  }

  double getF2(boolean betaIsZero, double s, double t, double df, double d1, double d2, double variance, double alpha, double beta, double eDelta, double delta, double rho) {
    double phi1 = getPhi1(betaIsZero, variance, t, rho, alpha, beta, eDelta, delta);
    double phi2 = getPhi2(betaIsZero, variance, rho, alpha, beta, eDelta, delta, phi1);
    double phi3 = getPhi3(betaIsZero, variance, t, rho, alpha, beta, eDelta, delta);
    double phi4 = 2 * phi3;
    double nD1 = _normalProbabilityDistribution.getPDF(d1);
    double dCdSdV = getdCdSdV(s, df, nD1, d2, variance);
    double dCdVdV = getdCdVdV(s, df, nD1, d1, d2, variance);
    double dCdSdVdV = getdCdSdVdV(s, df, nD1, d1, d2, variance);
    double dCdVdVdV = getdCdVdVdV(s, df, nD1, d1, d2, variance);
    return (phi1 * dCdSdV + phi2 * dCdVdV / t + phi3 * dCdSdVdV / t + phi4 * dCdVdVdV / (t * t)) / t;
  }
}
