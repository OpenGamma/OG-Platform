/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;

import com.google.common.collect.Sets;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class VolatilityCubeFunction extends AbstractFunction {
  //TODO use this class to get data in useable form
  private final VolatilityCubeFunctionHelper _helper;
  private ValueSpecification _cubeResult;
  private HashSet<ValueSpecification> _results;

  public VolatilityCubeFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public VolatilityCubeFunction(final Currency currency, final String definitionName) {
    _helper = new VolatilityCubeFunctionHelper(currency, definitionName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ComputationTargetSpecification currencyTargetSpec = new ComputationTargetSpecification(_helper.getKey().getCurrency());
    _cubeResult = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_CUBE_DATA, currencyTargetSpec,
        createValueProperties().with(ValuePropertyNames.CUBE, _helper.getKey().getName()).get());
    _results = Sets.newHashSet(_cubeResult);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
    final Set<ValueRequirement> requirements = Sets.newHashSet(getMarketDataRequirement());
    return new AbstractFunction.AbstractInvokingCompiledFunction() {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
          final Map<ValueSpecification, ValueRequirement> inputs) {
        return _results;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        return _results;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
          final ValueRequirement desiredValue) {
        return requirements;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        return _helper.getKey().getCurrency().getUniqueId().equals(target.getUniqueId());
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        @SuppressWarnings("unused")
        final VolatilityCubeData data = (VolatilityCubeData) inputs.getValue(getMarketDataRequirement());
        //TODO this is where the data should be sorted into useable form
        return Sets.newHashSet(new ComputedValue(_cubeResult, 0xdeadbeef));
      }
    };
  }

  private ValueRequirement getMarketDataRequirement() {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA,
        new ComputationTargetSpecification(_helper.getKey().getCurrency()),
        ValueProperties.with(ValuePropertyNames.CUBE, _helper.getKey().getName()).get());
  }

}
