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
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * For a curve name, pulls the {@link CurveSpecification} from the database.
 */
public class CurveSpecificationFunction extends AbstractFunction {
  /** The curve name */
  private final String _curveName;

  private CurveDefinitionSource _curveDefinitionSource;
  private CurveSpecificationBuilder _curveSpecificationBuilder;

  /**
   * @param curveName The curve name, not null
   */
  public CurveSpecificationFunction(final String curveName) {
    ArgumentChecker.notNull(curveName, "curve name");
    _curveName = curveName;
  }

  /**
   * Gets the curve name.
   *
   * @return The curve name
   */
  public String getCurveName() {
    return _curveName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveDefinitionSource = ConfigDBCurveDefinitionSource.init(context, this);
    _curveSpecificationBuilder = ConfigDBCurveSpecificationBuilder.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final AbstractCurveSpecification curveSpecification = CurveUtils.getSpecification(atInstant, _curveDefinitionSource, _curveSpecificationBuilder, atZDT.toLocalDate(), _curveName);
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, _curveName).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties);
    return new MyCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000), curveSpecification, spec);
  }

  /**
   * Function that creates a {@link CurveSpecification}.
   */
  protected class MyCompiledFunction extends AbstractInvokingCompiledFunction {
    /** The value specification of the result */
    private final ValueSpecification _spec;
    /** The result */
    private final Set<ComputedValue> _result;

    /**
     * @param earliestInvocation The earliest time at which this function is valid
     * @param latestInvocation The latest time at which this function is valid
     * @param specification The curve specification
     * @param spec The result specification
     */
    public MyCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final AbstractCurveSpecification specification, final ValueSpecification spec) {
      super(earliestInvocation, latestInvocation);
      _spec = spec;
      _result = Collections.singleton(new ComputedValue(spec, specification));
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      return _result;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.NULL;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
      return Collections.singleton(_spec);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
      return Collections.emptySet();
    }

  }
}
