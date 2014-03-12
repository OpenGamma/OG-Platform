/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE;
import static com.opengamma.engine.value.ValuePropertyNames.CUBE;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_CUBE_DEFN;

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
 * Gets a {@link VolatilityCubeDefinition} from the database.
 */
public class VolatilityCubeDefinitionFunction extends AbstractFunction {
  /** The cube definition name */
  private final String _cubeDefinitionName;
  /** The volatility cube definition source */
  private ConfigDBVolatilityCubeDefinitionSource _volatilityCubeDefinitionSource;

  /**
   * @param cubeDefinitionName The cube definition name, not null
   */
  public VolatilityCubeDefinitionFunction(final String cubeDefinitionName) {
    ArgumentChecker.notNull(cubeDefinitionName, "cubeDefinitionName");
    _cubeDefinitionName = cubeDefinitionName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilityCubeDefinitionSource = ConfigDBVolatilityCubeDefinitionSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext outerContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final VolatilityCubeDefinition<?, ?, ?> definition = _volatilityCubeDefinitionSource.getDefinition(_cubeDefinitionName);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube definition called " + _cubeDefinitionName);
    }
    final ValueProperties properties = createValueProperties()
        .with(CUBE, definition.getName())
        .with(PROPERTY_CUBE_QUOTE_TYPE, definition.getCubeQuoteType())
        .get();
    final ValueSpecification spec = new ValueSpecification(VOLATILITY_CUBE_DEFN, ComputationTargetSpecification.NULL, properties);
    final Set<ComputedValue> result = Collections.singleton(new ComputedValue(spec, definition));
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
