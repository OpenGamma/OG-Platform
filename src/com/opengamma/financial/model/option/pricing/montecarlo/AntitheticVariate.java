/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.montecarlo;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class AntitheticVariate<T extends OptionDefinition, U extends StandardOptionDataBundle> {

  public Function1D<Double, Double> getVariateFunction(final Function1D<Double, Double> pathGenerator) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double e) {
        return pathGenerator.evaluate(-e);
      }
    };
  }
}
