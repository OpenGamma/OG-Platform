/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.surface;

import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.SURFACE_DEFINITION;

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
 * Gets a {@link SurfaceDefinition} from the database.
 */
public class SurfaceDefinitionFunction extends AbstractFunction {
  /** The surface definition name */
  private final String _surfaceDefinitionName;
  /** The surface definition source */
  private ConfigDBSurfaceDefinitionSource _surfaceDefinitionSource;

  /**
   * @param surfaceDefinitionName The surface definition name, not null
   */
  public SurfaceDefinitionFunction(final String surfaceDefinitionName) {
    ArgumentChecker.notNull(surfaceDefinitionName, "surfaceDefinitionName");
    _surfaceDefinitionName = surfaceDefinitionName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _surfaceDefinitionSource = ConfigDBSurfaceDefinitionSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext outerContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final SurfaceDefinition<?, ?> definition = _surfaceDefinitionSource.getDefinition(_surfaceDefinitionName);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get definition called " + _surfaceDefinitionName);
    }
    final ValueProperties properties = createValueProperties()
        .with(SURFACE, _surfaceDefinitionName)
        .get();
    final ValueSpecification spec = new ValueSpecification(SURFACE_DEFINITION, ComputationTargetSpecification.NULL, properties);
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
