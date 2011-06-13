/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.swap.FixedFloatSwapFunction;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.tuple.Pair;

/**
 * Hack to ensure that all outputs can be satisfied. Designed as a low-priority fall-back function which simply returns
 * an empty vector. It is not possible to return null, and if an output cannot be satisfied by any function at all then
 * aggregates of that output can never be satisfied.
 */
public class NotApplicableYieldCurveNodeSensitivityFixedFloatSwapFunction extends FixedFloatSwapFunction {
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final SwapSecurity security = (SwapSecurity) target.getSecurity();
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    final String forwardCurveName = curveNames.getFirst();
    final String fundingCurveName = curveNames.getSecond();
    
    String curveCurrency = getDesiredCurveCurrency(desiredValues);
    ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, security), createValueProperties()
        .with(ValuePropertyNames.CURVE_CURRENCY, curveCurrency)
        .with(ValuePropertyNames.CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get());
    return Collections.singleton(new ComputedValue(specification, new DoubleLabelledMatrix1D(new Double[0], new double[0])));    
  }

  @Override
  protected Set<ComputedValue> getComputedValues(FunctionInputs inputs, Security security, Swap<?, ?> swap, YieldCurveBundle bundle, String forwardCurveName, String fundingCurveName) {
    return null;
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(context, desiredValue);
    if (curveNames.getFirst().equals(curveNames.getSecond())) {
      return Sets.newHashSet(getCurveRequirement(target, curveNames.getFirst(), null, null), getJacobianRequirement(target, curveNames.getFirst(), curveNames.getSecond()));
    } else {
      return Sets.newHashSet(
          getCurveRequirement(target, curveNames.getFirst(), curveNames.getFirst(), curveNames.getSecond()),
          getCurveRequirement(target, curveNames.getSecond(), curveNames.getFirst(), curveNames.getSecond()),
          getJacobianRequirement(target, curveNames.getFirst(), curveNames.getSecond()));
    }
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), createValueProperties()
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).get()));
  }
  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), createValueProperties()
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .with(ValuePropertyNames.CURVE, curveNames.getFirst())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst())
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond()).get()));
  }
  
  private String getDesiredCurveCurrency(Collection<ValueRequirement> desiredValues) {
    String curveCurrency = null;
    for (ValueRequirement desiredValue : desiredValues) {
      String desiredCurveCurrency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
      if (desiredCurveCurrency != null) {
        curveCurrency = desiredCurveCurrency;
        break;
      }
    }
    return curveCurrency;
  }

}
