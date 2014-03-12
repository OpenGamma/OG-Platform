/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValueRequirementNames.STANDARD_VOLATILITY_CUBE_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.SURFACE_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.core.marketdatasnapshot.SurfaceData;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.volatility.CubeQuoteType;
import com.opengamma.financial.analytics.volatility.VolatilityQuoteUnits;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class MoneynessLognormalVolatilityCubeConverterFunction extends StandardVolatilityCubeDataFunction {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final VolatilityCubeData<Tenor, Tenor, Double> volatilityCubeData = (VolatilityCubeData<Tenor, Tenor, Double>) inputs.getValue(VOLATILITY_CUBE_MARKET_DATA);
    final SurfaceData<Tenor, Tenor> forwardSurfaceData = (SurfaceData<Tenor, Tenor>) inputs.getValue(SURFACE_DATA);
    final Map<Triple<Tenor, Tenor, Double>, Double> values = new HashMap<>();
    for (final Tenor x : volatilityCubeData.getXs()) {
      for (final Tenor y : volatilityCubeData.getYs()) {
        final Double forward = forwardSurfaceData.getValue(x, y);
        if (forward != null) {
          for (final Double z : volatilityCubeData.getZs()) {
            final Double data = volatilityCubeData.getVolatility(x, y, z);
            if (data != null) {
              final double strike = forward * (1 + z);
              values.put(Triple.of(x, y, strike), data);
            }
          }
        }
      }
    }
    final VolatilityCubeData<Tenor, Tenor, Double> resultCube = new VolatilityCubeData<>(volatilityCubeData.getDefinitionName(),
        volatilityCubeData.getSpecificationName(), values);
    final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy().get();
    final ValueSpecification spec = new ValueSpecification(STANDARD_VOLATILITY_CUBE_DATA, ComputationTargetSpecification.NULL, properties);
    return Collections.singleton(new ComputedValue(spec, resultCube));
  }

  @Override
  protected ValueProperties getInputCubeProperties(final Set<String> definitionNames, final Set<String> specificationNames) {
    return ValueProperties.builder()
        .with(PROPERTY_CUBE_DEFINITION, definitionNames)
        .with(PROPERTY_CUBE_SPECIFICATION, specificationNames)
        .with(PROPERTY_CUBE_QUOTE_TYPE, CubeQuoteType.EXPIRY_MATURITY_MONEYNESS.getName())
        .with(PROPERTY_CUBE_UNITS, VolatilityQuoteUnits.LOGNORMAL.getName())
        .get();
  }

  @Override
  protected ValueProperties getInputSurfaceProperties(final Set<String> definitionNames, final Set<String> specificationNames,
      final Set<String> calculationMethodNames) {
    if (calculationMethodNames == null) {
      return ValueProperties.builder()
          .with(PROPERTY_SURFACE_DEFINITION, definitionNames)
          .with(PROPERTY_SURFACE_SPECIFICATION, specificationNames)
          .get();
    }
    return ValueProperties.builder()
        .with(PROPERTY_SURFACE_DEFINITION, definitionNames)
        .with(PROPERTY_SURFACE_SPECIFICATION, specificationNames)
        .with(SURFACE_CALCULATION_METHOD, calculationMethodNames)
        .get();
  }

  @Override
  protected String getCubeQuoteUnits() {
    return VolatilityQuoteUnits.LOGNORMAL.getName();
  }

}
