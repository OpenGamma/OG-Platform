/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.regression.LeastSquaresRegression;
import com.opengamma.analytics.math.regression.LeastSquaresRegressionResult;
import com.opengamma.analytics.math.regression.OrdinaryLeastSquaresRegression;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * This is a regression of the implied volatilities of options against, $k$, $k^2$, $t$, $t^2$, and $k\times t$, where $k$
 * and $t$ are the option strike and time-to-expiry (in years) respectively.  It therefore finds the best quadratic fit (in $k$ and $t$)
 * to the market implied volatilities, i.e. the volatility surface is model as $\sigma(k,t) = a_0 + a_1k + a_2k^2 + a_3t + a_4t^2 +a_5kt$
 * @see http://www.econometricsociety.org/meetings/esem02/cdrom/papers/221/cj.pdf
 */
public class PractitionerBlackScholesVolatilitySurfaceModel implements VolatilitySurfaceModel<Map<OptionDefinition, Double>, StandardOptionDataBundle> {
  private static final Logger s_logger = LoggerFactory.getLogger(PractitionerBlackScholesVolatilitySurfaceModel.class);
  private final VolatilitySurfaceModel<Map<OptionDefinition, Double>, StandardOptionDataBundle> _bsmVolatilityModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  private static final int DEGREE = 5;
  private final LeastSquaresRegression _regression;
  private static final Double[] EMPTY_ARRAY = new Double[0];
  private final Function<Double, double[]> _independentVariableFunction = new Function<Double, double[]>() {

    @Override
    public double[] evaluate(final Double... x) {
      final double t = x[0];
      final double k = x[1];
      final double[] result = new double[DEGREE];
      result[0] = k;
      result[1] = k * k;
      result[2] = t;
      result[3] = t * t;
      result[4] = k * t;
      return result;
    }

  };

  public PractitionerBlackScholesVolatilitySurfaceModel() {
    _regression = new OrdinaryLeastSquaresRegression();
  }

  @Override
  /**
   * A minimum of 5 distinct option prices are required.
   */
  public VolatilitySurface getSurface(final Map<OptionDefinition, Double> prices, final StandardOptionDataBundle data) {
    Validate.notNull(prices, "prices");
    Validate.notNull(data, "data");
    if (prices.size() < DEGREE) {
      throw new IllegalArgumentException("Price map contained " + prices.size() + " data point(s); need at least " + DEGREE);
    }
    final List<Double> kList = new ArrayList<>();
    final List<Double> tList = new ArrayList<>();
    final List<Double> sigmaList = new ArrayList<>();
    double k, t, sigma;
    for (final Map.Entry<OptionDefinition, Double> entry : prices.entrySet()) {
      k = entry.getKey().getStrike();
      t = entry.getKey().getTimeToExpiry(data.getDate());
      try {
        sigma = _bsmVolatilityModel.getSurface(Collections.<OptionDefinition, Double>singletonMap(entry.getKey(), entry.getValue()), data).getVolatility(DoublesPair.of(t, k));
        kList.add(k);
        tList.add(t);
        sigmaList.add(sigma);
      } catch (final Exception e) {
        s_logger.info("Problem getting BSM volatility for " + entry.getKey() + ", not using this option in regression. Error was: ", e);
      }
    }
    return new VolatilitySurface(FunctionalDoublesSurface.from(new MySurfaceFunction(getRegressionResult(kList.toArray(EMPTY_ARRAY), tList.toArray(EMPTY_ARRAY), sigmaList.toArray(EMPTY_ARRAY)))));
  }

  private LeastSquaresRegressionResult getRegressionResult(final Double[] kArray, final Double[] tArray, final Double[] sigmaArray) {
    final int length = kArray.length;
    final double[][] x = new double[length][DEGREE];
    final double[] y = new double[length];
    Double k;
    Double t;
    Double sigma;
    for (int i = 0; i < kArray.length; i++) {
      k = kArray[i];
      t = tArray[i];
      sigma = sigmaArray[i];
      x[i] = _independentVariableFunction.evaluate(t, k);
      y[i] = sigma;
    }
    return _regression.regress(x, null, y, true);
  }

  private class MySurfaceFunction implements Function<Double, Double> {
    private final LeastSquaresRegressionResult _result;

    public MySurfaceFunction(final LeastSquaresRegressionResult result) {
      _result = result;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(final Double... tk) {
      Validate.notNull(tk, "tk pair");
      final double t = tk[0];
      final double k = tk[1];
      return _result.getPredictedValue(_independentVariableFunction.evaluate(t, k));
    }
  }
}
