/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class IRFutureOptionVolatilitySurfaceAndFuturePriceDataFunction extends AbstractFunction {
  private final String _definitionName;
  private final String _specificationName;
  private final String _volSurfaceInstrumentType;
  private final String _priceCurveInstrumentType;

  public IRFutureOptionVolatilitySurfaceAndFuturePriceDataFunction(final String definitionName, final String specificationName, final String volSurfaceInstrumentType,
      final String priceCurveInstrumentType) {
    Validate.notNull(definitionName, "Definition Name");
    Validate.notNull(volSurfaceInstrumentType, "Instrument Type");
    Validate.notNull(specificationName, "Specification Name");
    _definitionName = definitionName;
    _specificationName = specificationName;
    _volSurfaceInstrumentType = volSurfaceInstrumentType;
    _priceCurveInstrumentType = priceCurveInstrumentType;
  }

  public String getDefinitionName() {
    return _definitionName;
  }

  public String getSpecificationName() {
    return _specificationName;
  }

  @SuppressWarnings("unchecked")
  private VolatilitySurfaceDefinition<Object, Object> getSurfaceDefinition(final ConfigDBVolatilitySurfaceDefinitionSource source, final ComputationTarget target) {
    final String definitionName = _definitionName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceDefinition<Object, Object> definition = (VolatilitySurfaceDefinition<Object, Object>) source.getDefinition(definitionName, _volSurfaceInstrumentType);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface definition with name " + definitionName);
    }
    return definition;
  }

  private VolatilitySurfaceSpecification getSurfaceSpecification(final ConfigDBVolatilitySurfaceSpecificationSource source, final ComputationTarget target) {
    final String specificationName = _specificationName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = source.getSpecification(specificationName, _volSurfaceInstrumentType);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface specification with name " + specificationName);
    }
    return specification;
  }

  @SuppressWarnings("unchecked")
  private FuturePriceCurveDefinition<Object> getCurveDefinition(final ConfigDBFuturePriceCurveDefinitionSource source, final ComputationTarget target) {
    final String definitionName = _definitionName + "_" + target.getUniqueId().getValue();
    return (FuturePriceCurveDefinition<Object>) source.getDefinition(definitionName, _priceCurveInstrumentType);
  }

  private FuturePriceCurveSpecification getCurveSpecification(final ConfigDBFuturePriceCurveSpecificationSource source, final ComputationTarget target) {
    final String specificationName = _specificationName + "_" + target.getUniqueId().getValue();
    return source.getSpecification(specificationName, _priceCurveInstrumentType);
  }

  @SuppressWarnings("unchecked")
  public static Set<ValueRequirement> buildRequirements(final VolatilitySurfaceSpecification volSurfaceSpecification,
      final VolatilitySurfaceDefinition<Object, Object> volSurfaceDefinition,
      final FuturePriceCurveSpecification futurePriceCurveSpecification,
      final FuturePriceCurveDefinition<Object> futurePriceCurveDefinition,
      final ZonedDateTime atInstant) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final SurfaceInstrumentProvider<Object, Double> volSurfaceProvider = (SurfaceInstrumentProvider<Object, Double>) volSurfaceSpecification.getSurfaceInstrumentProvider();
    final FuturePriceCurveInstrumentProvider<Object> futurePriceCurveProvider = (FuturePriceCurveInstrumentProvider<Object>) futurePriceCurveSpecification.getCurveInstrumentProvider();
    if (!Arrays.equals(volSurfaceDefinition.getXs(), futurePriceCurveDefinition.getXs())) {
      throw new OpenGammaRuntimeException("Do not have the same number of future options as futures (in the time direction)");
    }
    for (final Object x : volSurfaceDefinition.getXs()) {
      // don't care what these are
      for (final Object y : volSurfaceDefinition.getYs()) {
        final ExternalId identifier = volSurfaceProvider.getInstrument(x, (Double) y, atInstant.toLocalDate());
        result.add(new ValueRequirement(volSurfaceProvider.getDataFieldName(), identifier));
      }
    }
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
    final ConfigDBVolatilitySurfaceDefinitionSource surfaceDefinitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    final ConfigDBVolatilitySurfaceSpecificationSource surfaceSpecificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final ConfigDBFuturePriceCurveDefinitionSource curveDefinitionSource = new ConfigDBFuturePriceCurveDefinitionSource(configSource);
    final ConfigDBFuturePriceCurveSpecificationSource curveSpecificationSource = new ConfigDBFuturePriceCurveSpecificationSource(configSource);
    //TODO ENG-252 see MarketInstrumentImpliedYieldCurveFunction; need to work out the expiry more efficiently
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {
      private final DateAdjuster _nextExpiryAdjuster = new NextExpiryAdjuster();
      private final DateAdjuster _firstOfMonthAdjuster = DateAdjusters.firstDayOfMonth();

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext myContext, final ComputationTarget target) {
        final VolatilitySurfaceDefinition<Object, Object> volSurfaceDefinition = getSurfaceDefinition(surfaceDefinitionSource, target);
        final FuturePriceCurveSpecification priceCurveSpecification = getCurveSpecification(curveSpecificationSource, target);
        final ValueSpecification volSurfaceResult = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA,
            new ComputationTargetSpecification(volSurfaceDefinition.getTarget()),
            createValueProperties().with(ValuePropertyNames.SURFACE, _definitionName)
                .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, _volSurfaceInstrumentType).get());
        final ValueSpecification futurePriceCurveResult = new ValueSpecification(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA,
            new ComputationTargetSpecification(priceCurveSpecification.getTarget()),
            createValueProperties().with(ValuePropertyNames.CURVE, _definitionName)
                .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, _priceCurveInstrumentType).get());
        return Sets.newHashSet(volSurfaceResult, futurePriceCurveResult);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext myContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final VolatilitySurfaceDefinition<Object, Object> volSurfaceDefinition = getSurfaceDefinition(surfaceDefinitionSource, target);
        final VolatilitySurfaceSpecification volSurfaceSpecification = getSurfaceSpecification(surfaceSpecificationSource, target);
        final FuturePriceCurveDefinition<Object> priceCurveDefinition = getCurveDefinition(curveDefinitionSource, target);
        final FuturePriceCurveSpecification priceCurveSpecification = getCurveSpecification(curveSpecificationSource, target);
        final Set<ValueRequirement> requirements = Collections.unmodifiableSet(buildRequirements(volSurfaceSpecification, volSurfaceDefinition, priceCurveSpecification, priceCurveDefinition,
            atInstant));
        return requirements;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext myContext, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
      }

      @SuppressWarnings({"unchecked", "synthetic-access" })
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final VolatilitySurfaceDefinition<Object, Object> volSurfaceDefinition = getSurfaceDefinition(surfaceDefinitionSource, target);
        final VolatilitySurfaceSpecification volSurfaceSpecification = getSurfaceSpecification(surfaceSpecificationSource, target);
        final FuturePriceCurveDefinition<Object> priceCurveDefinition = getCurveDefinition(curveDefinitionSource, target);
        final FuturePriceCurveSpecification priceCurveSpecification = getCurveSpecification(curveSpecificationSource, target);
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final Map<Pair<Double, Double>, Double> volatilityValues = new HashMap<Pair<Double, Double>, Double>();
        final Map<Double, Double> futurePriceValues = new HashMap<Double, Double>();
        final List<Double> ts = new ArrayList<Double>();
        final List<Double> ks = new ArrayList<Double>();
        for (final Object x : volSurfaceDefinition.getXs()) {
          final Number xNum = (Number) x;
          final double t = getTime(xNum, now);
          final FuturePriceCurveInstrumentProvider<Number> futurePriceCurveProvider = (FuturePriceCurveInstrumentProvider<Number>) priceCurveSpecification.getCurveInstrumentProvider();
          ExternalId identifier = futurePriceCurveProvider.getInstrument(xNum, now.toLocalDate());
          ValueRequirement requirement = new ValueRequirement(futurePriceCurveProvider.getDataFieldName(), identifier);
          if (inputs.getValue(requirement) != null) {
            final Double futurePrice = (Double) inputs.getValue(requirement);
            futurePriceValues.put(t, futurePrice);
          }
          for (final Object y : volSurfaceDefinition.getYs()) {
            final Double yNum = (Double) y;
            final SurfaceInstrumentProvider<Number, Double> volSurfaceProvider = (SurfaceInstrumentProvider<Number, Double>) volSurfaceSpecification.getSurfaceInstrumentProvider();
            identifier = volSurfaceProvider.getInstrument(xNum, yNum, now.toLocalDate());
            requirement = new ValueRequirement(volSurfaceProvider.getDataFieldName(), identifier);
            final double k = yNum;
            ts.add(t);
            ks.add(k);
            if (inputs.getValue(requirement) != null) {
              final Double volatility = (Double) inputs.getValue(requirement);
              volatilityValues.put(Pair.of(t, k), volatility / 100);
            } else {
              volatilityValues.put(Pair.of(t, k), null);
            }
          }
        }
        final VolatilitySurfaceData<Double, Double> volSurfaceData = new VolatilitySurfaceData<Double, Double>(volSurfaceDefinition.getName(), volSurfaceSpecification.getName(),
            volSurfaceDefinition.getTarget(),
            ts.toArray(new Double[0]), ks.toArray(new Double[0]), volatilityValues);
        final ValueSpecification volSurfaceResult = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA,
            new ComputationTargetSpecification(volSurfaceDefinition.getTarget()),
            createValueProperties().with(ValuePropertyNames.SURFACE, _definitionName)
                .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, _volSurfaceInstrumentType).get());
        final ValueSpecification futurePriceCurveResult = new ValueSpecification(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA,
            new ComputationTargetSpecification(priceCurveSpecification.getTarget()),
            createValueProperties().with(ValuePropertyNames.CURVE, _definitionName)
                .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, _priceCurveInstrumentType).get());
        final ComputedValue volSurfaceResultValue = new ComputedValue(volSurfaceResult, volSurfaceData);
        final FuturePriceCurveData<Double> futurePriceCurveData = new FuturePriceCurveData<Double>(priceCurveDefinition.getName(), priceCurveSpecification.getName(),
            priceCurveDefinition.getTarget(), ts.toArray(new Double[0]), futurePriceValues);
        final ComputedValue futurePriceCurveResultValue = new ComputedValue(futurePriceCurveResult, futurePriceCurveData);
        return Sets.newHashSet(volSurfaceResultValue, futurePriceCurveResultValue);
      }

      @Override
      public boolean canHandleMissingInputs() {
        return true;
      }

      private double getTime(final Number x, final ZonedDateTime now) {
        final LocalDate today = now.toLocalDate();
        final int n = x.intValue();
        if (n == 1) {
          final LocalDate nextExpiry = today.with(_nextExpiryAdjuster);
          final LocalDate previousMonday = nextExpiry.minusDays(2); //TODO this should take a calendar and do two business days, and should use a convention for the number of days
          return DateUtils.getDaysBetween(today, previousMonday) / 365.; //TODO or use daycount?
        }
        final LocalDate date = today.with(_firstOfMonthAdjuster);
        final LocalDate plusMonths = date.plusMonths(n * 3); //TODO this is hard-coding the futures to be quarterly
        final LocalDate thirdWednesday = plusMonths.with(_nextExpiryAdjuster);
        final LocalDate previousMonday = thirdWednesday.minusDays(2); //TODO this should take a calendar and do two business days and also use a convention for the number of days
        return DateUtils.getDaysBetween(today, previousMonday) / 365.; //TODO or use daycount?
      }
    };
  }
}
