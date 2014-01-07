/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Triple;

/**
 * Function to produce {@link InterpolatedYieldCurveSpecificationWithSecurities} and {@link YieldCurveData} values for a
 * named curve/currency pair. An instance must be created and put into the repository for each curve
 * definition to be made available to downstream functions which can reference the required curves using property
 * constraints.
 */
public class YieldCurveDataFunction extends AbstractFunction {

  private final YieldCurveFunctionHelper _helper;
  private final String _curveDefinitionName;
  private final ComputationTargetSpecification _targetSpec;

  private ValueSpecification _curveSpec;
  private ValueSpecification _curveDataSpec;

  public YieldCurveDataFunction(final String currency, final String curveDefinitionName) {
    this(Currency.of(currency), curveDefinitionName);
  }

  public YieldCurveDataFunction(final Currency currency, final String curveDefinitionName) {
    _helper = new YieldCurveFunctionHelper(currency, curveDefinitionName);
    _curveDefinitionName = curveDefinitionName;
    _targetSpec = ComputationTargetSpecification.of(currency);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _helper.init(context, this);
    ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, _curveDefinitionName).get();
    _curveSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SPEC, _targetSpec, properties);
    _curveDataSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_DATA, _targetSpec, properties);
  }

  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final InterpolatedYieldCurveSpecification _curveSpecification;

    private CompiledImpl(final Instant earliest, final Instant latest, final InterpolatedYieldCurveSpecification curveSpecification) {
      super(earliest, latest);
      _curveSpecification = curveSpecification;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.CURRENCY;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return _helper.getCurrency().equals(target.getValue());
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return ImmutableSet.of(_curveSpec, _curveDataSpec);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      return Collections.singleton(_helper.getMarketDataValueRequirement());
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      try {
        final FixedIncomeStripIdentifierAndMaturityBuilder builder =
            new FixedIncomeStripIdentifierAndMaturityBuilder(OpenGammaExecutionContext.getRegionSource(executionContext),
                                                             OpenGammaExecutionContext.getConventionBundleSource(executionContext),
                                                             executionContext.getSecuritySource(),
                                                             OpenGammaExecutionContext.getHolidaySource(executionContext));
        final SnapshotDataBundle marketData = _helper.getMarketDataMap(inputs);
        final InterpolatedYieldCurveSpecificationWithSecurities curveSpecificationWithSecurities = builder.resolveToSecurity(_curveSpecification, marketData);
        YieldCurveData curveData = new YieldCurveData(curveSpecificationWithSecurities, marketData.getDataPoints());
        return ImmutableSet.of(new ComputedValue(_curveSpec, curveSpecificationWithSecurities),
                               new ComputedValue(_curveDataSpec, curveData));
      } catch (final OpenGammaRuntimeException e) {
        throw new OpenGammaRuntimeException("Error in constructing " + _helper.getCurveName() + "_" + _helper.getCurrency() + ": " + e.getMessage());
      }
    }

  }

  @SuppressWarnings("synthetic-access")
  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    _helper.init(context, this);
    Triple<Instant, Instant, InterpolatedYieldCurveSpecification> compile = _helper.compile(context, atInstant, this);
    return new CompiledImpl(compile.getFirst(), compile.getSecond(), compile.getThird());
  }

}
