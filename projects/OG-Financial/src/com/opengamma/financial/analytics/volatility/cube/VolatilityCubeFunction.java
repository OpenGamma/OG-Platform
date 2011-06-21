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
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class VolatilityCubeFunction extends AbstractFunction {

  private final VolatilityCubeFunctionHelper _helper;
  private ValueSpecification _cubeResult;
  private HashSet<ValueSpecification> _results;

  public VolatilityCubeFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public VolatilityCubeFunction(Currency currency, String definitionName) {
    _helper = new VolatilityCubeFunctionHelper(currency, definitionName);
  }
  
  @Override
  public void init(FunctionCompilationContext context) {
    
    ComputationTargetSpecification currencyTargetSpec = new ComputationTargetSpecification(_helper.getKey().getCurrency());
    _cubeResult = new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE, currencyTargetSpec,
        createValueProperties().with(ValuePropertyNames.CUBE, _helper.getKey().getName()).get());
    _results = Sets.newHashSet(_cubeResult);
  }

  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, InstantProvider atInstant) {
    final Set<ValueRequirement> requirements = Sets.newHashSet(getMarketDataRequirement());
    return new AbstractFunction.AbstractInvokingCompiledFunction() {
      
      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }
      
      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target,
          Map<ValueSpecification, ValueRequirement> inputs) {
        return _results;
      }
      
      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return _results;
      }
      
      @Override
      public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target,
          ValueRequirement desiredValue) {
        return requirements;
      }
      
      @Override
      public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
        return _helper.getKey().getCurrency().getUniqueId().equals(target.getUniqueId());
      }

      @Override
      public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
          ComputationTarget target, Set<ValueRequirement> desiredValues) {
        
        VolatilityCubeData data = (VolatilityCubeData) inputs.getValue(getMarketDataRequirement());
        Map<Tenor, Map<Tenor, Pair<double[], double[]>>> smiles = data.getSmiles();
        //TODO this
        return Sets.newHashSet(new ComputedValue(_cubeResult, smiles.size()));
      }
    };
  }

  private ValueRequirement getMarketDataRequirement() {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA,
        new ComputationTargetSpecification(_helper.getKey().getCurrency()),
        ValueProperties.with(ValuePropertyNames.CUBE, _helper.getKey().getName()).get());
  }

}
