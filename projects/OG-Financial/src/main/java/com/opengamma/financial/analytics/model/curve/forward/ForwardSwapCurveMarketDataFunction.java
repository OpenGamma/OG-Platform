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
import javax.time.calendar.Period;
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
import com.opengamma.financial.analytics.forwardcurve.ConfigDBForwardSwapCurveDefinitionSource;
import com.opengamma.financial.analytics.forwardcurve.ConfigDBForwardSwapCurveSpecificationSource;
import com.opengamma.financial.analytics.forwardcurve.ForwardSwapCurveDefinition;
import com.opengamma.financial.analytics.forwardcurve.ForwardSwapCurveInstrumentProvider;
import com.opengamma.financial.analytics.forwardcurve.ForwardSwapCurveSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class ForwardSwapCurveMarketDataFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ForwardSwapCurveMarketDataFunction.class);
  /** Name of the calculation method */
  public static final String FORWARD_SWAP_QUOTES = "ForwardSwapQuotes";
  /** Name of the forward tenor property */
  public static final String PROPERTY_FORWARD_TENOR = "ForwardTenor";

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBForwardSwapCurveDefinitionSource curveDefinitionSource = new ConfigDBForwardSwapCurveDefinitionSource(configSource);
    final ConfigDBForwardSwapCurveSpecificationSource curveSpecificationSource = new ConfigDBForwardSwapCurveSpecificationSource(configSource);
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        final ValueProperties properties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE)
            .withAny(PROPERTY_FORWARD_TENOR).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FORWARD_SWAP_CURVE_MARKET_DATA, target.toSpecification(), properties);
        return Collections.singleton(spec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        final Set<String> forwardTenorNames = constraints.getValues(PROPERTY_FORWARD_TENOR);
        if (forwardTenorNames == null || forwardTenorNames.size() != 1) {
          return null;
        }
        final Currency currency = Currency.of(target.getUniqueId().getValue());
        final String curveName = curveNames.iterator().next();
        final String forwardTenorName = forwardTenorNames.iterator().next();
        final ForwardSwapCurveDefinition definition = curveDefinitionSource.getDefinition(curveName, currency.toString());
        if (definition == null) {
          throw new OpenGammaRuntimeException("Couldn't find a forward swap curve definition called " + curveName + " with target " + target);
        }
        final ForwardSwapCurveSpecification specification = curveSpecificationSource.getSpecification(curveName, currency.toString());
        if (specification == null) {
          throw new OpenGammaRuntimeException("Couldn't find a forward swap curve specification called " + curveName + " with target " + target);
        }
        final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
        final ForwardSwapCurveInstrumentProvider provider = (ForwardSwapCurveInstrumentProvider) specification.getCurveInstrumentProvider();
        final Tenor forwardTenor = new Tenor(Period.parse(forwardTenorName));
        for (final Tenor tenor : definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(atInstant.toLocalDate(), tenor, forwardTenor);
          requirements.add(new ValueRequirement(provider.getDataFieldName(), identifier));
        }
        requirements.add(new ValueRequirement(provider.getDataFieldName(), provider.getSpotInstrument(forwardTenor)));
        return requirements;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getUniqueId() == null) {
          return false;
        }
        return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final String forwardTenorName = desiredValue.getConstraint(PROPERTY_FORWARD_TENOR);
        final Currency currencyPair = Currency.of(target.getUniqueId().getValue());
        final ForwardSwapCurveDefinition definition = curveDefinitionSource.getDefinition(curveName, currencyPair.toString());
        if (definition == null) {
          throw new OpenGammaRuntimeException("Couldn't find a forward swap curve definition called " + curveName + " for target " + target);
        }
        final ForwardSwapCurveSpecification specification = curveSpecificationSource.getSpecification(curveName, currencyPair.toString());
        if (specification == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + curveName + " for target " + target);
        }
        final ForwardSwapCurveInstrumentProvider provider = (ForwardSwapCurveInstrumentProvider) specification.getCurveInstrumentProvider();
        final Tenor forwardTenor = new Tenor(Period.parse(forwardTenorName));
        final ValueRequirement spotRequirement = new ValueRequirement(provider.getDataFieldName(), provider.getSpotInstrument(forwardTenor));
        if (inputs.getValue(spotRequirement) == null) {
          throw new OpenGammaRuntimeException("Could not get value for spot; requirement was " + spotRequirement);
        }
        final Double spot = (Double) inputs.getValue(spotRequirement);
        final Map<ExternalId, Double> data = new HashMap<ExternalId, Double>();
        for (final Tenor tenor : definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(now.toLocalDate(), tenor, forwardTenor);
          final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), identifier);
          if (inputs.getValue(requirement) != null) {
            final Double spread = (Double) inputs.getValue(requirement);
            data.put(identifier, spot + spread);
          }
        }
        if (data.isEmpty()) {
          throw new OpenGammaRuntimeException("Could not get any market data for curve name " + curveName);
        }
        return Collections.singleton(new ComputedValue(getResultSpec(target, curveName, forwardTenorName), data));
      }

      private ValueSpecification getResultSpec(final ComputationTarget target, final String curveName, final String forwardTenor) {
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CURVE, curveName)
            .with(PROPERTY_FORWARD_TENOR, forwardTenor).get();
        return new ValueSpecification(ValueRequirementNames.FORWARD_SWAP_CURVE_MARKET_DATA, target.toSpecification(), properties);
      }
    };
  }
}
