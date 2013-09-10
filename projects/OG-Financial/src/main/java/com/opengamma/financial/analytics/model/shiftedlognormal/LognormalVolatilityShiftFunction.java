/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.shiftedlognormal;

import static com.opengamma.engine.value.ValueRequirementNames.LOGNORMAL_SURFACE_SHIFTS;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function that supplies shift parameters for lognormal volatility surfaces.
 */
public class LognormalVolatilityShiftFunction extends AbstractFunction.NonCompiledInvoker {
  /** Hard-coded constant shifts curve */
  private static final DoublesCurve SHIFTS = ConstantDoublesCurve.from(0.0001);
  /** The curve name property */
  public static final String SHIFT_CURVE = "ShiftCurve";
  /** The default value */
  public static final String TEST = "Test";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueProperties properties = createValueProperties()
        .with(SHIFT_CURVE, TEST)
        .get();
    final ValueSpecification spec = new ValueSpecification(LOGNORMAL_SURFACE_SHIFTS, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, SHIFTS));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(SHIFT_CURVE, TEST)
        .get();
    return Collections.singleton(new ValueSpecification(LOGNORMAL_SURFACE_SHIFTS, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

}
