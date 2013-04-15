/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward.deprecated;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class DeprecatedFunctions extends AbstractFunctionConfigurationBean {

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXForwardPresentValueFunctionDeprecated.class));
    functions.add(functionConfiguration(FXForwardCurrencyExposureFunctionDeprecated.class));
    functions.add(functionConfiguration(FXForwardYCNSFunctionDeprecated.class));
    functions.add(functionConfiguration(FXForwardPresentValueCurveSensitivityFunctionDeprecated.class));
  }

}
