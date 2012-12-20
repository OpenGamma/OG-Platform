/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

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
public class CAPMFromRegressionDefaultPropertiesFunction extends DefaultPropertyFunction {
  private static final String[] VALUE_NAMES = new String[] {ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED,
                                                            ValueRequirementNames.CAPM_REGRESSION_ALPHA,
                                                            ValueRequirementNames.CAPM_REGRESSION_BETA,
                                                            ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR,
                                                            ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES,
                                                            ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES,
                                                            ValueRequirementNames.CAPM_REGRESSION_R_SQUARED,
                                                            ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS,
                                                            ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS,
                                                            ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA,
                                                            ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA,
                                                            ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS,
                                                            ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS
  };
  private final String _samplingPeriodName;
  private final String _scheduleCalculatorName;
  private final String _samplingFunctionName;
  private final String _returnCalculatorName;
  
  public CAPMFromRegressionDefaultPropertiesFunction(final String samplingPeriodName, final String scheduleCalculatorName, final String samplingFunctionName,
      final String returnCalculatorName, final ComputationTargetType target) {
    super(target, true);
    ArgumentChecker.notNull(samplingPeriodName, "sampling period name");
    ArgumentChecker.notNull(scheduleCalculatorName, "schedule calculator name");
    ArgumentChecker.notNull(samplingFunctionName, "sampling function name");
    ArgumentChecker.notNull(returnCalculatorName, "return calculator name");
    _samplingPeriodName = samplingPeriodName;
    _scheduleCalculatorName = scheduleCalculatorName;
    _samplingFunctionName = samplingFunctionName;
    _returnCalculatorName = returnCalculatorName;
  }
  
  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : VALUE_NAMES) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SAMPLING_PERIOD);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SCHEDULE_CALCULATOR);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SAMPLING_FUNCTION);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.RETURN_CALCULATOR);
    }
  }
  
  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.SAMPLING_PERIOD.equals(propertyName)) {
      return Collections.singleton(_samplingPeriodName);
    }
    if (ValuePropertyNames.SCHEDULE_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_scheduleCalculatorName);
    }
    if (ValuePropertyNames.SAMPLING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_samplingFunctionName);
    }
    if (ValuePropertyNames.RETURN_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_returnCalculatorName);
    }
    return null;
  }
}
