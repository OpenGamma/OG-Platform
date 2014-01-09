/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.equity.EquityOptionBlackVegaCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
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
 * Calculates the vega of an equity index option using the Black method.
 */
public class ListedEquityOptionBlackVegaFunction extends ListedEquityOptionBlackFunction {
  /** The vega calculator */
  private static final InstrumentDerivativeVisitor<StaticReplicationDataBundle, Double> CALCULATOR = EquityOptionBlackVegaCalculator.getInstance();

  /**
   * Default constructor
   */
  public ListedEquityOptionBlackVegaFunction() {
    super(ValueRequirementNames.VEGA);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final double vega = derivative.accept(CALCULATOR, market) / 100.;
    return Collections.singleton(new ComputedValue(resultSpec, vega));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> resultsWithCcy = super.getResults(context, target, inputs);
    return getResultsWithoutCurrency(resultsWithCcy);
  }
}
