/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome.deprecated;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class DeprecatedFunctions extends AbstractFunctionConfigurationBean {

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(InterestRateInstrumentParRateFunctionDeprecated.class));
    functions.add(functionConfiguration(InterestRateInstrumentPresentValueFunctionDeprecated.class));
    functions.add(functionConfiguration(InterestRateInstrumentParRateCurveSensitivityFunctionDeprecated.class));
    functions.add(functionConfiguration(InterestRateInstrumentParRateParallelCurveSensitivityFunctionDeprecated.class));
    functions.add(functionConfiguration(InterestRateInstrumentPV01FunctionDeprecated.class));
    functions.add(functionConfiguration(InterestRateInstrumentYieldCurveNodeSensitivitiesFunctionDeprecated.class));
  }

}
