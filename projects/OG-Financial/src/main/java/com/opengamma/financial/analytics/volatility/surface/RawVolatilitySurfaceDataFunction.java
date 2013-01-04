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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Gets volatility surface data from definitions and specifications. No financial modelling is done and the data points can be any type of data (e.g. price, implied lognormal volatility).
 */
public abstract class RawVolatilitySurfaceDataFunction extends AbstractFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(RawVolatilitySurfaceDataFunction.class);
  /**
   * Value specification property for the surface result. This allows surface to be distinguished by instrument type (e.g. an FX volatility
   * surface, swaption ATM volatility surface).
   */
  private final String _instrumentType;

  /**
   * @param instrumentType The instrument type, not null
   */
  public RawVolatilitySurfaceDataFunction(final String instrumentType) {
    ArgumentChecker.notNull(instrumentType, "Instrument Type");
    _instrumentType = instrumentType;
  }

  /**
   * Checks that the type of the unique identifier is correct
   * @param target The computation target
   * @return true if the unique id of the target matches that of the function
   */
  public abstract boolean isCorrectIdType(ComputationTarget target);

  /**
   * Gets a volatility surface definition from a name, target and instrument type. The full name of the surface is constructed as<p>
   * [SURFACE NAME]_[TARGET NAME]_[INSTRUMENT TYPE]<p>
   * @param definitionSource The surface definition source
   * @param target The computation target
   * @param definitionName The definition name
   * @return The volatility surface definition
   * @throws OpenGammaRuntimeException if a volatility surface definition with the full name is not found
   */
  protected abstract VolatilitySurfaceDefinition<?, ?> getDefinition(VolatilitySurfaceDefinitionSource definitionSource, ComputationTarget target, String definitionName);


  /**
   * Gets a volatility surface specification from a name, target and instrument type. The full name of the surface is constructed as<p>
   * [SURFACE NAME]_[TRIMMED TARGET]_[INSTRUMENT TYPE]<p>
   * @param specificationSource The surface specification source
   * @param target The computation target
   * @param specificationName The specification name
   * @return The volatility surface specification
   * @throws OpenGammaRuntimeException if a volatility surface specification with the full name is not found
   */
  protected abstract VolatilitySurfaceSpecification getSpecification(VolatilitySurfaceSpecificationSource specificationSource, ComputationTarget target, String specificationName);

  /**
   * @return The instrument type of the surface
   */
  protected String getInstrumentType() {
    return _instrumentType;
  }

  /**
   * Uses the volatility surface definition and specification to work out which market data requirements are needed to construct
   * the surface with the given name and type.
   * @param <X> The type of the x-axis data
   * @param <Y> The type of the y-axis data
   * @param specification The volatility specification
   * @param definition The volatility definition
   * @param atInstant The time stamp of the surface
   * @param surfaceName The surface name
   * @param instrumentType The instrument type
   * @return A set of market data value requirements, or null if the volatility surface specification or definition is null
   */
  public static <X, Y> Set<ValueRequirement> buildDataRequirements(final VolatilitySurfaceSpecification specification, final VolatilitySurfaceDefinition<X, Y> definition,
      final ZonedDateTime atInstant, final String surfaceName, final String instrumentType) {
    if (specification == null) {
      s_logger.error("Volatility surface specification called {} for instrument type {} was null", surfaceName, instrumentType);
      return null;
    }
    if (definition == null) {
      s_logger.error("Volatility surface definition called {} for instrument type {} was null", surfaceName, instrumentType);
      return null;
    }
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final SurfaceInstrumentProvider<X, Y> provider = (SurfaceInstrumentProvider<X, Y>) specification.getSurfaceInstrumentProvider();
    for (final X x : definition.getXs()) {
      for (final Y y : definition.getYs()) {
        final ExternalId identifier = provider.getInstrument(x, y, atInstant.toLocalDate());
        result.add(new ValueRequirement(provider.getDataFieldName(), identifier));
      }
    }
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
    /** The valuation time */
    private final ZonedDateTime _now;
    /** Source for volatility surface definitions */
    private final ConfigDBVolatilitySurfaceDefinitionSource _definitionSource;
    /** Source for volatility surface specifications */
    private final ConfigDBVolatilitySurfaceSpecificationSource _specificationSource;

    /**
     * @param from Earliest time that the invoker is valid
     * @param to Latest time that the invoker is valid
     * @param now The valuation time
     * @param definitionSource The volatility surface definition source
     * @param specificationSource The volatility surface specification source
     */
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
      final VolatilitySurfaceDefinition<?, ?> definition = getDefinition(_definitionSource, target, surfaceName);
      final VolatilitySurfaceSpecification specification = getSpecification(_specificationSource, target, surfaceName);
      return buildDataRequirements(specification, definition, _now, surfaceName, instrumentType);
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
      final VolatilitySurfaceDefinition<?, ?> definition = getDefinition(_definitionSource, target, surfaceName);
      final Object specificationObject = getSpecification(_specificationSource, target, surfaceName);
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

    /**
     * Gets the definition source.
     * @return The definition source
     */
    protected ConfigDBVolatilitySurfaceDefinitionSource getDefinitionSource() {
      return _definitionSource;
    }

    /**
     * Gets the specification source.
     * @return The specification source
     */
    protected ConfigDBVolatilitySurfaceSpecificationSource getSpecificationSource() {
      return _specificationSource;
    }
  };
}
