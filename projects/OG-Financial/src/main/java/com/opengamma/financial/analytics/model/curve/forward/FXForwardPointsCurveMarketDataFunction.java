/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification.QuoteType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class FXForwardPointsCurveMarketDataFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardPointsCurveMarketDataFunction.class);
  /** Name of the calculation method */
  public static final String FX_FORWARD_QUOTES = "FXForwardQuotes";

  private ConfigDBFXForwardCurveSpecificationSource _fxForwardCurveSpecificationSource;
  private ConfigDBFXForwardCurveDefinitionSource _fxForwardCurveDefinitionSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _fxForwardCurveSpecificationSource = ConfigDBFXForwardCurveSpecificationSource.init(context, this);
    _fxForwardCurveDefinitionSource = ConfigDBFXForwardCurveDefinitionSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext myContext, final ComputationTarget target) {
        @SuppressWarnings("synthetic-access")
        final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURVE).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_FORWARD_POINTS_CURVE_MARKET_DATA, target.toSpecification(), properties);
        return Collections.singleton(spec);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext myContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          s_logger.error("Asked for FX forward curve market data, but did not supply a single FX forward curve name. The property Curve must be set.");
          return null;
        }
        final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(target.getUniqueId());
        final String curveName = curveNames.iterator().next();
        final FXForwardCurveDefinition definition = _fxForwardCurveDefinitionSource.getDefinition(curveName, currencyPair.toString());
        if (definition == null) {
          s_logger.error("Couldn't find FX forward curve definition called " + curveName + " with target " + target);
          return null;
        }
        final FXForwardCurveSpecification specification = _fxForwardCurveSpecificationSource.getSpecification(curveName, currencyPair.toString());
        if (specification == null) {
          s_logger.error("Couldn't find FX forward curve specification called " + curveName + " with target " + target);
          return null;
        }
        final QuoteType quoteType = specification.getQuoteType();
        if (quoteType != FXForwardCurveSpecification.QuoteType.Points) {
          s_logger.error("Cannot handle quote type " + quoteType);
          return null;
        }
        final Set<ValueRequirement> requirements = new HashSet<>();
        final FXForwardCurveInstrumentProvider provider = specification.getCurveInstrumentProvider();
        for (final Tenor tenor : definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(atZDT.toLocalDate(), tenor);
          requirements.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier));
        }
        return requirements;
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(target.getUniqueId());
        final FXForwardCurveDefinition definition = _fxForwardCurveDefinitionSource.getDefinition(curveName, currencyPair.toString());
        if (definition == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + curveName + " for target " + target);
        }
        final FXForwardCurveSpecification specification = _fxForwardCurveSpecificationSource.getSpecification(curveName, currencyPair.toString());
        if (specification == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + curveName + " for target " + target);
        }
        final FXForwardCurveInstrumentProvider provider = specification.getCurveInstrumentProvider();
        final Map<ExternalId, Double> data = new HashMap<>();
        final boolean isRegular = specification.isMarketQuoteConvention();
        for (final Tenor tenor : definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(now.toLocalDate(), tenor);
          final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier);
          if (inputs.getValue(requirement) != null) {
            final Double value = (Double) inputs.getValue(requirement);
            data.put(identifier, isRegular ? value : 1 / value);
          }
        }
        if (data.isEmpty()) {
          throw new OpenGammaRuntimeException("Could not get any market data for curve name " + curveName);
        }
        return Collections.singleton(new ComputedValue(getResultSpec(target, curveName), data));
      }

      private ValueSpecification getResultSpec(final ComputationTarget target, final String curveName) {
        @SuppressWarnings("synthetic-access")
        final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, curveName).get();
        return new ValueSpecification(ValueRequirementNames.FX_FORWARD_POINTS_CURVE_MARKET_DATA, target.toSpecification(), properties);
      }

      @Override
      public boolean canHandleMissingRequirements() {
        return true;
      }

      @Override
      public boolean canHandleMissingInputs() {
        return true;
      }
    };
  }

}
