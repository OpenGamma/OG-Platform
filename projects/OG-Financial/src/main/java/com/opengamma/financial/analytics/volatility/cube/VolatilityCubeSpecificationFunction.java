/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS;
import static com.opengamma.engine.value.ValuePropertyNames.CUBE;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_CUBE_SPEC;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Gets a {@link VolatilityCubeSpecification} from the database.
 */
public class VolatilityCubeSpecificationFunction extends AbstractFunction {
  /** The cube definition name */
  private final String _cubeSpecificationName;
  /** The volatility cube specification source */
  private ConfigDBVolatilityCubeSpecificationSource _volatilityCubeSpecificationSource;

  /**
   * @param cubeSpecificationName The cube specification name, not null
   */
  public VolatilityCubeSpecificationFunction(final String cubeSpecificationName) {
    ArgumentChecker.notNull(cubeSpecificationName, "cubeSpecificationName");
    _cubeSpecificationName = cubeSpecificationName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilityCubeSpecificationSource = ConfigDBVolatilityCubeSpecificationSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext outerContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final VolatilityCubeSpecification specification = _volatilityCubeSpecificationSource.getSpecification(_cubeSpecificationName);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube specification called " + _cubeSpecificationName);
    }
    final ValueProperties properties = createValueProperties()
        .with(CUBE, specification.getName())
        .with(PROPERTY_CUBE_QUOTE_TYPE, specification.getCubeQuoteType())
        .with(PROPERTY_CUBE_UNITS, specification.getVolatilityQuoteUnits())
        .get();
    final ValueSpecification spec = new ValueSpecification(VOLATILITY_CUBE_SPEC, ComputationTargetSpecification.NULL, properties);
    final Set<ComputedValue> result = Collections.singleton(new ComputedValue(spec, specification));
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        return result;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.NULL;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        return Collections.singleton(spec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        return Collections.emptySet();
      }

    };
  }

}
