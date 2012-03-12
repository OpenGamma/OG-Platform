/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class FXForwardCurveFromYieldCurveDefaultPropertiesFunction extends DefaultPropertyFunction {
  private final String _curveName;
  private final String _payCurveName;
  private final String _receiveCurveName;

  public FXForwardCurveFromYieldCurveDefaultPropertiesFunction(final String curveName, final String payCurveName, final String receiveCurveName) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(curveName, "curve name");
    ArgumentChecker.notNull(payCurveName, "pay curve name");
    ArgumentChecker.notNull(receiveCurveName, "receive curve name");
    _curveName = curveName;
    _payCurveName = payCurveName;
    _receiveCurveName = receiveCurveName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.PAY_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.RECEIVE_CURVE);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(_curveName);
    }
    if (ValuePropertyNames.PAY_CURVE.equals(propertyName)) {
      return Collections.singleton(_payCurveName);
    }
    if (ValuePropertyNames.RECEIVE_CURVE.equals(propertyName)) {
      return Collections.singleton(_receiveCurveName);
    }
    return null;
  }
}
