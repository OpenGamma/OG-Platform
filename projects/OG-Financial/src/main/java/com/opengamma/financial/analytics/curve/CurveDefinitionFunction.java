/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Pulls a {@link CurveDefinition} from the config database.
 */
public class CurveDefinitionFunction extends AbstractFunction {
  /** The curve name */
  private final String _curveName;

  private CurveDefinitionSource _curveDefinitionSource;

  /**
   * @param curveName The curve name, not null
   */
  public CurveDefinitionFunction(final String curveName) {
    ArgumentChecker.notNull(curveName, "curve name");
    _curveName = curveName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveDefinitionSource = ConfigDBCurveDefinitionSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final AbstractCurveDefinition curveDefinition = _curveDefinitionSource.getDefinition(_curveName);
    if (curveDefinition == null) {
      throw new OpenGammaRuntimeException("Could not get curve definition called " + _curveName);
    }
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, _curveName).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties);
    final Set<ComputedValue> result = Collections.singleton(new ComputedValue(spec, curveDefinition));
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        return result;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.NULL;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext myContext, final ComputationTarget target) {
        return Collections.singleton(spec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext myContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        return Collections.emptySet();
      }

    };
  }
}
