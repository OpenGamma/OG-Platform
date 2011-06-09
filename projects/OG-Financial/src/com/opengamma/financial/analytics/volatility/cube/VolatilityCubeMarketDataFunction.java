/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.time.InstantProvider;

import com.google.common.collect.Sets;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.StructuredMarketDataDataSourcingFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class VolatilityCubeMarketDataFunction extends AbstractFunction {

  private ValueSpecification _marketDataResult;
  private ValueSpecification _definitionResult;
  private Set<ValueSpecification> _results;
  private final VolatilityCubeFunctionHelper _helper;
  private VolatilityCubeDefinition _definition;

  public VolatilityCubeMarketDataFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public VolatilityCubeMarketDataFunction(Currency currency, String definitionName) {
    _helper = new VolatilityCubeFunctionHelper(currency, definitionName);
  }
  
  @Override
  public void init(final FunctionCompilationContext context) {
    _definition = _helper.init(context, this);
    
    final ComputationTargetSpecification currencySpec = new ComputationTargetSpecification(_helper.getKey().getCurrency());
    
    _marketDataResult = new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, currencySpec,
        createValueProperties().with(ValuePropertyNames.CUBE, _helper.getKey().getName()).get());
    _definitionResult = new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_DEFN, currencySpec,
        createValueProperties().with(ValuePropertyNames.CUBE, _helper.getKey().getName()).get());
    _results = Sets.newHashSet(_marketDataResult, _definitionResult);
  }
  
  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, InstantProvider atInstant) {
    Triple<InstantProvider, InstantProvider, VolatilityCubeSpecification> compile = _helper.compile(context, atInstant);
    return new CompiledImpl(compile.getFirst(), compile.getSecond(), buildRequirements(compile.getThird(), context),
        _helper.getKey());
  }
  
  private Set<ValueRequirement> buildRequirements(VolatilityCubeSpecification third, FunctionCompilationContext context) {
    return new HashSet<ValueRequirement>(); //TODO: This, when we've worked out the tickers
  }

  private VolatilityCubeData buildMarketDataMap(final FunctionInputs inputs) {
    //TODO: this    
    VolatilityCubeData volatilityCubeData = new VolatilityCubeData();
    volatilityCubeData.setDataPoints(new HashMap<VolatilityPoint, Double>());
    return volatilityCubeData;
  }
  
  /**
   * 
   */
  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction implements StructuredMarketDataDataSourcingFunction {

    private final Set<ValueRequirement> _requirements;
    private final VolatilityCubeKey _volatilityCubeKey;

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest,
        final Set<ValueRequirement> requirements,  VolatilityCubeKey volatilityCubeKey) {
      super(earliest, latest);
      _requirements = requirements;
      _volatilityCubeKey = volatilityCubeKey;
    }

    public VolatilityCubeKey getVolatilityCubeKey() {
      return _volatilityCubeKey;
    }

    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
        ComputationTarget target, Set<ValueRequirement> desiredValues) {
      VolatilityCubeData map = buildMarketDataMap(inputs);
      return Sets.newHashSet(new ComputedValue(_marketDataResult, map), new ComputedValue(_definitionResult, _definition));
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target,
        ValueRequirement desiredValue) {
      if (canApplyTo(context, target)) {
        return _requirements;
      }
      return null;
    }

    @Override
    public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
      return _results;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE;
    }

    @Override
    public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
      return _helper.canApplyTo(context, target);
    }

    @Override
    public Set<Pair<StructuredMarketDataKey, ValueSpecification>> getStructuredMarketData() {
      HashSet<Pair<StructuredMarketDataKey, ValueSpecification>> ret = new HashSet<Pair<StructuredMarketDataKey, ValueSpecification>>();
      ret.add(Pair.of((StructuredMarketDataKey) _volatilityCubeKey, _marketDataResult));
      return ret;
    }
  }
}
