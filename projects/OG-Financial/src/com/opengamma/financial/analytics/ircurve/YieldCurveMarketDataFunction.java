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

import com.google.common.collect.Sets;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.security.SecurityUtils;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class YieldCurveMarketDataFunction extends AbstractFunction {

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
    
    final ComputationTargetSpecification currencySpec = new ComputationTargetSpecification(_helper.getYieldCurveKey().getCurrency());
    
    _marketDataResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, _helper.getYieldCurveKey().getName()).get());
    _results = Sets.newHashSet(_marketDataResult);
  }

  /**
   * 
   */
  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction implements
      StructuredMarketDataDataSourcingFunction {

    private final Set<ValueRequirement> _requirements;
    private final YieldCurveKey _yieldCurveKey;

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest,
        final Set<ValueRequirement> requirements, YieldCurveKey yieldCurveKey) {
      super(earliest, latest);
      _requirements = requirements;
      _yieldCurveKey = yieldCurveKey;
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
    public Set<Pair<StructuredMarketDataKey, ValueSpecification>> getStructuredMarketData() {
      HashSet<Pair<StructuredMarketDataKey, ValueSpecification>> ret = new HashSet<Pair<StructuredMarketDataKey, ValueSpecification>>();
      ret.add(Pair.of((StructuredMarketDataKey) _yieldCurveKey, _marketDataResult));
      return ret;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE;
    }

    @Override
    public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
      return _helper.canApplyTo(context, target);
    }

  }

  public static Set<ValueRequirement> buildRequirements(final InterpolatedYieldCurveSpecification specification,
      final FunctionCompilationContext context) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final FixedIncomeStripWithIdentifier strip : specification.getStrips()) {
      result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, strip.getSecurity()));
    }
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext
        .getConventionBundleSource(context);
    final ConventionBundle conventionBundle = conventionBundleSource.getConventionBundle(Identifier.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, specification.getCurrency().getCode() + "_SWAP"));
    final ConventionBundle referenceRateConvention = conventionBundleSource.getConventionBundle(IdentifierBundle
        .of(conventionBundle.getSwapFloatingLegInitialRate()));
    final Identifier initialRefRateId = SecurityUtils.bloombergTickerSecurityId(referenceRateConvention
        .getIdentifiers().getIdentifier(SecurityUtils.BLOOMBERG_TICKER));
    result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, initialRefRateId));
    return Collections.unmodifiableSet(result);
  }

  private SnapshotDataBundle buildMarketDataMap(final FunctionInputs inputs) {
    final Map<UniqueIdentifier, Double> marketDataMap = new HashMap<UniqueIdentifier, Double>();
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
    Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification> compile = _helper.compile(context,
        atInstant);
    return new CompiledImpl(compile.getFirst(), compile.getSecond(), buildRequirements(compile.getThird(), context),
        _helper.getYieldCurveKey());
  }
}
