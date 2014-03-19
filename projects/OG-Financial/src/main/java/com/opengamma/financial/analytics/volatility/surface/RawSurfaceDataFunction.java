/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.SURFACE_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.SURFACE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.SURFACE_SPECIFICATION;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.SurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.surface.ConfigDBSurfaceDefinitionSource;
import com.opengamma.financial.analytics.surface.ConfigDBSurfaceSpecificationSource;
import com.opengamma.financial.analytics.surface.SurfaceDefinition;
import com.opengamma.financial.analytics.surface.SurfaceDefinitionSource;
import com.opengamma.financial.analytics.surface.SurfaceSpecification;
import com.opengamma.financial.analytics.surface.SurfaceSpecificationSource;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Constructs a surface from a definition and specification.
 * The market data is not manipulated (i.e. quotes are produced for each ticker,
 * but it is up to down-stream functions to put it in the form that the analytics
 * are expecting).
 */
public class RawSurfaceDataFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(RawSurfaceDataFunction.class);
  /** The surface definition source */
  private SurfaceDefinitionSource _surfaceDefinitionSource;
  /** The surface specification source */
  private SurfaceSpecificationSource _surfaceSpecificationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _surfaceDefinitionSource = ConfigDBSurfaceDefinitionSource.init(context, this);
    _surfaceSpecificationSource = ConfigDBSurfaceSpecificationSource.init(context, this);
  }

  /**
   * Builds the market data requirements for a surface.
   * @param <X> The type of the x axis data
   * @param <Y> The type of the y axis data
   * @param specificationSource The specification source
   * @param definitionSource The definition source
   * @param specificationName The specification name
   * @param definitionName The definition name
   * @return The set of market data ids required to populate to surface data snapshot object
   * @throws OpenGammaRuntimeException If the surface specification or definition is null
   */
  public static <X, Y> Set<ValueRequirement> buildDataRequirements(final SurfaceSpecificationSource specificationSource,
      final SurfaceDefinitionSource definitionSource, final String specificationName, final String definitionName) {
    final SurfaceSpecification specification = specificationSource.getSpecification(specificationName);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get surface specification named " + specificationName);
    }
    final SurfaceDefinition<X, Y> definition = (SurfaceDefinition<X, Y>) definitionSource.getDefinition(definitionName);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get surface definition named " + definitionName);
    }
    final SurfaceInstrumentProvider<X, Y> provider = (SurfaceInstrumentProvider<X, Y>) specification.getSurfaceInstrumentProvider();
    final Set<ValueRequirement> result = new HashSet<>();
    for (final X x : definition.getXs()) {
      for (final Y y : definition.getYs()) {
        if (x instanceof String && y instanceof String) {
          try {
            //TODO the type is not picked up successfully
            final Tenor xTenor = Tenor.parse((String) x);
            final Tenor yTenor = Tenor.parse((String) y);
            final ExternalId identifier = provider.getInstrument((X) xTenor, (Y) yTenor);
            result.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier));
          } catch (final DateTimeParseException e) {
            final ExternalId identifier = provider.getInstrument(x, y);
            result.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier));
          }
        } else {
          final ExternalId identifier = provider.getInstrument(x, y);
          result.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier));
        }
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String definitionName = desiredValue.getConstraint(PROPERTY_SURFACE_DEFINITION);
    final String specificationName = desiredValue.getConstraint(PROPERTY_SURFACE_SPECIFICATION);
    final Object definitionObject = inputs.getValue(SURFACE_DEFINITION);
    if (definitionObject == null) {
      throw new OpenGammaRuntimeException("Could not get surface definition called " + definitionName);
    }
    final Object specificationObject = inputs.getValue(SURFACE_SPECIFICATION);
    if (specificationObject == null) {
      throw new OpenGammaRuntimeException("Could not get surface specification called " + specificationName);
    }
    final SurfaceDefinition<Object, Object> definition = (SurfaceDefinition<Object, Object>) definitionObject;
    final SurfaceSpecification specification = (SurfaceSpecification) specificationObject;
    final SurfaceInstrumentProvider<Object, Object> provider = (SurfaceInstrumentProvider<Object, Object>) specification.getSurfaceInstrumentProvider();
    final Map<Pair<Tenor, Tenor>, Double> data = new HashMap<>();
    for (final Object x : definition.getXs()) {
      for (final Object y : definition.getYs()) {
        try {
          //TODO the type is not picked up successfully
          final Tenor xTenor = (x instanceof Tenor) ? (Tenor) x : Tenor.parse((String) x);
          final Tenor yTenor = (y instanceof Tenor) ? (Tenor) y : Tenor.parse((String) y);
          final ExternalId identifier = provider.getInstrument(xTenor, yTenor);

          final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier);
          final Object volatilityObject = inputs.getValue(requirement);
          if (volatilityObject != null) {
            final Double volatility = (Double) volatilityObject;
            final Pair<Tenor, Tenor> coordinate = Pairs.<Tenor, Tenor>of(xTenor, yTenor);
            data.put(coordinate, volatility);
          } else {
            s_logger.info("Could not get market data for {}", identifier);
          }
        } catch (final Exception e) {
          final ExternalId identifier = provider.getInstrument(x, y);
          s_logger.warn("Could not get market data for ticker {}. expiry = {}, maturity = {}", identifier, x, y);
        }
      }
    }
    final SurfaceData<Tenor, Tenor> surfaceData = new SurfaceData<>(definitionName + "_" + specificationName, data);
    final ValueProperties properties = createValueProperties()
        .with(PROPERTY_SURFACE_DEFINITION, definitionName)
        .with(PROPERTY_SURFACE_SPECIFICATION, specificationName)
        .with(PROPERTY_SURFACE_QUOTE_TYPE, specification.getQuoteType())
        .with(PROPERTY_SURFACE_UNITS, specification.getQuoteUnits()).get();
    return Collections.singleton(new ComputedValue(new ValueSpecification(SURFACE_DATA, target.toSpecification(), properties),
        surfaceData));
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(PROPERTY_SURFACE_DEFINITION)
        .withAny(PROPERTY_SURFACE_SPECIFICATION)
        .withAny(PROPERTY_SURFACE_QUOTE_TYPE)
        .withAny(PROPERTY_SURFACE_UNITS).get();
    return Collections.singleton(new ValueSpecification(SURFACE_DATA, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> definitionNames = constraints.getValues(PROPERTY_SURFACE_DEFINITION);
    if (definitionNames == null || definitionNames.size() != 1) {
      return null;
    }
    final Set<String> specificationNames = constraints.getValues(PROPERTY_SURFACE_SPECIFICATION);
    if (specificationNames == null || specificationNames.size() != 1) {
      return null;
    }
    final String definitionName = Iterables.getOnlyElement(definitionNames);
    final String specificationName = Iterables.getOnlyElement(specificationNames);
    final Set<ValueRequirement> requirements = buildDataRequirements(_surfaceSpecificationSource, _surfaceDefinitionSource, specificationName, definitionName);
    final ValueProperties definitionProperties = ValueProperties.builder()
        .with(SURFACE, definitionNames)
        .get();
    requirements.add(new ValueRequirement(SURFACE_DEFINITION, ComputationTargetSpecification.NULL, definitionProperties));
    final ValueProperties specificationProperties = ValueProperties.builder()
        .with(SURFACE, specificationNames)
        .get();
    requirements.add(new ValueRequirement(SURFACE_SPECIFICATION, ComputationTargetSpecification.NULL, specificationProperties));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String definitionName = null;
    String specificationName = null;
    String surfaceQuoteType = null;
    String surfaceUnits = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification key = entry.getKey();
      if (key.getValueName().equals(SURFACE_DEFINITION)) {
        definitionName = key.getProperty(SURFACE);
        if (specificationName != null) {
          break; // don't want to loop through all market data points
        }
      } else if (key.getValueName().equals(SURFACE_SPECIFICATION)) {
        specificationName = key.getProperty(SURFACE);
        surfaceQuoteType = key.getProperty(PROPERTY_SURFACE_QUOTE_TYPE);
        surfaceUnits = key.getProperty(PROPERTY_SURFACE_UNITS);
        if (definitionName != null) {
          break; // don't want to loop through all market data points
        }
      }
    }
    if (definitionName == null || specificationName == null) {
      return null;
    }
    final ValueProperties properties = createValueProperties()
        .with(PROPERTY_SURFACE_DEFINITION, definitionName)
        .with(PROPERTY_SURFACE_SPECIFICATION, specificationName)
        .with(PROPERTY_SURFACE_QUOTE_TYPE, surfaceQuoteType)
        .with(PROPERTY_SURFACE_UNITS, surfaceUnits)
        .get();
    return Collections.singleton(new ValueSpecification(SURFACE_DATA, target.toSpecification(), properties));
  }

}
