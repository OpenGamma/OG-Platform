/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutput;
import com.opengamma.financial.analytics.DoubleCurrencyLabelledMatrix2D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.SumUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculates simple, linear aggregates at the portfolio node level over merged outputs.
 */
public class MergedOutputLinearAggregationPortfolioNodeFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return ImmutableSet.of(ValueSpecification.of(ValueRequirementNames.MERGED_OUTPUT, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    String mergedOutputName = desiredValue.getConstraint(ValuePropertyNames.NAME);
    ViewCalculationConfiguration calcConfig = context.getViewCalculationConfiguration();
    MergedOutput mergedOutput = calcConfig.getMergedOutput(mergedOutputName);
    if (mergedOutput == null) {
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final PortfolioNode node = target.getPortfolioNode();
    for (final Position position : node.getPositions()) {
      requirements.add(new ValueRequirement(ValueRequirementNames.MERGED_OUTPUT, ComputationTargetType.POSITION, position.getUniqueId(), desiredValue.getConstraints()));
    }
    for (final PortfolioNode childNode : node.getChildNodes()) {
      requirements.add(new ValueRequirement(ValueRequirementNames.MERGED_OUTPUT, ComputationTargetType.PORTFOLIO_NODE, childNode.getUniqueId(), desiredValue.getConstraints()));
    }
    return requirements;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    ValueRequirement exampleInput = Iterables.getFirst(inputs.values(), null);
    if (exampleInput == null) {
      return null;
    }
    String mergedOutputName = exampleInput.getConstraint(ValuePropertyNames.NAME);
    ValueProperties properties = createValueProperties().with(ValuePropertyNames.NAME, mergedOutputName).get();
    return ImmutableSet.of(ValueSpecification.of(ValueRequirementNames.MERGED_OUTPUT, target.toSpecification(), properties));
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // TODO jonathan 2014-01-13 -- as a proof-of-concept this supports aggregating very specific types. It should be
    // extended as required. 
    ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    String mergedOutputName = desiredValue.getConstraint(ValuePropertyNames.NAME);
    Object value = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      Object inputValue = input.getValue();
      if (inputValue instanceof String) {
        if (((String) inputValue).length() == 0) {
          continue;
        }
      }
      String ccyCode = input.getSpecification().getProperty(ValuePropertyNames.CURRENCY);
      Currency ccy = ccyCode != null ? Currency.parse(ccyCode) : null;
      if (ccy != null) {
        // When merging outputs we want to allow aggregation across currencies, so we add another dimension to known
        // data structures to include the currency
        if (inputValue instanceof Double) {
          inputValue = CurrencyAmount.of(ccy, (double) inputValue);
        } else if (inputValue instanceof DoubleLabelledMatrix1D) {
          DoubleLabelledMatrix1D inputMatrix = (DoubleLabelledMatrix1D) inputValue;
          // TODO jonathan 2014-01-08 -- an optional constraint should control whether we aggregate by key or label
          // We default to this since we usually want to see the matrix aggregated across currencies by label (e.g. 7D)
          // rather than repeating labels where the keys are slightly different (e.g. due to holiday differences)
          if (value != null && !(value instanceof DoubleCurrencyLabelledMatrix2D)) {
            throw new OpenGammaRuntimeException("Unable to aggregate " + value.getClass() + " with " + DoubleCurrencyLabelledMatrix2D.class);
          }
          inputValue = new DoubleCurrencyLabelledMatrix2D(
              inputMatrix.getKeys(), inputMatrix.getLabels(), inputMatrix.getLabelsTitle(),
              new Currency[] {ccy}, new Currency[] {ccy}, ValuePropertyNames.CURRENCY,
              new double[][] {inputMatrix.getValues()}, inputMatrix.getValuesTitle());
        }
      }
      String requirementDisplayName = ValueRequirementNames.MERGED_OUTPUT + " (" + mergedOutputName + ")";
      value = addValue(value, inputValue, requirementDisplayName);
    }
    if (value == null) {
      value = "";
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.MERGED_OUTPUT, target.toSpecification(), desiredValue.getConstraints()), value));
  }
  
  protected Object addValue(Object previousSum, Object currentValue, String requirementDisplayName) {
    if (previousSum == null) {
      return currentValue;
    }
    if (previousSum instanceof DoubleCurrencyLabelledMatrix2D && currentValue instanceof DoubleCurrencyLabelledMatrix2D) {
      DoubleCurrencyLabelledMatrix2D previousSumMatrix = (DoubleCurrencyLabelledMatrix2D) previousSum;
      DoubleCurrencyLabelledMatrix2D currentValueMatrix = (DoubleCurrencyLabelledMatrix2D) currentValue;
      return previousSumMatrix.addUsingDoubleLabels(currentValueMatrix);
    }
    return SumUtils.addValue(previousSum, currentValue, requirementDisplayName);
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }
  
}
