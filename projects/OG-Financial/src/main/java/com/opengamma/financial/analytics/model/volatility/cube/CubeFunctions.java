/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.volatility.cube.defaults.DefaultsFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class CubeFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new CubeFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource defaults() {
    final DefaultsFunctions factory = new DefaultsFunctions();
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  public static RepositoryConfigurationSource defaults(final double alphaStartValue, final double betaStartValue, final double rhoStartValue, final double nuStartValue, final boolean useFixedAlpha,
      final boolean useFixedBeta, final boolean useFixedRho, final boolean useFixedNu, final double eps, final String xInterpolator, final String xExtrapolator, final String yInterpolator,
      final String yExtrapolator, final String forwardCurveCalculationMethod, final String forwardCurveInterpolator, final String forwardCurveLeftExtrapolator,
      final String forwardCurveRightExtrapolator) {
    final DefaultsFunctions factory = new DefaultsFunctions();
    factory.setAlphaStartValue(alphaStartValue);
    factory.setBetaStartValue(betaStartValue);
    factory.setRhoStartValue(rhoStartValue);
    factory.setNuStartValue(nuStartValue);
    factory.setUseFixedAlpha(useFixedAlpha);
    factory.setUseFixedBeta(useFixedBeta);
    factory.setUseFixedRho(useFixedRho);
    factory.setUseFixedNu(useFixedNu);
    factory.setEps(eps);
    factory.setXInterpolator(xInterpolator);
    factory.setXExtrapolator(xExtrapolator);
    factory.setYInterpolator(yInterpolator);
    factory.setYExtrapolator(yExtrapolator);
    factory.setForwardCurveCalculationMethod(forwardCurveCalculationMethod);
    factory.setForwardCurveInterpolator(forwardCurveInterpolator);
    factory.setForwardCurveLeftExtrapolator(forwardCurveLeftExtrapolator);
    factory.setForwardCurveRightExtrapolator(forwardCurveRightExtrapolator);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(RawSwaptionVolatilityCubeDataFunction.class));
    functions.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingFunction.class));
    functions.add(functionConfiguration(SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.class));
  }

}
