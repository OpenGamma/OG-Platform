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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Gets volatility surface data from definitions and specifications. No financial modelling is done and the data points can be any type of data (e.g. price, implied lognormal volatility).
 */
public abstract class RawVolatilitySurfaceDataFunction extends AbstractFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(RawVolatilitySurfaceDataFunction.class);
  /**
   * Value specification property for the surface result. This allows surface to be distinguished by instrument type (e.g. an FX volatility surface, swaption ATM volatility surface).
   */
  private final String _instrumentType;

  private ConfigDBVolatilitySurfaceDefinitionSource _volatilitySurfaceDefinitionSource;
  private ConfigDBVolatilitySurfaceSpecificationSource _volatilitySurfaceSpecificationSource;

  /**
   * @param instrumentType The instrument type, not null
   */
  public RawVolatilitySurfaceDataFunction(final String instrumentType) {
    ArgumentChecker.notNull(instrumentType, "Instrument Type");
    _instrumentType = instrumentType;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilitySurfaceDefinitionSource = ConfigDBVolatilitySurfaceDefinitionSource.init(context, this);
    _volatilitySurfaceSpecificationSource = ConfigDBVolatilitySurfaceSpecificationSource.init(context, this);
  }

  /**
   * Gets the target type for the surface
   * 
   * @return The target type
   */
  protected abstract ComputationTargetType getTargetType();

  /**
   * Determines whether this function applies to the target
   * 
   * @param context The compilation context
   * @param target The computation target
   * @return true if this function applies to the target
   */
  protected boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return true;
  }

  /**
   * Gets a volatility surface definition from a name, target and instrument type. The full name of the surface is constructed as
   * <p>
   * [SURFACE NAME]_[TARGET NAME]_[INSTRUMENT TYPE]
   * <p>
   * 
   * @param definitionSource The surface definition source
   * @param versionCorrection The version/correction timestamp
   * @param target The computation target
   * @param definitionName The definition name
   * @return The volatility surface definition
   * @throws OpenGammaRuntimeException if a volatility surface definition with the full name is not found
   */
  protected abstract VolatilitySurfaceDefinition<?, ?> getDefinition(VolatilitySurfaceDefinitionSource definitionSource, VersionCorrection versionCorrection, ComputationTarget target,
      String definitionName);

  /**
   * Gets a volatility surface specification from a name, target and instrument type. The full name of the surface is constructed as
   * <p>
   * [SURFACE NAME]_[TRIMMED TARGET]_[INSTRUMENT TYPE]
   * <p>
   * 
   * @param specificationSource The surface specification source
   * @param versionCorrection The version/correction timestamp
   * @param target The computation target
   * @param specificationName The specification name
   * @return The volatility surface specification
   * @throws OpenGammaRuntimeException if a volatility surface specification with the full name is not found
   */
  protected abstract VolatilitySurfaceSpecification getSpecification(VolatilitySurfaceSpecificationSource specificationSource, VersionCorrection versionCorrection, ComputationTarget target,
      String specificationName);

  /**
   * @return The instrument type of the surface
   */
  protected String getInstrumentType() {
    return _instrumentType;
  }

  /**
   * Uses the volatility surface definition and specification to work out which market data requirements are needed to construct the surface with the given name and type.
   * 
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
    final Set<ValueRequirement> result = new HashSet<>();
    final SurfaceInstrumentProvider<X, Y> provider = (SurfaceInstrumentProvider<X, Y>) specification.getSurfaceInstrumentProvider();
    for (final X x : definition.getXs()) {
      for (final Y y : definition.getYs()) {
        final ExternalId identifier = provider.getInstrument(x, y, atInstant.toLocalDate());
        result.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier));
      }
    }
    return result;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext myContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new CompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000), atZDT, _volatilitySurfaceDefinitionSource,
        _volatilitySurfaceSpecificationSource);
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
      super(from.toInstant(), to.toInstant());
      _now = now;
      _definitionSource = definitionSource;
      _specificationSource = specificationSource;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), createValueProperties()
          .withAny(ValuePropertyNames.SURFACE).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
          .withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE).withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS).get()));
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      // REVIEW 2013-11-06 Andrew -- This logic with the instrument type is not necessary - see getResults
      final String instrumentType = desiredValue.getConstraints().getStrictValue(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
      if (instrumentType == null) {
        return null;
      }
      if (!_instrumentType.equals(instrumentType)) {
        s_logger.error("Instrument type {} did not match that required {}", instrumentType, _instrumentType);
        return null;
      }
      final String surfaceName = desiredValue.getConstraints().getStrictValue(ValuePropertyNames.SURFACE);
      if (surfaceName == null) {
        return null;
      }
      try {
        final VolatilitySurfaceDefinition<?, ?> definition = getDefinition(_definitionSource, context.getComputationTargetResolver().getVersionCorrection(), target, surfaceName);
        if (definition == null) {
          s_logger.error("Could not get volatility surface definition for instrument type {} with target {} called {}", new Object[] {target, _instrumentType, surfaceName });
          return null;
        }
        final VolatilitySurfaceSpecification specification = getSpecification(_specificationSource, context.getComputationTargetResolver().getVersionCorrection(), target, surfaceName);
        if (specification == null) {
          s_logger.error("Could not get volatility surface specification for instrument type {} with target {} called {}", new Object[] {target, _instrumentType, surfaceName });
          return null;
        }
        final Set<ValueRequirement> requirements = buildDataRequirements(specification, definition, _now, surfaceName, instrumentType);
        final ValueProperties definitionProperties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surfaceName)
            .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType).get();
        final ValueProperties specificationProperties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surfaceName)
            .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
            .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
            .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits()).get();
        requirements.add(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_SPEC, target.toSpecification(), specificationProperties));
        requirements.add(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DEFINITION, target.toSpecification(), definitionProperties));
        return requirements;
      } catch (final Exception e) {
        s_logger.error(e.getMessage());
        return null;
      }
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      String surfaceQuoteType = null;
      String surfaceQuoteUnits = null;
      String surfaceName = null;
      for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
        final ValueSpecification key = entry.getKey();
        if (key.getValueName().equals(ValueRequirementNames.VOLATILITY_SURFACE_SPEC)) {
          surfaceQuoteType = key.getProperty(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE);
          surfaceQuoteUnits = key.getProperty(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS);
          surfaceName = key.getProperty(ValuePropertyNames.SURFACE);
          break;
        }
      }
      if (surfaceName == null) {
        return null;
      }
      assert surfaceQuoteType != null;
      assert surfaceQuoteUnits != null;
      return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), createValueProperties()
          .with(ValuePropertyNames.SURFACE, surfaceName).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
          .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, surfaceQuoteType).with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, surfaceQuoteUnits).get()));
    }

    @Override
    public ComputationTargetType getTargetType() {
      return RawVolatilitySurfaceDataFunction.this.getTargetType();
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return RawVolatilitySurfaceDataFunction.this.canApplyTo(context, target);
    }

    @SuppressWarnings({"synthetic-access" })
    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
      final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
      final String instrumentType = desiredValue.getConstraint(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
      final Object definitionObject = inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE_DEFINITION);
      if (definitionObject == null) {
        throw new OpenGammaRuntimeException("Could not get volatility surface definition");
      }
      final Object specificationObject = inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE_SPEC);
      if (specificationObject == null) {
        throw new OpenGammaRuntimeException("Could not get volatility surface specification");
      }
      @SuppressWarnings("unchecked")
      final VolatilitySurfaceDefinition<Object, Object> definition = (VolatilitySurfaceDefinition<Object, Object>) definitionObject;
      final VolatilitySurfaceSpecification specification = (VolatilitySurfaceSpecification) specificationObject;
      final LocalDate valuationDate = LocalDate.now(executionContext.getValuationClock());
      final SurfaceInstrumentProvider<Object, Object> provider = (SurfaceInstrumentProvider<Object, Object>) specification.getSurfaceInstrumentProvider();
      final Map<Pair<Object, Object>, Double> volatilityValues = new HashMap<>();
      final ObjectArrayList<Object> xList = new ObjectArrayList<>();
      final ObjectArrayList<Object> yList = new ObjectArrayList<>();
      for (final Object x : definition.getXs()) {
        for (final Object y : definition.getYs()) {
          final ExternalId identifier = provider.getInstrument(x, y, valuationDate);
          final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier);
          final Double volatility = (Double) inputs.getValue(requirement);
          if (volatility != null) {
            xList.add(x);
            yList.add(y);
            volatilityValues.put(Pairs.of(x, y), volatility);
          } else {
            s_logger.info("Missing value {}", identifier.toString());
          }
        }
      }
      final VolatilitySurfaceData<Object, Object> volSurfaceData = new VolatilitySurfaceData<>(definition.getName(), specification.getName(), definition.getTarget(), definition.getXs(),
          definition.getYs(), volatilityValues);
      final ValueSpecification result = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), createValueProperties()
          .with(ValuePropertyNames.SURFACE, surfaceName).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
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
     * 
     * @return The definition source
     */
    protected ConfigDBVolatilitySurfaceDefinitionSource getDefinitionSource() {
      return _definitionSource;
    }

    /**
     * Gets the specification source.
     * 
     * @return The specification source
     */
    protected ConfigDBVolatilitySurfaceSpecificationSource getSpecificationSource() {
      return _specificationSource;
    }
  };
}
