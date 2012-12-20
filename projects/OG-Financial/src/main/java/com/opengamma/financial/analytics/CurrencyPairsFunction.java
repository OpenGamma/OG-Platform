/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Set;

import javax.time.InstantProvider;

import com.google.common.collect.Iterables;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * 
 */
public class CurrencyPairsFunction extends AbstractFunction {
  /** Name of the currency pairs name property */
  public static final String CURRENCY_PAIRS_NAME = "CurrencyPairsName";

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
    return new AbstractInvokingCompiledFunction() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final String name = desiredValue.getConstraint(CURRENCY_PAIRS_NAME);
        final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
        final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
        final CurrencyPairs currencyPairs = currencyPairsSource.getCurrencyPairs(name);
        return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.CURRENCY_PAIRS, target.toSpecification(),
            createValueProperties().with(CURRENCY_PAIRS_NAME, name).get()), currencyPairs));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext myContext, final ComputationTarget target) {
        return true;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext myContext, final ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.CURRENCY_PAIRS, target.toSpecification(),
            createValueProperties().withAny(CURRENCY_PAIRS_NAME).get()));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext myContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final Set<String> names = desiredValue.getConstraints().getValues(CURRENCY_PAIRS_NAME);
        if (names == null || names.size() != 1) {
          return null;
        }
        return Collections.emptySet();
      }

    };
  }

}
