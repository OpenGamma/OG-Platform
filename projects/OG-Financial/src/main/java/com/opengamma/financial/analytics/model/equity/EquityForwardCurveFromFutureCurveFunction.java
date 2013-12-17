/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.ExternalScheme;

/**
 *
 */
public class EquityForwardCurveFromFutureCurveFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Set<ExternalScheme> s_validSchemes = ImmutableSet.of(
      ExternalSchemes.BLOOMBERG_TICKER,
      ExternalSchemes.BLOOMBERG_TICKER_WEAK,
      ExternalSchemes.BLOOMBERG_BUID,
      ExternalSchemes.BLOOMBERG_BUID_WEAK,
      ExternalSchemes.ACTIVFEED_TICKER);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String interpolatorName = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    final String leftExtrapolatorName = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    final String rightExtrapolatorName = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);

    final Object objectFuturePriceData = inputs.getValue(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA);
    if (objectFuturePriceData == null) {
      throw new OpenGammaRuntimeException("Could not get futures curve " + curveName);
    }
    final NodalDoublesCurve futurePriceData = (NodalDoublesCurve) objectFuturePriceData;

    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);

    final ForwardCurve curve = new ForwardCurve(InterpolatedDoublesCurve.from(futurePriceData.getXData(), futurePriceData.getYData(), interpolator));

    final ValueProperties properties = createValueProperties()
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR, interpolatorName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR, leftExtrapolatorName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR, rightExtrapolatorName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_FUTURE_PRICE)
        .get();

    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(resultSpec, curve));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getValue() instanceof ExternalIdentifiable) {
      final ExternalId identifier = ((ExternalIdentifiable) target.getValue()).getExternalId();
      return s_validSchemes.contains(identifier.getScheme());
    }
    return false;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_FUTURE_PRICE)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ValueProperties constraints = desiredValue.getConstraints();

    // curve
    final String curveName = constraints.getStrictValue(ValuePropertyNames.CURVE);
    if (curveName == null) {
      return null;
    }

    // interpolator
    final String interpolatorName = constraints.getStrictValue(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    if (interpolatorName == null) {
      return null;
    }

    // interpolator left extrapolator
    final String leftExtrapolatorName = constraints.getStrictValue(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    if (leftExtrapolatorName == null) {
      return null;
    }

    // interpolator right extrapolator
    final String rightExtrapolatorName = constraints.getStrictValue(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    if (rightExtrapolatorName == null) {
      return null;
    }

    final ValueProperties futureCurveProperties = ValueProperties.builder()
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_FUTURE_PRICE)
        .with(ValuePropertyNames.CURVE, curveName)
        .get();

    requirements.add(new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), futureCurveProperties));
    return requirements;
  }

}
