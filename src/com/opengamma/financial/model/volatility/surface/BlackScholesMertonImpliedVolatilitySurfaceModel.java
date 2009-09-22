/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Date;
import java.util.Map;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.SingleRootFinder;

public class BlackScholesMertonImpliedVolatilitySurfaceModel implements VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  private final AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> _bsm = new BlackScholesMertonModel();
  private SingleRootFinder<StandardOptionDataBundle, Double, Double> _rootFinder;
  private final double EPS = 1e-9;

  @Override
  public VolatilitySurface getSurface(Map<EuropeanVanillaOptionDefinition, Double> prices, StandardOptionDataBundle data) {
    Map.Entry<EuropeanVanillaOptionDefinition, Double> entry = prices.entrySet().iterator().next();
    Double price = entry.getValue();
    Function1D<StandardOptionDataBundle, Double> pricingFunction = _bsm.getPricingFunction(entry.getKey());
    _rootFinder = new MyBisectionSingleRootFinder(data, price);
    double sigma = _rootFinder.getRoot(pricingFunction, 0., 10., EPS);
    return new ConstantVolatilitySurface(data.getDate(), sigma);
  }

  private class MyMutableStandardOptionDataBundle extends StandardOptionDataBundle {
    private VolatilitySurface _mutableSurface;
    private final Date _date;

    public MyMutableStandardOptionDataBundle(StandardOptionDataBundle data) {
      super(data.getDiscountCurve(), data.getCostOfCarry(), data.getVolatilitySurface(), data.getSpot(), data.getDate());
      _mutableSurface = data.getVolatilitySurface();
      _date = data.getDate();
    }

    public void setVolatility(double sigma) {
      _mutableSurface = new ConstantVolatilitySurface(_date, sigma);
    }

    @Override
    public VolatilitySurface getVolatilitySurface() {
      return _mutableSurface;
    }
  }

  private class MyBisectionSingleRootFinder implements SingleRootFinder<StandardOptionDataBundle, Double, Double> {
    private final MyMutableStandardOptionDataBundle _data;
    private final double _price;
    private static final int MAX_ATTEMPTS = 1000;

    public MyBisectionSingleRootFinder(StandardOptionDataBundle data, double price) {
      _data = new MyMutableStandardOptionDataBundle(data);
      _price = price;
    }

    @Override
    public Double getRoot(Function<StandardOptionDataBundle, Double> function, Double lowVol, Double highVol, Double accuracy) {
      _data.setVolatility(lowVol);
      Double lowPrice = function.evaluate(_data) - _price;
      if (Math.abs(lowPrice) < accuracy)
        return lowVol;
      _data.setVolatility(highVol);
      Double highPrice = function.evaluate(_data) - _price;
      if (Math.abs(highPrice) < accuracy)
        return highVol;
      double dVol, midVol, rootVol;
      if (lowPrice < 0) {
        dVol = highVol - lowVol;
        rootVol = lowVol;
      } else {
        dVol = lowVol - highVol;
        rootVol = highVol;
      }
      for (int i = 0; i < MAX_ATTEMPTS; i++) {
        dVol *= 0.5;
        midVol = rootVol + dVol;
        _data.setVolatility(midVol);
        highPrice = function.evaluate(_data);
        if (highPrice <= 0)
          rootVol = midVol;
        if (Math.abs(dVol) < accuracy || Math.abs(highVol) < ZERO)
          return rootVol;
      }
      throw new OptionPricingException("Could not find volatility in " + MAX_ATTEMPTS + " attempts");
    }
  }
}