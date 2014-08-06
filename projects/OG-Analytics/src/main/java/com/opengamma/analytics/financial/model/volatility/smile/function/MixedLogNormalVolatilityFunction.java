/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public final class MixedLogNormalVolatilityFunction extends VolatilityFunctionProvider<MixedLogNormalModelData> {
  private static final MixedLogNormalVolatilityFunction INSTANCE = new MixedLogNormalVolatilityFunction();

  public static MixedLogNormalVolatilityFunction getInstance() {
    return INSTANCE;
  }

  private MixedLogNormalVolatilityFunction() {
  }

  @Override
  public Function1D<MixedLogNormalModelData, Double> getVolatilityFunction(final EuropeanVanillaOption option, final double forward) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.isTrue(forward >= 0.0, "forward must be greater than zero");

    return new Function1D<MixedLogNormalModelData, Double>() {
      @Override
      public Double evaluate(final MixedLogNormalModelData data) {
        ArgumentChecker.notNull(data, "data");
        return getVolatility(option, forward, data);
      }
    };
  }

  public double getVolatility(final EuropeanVanillaOption option, final double forward, final MixedLogNormalModelData data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double price = getPrice(option, forward, data);
    final double t = option.getTimeToExpiry();
    final double k = option.getStrike();
    final boolean isCall = option.isCall();
    return BlackFormulaRepository.impliedVolatility(price, forward, k, t, isCall);
  }

  public double getPrice(final EuropeanVanillaOption option, final double forward, final MixedLogNormalModelData data) {
    final double[] w = data.getWeights();
    final double[] sigma = data.getVolatilities();
    final double[] rf = data.getRelativeForwards();
    final int n = w.length;
    final double t = option.getTimeToExpiry();
    final double k = option.getStrike();
    final boolean isCall = option.isCall();
    final double kStar = k / forward;
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += w[i] * BlackFormulaRepository.price(rf[i], kStar, t, sigma[i], isCall);
    }
    return forward * sum;
  }

  @Override
  public Function1D<MixedLogNormalModelData, double[]> getVolatilityAdjointFunction(final EuropeanVanillaOption option, final double forward) {
    ArgumentChecker.notNull(option, "option");
    final double strike = option.getStrike();
    final double expiry = option.getTimeToExpiry();

    return new Function1D<MixedLogNormalModelData, double[]>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public double[] evaluate(final MixedLogNormalModelData data) {
        return getVolatilityAdjoint(forward, strike, expiry, data);
      }
    };
  }

  @Override
  public Function1D<MixedLogNormalModelData, double[][]> getVolatilityAdjointFunction(final double forward, final double[] strikes, final double timeToExpiry) {
    return getVolatilityAdjointFunctionByCallingSingleStrikes(forward, strikes, timeToExpiry);
  }

  @Override
  public Function1D<MixedLogNormalModelData, double[]> getModelAdjointFunction(final EuropeanVanillaOption option, final double forward) {
    ArgumentChecker.notNull(option, "option");
    final double strike = option.getStrike();
    final double expiry = option.getTimeToExpiry();

    return new Function1D<MixedLogNormalModelData, double[]>() {
      @Override
      public double[] evaluate(final MixedLogNormalModelData data) {
        return getModelAjoint(forward, strike, expiry, data);
      }
    };
  }

  @Override
  public Function1D<MixedLogNormalModelData, double[][]> getModelAdjointFunction(final double forward, final double[] strikes, final double timeToExpiry) {
    return getModelAdjointFunctionByCallingSingleStrikes(forward, strikes, timeToExpiry);
  }

  private double[] getVolatilityAdjoint(final double forward, final double strike, final double expiry, final MixedLogNormalModelData data) {

    final int nParms = data.getNumberOfParameters();
    final boolean isCall = strike >= forward;

    final double[] sigmas = data.getVolatilities();
    final double[] rFwds = data.getRelativeForwards();
    final double[] w = data.getWeights();
    final int n = sigmas.length;
    final double[] deltas = new double[n];
    final double[] dualDeltas = new double[n];
    for (int i = 0; i < n; i++) {
      final double f = forward * rFwds[i];
      deltas[i] = BlackFormulaRepository.delta(f, strike, expiry, sigmas[i], isCall);
      dualDeltas[i] = BlackFormulaRepository.dualDelta(f, strike, expiry, sigmas[i], isCall);
    }

    final double impVol = getVolatility(new EuropeanVanillaOption(strike, expiry, isCall), forward, data);
    final double vega = BlackFormulaRepository.vega(forward, strike, expiry, impVol);
    final double delta = BlackFormulaRepository.delta(forward, strike, expiry, impVol, isCall);
    final double dualDelta = BlackFormulaRepository.dualDelta(forward, strike, expiry, impVol, isCall);

    final double[] res = new double[nParms + 3];
    res[0] = impVol;
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += w[i] * rFwds[i] * deltas[i];
    }
    res[1] = (sum - delta) / vega; //fBar
    sum = 0.0;
    for (int i = 0; i < n; i++) {
      sum += w[i] * dualDeltas[i];
    }
    res[2] = (sum - dualDelta) / vega; //strikeBar

    //calculate the sensitivity to model parameters
    final double[] modelAjoint = getModelAjoint(forward, strike, expiry, data, deltas, vega);
    System.arraycopy(modelAjoint, 0, res, 3, nParms);

    return res;
  }

  public double[] getModelAjoint(final double forward, final double strike, final double expiry, final MixedLogNormalModelData data) {

    final boolean isCall = strike >= forward;
    final double[] sigmas = data.getVolatilities();
    final double[] rFwds = data.getRelativeForwards();
    final int n = sigmas.length;
    final double[] deltas = new double[n];

    for (int i = 0; i < n; i++) {
      deltas[i] = BlackFormulaRepository.delta(forward * rFwds[i], strike, expiry, sigmas[i], isCall);
    }

    final double impVol = getVolatility(new EuropeanVanillaOption(strike, expiry, isCall), forward, data);
    final double vega = BlackFormulaRepository.vega(forward, strike, expiry, impVol);

    return getModelAjoint(forward, strike, expiry, data, deltas, vega);
  }

  private double[] getModelAjoint(final double forward, final double strike, final double expiry, final MixedLogNormalModelData data, final double[] deltas, final double vega) {
    final boolean isCall = strike >= forward;
    final int nParms = data.getNumberOfParameters();
    final double[] sigmas = data.getVolatilities();
    final double[] rFwds = data.getRelativeForwards();
    final double[] w = data.getWeights();
    final int n = sigmas.length;
    final double[] prices = new double[n];
    final double[] vegas = new double[n];
    for (int i = 0; i < n; i++) {
      final double f = forward * rFwds[i];
      prices[i] = BlackFormulaRepository.price(f, strike, expiry, sigmas[i], isCall);
      vegas[i] = BlackFormulaRepository.vega(f, strike, expiry, sigmas[i]);
    }

    final double[] res = new double[nParms];
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      sum += w[i] * vegas[i];
    }
    res[0] = sum / vega;
    for (int i = 1; i < n; i++) {
      sum = 0.0;
      for (int j = i; j < n; j++) {
        sum += w[j] * vegas[j];
      }
      res[i] = sum / vega;
    }

    final double[][] wJac = data.getWeightsJacobian();
    for (int i = 0; i < n - 1; i++) {
      sum = 0.0;
      for (int j = 0; j < n; j++) {
        sum += prices[j] * wJac[j][i];
      }
      res[n + i] = sum / vega;
    }

    if (nParms > 2 * n - 1) {
      final double[][] fJac = data.getRelativeForwardsJacobian();
      for (int i = 0; i < n - 1; i++) {
        sum = 0.0;
        for (int j = 0; j < n; j++) {
          sum -= rFwds[j] * deltas[j] * wJac[j][i];
        }
        res[n + i] += sum * forward / vega;

        sum = 0.0;
        for (int j = 0; j < n; j++) {
          sum += deltas[j] * fJac[j][i];
        }
        res[2 * n - 1 + i] = sum * forward / vega;
      }
    }

    return res;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Mixed log normal";
  }

  @Override
  public int getNumberOfParameters() {
    throw new NotImplementedException();
  }

  @Override
  public MixedLogNormalModelData toModelData(final double[] parameters) {
    return new MixedLogNormalModelData(parameters);
  }
}
