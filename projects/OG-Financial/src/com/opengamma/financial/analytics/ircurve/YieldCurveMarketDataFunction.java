/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class YieldCurveMarketDataFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveMarketDataFunction.class);
  private ValueSpecification _marketDataResult;
  private Set<ValueSpecification> _results;
  private final YieldCurveFunctionHelper _helper;

  public YieldCurveMarketDataFunction(final String currency, final String curveDefinitionName) {
    this(Currency.of(currency), curveDefinitionName);
  }

  public YieldCurveMarketDataFunction(Currency currency, String curveDefinitionName) {
    _helper = new YieldCurveFunctionHelper(currency, curveDefinitionName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _helper.init(context, this);
    
    final ComputationTargetSpecification currencySpec = new ComputationTargetSpecification(_helper.getCurrency());
    _marketDataResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, _helper.getCurveName()).get());
    _results = Sets.newHashSet(_marketDataResult);
  }

  /**
   * 
   */
  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final Set<ValueRequirement> _requirements;

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest, final Set<ValueRequirement> requirements) {
      super(earliest, latest);
      _requirements = requirements;
    }

    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
        ComputationTarget target, Set<ValueRequirement> desiredValues) {
      SnapshotDataBundle map = buildMarketDataMap(inputs);
      return Sets.newHashSet(new ComputedValue(_marketDataResult, map));
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
      return _helper.canApplyTo(target);
    }

  }

  public static Set<ValueRequirement> buildRequirements(final InterpolatedYieldCurveSpecification specification,
      final FunctionCompilationContext context) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final FixedIncomeStripWithIdentifier strip : specification.getStrips()) {
      result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, strip.getSecurity()));
    }
    return Collections.unmodifiableSet(result);
  }

  private SnapshotDataBundle buildMarketDataMap(final FunctionInputs inputs) {
    final Map<UniqueId, Double> marketDataMap = new HashMap<UniqueId, Double>();
    for (final ComputedValue value : inputs.getAllValues()) {
      final ComputationTargetSpecification targetSpecification = value.getSpecification().getTargetSpecification();
      marketDataMap.put(targetSpecification.getUniqueId(), (Double) value.getValue());
    }
    SnapshotDataBundle snapshotDataBundle = new SnapshotDataBundle();
    snapshotDataBundle.setDataPoints(marketDataMap);
    return snapshotDataBundle;
  }

  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, InstantProvider atInstant) {
    try {
      Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification> compile = _helper.compile(context, atInstant);
      return new CompiledImpl(compile.getFirst(), compile.getSecond(), buildRequirements(compile.getThird(), context));
    } catch (OpenGammaRuntimeException ogre) {
      s_logger.error("Function {} calculating {} on {} couldn't compile, rethrowing...", new Object[] {getShortName(), _helper.getCurveName(), _helper.getCurrency() });
      throw ogre;
    }
    
  }
}
