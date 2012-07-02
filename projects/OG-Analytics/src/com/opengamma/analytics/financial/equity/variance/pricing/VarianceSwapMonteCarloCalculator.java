/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import java.util.Arrays;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * Monte Carlo calculator to price a variance swap in the presence of discrete dividends. <p>
 * <b>Note</b> this is primarily to test other numerical methods 
 */
public class VarianceSwapMonteCarloCalculator {

  private static final int N_SIM_VARIABLES = 3;
  private static final int DAYS_PER_YEAR = 252;

  private final boolean _useAntithetics;
  private final boolean _calculateVariance;
  private final NormalDistribution _norm;

  public VarianceSwapMonteCarloCalculator(final int seed) {
    _useAntithetics = true;
    _calculateVariance = true;
    final RandomEngine random = new MersenneTwister64(seed);
    _norm = new NormalDistribution(0, 1.0, random);
  }

  public VarianceSwapMonteCarloCalculator(final int seed, final boolean useAntithetics, final boolean calculateVariance) {
    _useAntithetics = useAntithetics;
    _calculateVariance = calculateVariance;
    final RandomEngine random = new MersenneTwister64(seed);
    _norm = new NormalDistribution(0, 1.0, random);
  }

  public double[] solve(final double spot, final AffineDividends dividends, final double expiry,
      final YieldAndDiscountCurve discountCurve, final LocalVolatilitySurfaceStrike localVol, final int nSims) {

    ArgumentChecker.notNull(dividends, "null dividends");
    ArgumentChecker.notNegative(expiry, "negative expiry");
    ArgumentChecker.notNegative(spot, "negative spot");
    ArgumentChecker.notNull(discountCurve, "null discountCurve");
    ArgumentChecker.notNull(localVol, "null localVol");
    ArgumentChecker.notNegative(nSims, "negative nSims");

    MonteCarloPath mc = new MonteCarloPath(dividends, expiry, spot, discountCurve, localVol);
    return mc.runMC(nSims, _useAntithetics, _calculateVariance);

  }

  private class MonteCarloPath {

    private final AffineDividends _dividends;
    private final double _spot;
    private final int[] _steps;
    private final int _nSteps;
    private final int _nDivs;
    private final LocalVolatilitySurfaceStrike _localVol;
    private final double _dt;
    private final double _rootDt;
    private final double[] _drift;

    public MonteCarloPath(final AffineDividends dividends, final double expiry, final double spot,
        final YieldAndDiscountCurve discountCurve, final LocalVolatilitySurfaceStrike localVol) {

      _dividends = dividends;
      _spot = spot;
      _localVol = localVol;

      final int nDivs = dividends.getNumberOfDividends();

      _dt = 1.0 / DAYS_PER_YEAR;
      _rootDt = Math.sqrt(_dt);
      _nSteps = (int) Math.ceil((expiry * DAYS_PER_YEAR)); //Effectively move expiry to end of day
      _drift = new double[_nSteps];
      double[] logP = new double[_nSteps + 1];
      logP[0] = 0.0;
      for (int i = 0; i < _nSteps; i++) {
        double t = (i + 1) * _dt;
        logP[i + 1] = -discountCurve.getInterestRate(t) * t;
        _drift[i] = -(logP[i + 1] - logP[i]) / _dt; //forward difference to get drift 
      }

      int nDivsBeforeExpiry = 0;
      int totalSteps = 0;
      int[] steps = new int[nDivs + 1];

      if (nDivs == 0 || dividends.getTau(0) > expiry) { //no dividends or first dividend after option expiry 
        steps[0] = (int) (Math.ceil(expiry * DAYS_PER_YEAR));
        totalSteps = steps[0];
      } else {
        steps[0] = (int) (Math.ceil(dividends.getTau(0) * DAYS_PER_YEAR)) - 1; //Effectively move dividend payment to end of day, and take steps up to the end of the day before
        totalSteps += steps[0] + 1;
        nDivsBeforeExpiry++;
        for (int i = 1; i < nDivs; i++) {
          if (dividends.getTau(i) > expiry) { //if dividend after expiry, step steps up to expiry and not not consider anymore dividends 
            steps[i] = ((int) Math.ceil(expiry * DAYS_PER_YEAR)) - totalSteps;
            totalSteps += steps[i];
            break;
          }
          steps[i] = ((int) Math.ceil(dividends.getTau(i) * DAYS_PER_YEAR)) - totalSteps - 1;
          totalSteps += steps[i] + 1;
          nDivsBeforeExpiry++;
        }

        if (dividends.getTau(nDivs - 1) < expiry) {
          steps[nDivs] = ((int) Math.ceil(expiry * DAYS_PER_YEAR)) - totalSteps;
          totalSteps += steps[nDivs];
        }
      }

      //check
      ArgumentChecker.isTrue(totalSteps == _nSteps, "got the steps wrong");

      //only care about the dividends that occur before expiry 
      _steps = Arrays.copyOfRange(steps, 0, nDivsBeforeExpiry + 1);
      _nDivs = nDivsBeforeExpiry;

    }

    public double[] runMC(final int nSims, final boolean useAntithetics, final boolean calErrors) {

      final int nVar = calErrors ? 2 * N_SIM_VARIABLES : N_SIM_VARIABLES;
      double[] res = new double[nVar];
      double[] temp;
      for (int i = 0; i < nSims; i++) {
        final double[] z = getNormals();
        temp = runPath(z);
        for (int j = 0; j < N_SIM_VARIABLES; j++) {
          res[j] += temp[j];
        }
        if (calErrors) {
          for (int j = 0; j < N_SIM_VARIABLES; j++) {
            res[j + N_SIM_VARIABLES] += temp[j] * temp[j];
          }
        }
        if (useAntithetics) {
          for (int k = 0; k < _nSteps; k++) {
            z[k] *= -1.0;
          }
          temp = runPath(z);
          for (int j = 0; j < N_SIM_VARIABLES; j++) {
            res[j] += temp[j];
          }
          if (calErrors) {
            for (int j = 0; j < N_SIM_VARIABLES; j++) {
              res[j + N_SIM_VARIABLES] += temp[j] * temp[j];
            }
          }
        }
      }
      final int n = useAntithetics ? 2 * nSims : nSims;
      for (int j = 0; j < N_SIM_VARIABLES; j++) {
        res[j] /= n;
      }
      if (calErrors) {
        for (int j = 0; j < N_SIM_VARIABLES; j++) {
          res[j + N_SIM_VARIABLES] = (res[j + N_SIM_VARIABLES] - n * FunctionUtils.square(res[j])) / (n - 1) / n;
        }
      }

      return res;
    }

    /**
     * 
     * @param z Set of iid standard normal random variables 
     * @return The final value of spot and the realized variance with and without dividends correction
     */
    public double[] runPath(final double[] z) {

      double sOld = _spot; //previous value of stock
      double s = 0; //current value of stock
      double sTotOld = _spot; //previous value of total returns process
      double sTot = 0; //current value of total returns process
      double rv1 = 0.0; //Accumulator of realised variance (with correction made for dividends)
      double rv2 = 0.0; //Accumulator of realised variance (without correction made for dividends)
      double t = 0.0; //current time
      double vol; //value of local vol at start of step
      double mu; //The drift at start of step
      double ret; //The daily return 
      double temp;

      int tSteps = 0;
      for (int k = 0; k < _nDivs; k++) {
        final int steps = _steps[k];
        //simulate the continuous path between dividends
        for (int i = 0; i < steps; i++) {
          vol = _localVol.getVolatility(t, sOld);
          mu = _drift[tSteps];
          //this Euler step is exact if the volatility and drift are constant, otherwise it is subject to discretisation error
          ret = (mu - vol * vol / 2) * _dt + vol * _rootDt * z[tSteps];
          temp = Math.exp(ret);
          s = sOld * temp;
          sTot = sTotOld * temp;
          temp = ret * ret;
          rv1 += temp;
          rv2 += temp;
          sOld = s;
          sTotOld = sTot;
          t += _dt;
          tSteps++;
        }

        //simulate the path on dividend day
        //The dividend payment is effectively moved to the end of the day
        vol = _localVol.getVolatility(t, sOld);
        mu = _drift[tSteps];
        temp = Math.exp((mu - vol * vol / 2) * _dt + vol * _rootDt * z[tSteps]);
        double sm = sOld * temp; //the stock price immediately before the dividend payment 
        sTot = sTotOld * temp;
        s = sm * (1 - _dividends.getBeta(k)) - _dividends.getAlpha(k);
        rv1 += FunctionUtils.square(Math.log(sm / sOld));
        rv2 += FunctionUtils.square(Math.log(s / sOld));
        sOld = s;
        sTotOld = sTot;
        t += _dt;
        tSteps++;
      }

      //simulate the remaining continuous path from the last dividend to the expiry 
      final int steps = _steps[_nDivs];
      //simulate the continuous path between dividends
      for (int i = 0; i < steps; i++) {
        vol = _localVol.getVolatility(t, sOld);
        mu = _drift[tSteps];
        ret = (mu - vol * vol / 2) * _dt + vol * _rootDt * z[tSteps]; //this Euler step will be subject to discretisation error
        temp = Math.exp(ret);
        sTot = sTotOld * temp;
        s = sOld * temp;
        rv1 += ret * ret;
        rv2 += ret * ret;
        sOld = s;
        sTotOld = sTot;
        t += _dt;
        tSteps++;
      }

      //correct for mean and bias
      rv1 -= FunctionUtils.square(Math.log(sTot / _spot)) / _nSteps;
      rv2 -= FunctionUtils.square(Math.log(s / _spot)) / _nSteps;
      double biasCorr = ((double) DAYS_PER_YEAR) / (_nSteps - 1);
      rv1 *= biasCorr;
      rv2 *= biasCorr;

      return new double[] {s, rv1, rv2 };
    }

    private double[] getNormals() {
      double[] z = new double[_nSteps];
      for (int i = 0; i < _nSteps; i++) {
        z[i] = _norm.nextRandom();
      }
      return z;
    }

  }
}
