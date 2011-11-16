/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.fourier.FourierPricer;
import com.opengamma.financial.model.option.pricing.fourier.MartingaleCharacteristicExponent;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.minimization.UncoupledParameterTransforms;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class HestonFourierSmileFitter extends HestonFFTSmileFitter {

  private static final FourierPricer FOURIER_PRICER = new FourierPricer();

  /**
   * @param fixVol0 True if initial value of vol the same as mean reversion level 
   */
  public HestonFourierSmileFitter(boolean fixVol0) {
    super(fixVol0);
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] errors, final double[] initialFitParameters, final BitSet fixed) {
    testData(options, data, errors, initialFitParameters, fixed, getnParams());
    final int n = options.length;
    final double[] strikes = new double[n];
    final double[] blackVols = new double[n];
    final double maturity = options[0].getTimeToExpiry();
    final double forward = data[0].getForward();
    strikes[0] = options[0].getStrike();
    blackVols[0] = data[0].getBlackVolatility();
    for (int i = 1; i < n; i++) {
      Validate.isTrue(CompareUtils.closeEquals(options[i].getTimeToExpiry(), maturity),
          "All options must have the same maturity " + maturity + "; have one with maturity " + options[i].getTimeToExpiry());
      strikes[i] = options[i].getStrike();
      blackVols[i] = data[i].getBlackVolatility();
    }
    final UncoupledParameterTransforms transforms = new UncoupledParameterTransforms(new DoubleMatrix1D(initialFitParameters), getTransforms(), fixed);
    final double limitSigma = (blackVols[0] + blackVols[blackVols.length - 1]) / 2.0;
    final BlackFunctionData blackData = new BlackFunctionData(forward, 1, limitSigma);
    final ParameterizedFunction<Double, DoubleMatrix1D, Double> function = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike, final DoubleMatrix1D fp) {
        final MartingaleCharacteristicExponent ce = getCharacteristicExponent(transforms, fp);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, maturity, true);
        final double price = FOURIER_PRICER.priceFromVol(blackData, option, ce, getAlpha(), getLimitTolerance(), true);
        final double vol = BLACK_IMPLIED_VOL_FORMULA.getImpliedVolatility(blackData, option, price);
        return vol;
      }
    };

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialFitParameters));
    final LeastSquareResults results = errors == null ? SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(blackVols), function, fp) : SOLVER.solve(new DoubleMatrix1D(strikes),
        new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), function, fp);
    return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getParameters()), new DoubleMatrix2D(new double[getnParams()][getnParams()]));
  }

}
