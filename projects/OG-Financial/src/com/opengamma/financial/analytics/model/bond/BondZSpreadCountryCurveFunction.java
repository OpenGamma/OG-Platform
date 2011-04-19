/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;

/**
 * 
 */
public class BondZSpreadCountryCurveFunction extends BondZSpreadFunction {

  @Override
  public String getShortName() {
    return "BondZSpreadCountryCurveFunction";
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String curveName = YieldCurveFunction.getCurveName(context, desiredValue);
    return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, BondFunctionUtils.getCountryID(target), 
      ValueProperties.with(ValuePropertyNames.CURVE, curveName).get()), 
      new ValueRequirement(ValueRequirementNames.DIRTY_PRICE, ComputationTargetType.SECURITY, target.getSecurity().getUniqueId()));
  }

}
