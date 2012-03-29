/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * 
 */
public class InterestRateFutureOptionPresentValueFunction extends InterestRateFutureOptionFunction {
  private static final PresentValueSABRCalculator CALCULATOR = PresentValueSABRCalculator.getInstance();

  @Override
  protected Set<ComputedValue> getResults(final InstrumentDerivative irFutureOption, final SABRInterestRateDataBundle data, final ComputationTarget target, final FunctionInputs inputs,
      final String forwardCurveName, final String fundingCurveName, final String surfaceName, final String curveCalculationMethod) {
    final double presentValue = CALCULATOR.visit(irFutureOption, data);
    return Collections.singleton(new ComputedValue(getSpecification(target, forwardCurveName, fundingCurveName, surfaceName, curveCalculationMethod), presentValue));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getSpecification(target));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> forwardCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    if (forwardCurves == null || forwardCurves.size() != 1) {
      return null;
    }
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationMethodNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethodNames == null || curveCalculationMethodNames.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurves.iterator().next();
    final String fundingCurveName = fundingCurves.iterator().next();
    final String curveCalculationMethodName = curveCalculationMethodNames.iterator().next();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getSurfaceRequirement(target, surfaceNames.iterator().next()));
    if (forwardCurveName.equals(fundingCurveName)) {
      requirements.add(getCurveRequirement(target, forwardCurveName, null, null, curveCalculationMethodName));
      return requirements;
    }
    requirements.add(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethodName));
    requirements.add(getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethodName));
    return requirements;
  }

  private ValueSpecification getSpecification(final ComputationTarget target) {
    return new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(),
        createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .with(ValuePropertyNames.SMILE_FITTING_METHOD, SURFACE_FITTING_NAME).get());
  }

  private ValueSpecification getSpecification(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName, final String surfaceName,
      final String curveCalculationMethod) {
    return new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(),
        createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.SMILE_FITTING_METHOD, SURFACE_FITTING_NAME).get());
  }
}
