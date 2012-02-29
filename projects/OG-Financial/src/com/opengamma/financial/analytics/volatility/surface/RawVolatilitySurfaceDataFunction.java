/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

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
import com.opengamma.id.ExternalId;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class RawVolatilitySurfaceDataFunction extends AbstractFunction {
  /**
   * Value specification property for the surface result. This allows surface to be distinguished by instrument type (e.g. an FX volatility
   * surface, swaption ATM volatility surface).
   */
  public static final String PROPERTY_SURFACE_INSTRUMENT_TYPE = "InstrumentType";

  private final String _definitionName;
  private final String _specificationName;
  private final String _instrumentType;

  public RawVolatilitySurfaceDataFunction(final String definitionName, final String specificationName, final String instrumentType) {
    Validate.notNull(definitionName, "Definition Name");
    Validate.notNull(instrumentType, "Instrument Type");
    Validate.notNull(specificationName, "Specification Name");
    _definitionName = definitionName;
    _instrumentType = instrumentType;
    _specificationName = specificationName;
  }

  public String getDefinitionName() {
    return _definitionName;
  }

  public String getSpecificationName() {
    return _specificationName;
  }

  public abstract boolean isCorrectIdType(ComputationTarget target);

  @Override
  public String getShortName() {
    return _definitionName + " for " + _instrumentType + " from " + _specificationName + " Volatility Surface Data";
  }

  @SuppressWarnings("unchecked")
  public static <X, Y> Set<ValueRequirement> buildRequirements(final VolatilitySurfaceSpecification specification,
      final VolatilitySurfaceDefinition<X, Y> definition,
      final ZonedDateTime atInstant) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final SurfaceInstrumentProvider<X, Y> provider = (SurfaceInstrumentProvider<X, Y>) specification.getSurfaceInstrumentProvider();
    for (final X x : definition.getXs()) {
      // don't care what these are
      for (final Y y : definition.getYs()) {
        final ExternalId identifier = provider.getInstrument(x, y, atInstant.toLocalDate());
        result.add(new ValueRequirement(provider.getDataFieldName(), identifier));
      }
    }
    return result;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceDefinitionSource definitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    final ConfigDBVolatilitySurfaceSpecificationSource specificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext myContext, final ComputationTarget target) {
        final VolatilitySurfaceDefinition<Object, Object> definition = getSurfaceDefinition(definitionSource, target);
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DATA, new ComputationTargetSpecification(definition.getTarget()),
            createValueProperties().with(ValuePropertyNames.SURFACE, _definitionName).with(PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType).get()));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext myContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final VolatilitySurfaceDefinition<Object, Object> definition = getSurfaceDefinition(definitionSource, target);
        final VolatilitySurfaceSpecification specification = getSurfaceSpecification(specificationSource, target);
        final Set<ValueRequirement> requirements = Collections.unmodifiableSet(buildRequirements(specification, definition, atInstant));
        return requirements;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext myContext, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        try {
          getResults(myContext, target);
        } catch (final OpenGammaRuntimeException e) {
          return false;
        }
        return isCorrectIdType(target);
      }

      @SuppressWarnings({"unchecked", "synthetic-access" })
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final VolatilitySurfaceDefinition<Object, Object> definition = getSurfaceDefinition(definitionSource, target);
        final VolatilitySurfaceSpecification specification = getSurfaceSpecification(specificationSource, target);
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final Map<Pair<Object, Object>, Double> volatilityValues = new HashMap<Pair<Object, Object>, Double>();
        for (final Object x : definition.getXs()) {
          for (final Object y : definition.getYs()) {
            final SurfaceInstrumentProvider<Object, Object> provider = (SurfaceInstrumentProvider<Object, Object>) specification.getSurfaceInstrumentProvider();
            final ExternalId identifier = provider.getInstrument(x, y, now.toLocalDate());
            final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), identifier);
            if (inputs.getValue(requirement) != null) {
              final Double volatility = (Double) inputs.getValue(requirement);
              volatilityValues.put(Pair.of(x, y), volatility);
            }
          }
        }
        final VolatilitySurfaceData<?, ?> volSurfaceData = new VolatilitySurfaceData<Object, Object>(definition.getName(), specification.getName(),
            definition.getTarget(),
            definition.getXs(), definition.getYs(), volatilityValues);
        final ValueSpecification result = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DATA, new ComputationTargetSpecification(definition.getTarget()),
            createValueProperties().with(ValuePropertyNames.SURFACE, _definitionName).with(PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType).get());
        final ComputedValue resultValue = new ComputedValue(result, volSurfaceData);
        return Collections.singleton(resultValue);
      }

      @Override
      public boolean canHandleMissingInputs() {
        return true;
      }

      @SuppressWarnings({"unchecked", "synthetic-access" })
      private VolatilitySurfaceDefinition<Object, Object> getSurfaceDefinition(final ConfigDBVolatilitySurfaceDefinitionSource source, final ComputationTarget target) {
        final String definitionName = _definitionName + "_" + target.getUniqueId().getValue();
        final VolatilitySurfaceDefinition<Object, Object> definition = (VolatilitySurfaceDefinition<Object, Object>) source.getDefinition(definitionName, _instrumentType);
        if (definition == null) {
          throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + definitionName);
        }
        return definition;
      }

      @SuppressWarnings("synthetic-access")
      private VolatilitySurfaceSpecification getSurfaceSpecification(final ConfigDBVolatilitySurfaceSpecificationSource source, final ComputationTarget target) {
        final String specificationName = _specificationName + "_" + target.getUniqueId().getValue();
        final VolatilitySurfaceSpecification specification = source.getSpecification(specificationName, _instrumentType);
        if (specification == null) {
          throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + specificationName);
        }
        return specification;
      }
    };
  }
}
