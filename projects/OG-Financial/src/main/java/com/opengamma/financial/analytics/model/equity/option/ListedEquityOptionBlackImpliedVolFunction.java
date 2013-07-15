/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.equity.EquityOptionBlackImpliedVolatilityCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the Black implied volatility of an equity index option.
 */
public class ListedEquityOptionBlackImpliedVolFunction extends ListedEquityOptionBlackFunction {
  /** Implied volatility calculator */
  private static final InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> CALCULATOR = EquityOptionBlackImpliedVolatilityCalculator.getInstance();

  /**
   * Sets the result to {@link ValueRequirementNames#IMPLIED_VOLATILITY}
   */
  public ListedEquityOptionBlackImpliedVolFunction() {
    super(ValueRequirementNames.IMPLIED_VOLATILITY);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final double impliedVol = derivative.accept(CALCULATOR, market);
    return Collections.singleton(new ComputedValue(resultSpec, impliedVol));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> resultsWithCcy = super.getResults(context, target, inputs);
    return getResultsWithoutCurrency(resultsWithCcy);
  }

}
