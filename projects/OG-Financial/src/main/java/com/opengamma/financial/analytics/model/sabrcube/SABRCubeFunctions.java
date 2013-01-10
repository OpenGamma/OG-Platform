/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.List;
import java.util.Map;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class SABRCubeFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new SABRCubeFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource deprecated() {
    return new Deprecated().getObjectCreating();
  }

  /**
   * Function repository configuration source for the deprecated functions contained in this package.
   */
  public static class Deprecated extends AbstractRepositoryConfigurationBean {

    @Override
    public void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVCurveSensitivityFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPresentValueFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunctionDeprecated.Alpha.class));
      functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunctionDeprecated.Nu.class));
      functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunctionDeprecated.Rho.class));
      functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationVegaFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRCMSSpreadNoExtrapolationYCNSFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRNoExtrapolationPVCurveSensitivityFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRNoExtrapolationPresentValueFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated.Alpha.class));
      functions.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated.Nu.class));
      functions.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated.Rho.class));
      functions.add(functionConfiguration(SABRNoExtrapolationVegaFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRNoExtrapolationYCNSFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRRightExtrapolationPVCurveSensitivityFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRRightExtrapolationPresentValueFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunctionDeprecated.Alpha.class));
      functions.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunctionDeprecated.Nu.class));
      functions.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunctionDeprecated.Rho.class));
      functions.add(functionConfiguration(SABRRightExtrapolationVegaFunctionDeprecated.class));
      functions.add(functionConfiguration(SABRRightExtrapolationYCNSFunctionDeprecated.class));
    }

  }

  public static RepositoryConfigurationSource defaults(final Map<String, CurrencyInfo> perCurrencyInfo) {
    final DefaultPropertiesFunctions factory = new DefaultPropertiesFunctions();
    factory.setPerCurrencyInfo(perCurrencyInfo);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  public static RepositoryConfigurationSource defaults(final Map<String, CurrencyInfo> perCurrencyInfo, final String fittingMethod, final String xInterpolator, final String xLeftExtrapolator,
      final String xRightExtrapolator, final String yInterpolator, final String yLeftExtrapolator, final String yRightExtrapolator, final double cutOff, final double mu) {
    final DefaultPropertiesFunctions factory = new DefaultPropertiesFunctions();
    factory.setPerCurrencyInfo(perCurrencyInfo);
    factory.setXInterpolator(xInterpolator);
    factory.setXLeftExtrapolator(xLeftExtrapolator);
    factory.setXRightExtrapolator(xRightExtrapolator);
    factory.setYInterpolator(yInterpolator);
    factory.setYLeftExtrapolator(yLeftExtrapolator);
    factory.setYRightExtrapolator(yRightExtrapolator);
    factory.setCutOff(cutOff);
    factory.setMu(mu);
    factory.afterPropertiesSet();
    return factory.getObject();
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
