/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.commodity.calculator.CommodityFutureOptionBlackForwardGammaCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public class CommodityFutureOptionBlackForwardGammaFunction extends CommodityFutureOptionBlackFunction {

  /**
   * Default constructor
   */
  public CommodityFutureOptionBlackForwardGammaFunction() {
    super(ValueRequirementNames.FORWARD_GAMMA);
  }
  
  @Override
  protected boolean getFunctionIncludesCurrencyProperty() {
    return false;
  }
  
  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final double forwardGamma = derivative.accept(CommodityFutureOptionBlackForwardGammaCalculator.getInstance(), market);
    final ValueSpecification spec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    return Collections.singleton(new ComputedValue(spec, forwardGamma));
  }
}
