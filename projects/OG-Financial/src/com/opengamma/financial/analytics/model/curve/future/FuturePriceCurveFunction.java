/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.future;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBFuturePriceCurveDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBFuturePriceCurveSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveDefinition;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.math.curve.NodalDoublesCurve;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class FuturePriceCurveFunction extends AbstractFunction {

  @SuppressWarnings("unchecked")
  private FuturePriceCurveDefinition<Object> getCurveDefinition(final ConfigDBFuturePriceCurveDefinitionSource source, final ComputationTarget target,
      final String definitionName) {
    final String fullDefinitionName = definitionName + "_" + target.getUniqueId().getValue();
    return (FuturePriceCurveDefinition<Object>) source.getDefinition(fullDefinitionName, InstrumentTypeProperties.IR_FUTURE_PRICE);
  }

  private FuturePriceCurveSpecification getCurveSpecification(final ConfigDBFuturePriceCurveSpecificationSource source, final ComputationTarget target,
      final String specificationName) {
    final String fullSpecificationName = specificationName + "_" + target.getUniqueId().getValue();
    return source.getSpecification(fullSpecificationName, InstrumentTypeProperties.IR_FUTURE_PRICE);
  }

  @SuppressWarnings("unchecked")
  public static Set<ValueRequirement> buildRequirements(final FuturePriceCurveSpecification futurePriceCurveSpecification, final FuturePriceCurveDefinition<Object> futurePriceCurveDefinition,
      final ZonedDateTime atInstant) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final FuturePriceCurveInstrumentProvider<Object> futurePriceCurveProvider = (FuturePriceCurveInstrumentProvider<Object>) futurePriceCurveSpecification.getCurveInstrumentProvider();
    for (final Object x : futurePriceCurveDefinition.getXs()) {
      final ExternalId identifier = futurePriceCurveProvider.getInstrument(x, atInstant.toLocalDate());
      result.add(new ValueRequirement(futurePriceCurveProvider.getDataFieldName(), identifier));
    }
    return result;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBFuturePriceCurveDefinitionSource curveDefinitionSource = new ConfigDBFuturePriceCurveDefinitionSource(configSource);
    final ConfigDBFuturePriceCurveSpecificationSource curveSpecificationSource = new ConfigDBFuturePriceCurveSpecificationSource(configSource);
    //TODO ENG-252 see MarketInstrumentImpliedYieldCurveFunction; need to work out the expiry more efficiently
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public final boolean canApplyTo(final FunctionCompilationContext myContext, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext myContext, final ComputationTarget target) {
        final ValueProperties curveProperties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE)
            .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_PRICE).get();
        final ValueSpecification futurePriceCurveResult = new ValueSpecification(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA,
            target.toSpecification(),
            curveProperties);
        return Collections.singleton(futurePriceCurveResult);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext myContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final String curveName;
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          throw new OpenGammaRuntimeException("Can only get a single curve; asked for " + curveNames);
        }
        curveName = curveNames.iterator().next();
        //TODO use separate definition and specification names?
        final String curveDefinitionName = curveName;
        final String curveSpecificationName = curveName;
        final FuturePriceCurveDefinition<Object> priceCurveDefinition = getCurveDefinition(curveDefinitionSource, target,
            curveDefinitionName);
        final FuturePriceCurveSpecification priceCurveSpecification = getCurveSpecification(curveSpecificationSource, target,
            curveSpecificationName);
        final Set<ValueRequirement> requirements = Collections.unmodifiableSet(buildRequirements(priceCurveSpecification, priceCurveDefinition, atInstant));
        return requirements;
      }

      @Override
      public boolean canHandleMissingInputs() {
        return true;
      }

      @SuppressWarnings({"unchecked", "synthetic-access" })
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        //TODO use separate definition and specification names?
        final String curveDefinitionName = curveName;
        final String curveSpecificationName = curveName;
        final FuturePriceCurveDefinition<Object> priceCurveDefinition = getCurveDefinition(curveDefinitionSource, target, curveDefinitionName);
        final FuturePriceCurveSpecification priceCurveSpecification = getCurveSpecification(curveSpecificationSource, target, curveSpecificationName);
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final DoubleArrayList xList = new DoubleArrayList();
        final DoubleArrayList prices = new DoubleArrayList();
        final FuturePriceCurveInstrumentProvider<Number> futurePriceCurveProvider = (FuturePriceCurveInstrumentProvider<Number>) priceCurveSpecification.getCurveInstrumentProvider();
        for (final Object x : priceCurveDefinition.getXs()) {
          final Number xNum = (Number) x;
          final ExternalId identifier = futurePriceCurveProvider.getInstrument(xNum, now.toLocalDate());
          final ValueRequirement requirement = new ValueRequirement(futurePriceCurveProvider.getDataFieldName(), identifier);
          Double futurePrice = null;
          if (inputs.getValue(requirement) != null) {
            futurePrice = (Double) inputs.getValue(requirement);
            if (futurePrice != null) {
              xList.add(xNum.doubleValue());
              prices.add(futurePrice);
            }
          }
        }
        final ValueSpecification futurePriceCurveResult = new ValueSpecification(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA,
            new ComputationTargetSpecification(priceCurveSpecification.getTarget()),
            createValueProperties()
            .with(ValuePropertyNames.CURVE, curveName)
            .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_PRICE).get());
        final NodalDoublesCurve curve = NodalDoublesCurve.from(xList.toDoubleArray(), prices.toDoubleArray());
        final ComputedValue futurePriceCurveResultValue = new ComputedValue(futurePriceCurveResult, curve);
        return Sets.newHashSet(futurePriceCurveResultValue);
      }
    };
  }
}
