/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class ConstantElasticityOfVarianceModel extends AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> {

  @Override
  public Function1D<StandardOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    // TODO Auto-generated method stub
    return null;
  }

}
