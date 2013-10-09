/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Triple;

/**
 * Function to produce {@link InterpolatedYieldCurveSpecificationWithSecurities} values for a named curve/currency pair. An instance must be created and put into the repository for each curve
 * definition to be made available to downstream functions which can reference the required curves using property constraints.
 */
public class YieldCurveSpecificationFunction extends AbstractFunction {

  private final YieldCurveFunctionHelper _helper;
  private final String _curveName;
  private final ComputationTargetSpecification _targetSpec;

  private ValueSpecification _resultSpec;

  public YieldCurveSpecificationFunction(final String currency, final String curveDefinitionName) {
    this(Currency.of(currency), curveDefinitionName);
  }

  public YieldCurveSpecificationFunction(final Currency currency, final String curveDefinitionName) {
    _helper = new YieldCurveFunctionHelper(currency, curveDefinitionName);
    _curveName = curveDefinitionName;
    _targetSpec = ComputationTargetSpecification.of(currency);
  }

  protected YieldCurveFunctionHelper getHelper() {
    return _helper;
  }

  protected String getCurveName() {
    return _curveName;
  }

  protected ComputationTargetSpecification getTargetSpecification() {
    return _targetSpec;
  }

  protected ValueSpecification getResultSpecification() {
    return _resultSpec;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _helper.init(context, this);
    _resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SPEC, getTargetSpecification(), createValueProperties().with(ValuePropertyNames.CURVE, getCurveName()).get());
  }

  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final InterpolatedYieldCurveSpecification _curveSpecification;

    private CompiledImpl(final Instant earliest, final Instant latest, final InterpolatedYieldCurveSpecification curveSpecification) {
      super(earliest, latest);
      _curveSpecification = curveSpecification;
    }

    protected InterpolatedYieldCurveSpecification getCurveSpecification() {
      return _curveSpecification;
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
      return Collections.singleton(getResultSpecification());
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      return Collections.singleton(getHelper().getMarketDataValueRequirement());
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      try {
        final FixedIncomeStripIdentifierAndMaturityBuilder builder = new FixedIncomeStripIdentifierAndMaturityBuilder(OpenGammaExecutionContext.getRegionSource(executionContext),
            OpenGammaExecutionContext.getConventionBundleSource(executionContext), executionContext.getSecuritySource(), OpenGammaExecutionContext.getHolidaySource(executionContext));
        final SnapshotDataBundle marketData = getHelper().getMarketDataMap(inputs);
        final InterpolatedYieldCurveSpecificationWithSecurities curveSpecificationWithSecurities = builder.resolveToSecurity(getCurveSpecification(), marketData);
        return Collections.singleton(new ComputedValue(getResultSpecification(), curveSpecificationWithSecurities));
      } catch (final OpenGammaRuntimeException e) {
        throw new OpenGammaRuntimeException("Error in constructing " + _helper.getCurveName() + "_" + _helper.getCurrency() + ": " + e.getMessage());
      }
    }

  }

  @SuppressWarnings("synthetic-access")
  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    getHelper().init(context, this);
    final Triple<Instant, Instant, InterpolatedYieldCurveSpecification> compile = getHelper().compile(context, atInstant, this);
    return new CompiledImpl(compile.getFirst(), compile.getSecond(), compile.getThird());
  }

}
