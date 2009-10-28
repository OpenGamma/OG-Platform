/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.SingleRootFinder;

public class BlackScholesMertonImpliedVolatilitySurfaceModel implements VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> {
  private static final Logger s_Log = LoggerFactory.getLogger(BlackScholesMertonImpliedVolatilitySurfaceModel.class);
  private final AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> _bsm = new BlackScholesMertonModel();
  private SingleRootFinder<StandardOptionDataBundle, Double, Double> _rootFinder;

  @Override
  public VolatilitySurface getSurface(final Map<EuropeanVanillaOptionDefinition, Double> optionPrices, final StandardOptionDataBundle optionDataBundle) {
    if (optionPrices == null)
      throw new IllegalArgumentException("Option price map was null");
    if (optionPrices.isEmpty())
      throw new IllegalArgumentException("Option price map was empty");
    if (optionDataBundle == null)
      throw new IllegalArgumentException("Data bundle was null");
    if (optionPrices.size() > 1) {
      s_Log.info("Option price map had more than one entry: using the first pair to imply volatility");
    }
    final Map.Entry<EuropeanVanillaOptionDefinition, Double> entry = optionPrices.entrySet().iterator().next();
    final Double price = entry.getValue();
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = _bsm.getPricingFunction(entry.getKey());
    _rootFinder = new MyBisectionSingleRootFinder(new MyMutableStandardOptionDataBundle(optionDataBundle), price);
    final double sigma = _rootFinder.getRoot(pricingFunction, 0., 10.);
    return new ConstantVolatilitySurface(sigma);
  }

  private class MyMutableStandardOptionDataBundle extends StandardOptionDataBundle {
    private VolatilitySurface _mutableSurface;

    public MyMutableStandardOptionDataBundle(final StandardOptionDataBundle data) {
      super(data.getDiscountCurve(), data.getCostOfCarry(), data.getVolatilitySurface(), data.getSpot(), data.getDate());
      _mutableSurface = data.getVolatilitySurface();
    }

    public void setVolatility(final double sigma) {
      _mutableSurface = new ConstantVolatilitySurface(sigma);
    }

    @Override
    public VolatilitySurface getVolatilitySurface() {
      return _mutableSurface;
    }
  }

  private class MyBisectionSingleRootFinder implements SingleRootFinder<StandardOptionDataBundle, Double, Double> {
    private final MyMutableStandardOptionDataBundle _data;
    private final double _price;
    private static final double _accuracy = 1e-12;
    private static final double ZERO = 1e-16;
    private static final int MAX_ATTEMPTS = 10000;

    public MyBisectionSingleRootFinder(final StandardOptionDataBundle data, final double price) {
      _data = new MyMutableStandardOptionDataBundle(data);
      _price = price;
    }

    @Override
    public Double getRoot(final Function1D<StandardOptionDataBundle, Double> function, final Double lowVol, final Double highVol) {
      _data.setVolatility(lowVol);
      final Double lowPrice = function.evaluate(_data) - _price;
      if (Math.abs(lowPrice) < _accuracy)
        return lowVol;
      _data.setVolatility(highVol);
      Double highPrice = function.evaluate(_data) - _price;
      if (Math.abs(highPrice) < _accuracy)
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
        highPrice = function.evaluate(_data) - _price;
        if (highPrice <= 0) {
          rootVol = midVol;
        }
        if (Math.abs(dVol) < _accuracy || Math.abs(highVol) < ZERO)
          return rootVol;
      }
      throw new OptionPricingException("Could not find volatility in " + MAX_ATTEMPTS + " attempts");
    }
  }
}