/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.CapFloorDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class BlackCapModel extends AnalyticOptionModel<CapFloorDefinition, StandardOptionDataBundle> {

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final CapFloorDefinition definition) {
    if (definition == null)
      throw new IllegalArgumentException("Option definition was null");
    return new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardOptionDataBundle x) {
        // TODO Auto-generated method stub
        return null;
      }

    };
  }

}
