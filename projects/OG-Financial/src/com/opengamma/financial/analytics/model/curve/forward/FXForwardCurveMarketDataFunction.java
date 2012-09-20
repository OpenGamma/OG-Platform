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

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification.QuoteType;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class FXForwardCurveMarketDataFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardCurveMarketDataFunction.class);
  /** Name of the calculation method */
  public static final String FX_FORWARD_QUOTES = "FXForwardQuotes";

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBFXForwardCurveDefinitionSource curveDefinitionSource = new ConfigDBFXForwardCurveDefinitionSource(configSource);
    final ConfigDBFXForwardCurveSpecificationSource curveSpecificationSource = new ConfigDBFXForwardCurveSpecificationSource(configSource);
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURVE).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_FORWARD_CURVE_MARKET_DATA, target.toSpecification(), properties);
        return Collections.singleton(spec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(target.getUniqueId());
        final String curveName = curveNames.iterator().next();
        final FXForwardCurveDefinition definition = curveDefinitionSource.getDefinition(curveName, currencyPair.toString());
        if (definition == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + curveName + " with target " + target);
        }
        final FXForwardCurveSpecification specification = curveSpecificationSource.getSpecification(curveName, currencyPair.toString());
        if (specification == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + curveName + " with target " + target);
        }
        final QuoteType quoteType = specification.getQuoteType();
        if (quoteType != FXForwardCurveSpecification.QuoteType.Outright && quoteType != FXForwardCurveSpecification.QuoteType.Points) {
          s_logger.error("Cannot handle quote type " + quoteType);
          return null;
        }
        final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
        final FXForwardCurveInstrumentProvider provider = specification.getCurveInstrumentProvider();
        for (final Tenor tenor : definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(atInstant.toLocalDate(), tenor);
          requirements.add(new ValueRequirement(provider.getDataFieldName(), identifier));
        }
        requirements.add(new ValueRequirement(provider.getDataFieldName(), provider.getSpotInstrument()));
        return requirements;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getUniqueId() == null) {
          return false;
        }
        return UnorderedCurrencyPair.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(target.getUniqueId());
        final FXForwardCurveDefinition definition = curveDefinitionSource.getDefinition(curveName, currencyPair.toString());
        if (definition == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + curveName + " for target " + target);
        }
        final FXForwardCurveSpecification specification = curveSpecificationSource.getSpecification(curveName, currencyPair.toString());
        if (specification == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + curveName + " for target " + target);
        }
        final FXForwardCurveInstrumentProvider provider = specification.getCurveInstrumentProvider();
        final ValueRequirement spotRequirement = new ValueRequirement(provider.getDataFieldName(), provider.getSpotInstrument());
        if (inputs.getValue(spotRequirement) == null) {
          throw new OpenGammaRuntimeException("Could not get value for spot; requirement was " + spotRequirement);
        }
        final Double spot = (Double) inputs.getValue(spotRequirement);
        final Map<ExternalId, Double> data = new HashMap<ExternalId, Double>();
        boolean isRegular = specification.isMarketQuoteConvention();
        for (final Tenor tenor : definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(now.toLocalDate(), tenor);
          final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), identifier);
          if (inputs.getValue(requirement) != null) {
            final Double value = (Double) inputs.getValue(requirement);
            switch (specification.getQuoteType()) {
              case Points:
                data.put(identifier, isRegular ? spot + value : 1 / (spot + value));
                break;
              case Outright:
                data.put(identifier, isRegular ? value : 1 / value);
                break;
              default:
                throw new OpenGammaRuntimeException("Cannot handle quote type " + specification.getQuoteType());
            }
          }
        }
        if (data.isEmpty()) {
          throw new OpenGammaRuntimeException("Could not get any market data for curve name " + curveName);
        }
        return Collections.singleton(new ComputedValue(getResultSpec(target, curveName), data));
      }

      private ValueSpecification getResultSpec(final ComputationTarget target, final String curveName) {
        final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, curveName).get();
        return new ValueSpecification(ValueRequirementNames.FX_FORWARD_CURVE_MARKET_DATA, target.toSpecification(), properties);
      }
    };
  }

}
