/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.CapFloorDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class BlackCapletModel extends AnalyticOptionModel<CapFloorDefinition, StandardOptionDataBundle> {

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final CapFloorDefinition definition) {
    // TODO Auto-generated method stub
    return null;
  }

}
