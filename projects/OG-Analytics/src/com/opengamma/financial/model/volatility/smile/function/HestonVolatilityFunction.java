/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.fourier.FFTModelGreeks;
import com.opengamma.financial.model.option.pricing.fourier.FFTPricer;
import com.opengamma.financial.model.option.pricing.fourier.HestonCharacteristicExponent;
import com.opengamma.financial.model.option.pricing.fourier.MartingaleCharacteristicExponent;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.lang.annotation.ExternalFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class HestonVolatilityFunction extends VolatilityFunctionProvider<HestonModelData> {

  private static final FFTPricer FFT_PRICER = new FFTPricer();
  private static final Interpolator1D DEFAULT_INTERPOLATOR1D = CombinedInterpolatorExtrapolatorFactory.getInterpolator("DoubleQuadratic", "FlatExtrapolator", "FlatExtrapolator");
  private static final double DEFAULT_LIMIT_SIGMA = 0.3;
  private static final double DEFAULT_ALPHA = -0.5;

  private final double _limitSigma;
  private final double _alpha;
  private final double _limitTolerance;
  private final Interpolator1D _interpolator;

  public HestonVolatilityFunction() {
    _limitSigma = DEFAULT_LIMIT_SIGMA;
    _alpha = DEFAULT_ALPHA;
    _limitTolerance = 1e-8;
    _interpolator = DEFAULT_INTERPOLATOR1D;
  }

  /**
   * {@inheritDoc}
   * Only use this for testing. If you have a set of options with the same expiry but different strikes, use {@link getVolatilitySetFunction}
   */
  @Override
  public Function1D<HestonModelData, Double> getVolatilityFunction(EuropeanVanillaOption option, double forward) {

    final Function1D<HestonModelData, double[]> func = getVolatilityFunction(forward, new double[] {option.getStrike() }, option.getTimeToExpiry());

    return new Function1D<HestonModelData, Double>() {

      @Override
      public Double evaluate(HestonModelData x) {
        return func.evaluate(x)[0];
      }
    };
  }

  @Override
  public Function1D<HestonModelData, double[]> getVolatilityFunction(final double forward, final double[] strikes, final double timeToExpiry) {

    final int n = strikes.length;
    final double lowestStrike = strikes[0];
    final double highestStrike = strikes[n - 1];

    return new Function1D<HestonModelData, double[]>() {

      @Override
      public double[] evaluate(HestonModelData x) {
        final MartingaleCharacteristicExponent ce = new HestonCharacteristicExponent(x);
        //TODO calculations relating to the FFT setup are made each call, even though they will be very similar (depends on Characteristic
        // Exponent). Maybe worth calculating a typical setup, outside of this function
        final double[][] strikeNPrice = FFT_PRICER.price(forward, 1.0, timeToExpiry, true, ce, lowestStrike, highestStrike, n, _limitSigma, _alpha, _limitTolerance);
        final int m = strikeNPrice.length;
        final double[] k = new double[m];
        final double[] vol = new double[m];
        int count = 0;
        for (int i = 0; i < m; i++) {
          double strike = strikeNPrice[i][0];
          double price = strikeNPrice[i][1];
          if (price > 0.0) {
            double impVol;
            try {
              impVol = BlackFormulaRepository.impliedVolatility(price, forward, strike, timeToExpiry, true);
              k[count] = strike;
              vol[count] = impVol;
              count++;
            } catch (IllegalArgumentException e) {

              //impVol = BlackFormulaRepository.impliedVolatility(price, forward, strike, timeToExpiry, true);
            }
          }
        }
        final double[] res = new double[n];
        if (count == 0) { //i.e. every single price is invalid, which could happen with extreme parameters. All we can do without stopping the
          // fitter, is return zero vols.
          for (int i = 0; i < n; i++) {
            res[i] = 0.0;
          }
        } else {
          double[] validStrikes = new double[count];
          double[] validVols = new double[count];
          if (count == m) {
            validStrikes = k;
            validVols = vol;
          } else {
            validStrikes = Arrays.copyOfRange(k, 0, count);
            validVols = Arrays.copyOfRange(vol, 0, count);
          }
          final Interpolator1DDataBundle dataBundle = _interpolator.getDataBundleFromSortedArrays(validStrikes, validVols);
          for (int i = 0; i < n; i++) {
            res[i] = _interpolator.interpolate(dataBundle, strikes[i]);
          }
        }
        return res;
      }
    };
  }

  @ExternalFunction
  public double getVolatility(final double forward, final double strike, final double timeToExpiry, final double kappa, final double theta, final double vol0, final double omega,
      final double rho) {
    Function1D<HestonModelData, Double> func = getVolatilityFunction(new EuropeanVanillaOption(strike, timeToExpiry, true), forward);
    HestonModelData data = new HestonModelData(kappa, theta, vol0, omega, rho);
    return func.evaluate(data);
  }

  @ExternalFunction
  public double[] getVolatilitySet(final double forward, final double[] strikes, final double timeToExpiry, final double kappa, final double theta, final double vol0, final double omega,
      final double rho) {
    Function1D<HestonModelData, double[]> func = getVolatilityFunction(forward, strikes, timeToExpiry);
    HestonModelData data = new HestonModelData(kappa, theta, vol0, omega, rho);
    return func.evaluate(data);
  }

  @Override
  public Function1D<HestonModelData, double[]> getVolatilityAdjointFunction(EuropeanVanillaOption option, double forward) {

    final Function1D<HestonModelData, double[][]> func = getVolatilityAdjointFunction(forward, new double[] {option.getStrike() }, option.getTimeToExpiry());
    return new Function1D<HestonModelData, double[]>() {

      @Override
      public double[] evaluate(HestonModelData x) {
        double[][] temp = func.evaluate(x);
        Validate.isTrue(temp.length == 1);
        return temp[0];
      }

    };
  }

  @Override
  public Function1D<HestonModelData, double[][]> getVolatilityAdjointFunction(final double forward, final double[] strikes,
      final double timeToExpiry) {

    final FFTModelGreeks greekCal = new FFTModelGreeks();
    final int n = strikes.length;
    final double lowestStrike = strikes[0];
    final double highestStrike = strikes[n - 1];
    final double[][] nodeSense = new double[n][];

    return new Function1D<HestonModelData, double[][]>() {

      @Override
      public double[][] evaluate(HestonModelData x) {
        final MartingaleCharacteristicExponent ce = new HestonCharacteristicExponent(x);
        double[][] greeks = greekCal.getGreeks(forward, 1.0, timeToExpiry, true, ce, lowestStrike, highestStrike, n, _limitSigma, _alpha, _limitTolerance);
        //1st array is strikes and the second is prices (which we don't need)
        final double[] k = greeks[0];
        final double[] prices = greeks[1];
        final int m = k.length;
        final double[] vols = new double[m];
        final double[] vega = new double[m];

        for (int i = 0; i < m; i++) {
          vols[i] = BlackFormulaRepository.impliedVolatility(prices[i], forward, k[i], timeToExpiry, true);
        }
        for (int i = 0; i < m; i++) {
          vega[i] = BlackFormulaRepository.vega(forward, k[i], timeToExpiry, vols[i]);
        }

        final Interpolator1DDataBundle dataBundle = _interpolator.getDataBundleFromSortedArrays(k, vols);

        for (int i = 0; i < n; i++) {
          nodeSense[i] = _interpolator.getNodeSensitivitiesForValue(dataBundle, strikes[i]);
        }

        final int p = greeks.length;

        final double[][] volSense = new double[p - 2][m];
        for (int index = 0; index < p - 2; index++) {
          for (int i = 0; i < m; i++) {
            volSense[index][i] = greeks[index + 2][i] / vega[i]; //TODO here is where vega = 0 -> infinity
          }
        }

        //fake the price, forward, and strike sense since we don't used them
        double[][] res = new double[n][p + 1];
        for (int index = 0; index < p - 2; index++) {
          final double[] temp = volSense[index];
          for (int i = 0; i < n; i++) {
            final double[] tns = nodeSense[i];
            double sum = 0.0;
            for (int j = 0; j < m; j++) {
              sum += temp[j] * tns[j];
            }
            res[i][index + 3] = sum;
          }
        }

        return res;
      }
    };
  }

  @Override
  public Function1D<HestonModelData, double[]> getModelAdjointFunction(EuropeanVanillaOption option, double forward) {

    final Function1D<HestonModelData, double[][]> func = getModelAdjointFunction(forward, new double[] {option.getStrike() }, option.getTimeToExpiry());
    return new Function1D<HestonModelData, double[]>() {

      @Override
      public double[] evaluate(HestonModelData x) {
        double[][] temp = func.evaluate(x);
        Validate.isTrue(temp.length == 1);
        return temp[0];
      }

    };
  }

  @Override
  public Function1D<HestonModelData, double[][]> getModelAdjointFunction(final double forward, final double[] strikes,
      final double timeToExpiry) {
    final FFTModelGreeks greekCal = new FFTModelGreeks();
    final int n = strikes.length;
    final double lowestStrike = strikes[0];
    final double highestStrike = strikes[n - 1];
    final double[][] nodeSense = new double[n][];

    return new Function1D<HestonModelData, double[][]>() {

      @Override
      public double[][] evaluate(HestonModelData x) {
        final MartingaleCharacteristicExponent ce = new HestonCharacteristicExponent(x);
        double[][] greeks = greekCal.getGreeks(forward, 1.0, timeToExpiry, true, ce, lowestStrike, highestStrike, n, _limitSigma, _alpha, _limitTolerance);
        //1st array is strikes and the second is prices (which we don't need)

        final double[] k = greeks[0];
        final double[] prices = greeks[1];
        final int m = k.length;
        final double[] kTemp = new double[m];
        final double[] vols = new double[m];
        final double[] vega = new double[m];
        final double[][] modelGreeks = new double[5][m];
        int count = 0;
        for (int i = 0; i < m; i++) {
          double impVol;
          try {
            impVol = BlackFormulaRepository.impliedVolatility(prices[i], forward, k[i], timeToExpiry, true);
            vols[count] = impVol;
            vega[count] = BlackFormulaRepository.vega(forward, k[i], timeToExpiry, impVol);
            kTemp[count] = k[i];
            for (int j = 0; j < 5; j++) {
              modelGreeks[j][count] = greeks[j + 2][i];
            }
            count++;
          } catch (IllegalArgumentException e) {
            //do nothing
          }
        }

        double[] validStrikes = new double[count];
        double[] validVols = new double[count];
        double[] validVegas = new double[count];
        double[][] validModelGreeks = new double[5][count];
        if (count == m) {
          validStrikes = kTemp;
          validVols = vols;
          validVegas = vega;
          validModelGreeks = modelGreeks;
        } else {
          validStrikes = Arrays.copyOfRange(k, 0, count);
          validVols = Arrays.copyOfRange(vols, 0, count);
          validVegas = Arrays.copyOfRange(vega, 0, count);
          for (int j = 0; j < 5; j++) {
            validModelGreeks[j] = Arrays.copyOfRange(modelGreeks[j], 0, count);
          }
        }

        final Interpolator1DDataBundle dataBundle = _interpolator.getDataBundleFromSortedArrays(validStrikes, validVols);

        for (int i = 0; i < n; i++) {
          nodeSense[i] = _interpolator.getNodeSensitivitiesForValue(dataBundle, strikes[i]);
        }

        final int p = modelGreeks.length;

        final double[][] volSense = new double[p][count];
        for (int index = 0; index < p; index++) {
          for (int i = 0; i < count; i++) {
            volSense[index][i] = validModelGreeks[index][i] / validVegas[i];
          }
        }

        double[][] res = new double[n][p];
        for (int index = 0; index < p; index++) {
          final double[] temp = volSense[index];
          for (int i = 0; i < n; i++) {
            final double[] tns = nodeSense[i];
            double sum = 0.0;
            for (int j = 0; j < count; j++) {
              sum += temp[j] * tns[j];
            }
            res[i][index] = sum;
          }
        }

        return res;
      }
    };
  }

}
