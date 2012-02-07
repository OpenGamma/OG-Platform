/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.variance;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.equity.variance.VarianceSwapDataBundle2;
import com.opengamma.financial.equity.variance.VarianceSwapStaticReplicationFactory;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication2;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;

/**
 * 
 */
public class EquityVarianceSwapPresentValueFunction extends EquityVarianceSwapFunction {
  private static final VarianceSwapStaticReplicationFactory FACTORY = new VarianceSwapStaticReplicationFactory();

  //  private static final VarianceSwapStaticReplication2 CALCULATOR = new VarianceSwapStaticReplication2();

  public EquityVarianceSwapPresentValueFunction(final String curveDefinitionName, final String surfaceDefinitionName, final String forwardCalculationMethod,
      final String strikeParameterizationMethodName) {
    super(curveDefinitionName, surfaceDefinitionName, forwardCalculationMethod, strikeParameterizationMethodName);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  protected Set<ComputedValue> getResults(final ComputationTarget target, final FunctionInputs inputs, final VarianceSwap derivative, final VarianceSwapDataBundle2<?> market) {
    VarianceSwapStaticReplication2 cal = FACTORY.make(market);
    return Collections.singleton(new ComputedValue(getValueSpecification(target), cal.presentValue(derivative, market)));
  }

  @Override
  protected ValueSpecification getValueSpecification(final ComputationTarget target) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode()).get();
    return new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
  }

}
