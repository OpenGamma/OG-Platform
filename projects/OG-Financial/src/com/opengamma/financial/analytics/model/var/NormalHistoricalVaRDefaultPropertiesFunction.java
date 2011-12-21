/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

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
public abstract class NormalHistoricalVaRDefaultPropertiesFunction extends DefaultPropertyFunction {
  private final String _meanCalculator;
  private final String _stdDevCalculator;
  private final String _confidenceLevel;
  private final String _horizon;
  private final String _samplingPeriod;
  private final String _scheduleCalculator;
  private final String _samplingCalculator;
  
  public NormalHistoricalVaRDefaultPropertiesFunction(final String samplingPeriod, final String scheduleCalculator, final String samplingCalculator,
      final String meanCalculator, final String stdDevCalculator, final String confidenceLevel, final String horizon, final ComputationTargetType target) {
    super(target, true);
    ArgumentChecker.notNull(samplingPeriod, "sampling period name");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator name");
    ArgumentChecker.notNull(samplingCalculator, "time series sampling calculator name");
    ArgumentChecker.notNull(meanCalculator, "mean calculator name");
    ArgumentChecker.notNull(stdDevCalculator, "standard deviation calculator name");
    ArgumentChecker.notNull(confidenceLevel, "confidence level name");
    ArgumentChecker.notNull(horizon, "horizon name");
    _samplingPeriod = samplingPeriod;
    _scheduleCalculator = scheduleCalculator;
    _samplingCalculator = samplingCalculator;    
    _meanCalculator = meanCalculator;
    _stdDevCalculator = stdDevCalculator;
    _confidenceLevel = confidenceLevel;
    _horizon = horizon;
  }
  
  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.HISTORICAL_VAR, ValuePropertyNames.SAMPLING_PERIOD);
    defaults.addValuePropertyName(ValueRequirementNames.HISTORICAL_VAR, ValuePropertyNames.SCHEDULE_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.HISTORICAL_VAR, ValuePropertyNames.SAMPLING_FUNCTION);
    defaults.addValuePropertyName(ValueRequirementNames.HISTORICAL_VAR, ValuePropertyNames.MEAN_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.HISTORICAL_VAR, ValuePropertyNames.STD_DEV_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.HISTORICAL_VAR, ValuePropertyNames.CONFIDENCE_LEVEL);
    defaults.addValuePropertyName(ValueRequirementNames.HISTORICAL_VAR, ValuePropertyNames.HORIZON);    
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, 
      final String propertyName) {
    if (ValuePropertyNames.SAMPLING_PERIOD.equals(propertyName)) {
      return Collections.singleton(_samplingPeriod);
    } 
    if (ValuePropertyNames.SCHEDULE_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_scheduleCalculator);
    }
    if (ValuePropertyNames.SAMPLING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_samplingCalculator);
    }
    if (ValuePropertyNames.MEAN_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_meanCalculator);
    }
    if (ValuePropertyNames.STD_DEV_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_stdDevCalculator);
    }
    if (ValuePropertyNames.CONFIDENCE_LEVEL.equals(propertyName)) {
      return Collections.singleton(_confidenceLevel);
    }
    if (ValuePropertyNames.HORIZON.equals(propertyName)) {
      return Collections.singleton(_horizon);
    }
    return null;
  }

}
