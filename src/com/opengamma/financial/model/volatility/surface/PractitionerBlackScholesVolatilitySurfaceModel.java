/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function2D;
import com.opengamma.math.regression.AdaptiveLeastSquaresRegression;
import com.opengamma.math.regression.LeastSquaresRegression;
import com.opengamma.math.regression.LeastSquaresRegressionResult;
import com.opengamma.math.regression.NamedVariableLeastSquaresRegressionResult;
import com.opengamma.math.regression.OrdinaryLeastSquaresRegression;

/**
 * 
 * @author emcleod
 * 
 */
public class PractitionerBlackScholesVolatilitySurfaceModel implements VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  private static final Logger s_Log = LoggerFactory.getLogger(PractitionerBlackScholesVolatilitySurfaceModel.class);
  private final VolatilitySurfaceModel<OptionDefinition, StandardOptionDataBundle> _bsmVolatilityModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  protected final LeastSquaresRegression _regression;
  protected final Function<Double, Double[]> _independentVariableFunction = new Function2D<Double, Double[]>() {

    @Override
    public Double[] evaluate(final Double t, final Double k) {
      final Double[] result = new Double[5];
      result[0] = k;
      result[1] = k * k;
      result[2] = t;
      result[3] = t * t;
      result[4] = k * t;
      return result;
    }

  };

  public PractitionerBlackScholesVolatilitySurfaceModel(final boolean useAdaptiveModel) {
    final LeastSquaresRegression ols = new OrdinaryLeastSquaresRegression();
    if (useAdaptiveModel) {
      _regression = new AdaptiveLeastSquaresRegression(ols, 0.05);
    } else {
      _regression = ols;
    }
  }

  public PractitionerBlackScholesVolatilitySurfaceModel(final boolean useAdaptiveModel, final double significanceLevel) {
    final LeastSquaresRegression ols = new OrdinaryLeastSquaresRegression();
    if (useAdaptiveModel) {
      _regression = new AdaptiveLeastSquaresRegression(ols, significanceLevel);
    } else {
      s_Log.info("Significance level was provided, but model being used is not adaptive");
      _regression = ols;
    }
  }

  @Override
  public VolatilitySurface getSurface(final Map<EuropeanVanillaOptionDefinition, Double> prices, final StandardOptionDataBundle data) {
    final List<Double> kList = new ArrayList<Double>();
    final List<Double> tList = new ArrayList<Double>();
    final List<Double> sigmaList = new ArrayList<Double>();
    final boolean[] includeVariable = new boolean[5];
    int i = 0;
    Double k, t, sigma;
    for (final Map.Entry<EuropeanVanillaOptionDefinition, Double> entry : prices.entrySet()) {
      includeVariable[i++] = true;
      k = entry.getKey().getStrike();
      t = entry.getKey().getTimeToExpiry(data.getDate());
      try {
        sigma = _bsmVolatilityModel.getSurface(Collections.<OptionDefinition, Double> singletonMap(entry.getKey(), entry.getValue()), data).getVolatility(t, k);
        if (k != null && t != null && sigma != null) {
          kList.add(k);
          tList.add(t);
          sigmaList.add(sigma);
        } else {
          s_Log.info("Problem getting BSM volatility for " + entry.getKey() + ", not using this option in regression");
        }
      } catch (final Exception e) {
        s_Log.info("Problem getting BSM volatility for " + entry.getKey() + ", not using this option in regression. Error was: ", e);
      }
    }
    final Double[] emptyArray = new Double[0];
    return getVolatilitySurfaceForRegression(getRegressionResult(kList.toArray(emptyArray), tList.toArray(emptyArray), sigmaList.toArray(emptyArray)));
  }

  private LeastSquaresRegressionResult getRegressionResult(final Double[] kArray, final Double[] tArray, final Double[] sigmaArray) {
    final int length = kArray.length;
    final Double[][] x = new Double[length][5];
    final Double[] y = new Double[length];
    Double k;
    Double t;
    Double sigma;
    for (int i = 0; i < kArray.length; i++) {
      k = kArray[i];
      t = tArray[i];
      sigma = sigmaArray[i];
      x[i] = _independentVariableFunction.evaluate(k, t);
      y[i] = sigma;
    }
    return _regression.regress(x, null, y, true);
  }

  private VolatilitySurface getVolatilitySurfaceForRegression(final LeastSquaresRegressionResult result) {
    if (result instanceof NamedVariableLeastSquaresRegressionResult)
      return new VolatilitySurface() {

        @Override
        public Double getVolatility(final Double t, final Double k) {
          final Double[] values = _independentVariableFunction.evaluate(t, k);
          final Map<String, Double> namesAndValues = new HashMap<String, Double>();
          for (int i = 0; i < values.length; i++) {
            namesAndValues.put(Integer.toString(i), values[i]);
          }
          return ((NamedVariableLeastSquaresRegressionResult) result).getPredictedValue(namesAndValues);
        }

      };
    return new VolatilitySurface() {

      @Override
      public Double getVolatility(final Double t, final Double k) {
        return result.getPredictedValue(_independentVariableFunction.evaluate(t, k));
      }

    };
  }
}
