/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 * @deprecated The functions that this configuration adds are deprecated
 */
@Deprecated
public class SABRCubeFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new SABRCubeFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVCurveSensitivityFunction.class));
    functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPresentValueFunction.class));
    functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction.Alpha.class));
    functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction.Nu.class));
    functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction.Rho.class));
    functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationVegaFunction.class));
    functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationYCNSFunction.class));
    functions.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVCurveSensitivityFunction.class));
    functions.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPresentValueFunction.class));
    functions.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRSensitivityFunction.Alpha.class));
    functions.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRSensitivityFunction.Nu.class));
    functions.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRSensitivityFunction.Rho.class));
    functions.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRNodeSensitivityFunction.Alpha.class));
    functions.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRNodeSensitivityFunction.Nu.class));
    functions.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRNodeSensitivityFunction.Rho.class));
    functions.add(functionConfiguration(SABRCMSSpreadRightExtrapolationVegaFunction.class));
    functions.add(functionConfiguration(SABRCMSSpreadRightExtrapolationYCNSFunction.class));
    functions.add(functionConfiguration(SABRNoExtrapolationPVCurveSensitivityFunction.class));
    functions.add(functionConfiguration(SABRNoExtrapolationPresentValueFunction.class));
    functions.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunction.Alpha.class));
    functions.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunction.Nu.class));
    functions.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunction.Rho.class));
    functions.add(functionConfiguration(SABRNoExtrapolationVegaFunction.class));
    functions.add(functionConfiguration(SABRNoExtrapolationYCNSFunction.class));
    functions.add(functionConfiguration(SABRRightExtrapolationPVCurveSensitivityFunction.class));
    functions.add(functionConfiguration(SABRRightExtrapolationPresentValueFunction.class));
    functions.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunction.Alpha.class));
    functions.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunction.Nu.class));
    functions.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunction.Rho.class));
    functions.add(functionConfiguration(SABRRightExtrapolationPVSABRNodeSensitivityFunction.Alpha.class));
    functions.add(functionConfiguration(SABRRightExtrapolationPVSABRNodeSensitivityFunction.Nu.class));
    functions.add(functionConfiguration(SABRRightExtrapolationPVSABRNodeSensitivityFunction.Rho.class));
    functions.add(functionConfiguration(SABRRightExtrapolationVegaFunction.class));
    functions.add(functionConfiguration(SABRRightExtrapolationYCNSFunction.class));
  }
}
