/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValueRequirementNames.STANDARD_VOLATILITY_CUBE_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.SURFACE_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Base class for functions that take in a raw volatility market data cube and return it in
 * a standard form (where the z axis is changed to be an absolute strike value).
 */
public abstract class StandardVolatilityCubeDataFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(PROPERTY_CUBE_DEFINITION)
        .withAny(PROPERTY_CUBE_SPECIFICATION)
        .withAny(PROPERTY_SURFACE_DEFINITION)
        .withAny(PROPERTY_SURFACE_SPECIFICATION)
        .with(PROPERTY_CUBE_UNITS, getCubeQuoteUnits())
        .get();
    return Collections.singleton(new ValueSpecification(STANDARD_VOLATILITY_CUBE_DATA, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cubeDefinitionNames = constraints.getValues(PROPERTY_CUBE_DEFINITION);
    if (cubeDefinitionNames == null || cubeDefinitionNames.size() != 1) {
      return null;
    }
    final Set<String> cubeSpecificationNames = constraints.getValues(PROPERTY_CUBE_SPECIFICATION);
    if (cubeSpecificationNames == null || cubeSpecificationNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceDefinitionNames = constraints.getValues(PROPERTY_SURFACE_DEFINITION);
    if (surfaceDefinitionNames == null || surfaceDefinitionNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceSpecificationNames = constraints.getValues(PROPERTY_SURFACE_SPECIFICATION);
    if (surfaceSpecificationNames == null || surfaceSpecificationNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceCalculationMethodNames = constraints.getValues(SURFACE_CALCULATION_METHOD); // Don't insist the surface calculation method is set
    final ValueProperties cubeProperties = getInputCubeProperties(cubeDefinitionNames, cubeSpecificationNames);
    final ValueProperties surfaceProperties = getInputSurfaceProperties(surfaceDefinitionNames, surfaceSpecificationNames,
        surfaceCalculationMethodNames);
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(new ValueRequirement(VOLATILITY_CUBE_MARKET_DATA, ComputationTargetSpecification.NULL, cubeProperties));
    requirements.add(new ValueRequirement(SURFACE_DATA, ComputationTargetSpecification.NULL, surfaceProperties));
    return requirements;
  }

  /**
   * Gets the cube quote volatility units (e.g. lognormal or normal).
   * @return The quote volatility units
   */
  protected abstract String getCubeQuoteUnits();

  /**
   * Gets the properties for the raw input cube. Implementing classes should set the
   * {@link SurfaceAndCubePropertyNames#PROPERTY_CUBE_QUOTE_TYPE} and
   * {@link SurfaceAndCubePropertyNames#PROPERTY_CUBE_UNITS} properties.
   * @param definitionNames The definition name properties
   * @param specificationNames The specification name properties
   * @return The input cube properties
   */
  protected abstract ValueProperties getInputCubeProperties(Set<String> definitionNames, Set<String> specificationNames);

  /**
   * Gets the properties for the raw input surface. Implementing classes should set the
   * {@link SurfaceAndCurvePropertyNames#PROPERTY_SURFACE_QUOTE_TYPE} and
   * {@link SurfaceAndCurvePropertyNames#PROPERTY_SURFACE_UNITS} properties.
   * @param definitionNames The definition name properties
   * @param specificationNames The specification name properties
   * @param calculationMethodNames The calculation method name properties
   * @return the input surface properties
   */
  protected abstract ValueProperties getInputSurfaceProperties(Set<String> definitionNames, Set<String> specificationNames,
      Set<String> calculationMethodNames);
}
