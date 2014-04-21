/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;

/**
 *
 */
public abstract class BlackVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Object interpolatorObject = inputs.getValue(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR);
    if (interpolatorObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface interpolator");
    }
    final VolatilitySurfaceInterpolator surfaceInterpolator = (VolatilitySurfaceInterpolator) interpolatorObject;
    final SmileSurfaceDataBundle data = getData(inputs);
    final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface = surfaceInterpolator.getVolatilitySurface(data);
    final ValueProperties properties = getResultProperties(desiredValue);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, impliedVolatilitySurface));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String forwardCurveCalculationMethod = constraints.getStrictValue(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethod == null) {
      return null;
    }
    final String surfaceName = constraints.getStrictValue(SURFACE);
    if (surfaceName == null) {
      return null;
    }
    final String curveName = constraints.getStrictValue(CURVE);
    if (curveName == null) {
      return null;
    }
    final ValueRequirement forwardCurveRequirement = getForwardCurveRequirement(target, desiredValue);
    final ValueRequirement volatilitySurfaceRequirement = getVolatilityDataRequirement(target, surfaceName);
    final ValueRequirement interpolatorRequirement = getInterpolatorRequirement(target, desiredValue);
    return Sets.newHashSet(forwardCurveRequirement, volatilitySurfaceRequirement, interpolatorRequirement);
  }

  /**
   * Gets the data in a form that the analytics library can understand
   * 
   * @param inputs The inputs
   * @return The data
   */
  protected abstract SmileSurfaceDataBundle getData(final FunctionInputs inputs);

  /**
   * Gets the instrument type supported by the function
   * 
   * @return The instrument type
   */
  protected abstract String getInstrumentType();

  /**
   * Gets general result properties
   * 
   * @return The result properties
   */
  protected abstract ValueProperties getResultProperties();

  /**
   * Gets result properties with the constraints set
   * 
   * @param desiredValue The desired value
   * @return The result properties
   */
  protected abstract ValueProperties getResultProperties(final ValueRequirement desiredValue);

  private ValueRequirement getInterpolatorRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    return new ValueRequirement(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, ComputationTargetSpecification.NULL,
        BlackVolatilitySurfacePropertyUtils.addVolatilityInterpolatorProperties(ValueProperties.builder().get(), desiredValue).get());
  }

  /**
   * Gets the forward curve requirement
   * 
   * @param target The target
   * @param desiredValue The desired value
   * @return The forward curve requirement
   */
  protected abstract ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final ValueRequirement desiredValue);

  /**
   * Gets the volatility surface data requirement
   * 
   * @param target The target
   * @param surfaceName The surface name
   * @return The volatility surface data requirement
   */
  protected abstract ValueRequirement getVolatilityDataRequirement(final ComputationTarget target, final String surfaceName);

}
