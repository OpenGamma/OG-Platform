/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.regression.AdaptiveLeastSquaresRegression;
import com.opengamma.math.regression.LeastSquaresRegression;
import com.opengamma.math.regression.LeastSquaresRegressionResult;
import com.opengamma.math.regression.OrdinaryLeastSquaresRegression;

public class PractitionerBlackScholesVolatilitySurfaceModel implements VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  private static final Logger s_Log = LoggerFactory.getLogger(PractitionerBlackScholesVolatilitySurfaceModel.class);
  private final VolatilitySurfaceModel<OptionDefinition, StandardOptionDataBundle> _bsmVolatilityModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  private final LeastSquaresRegression _regression = new AdaptiveLeastSquaresRegression(new OrdinaryLeastSquaresRegression(), 0.05);

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
    return new MyVolatilitySurface(getRegressionResult(kList.toArray(emptyArray), tList.toArray(emptyArray), sigmaList.toArray(emptyArray)));
  }

  private LeastSquaresRegressionResult getRegressionResult(final Double[] kArray, final Double[] tArray, final Double[] sigmaArray) {
    final int length = kArray.length;
    final Double[][] x = new Double[length][5];
    final Double[][] weights = new Double[length][5];
    final Double[] y = new Double[length];
    final Double[] zeroes = new Double[] { 0., 0., 0., 0., 0. };
    Double k;
    Double t;
    Double sigma;
    for (int i = 0; i < kArray.length; i++) {
      k = kArray[i];
      t = tArray[i];
      sigma = sigmaArray[i];
      x[i][0] = k;
      x[i][1] = k * k;
      x[i][2] = t;
      x[i][3] = t * t;
      x[i][4] = k * t;
      weights[i] = zeroes;
      y[i] = sigma;
    }
    return _regression.regress(x, weights, y, true);
  }

  private class MyVolatilitySurface extends VolatilitySurface {
    private final LeastSquaresRegressionResult _result;

    public MyVolatilitySurface(final LeastSquaresRegressionResult result) {
      _result = result;
    }

    @Override
    public Double getVolatility(final Double t, final Double k) {
      return null;
    }
  }
}
