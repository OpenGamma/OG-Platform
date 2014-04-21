/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS;
import static com.opengamma.engine.value.ValuePropertyNames.CUBE;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_CUBE_DEFN;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_CUBE_SPEC;

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
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
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
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * Constructs a volatility cube from a definition and specification.
 * The market data is not manipulated (i.e. quotes are produced for each ticker,
 * but it is up to down-stream functions to put it in the form that the analytics
 * are expecting).
 */
public class RawVolatilityCubeDataFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(RawVolatilityCubeDataFunction.class);
  /** The volatility cube definition source */
  private VolatilityCubeDefinitionSource _volatilityCubeDefinitionSource;
  /** The volatility cube specification source */
  private VolatilityCubeSpecificationSource _volatilityCubeSpecificationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilityCubeDefinitionSource = ConfigDBVolatilityCubeDefinitionSource.init(context, this);
    _volatilityCubeSpecificationSource = ConfigDBVolatilityCubeSpecificationSource.init(context, this);
  }

  /**
   * Builds the market data requirements for a volatility cube.
   * @param <X> The type of the x axis data
   * @param <Y> The type of the y axis data
   * @param <Z> The type of the z axis data
   * @param specificationSource The specification source
   * @param definitionSource The definition source
   * @param specificationName The specification name
   * @param definitionName The definition name
   * @return The set of market data ids required to populate to volatility cube data snapshot object
   * @throws OpenGammaRuntimeException If the cube specification or definition is null or if the cube quote types
   * are not equal
   */
  public static <X, Y, Z> Set<ValueRequirement> buildDataRequirements(final VolatilityCubeSpecificationSource specificationSource,
      final VolatilityCubeDefinitionSource definitionSource, final String specificationName, final String definitionName) {
    final VolatilityCubeSpecification specification = specificationSource.getSpecification(specificationName);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube specification named " + specificationName);
    }
    final VolatilityCubeDefinition<X, Y, Z> definition = (VolatilityCubeDefinition<X, Y, Z>) definitionSource.getDefinition(definitionName);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube definition named " + definitionName);
    }
    if (!(definition.getCubeQuoteType().equals(specification.getCubeQuoteType()))) {
      throw new OpenGammaRuntimeException("Inconsistent cube quote type for definition (" + definition.getCubeQuoteType() +
          ") and specification (" + specification.getCubeQuoteType() + ")");
    }
    final CubeInstrumentProvider<X, Y, Z> provider = (CubeInstrumentProvider<X, Y, Z>) specification.getCubeInstrumentProvider();
    final Set<ValueRequirement> result = new HashSet<>();
    for (final X x : definition.getXs()) {
      for (final Y y : definition.getYs()) {
        for (final Z z : definition.getZs()) {
          if (x instanceof String && y instanceof String) {
            try {
              //TODO the type is not picked up successfully
              final Tenor xTenor = Tenor.parse((String) x);
              final Tenor yTenor = Tenor.parse((String) y);
              final Double zDouble = Double.parseDouble((String) z);
              final ExternalId identifier = provider.getInstrument((X) xTenor, (Y) yTenor, (Z) zDouble);
              result.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier));
            } catch (final DateTimeParseException e) {
              final ExternalId identifier = provider.getInstrument(x, y, z);
              result.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier));
            }
          } else {
            final ExternalId identifier = provider.getInstrument(x, y, z);
            result.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier));
          }
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
    final String definitionName = desiredValue.getConstraint(PROPERTY_CUBE_DEFINITION);
    final String specificationName = desiredValue.getConstraint(PROPERTY_CUBE_SPECIFICATION);
    final Object definitionObject = inputs.getValue(VOLATILITY_CUBE_DEFN);
    if (definitionObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube definition called " + definitionName);
    }
    final Object specificationObject = inputs.getValue(VOLATILITY_CUBE_SPEC);
    if (specificationObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube specification called " + specificationName);
    }
    final VolatilityCubeDefinition<Object, Object, Object> definition = (VolatilityCubeDefinition<Object, Object, Object>) definitionObject;
    final VolatilityCubeSpecification specification = (VolatilityCubeSpecification) specificationObject;
    final CubeInstrumentProvider<Object, Object, Object> provider = (CubeInstrumentProvider<Object, Object, Object>) specification.getCubeInstrumentProvider();
    final Map<Triple<Tenor, Tenor, Double>, Double> data = new HashMap<>();
    for (final Object xObj : definition.getXs()) {
      for (final Object yObj : definition.getYs()) {
        for (final Object zObj : definition.getZs()) { 
          final Tenor x = (xObj instanceof Tenor) ? (Tenor) xObj : Tenor.parse((String) xObj);
          final Tenor y = (yObj instanceof Tenor) ? (Tenor) yObj : Tenor.parse((String) yObj);
          final Double z = (zObj instanceof Double) ? (Double) zObj : Double.parseDouble((String) zObj);
          final ExternalId identifier = provider.getInstrument(x, y, z);
          final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier);
          final Object volatilityObject = inputs.getValue(requirement);
          if (volatilityObject != null) {
            final Double volatility = (Double) volatilityObject;
            final Triple<Tenor, Tenor, Double> coordinate = Triple.of(x, y, z);
            data.put(coordinate, volatility);
          } else {
            s_logger.info("Could not get market data for {}", identifier);
          }
        }
      }
    }
    final VolatilityCubeData<Tenor, Tenor, Double> volatilityCubeData = new VolatilityCubeData<>(definitionName, specificationName, data);
    final ValueProperties properties = createValueProperties()
        .with(PROPERTY_CUBE_DEFINITION, definitionName)
        .with(PROPERTY_CUBE_SPECIFICATION, specificationName)
        .with(PROPERTY_CUBE_QUOTE_TYPE, specification.getCubeQuoteType())
        .with(PROPERTY_CUBE_UNITS, specification.getVolatilityQuoteUnits()).get();
    return Collections.singleton(new ComputedValue(new ValueSpecification(VOLATILITY_CUBE_MARKET_DATA, target.toSpecification(), properties),
        volatilityCubeData));
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
        .withAny(PROPERTY_CUBE_DEFINITION)
        .withAny(PROPERTY_CUBE_SPECIFICATION)
        .withAny(PROPERTY_CUBE_QUOTE_TYPE)
        .withAny(PROPERTY_CUBE_UNITS).get();
    return Collections.singleton(new ValueSpecification(VOLATILITY_CUBE_MARKET_DATA, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> definitionNames = constraints.getValues(PROPERTY_CUBE_DEFINITION);
    if (definitionNames == null || definitionNames.size() != 1) {
      return null;
    }
    final Set<String> specificationNames = constraints.getValues(PROPERTY_CUBE_SPECIFICATION);
    if (specificationNames == null || specificationNames.size() != 1) {
      return null;
    }
    final String definitionName = Iterables.getOnlyElement(definitionNames);
    final String specificationName = Iterables.getOnlyElement(specificationNames);
    final Set<ValueRequirement> requirements = buildDataRequirements(_volatilityCubeSpecificationSource, _volatilityCubeDefinitionSource, specificationName, definitionName);
    final ValueProperties definitionProperties = ValueProperties.builder()
        .with(CUBE, definitionNames)
        .get();
    requirements.add(new ValueRequirement(VOLATILITY_CUBE_DEFN, ComputationTargetSpecification.NULL, definitionProperties));
    final ValueProperties specificationProperties = ValueProperties.builder()
        .with(CUBE, specificationNames)
        .get();
    requirements.add(new ValueRequirement(VOLATILITY_CUBE_SPEC, ComputationTargetSpecification.NULL, specificationProperties));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String definitionName = null;
    String specificationName = null;
    String cubeQuoteType = null;
    String cubeUnits = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification key = entry.getKey();
      if (key.getValueName().equals(VOLATILITY_CUBE_DEFN)) {
        definitionName = key.getProperty(CUBE);
        if (specificationName != null) {
          break; // don't want to have to loop through all market data points
        }
      } else if (key.getValueName().equals(VOLATILITY_CUBE_SPEC)) {
        specificationName = key.getProperty(CUBE);
        cubeQuoteType = key.getProperty(PROPERTY_CUBE_QUOTE_TYPE);
        cubeUnits = key.getProperty(PROPERTY_CUBE_UNITS);
        if (definitionName != null) {
          break; // don't want to have to loop through all market data points
        }
      }
    }
    if (definitionName == null || specificationName == null) {
      return null;
    }
    final ValueProperties properties = createValueProperties()
        .with(PROPERTY_CUBE_DEFINITION, definitionName)
        .with(PROPERTY_CUBE_SPECIFICATION, specificationName)
        .with(PROPERTY_CUBE_QUOTE_TYPE, cubeQuoteType)
        .with(PROPERTY_CUBE_UNITS, cubeUnits).get();
    return Collections.singleton(new ValueSpecification(VOLATILITY_CUBE_MARKET_DATA, target.toSpecification(), properties));
  }
}
