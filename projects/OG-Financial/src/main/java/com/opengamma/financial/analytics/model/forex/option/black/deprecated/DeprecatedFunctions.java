/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black.deprecated;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class DeprecatedFunctions extends AbstractRepositoryConfigurationBean {

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXOptionBlackPresentValueFunctionDeprecated.class));
    functions.add(functionConfiguration(FXOptionBlackCurrencyExposureFunctionDeprecated.class));
    functions.add(functionConfiguration(FXOptionBlackVegaFunctionDeprecated.class));
    functions.add(functionConfiguration(FXOptionBlackVegaMatrixFunctionDeprecated.class));
    functions.add(functionConfiguration(FXOptionBlackVegaQuoteMatrixFunctionDeprecated.class));
    functions.add(functionConfiguration(FXOptionBlackPresentValueCurveSensitivityFunctionDeprecated.class));
    functions.add(functionConfiguration(FXOptionBlackYCNSFunctionDeprecated.class));
  }

}
