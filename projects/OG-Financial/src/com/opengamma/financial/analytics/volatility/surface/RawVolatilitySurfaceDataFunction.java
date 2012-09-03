/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.id.ExternalId;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public abstract class RawVolatilitySurfaceDataFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(RawVolatilitySurfaceDataFunction.class);
  /**
   * Value specification property for the surface result. This allows surface to be distinguished by instrument type (e.g. an FX volatility
   * surface, swaption ATM volatility surface).
   */
  private final String _instrumentType;

  public RawVolatilitySurfaceDataFunction(final String instrumentType) {
    Validate.notNull(instrumentType, "Instrument Type");
    _instrumentType = instrumentType;
  }

  public abstract boolean isCorrectIdType(ComputationTarget target);

  protected String getInstrumentType() {
    return _instrumentType;
  }

  public static <X, Y> Set<ValueRequirement> buildDataRequirements(final VolatilitySurfaceSpecification specification, final VolatilitySurfaceDefinition<X, Y> definition,
      final ZonedDateTime atInstant, final String surfaceName, final String instrumentType) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    if (specification == null) {
      s_logger.error("Volatility surface specification called {} for instrument type {} was null", surfaceName, instrumentType);
      return null;
    }
    final SurfaceInstrumentProvider<X, Y> provider = (SurfaceInstrumentProvider<X, Y>) specification.getSurfaceInstrumentProvider();
    for (final X x : definition.getXs()) {
      for (final Y y : definition.getYs()) {
        final ExternalId identifier = provider.getInstrument(x, y, atInstant.toLocalDate());
        result.add(new ValueRequirement(provider.getDataFieldName(), identifier));
      }
    }
    final ValueProperties specProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits()).get();
    result.add(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_SPEC, specification.getTarget(), specProperties));
    return result;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext myContext, final InstantProvider atInstantProvider) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(myContext);
    final ConfigDBVolatilitySurfaceDefinitionSource definitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    final ConfigDBVolatilitySurfaceSpecificationSource specificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    return new CompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000), atInstant, definitionSource, specificationSource);
  }

  /**
   * Implementation of the compiled function
   */
  protected class CompiledFunction extends AbstractInvokingCompiledFunction {
    private final ZonedDateTime _now;
    private final ConfigDBVolatilitySurfaceDefinitionSource _definitionSource;
    private final ConfigDBVolatilitySurfaceSpecificationSource _specificationSource;

    public CompiledFunction(final ZonedDateTime from, final ZonedDateTime to, final ZonedDateTime now, final ConfigDBVolatilitySurfaceDefinitionSource definitionSource,
        final ConfigDBVolatilitySurfaceSpecificationSource specificationSource) {
      super(from, to);
      _now = now;
      _definitionSource = definitionSource;
      _specificationSource = specificationSource;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(),
          createValueProperties()
          .withAny(ValuePropertyNames.SURFACE)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
          .withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE)
          .withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS).get()));
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
      if (surfaceNames == null || surfaceNames.size() != 1) {
        s_logger.info("Can only get a single surface; asked for " + surfaceNames);
        return null;
      }
      final Set<String> instrumentTypes = desiredValue.getConstraints().getValues(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
      if (instrumentTypes == null || instrumentTypes.size() != 1) {
        s_logger.info("Did not specify a single instrument type; asked for " + instrumentTypes);
        return null;
      }
      final String surfaceName = surfaceNames.iterator().next();
      if (surfaceName == null) {
        throw new OpenGammaRuntimeException("Surface name was null");
      }
      final String instrumentType = instrumentTypes.iterator().next();
      if (instrumentType == null) {
        throw new OpenGammaRuntimeException("Instrument type was null");
      }
      final VolatilitySurfaceDefinition<Object, Object> definition = getSurfaceDefinition(target, surfaceName, instrumentType);
      final VolatilitySurfaceSpecification specification = getSurfaceSpecification(target, surfaceName, instrumentType);
      return buildDataRequirements(specification, definition, _now, surfaceName, instrumentType);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      String surfaceName = null;
      String instrumentType = null;
      String surfaceQuoteType = null;
      String surfaceUnits = null;
      for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
        final ValueSpecification spec = entry.getKey();
        if (spec.getValueName().equals(ValueRequirementNames.VOLATILITY_SURFACE_SPEC)) {
          surfaceName = spec.getProperty(ValuePropertyNames.SURFACE);
          instrumentType = spec.getProperty(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
          surfaceQuoteType = spec.getProperty(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE);
          surfaceUnits = spec.getProperty(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS);
          break;
        }
      }
      assert surfaceName != null;
      assert instrumentType != null;
      return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(),
          createValueProperties()
          .with(ValuePropertyNames.SURFACE, surfaceName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
          .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, surfaceQuoteType)
          .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, surfaceUnits).get()));
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      if (target.getType() != ComputationTargetType.PRIMITIVE) {
        return false;
      }
      return isCorrectIdType(target);
    }

    @SuppressWarnings({"synthetic-access" })
    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) {
      final ValueRequirement desiredValue = desiredValues.iterator().next();
      final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
      final String instrumentType = desiredValue.getConstraint(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
      final VolatilitySurfaceDefinition<Object, Object> definition = getSurfaceDefinition(target, surfaceName, instrumentType);
      final Object specificationObject = getSurfaceSpecification(target, surfaceName, instrumentType);
      final VolatilitySurfaceSpecification specification = (VolatilitySurfaceSpecification) specificationObject;
      final LocalDate valuationDate = executionContext.getValuationClock().today();
      final SurfaceInstrumentProvider<Object, Object> provider = (SurfaceInstrumentProvider<Object, Object>) specification.getSurfaceInstrumentProvider();
      final Map<Pair<Object, Object>, Double> volatilityValues = new HashMap<Pair<Object, Object>, Double>();
      final ObjectArrayList<Object> xList = new ObjectArrayList<Object>();
      final ObjectArrayList<Object> yList = new ObjectArrayList<Object>();
      for (final Object x : definition.getXs()) {
        for (final Object y : definition.getYs()) {
          final ExternalId identifier = provider.getInstrument(x, y, valuationDate);
          final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), identifier);
          final Double volatility = (Double) inputs.getValue(requirement);
          if (volatility != null) {
            xList.add(x);
            yList.add(y);
            volatilityValues.put(Pair.of(x, y), volatility);
          } else {
            s_logger.info("Missing value {}", identifier.toString());
          }
        }
      }
      final VolatilitySurfaceData<Object, Object> volSurfaceData = new VolatilitySurfaceData<Object, Object>(definition.getName(), specification.getName(),
          definition.getTarget(), definition.getXs(), definition.getYs(), volatilityValues);
      final ValueSpecification result = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(),
          createValueProperties()
          .with(ValuePropertyNames.SURFACE, surfaceName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
          .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
          .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits()).get());
      return Collections.singleton(new ComputedValue(result, volSurfaceData));
    }

    @Override
    public boolean canHandleMissingInputs() {
      return true;
    }

    @Override
    public boolean canHandleMissingRequirements() {
      return true;
    }

    protected VolatilitySurfaceDefinition<Object, Object> getSurfaceDefinition(final ComputationTarget target, final String definitionName, final String instrumentType) {
      final String fullDefinitionName = definitionName + "_" + target.getUniqueId().getValue();
      final VolatilitySurfaceDefinition<Object, Object> definition = (VolatilitySurfaceDefinition<Object, Object>) getDefinitionSource().getDefinition(fullDefinitionName, instrumentType);
      if (definition == null) {
        throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + instrumentType);
      }
      return definition;
    }

    protected VolatilitySurfaceSpecification getSurfaceSpecification(final ComputationTarget target, final String specificationName, final String instrumentType) {
      final String fullSpecificationName = specificationName + "_" + target.getUniqueId().getValue();
      final VolatilitySurfaceSpecification specification = getSpecificationSource().getSpecification(fullSpecificationName, instrumentType);
      if (specification == null) {
        throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName);
      }
      return specification;
    }

    protected ConfigDBVolatilitySurfaceDefinitionSource getDefinitionSource() {
      return _definitionSource;
    }

    protected ConfigDBVolatilitySurfaceSpecificationSource getSpecificationSource() {
      return _specificationSource;
    }
  };
}
