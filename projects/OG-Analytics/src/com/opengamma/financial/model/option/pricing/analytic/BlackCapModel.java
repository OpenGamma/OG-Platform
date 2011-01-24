/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.CapFloorDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class BlackCapModel extends AnalyticOptionModel<CapFloorDefinition, StandardOptionDataBundle> {

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final CapFloorDefinition definition) {
    Validate.notNull(definition);
    return new Function1D<StandardOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final StandardOptionDataBundle data) {
        Validate.notNull(data);
        //TODO
        return null;
      }

    };
  }

}
