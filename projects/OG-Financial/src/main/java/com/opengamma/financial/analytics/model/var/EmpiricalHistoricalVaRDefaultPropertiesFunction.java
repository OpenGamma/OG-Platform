/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class EmpiricalHistoricalVaRDefaultPropertiesFunction extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {ValueRequirementNames.HISTORICAL_VAR, ValueRequirementNames.CONDITIONAL_HISTORICAL_VAR};
  private final String _confidenceLevel;
  private final String _horizon;
  private final String _samplingPeriod;
  private final String _scheduleCalculator;
  private final String _samplingCalculator;

  public EmpiricalHistoricalVaRDefaultPropertiesFunction(final String samplingPeriod, final String scheduleCalculator, final String samplingCalculator,
      final String confidenceLevel, final String horizon) {
    super(ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION), true);
    ArgumentChecker.notNull(samplingPeriod, "sampling period name");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator name");
    ArgumentChecker.notNull(samplingCalculator, "time series sampling calculator name");
    ArgumentChecker.notNull(confidenceLevel, "confidence level name");
    ArgumentChecker.notNull(horizon, "horizon name");
    _samplingPeriod = samplingPeriod;
    _scheduleCalculator = scheduleCalculator;
    _samplingCalculator = samplingCalculator;
    _confidenceLevel = confidenceLevel;
    _horizon = horizon;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SAMPLING_PERIOD);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SCHEDULE_CALCULATOR);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SAMPLING_FUNCTION);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CONFIDENCE_LEVEL);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.HORIZON);
    }
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
    if (ValuePropertyNames.CONFIDENCE_LEVEL.equals(propertyName)) {
      return Collections.singleton(_confidenceLevel);
    }
    if (ValuePropertyNames.HORIZON.equals(propertyName)) {
      return Collections.singleton(_horizon);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.NORMAL_HISTORICAL_VAR;
  }

}
