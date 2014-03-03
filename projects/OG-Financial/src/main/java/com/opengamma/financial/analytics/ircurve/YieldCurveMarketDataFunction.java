/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.marketdata.ExternalIdBundleResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
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

  public YieldCurveMarketDataFunction(final Currency currency, final String curveDefinitionName) {
    _helper = new YieldCurveFunctionHelper(currency, curveDefinitionName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _helper.init(context, this);
    final ComputationTargetSpecification currencySpec = ComputationTargetSpecification.of(_helper.getCurrency());
    _marketDataResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, _helper.getCurveName()).get());
    _results = Sets.newHashSet(_marketDataResult);
  }

  /**
   *
   */
  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final Set<ValueRequirement> _requirements;

    private CompiledImpl(final Instant earliest, final Instant latest, final Set<ValueRequirement> requirements) {
      super(earliest, latest);
      _requirements = requirements;
    }

    @Override
    public boolean canHandleMissingRequirements() {
      return true;
    }

    @Override
    public boolean canHandleMissingInputs() {
      return true;
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
        final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final SnapshotDataBundle map = buildMarketDataMap(executionContext, inputs);
      return Sets.newHashSet(new ComputedValue(_marketDataResult, map));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
        final ValueRequirement desiredValue) {
      return _requirements;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return _results;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.CURRENCY;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return _helper.getCurrency().equals(target.getValue());
    }

  }

  public static Set<ValueRequirement> buildRequirements(final InterpolatedYieldCurveSpecification specification) {
    final Set<ValueRequirement> result = new HashSet<>();
    for (final FixedIncomeStripWithIdentifier strip : specification.getStrips()) {
      result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, strip.getSecurity()));
    }
    return Collections.unmodifiableSet(result);
  }

  private static SnapshotDataBundle buildMarketDataMap(final FunctionExecutionContext context, final FunctionInputs inputs) {
    final SnapshotDataBundle marketData = new SnapshotDataBundle();
    final ExternalIdBundleResolver resolver = new ExternalIdBundleResolver(context.getComputationTargetResolver());
    for (final ComputedValue value : inputs.getAllValues()) {
      final ExternalIdBundle identifiers = value.getSpecification().getTargetSpecification().accept(resolver);
      double rate = (Double) value.getValue();
      //TODO this is here because KWCDC Curncy is not normalized
      if (rate > 1.1) {
        s_logger.error("Normalizing {}, value {}", value.getSpecification(), rate);
        s_logger.warn("Performing normalization of rate in YieldCurveMarketDataFunction; if this is being used for anything other than KWCDC Curncy check market data normalization");
        rate /= 100;
      }
      marketData.setDataPoint(identifiers, rate);
    }
    return marketData;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    try {
      _helper.init(context, this);
      final Triple<Instant, Instant, InterpolatedYieldCurveSpecification> compile = _helper.compile(context, atInstant, this);
      return new CompiledImpl(compile.getFirst(), compile.getSecond(), buildRequirements(compile.getThird()));
    } catch (final OpenGammaRuntimeException ogre) {
      s_logger.error("Function {} calculating {} on {} couldn't compile, rethrowing...",
                     getShortName(), _helper.getCurveName(), _helper.getCurrency());
      throw ogre;
    }

  }
}
