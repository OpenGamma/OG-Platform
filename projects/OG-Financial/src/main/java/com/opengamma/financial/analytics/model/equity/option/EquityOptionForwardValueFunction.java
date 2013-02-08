/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.option.EquityOptionBlackMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;

/**
 * The <b>forward</b> value of the index, i.e. the fair strike of a forward agreement paying the index value at maturity,
 * as seen from the selected market data
 */
public class EquityOptionForwardValueFunction extends EquityOptionFunction {

  /**
   * Default constructor
   */
  public EquityOptionForwardValueFunction() {
    super(ValueRequirementNames.FORWARD);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    //FIXME use the type system
    if (derivative instanceof EquityIndexOption) {
      final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
      return Collections.singleton(new ComputedValue(resultSpec, model.forwardIndexValue((EquityIndexOption) derivative, market)));
    }
    final EquityOptionBlackMethod model = EquityOptionBlackMethod.getInstance();
    return Collections.singleton(new ComputedValue(resultSpec, model.forwardIndexValue((EquityOption) derivative, market)));
  }

  @Override
  protected String getCalculationMethod() {
    return CalculationPropertyNamesAndValues.BLACK_METHOD;
  }

  @Override
  protected String getModelType() {
    return CalculationPropertyNamesAndValues.ANALYTIC;
  }

  //TODO this function return values unnecessary properties - the surface name, currency, interpolator and calculation method, which are used
  // to construct the market data bundle.
}
