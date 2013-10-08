/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.equity.EquityOptionBlackThetaCalculator;
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
 * Calculates the spot delta of an equity index or equity option using the Black formula.
 */
public class ListedEquityOptionBlackThetaFunction extends ListedEquityOptionBlackFunction {
  /** Spot delta calculator */
  private static final InstrumentDerivativeVisitor<StaticReplicationDataBundle, Double> CALCULATOR = EquityOptionBlackThetaCalculator.getInstance();

  /**
   * Default constructor
   */
  public ListedEquityOptionBlackThetaFunction() {
    super(ValueRequirementNames.THETA);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final double theta = derivative.accept(CALCULATOR, market);
    return Collections.singleton(new ComputedValue(resultSpec, -theta / 365.));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> resultsWithCcy = super.getResults(context, target, inputs);
    return getResultsWithoutCurrency(resultsWithCcy);
  }
}

