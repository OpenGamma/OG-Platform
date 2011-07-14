/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;

/**
 * 
 */
public class InterestRateFutureOptionPresentValueFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _surfaceName;
  
  public InterestRateFutureOptionPresentValueFunction(String surfaceName) { //TODO add the curve names 
    Validate.notNull(surfaceName, "surface name");
    _surfaceName = surfaceName;
  }
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueSpecification specification = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(),
        createValueProperties()
            .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
            .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
            .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
            .with(ValuePropertyNames.SURFACE, _surfaceName).get());
    final double presentValue = 0.009;
    return Collections.singleton(new ComputedValue(specification, presentValue));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof IRFutureOptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String forwardCurveName = YieldCurveFunction.getForwardCurveName(context, desiredValue);
    final String fundingCurveName = YieldCurveFunction.getFundingCurveName(context, desiredValue);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getSurfaceRequirement(context, target, desiredValue));
    if (forwardCurveName.equals(fundingCurveName)) {
      requirements.add(getCurveRequirement(target, forwardCurveName, null, null));
      return requirements;
    }
    requirements.add(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName));
    requirements.add(getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(),
        createValueProperties()
            .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
            .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
            .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
            .with(ValuePropertyNames.SURFACE, _surfaceName).get()));
  }

  private ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName,
      final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName,
        advisoryForward, advisoryFunding);
  }

  private ValueRequirement getSurfaceRequirement(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.SURFACE, _surfaceName)
                                                .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE").get(); //TODO shouldn't hard-code the string in here
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, FinancialSecurityUtils.getCurrency(target.getSecurity()), properties);
  }
}
