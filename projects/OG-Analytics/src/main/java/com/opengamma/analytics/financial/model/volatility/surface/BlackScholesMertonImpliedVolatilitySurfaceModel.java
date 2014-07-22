/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.OptionPricingException;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.SingleRootFinder;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * This produces a flat volatility surface, with the implied volatility of the <b>single</b> option passed to the constructor  
 */
public class BlackScholesMertonImpliedVolatilitySurfaceModel implements VolatilitySurfaceModel<Map<OptionDefinition, Double>, StandardOptionDataBundle> {
  private static final Logger s_logger = LoggerFactory.getLogger(BlackScholesMertonImpliedVolatilitySurfaceModel.class);
  private final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> _bsm = new BlackScholesMertonModel();
  private SingleRootFinder<StandardOptionDataBundle, Double> _rootFinder;

  @Override
  /**
   * @param optionPrices a map containing a single entry from an OptionDefinition to a price 
   */
  public VolatilitySurface getSurface(final Map<OptionDefinition, Double> optionPrices, final StandardOptionDataBundle optionDataBundle) {
    Validate.notNull(optionPrices);
    ArgumentChecker.notEmpty(optionPrices, "option prices");
    Validate.notNull(optionDataBundle);
    if (optionPrices.size() > 1) {
      s_logger.info("Option price map had more than one entry: using the first pair to imply volatility");
    }
    final Map.Entry<OptionDefinition, Double> entry = optionPrices.entrySet().iterator().next();
    final Double price = entry.getValue();
    final Function1D<StandardOptionDataBundle, Double> pricingFunction = _bsm.getPricingFunction(entry.getKey());
    _rootFinder = new MyBisectionSingleRootFinder(optionDataBundle, price);
    return _rootFinder.getRoot(pricingFunction, optionDataBundle.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0))),
        optionDataBundle.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(10)))).getVolatilitySurface();
  }

  private static class MyBisectionSingleRootFinder implements SingleRootFinder<StandardOptionDataBundle, Double> {
    private final StandardOptionDataBundle _data;
    private final double _price;
    private final DoublesPair _origin = DoublesPair.of(0., 0.);
    private static final double ACCURACY = 1e-12;
    private static final int MAX_ATTEMPTS = 10000;

    public MyBisectionSingleRootFinder(final StandardOptionDataBundle data, final double price) {
      _data = data;
      _price = price;
    }

    @Override
    public StandardOptionDataBundle getRoot(final Function1D<StandardOptionDataBundle, Double> function, final StandardOptionDataBundle... volData) {
      final StandardOptionDataBundle lowVolData = volData[0];
      final StandardOptionDataBundle highVolData = volData[1];
      final Double lowPrice = function.evaluate(lowVolData) - _price;
      if (Math.abs(lowPrice) < ACCURACY) {
        return lowVolData;
      }
      Double highPrice = function.evaluate(highVolData) - _price;
      if (Math.abs(highPrice) < ACCURACY) {
        return highVolData;
      }
      final double highVol = highVolData.getVolatilitySurface().getVolatility(_origin);
      final double lowVol = lowVolData.getVolatilitySurface().getVolatility(_origin);
      double dVol, midVol, rootVol;
      if (lowPrice < 0) {
        dVol = highVol - lowVol;
        rootVol = lowVol;
      } else {
        dVol = lowVol - highVol;
        rootVol = highVol;
      }
      StandardOptionDataBundle midVolData;
      for (int i = 0; i < MAX_ATTEMPTS; i++) {
        dVol *= 0.5;
        midVol = rootVol + dVol;
        midVolData = _data.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from((midVol))));
        highPrice = function.evaluate(midVolData) - _price;
        if (highPrice <= 0) {
          rootVol = midVol;
        }
        if (Math.abs(dVol) < ACCURACY) {
          return _data.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from((midVol))));
        }
      }
      throw new OptionPricingException("Could not find volatility in " + MAX_ATTEMPTS + " attempts");
    }
  }
}
