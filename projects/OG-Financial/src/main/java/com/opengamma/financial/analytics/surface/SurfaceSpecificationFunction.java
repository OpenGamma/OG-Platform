/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.surface;

import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.SURFACE_SPECIFICATION;

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
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Gets a {@link SurfaceSpecification} from the database.
 */
public class SurfaceSpecificationFunction extends AbstractFunction {
  /** The surface specification name */
  private final String _surfaceSpecificationName;
  /** The surface specification source */
  private ConfigDBSurfaceSpecificationSource _surfaceSpecificationSource;

  /**
   * @param surfaceSpecificationName The surface specification name, not null
   */
  public SurfaceSpecificationFunction(final String surfaceSpecificationName) {
    ArgumentChecker.notNull(surfaceSpecificationName, "surfaceSpecificationName");
    _surfaceSpecificationName = surfaceSpecificationName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _surfaceSpecificationSource = ConfigDBSurfaceSpecificationSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext outerContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final SurfaceSpecification specification = _surfaceSpecificationSource.getSpecification(_surfaceSpecificationName);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get specification called " + _surfaceSpecificationName);
    }
    final ValueProperties properties = createValueProperties()
        .with(SURFACE, _surfaceSpecificationName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getQuoteType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits())
        .get();
    final ValueSpecification spec = new ValueSpecification(SURFACE_SPECIFICATION, ComputationTargetSpecification.NULL, properties);
    final Set<ComputedValue> result = Collections.singleton(new ComputedValue(spec, specification));
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT).toInstant(), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000).toInstant()) {

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
