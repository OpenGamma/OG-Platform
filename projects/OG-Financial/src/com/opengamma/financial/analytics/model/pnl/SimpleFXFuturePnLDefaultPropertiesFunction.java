/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

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
public class SimpleFXFuturePnLDefaultPropertiesFunction extends DefaultPropertyFunction {
  private final String _payCurveName;
  private final String _receiveCurveName;
  private final String _samplingPeriod;
  private final String _scheduleCalculator;
  private final String _samplingCalculator;
  
  public SimpleFXFuturePnLDefaultPropertiesFunction(final String payCurveName, final String receiveCurveName, final String samplingPeriod, 
      final String scheduleCalculator, final String samplingCalculator) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(payCurveName, "pay curve name");
    ArgumentChecker.notNull(receiveCurveName, "receive curve name");
    ArgumentChecker.notNull(samplingPeriod, "sampling period name");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator name");
    ArgumentChecker.notNull(samplingCalculator, "time series sampling calculator name");
    _payCurveName = payCurveName;
    _receiveCurveName = receiveCurveName;
    _samplingPeriod = samplingPeriod;
    _scheduleCalculator = scheduleCalculator;
    _samplingCalculator = samplingCalculator;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.PAY_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.RECEIVE_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_PERIOD);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SCHEDULE_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_FUNCTION);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, 
      final String propertyName) {
    if (ValuePropertyNames.PAY_CURVE.equals(propertyName)) {
      return Collections.singleton(_payCurveName);
    }
    if (ValuePropertyNames.RECEIVE_CURVE.equals(propertyName)) {
      return Collections.singleton(_receiveCurveName);
    }
    if (ValuePropertyNames.SAMPLING_PERIOD.equals(propertyName)) {
      return Collections.singleton(_samplingPeriod);
    } 
    if (ValuePropertyNames.SCHEDULE_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_scheduleCalculator);
    }
    if (ValuePropertyNames.SAMPLING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_samplingCalculator);
    }
    return null;
  }
}
