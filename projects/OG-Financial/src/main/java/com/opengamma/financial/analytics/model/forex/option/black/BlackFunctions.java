/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.forex.option.black.deprecated.DeprecatedFunctions;
import com.opengamma.financial.analytics.model.forex.option.vannavolga.FXOptionVannaVolgaPresentValueFunction;
import com.opengamma.financial.analytics.model.horizon.FXOptionBlackConstantSpreadThetaFunction;
import com.opengamma.financial.analytics.model.horizon.FXOptionBlackForwardSlideThetaFunction;
import com.opengamma.financial.analytics.model.horizon.FXOptionBlackVolatilitySurfaceConstantSpreadThetaFunction;
import com.opengamma.financial.analytics.model.horizon.FXOptionBlackVolatilitySurfaceForwardSlideThetaFunction;
import com.opengamma.financial.analytics.model.horizon.FXOptionBlackYieldCurvesConstantSpreadThetaFunction;
import com.opengamma.financial.analytics.model.horizon.FXOptionBlackYieldCurvesForwardSlideThetaFunction;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class BlackFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new BlackFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource deprecated() {
    return new DeprecatedFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource defaults() {
    final Defaults factory = new Defaults();
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  public static RepositoryConfigurationSource defaults(final double overhedge, final double relativeStrikeSmoothing) {
    final Defaults factory = new Defaults();
    factory.setOverhedge(overhedge);
    factory.setRelativeStrikeSmoothing(relativeStrikeSmoothing);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the defaults functions contained in this package.
   */
  public static class Defaults extends AbstractRepositoryConfigurationBean {

    private double _overhedge; /* = 0.0;*/
    private double _relativeStrikeSmoothing = 0.001;

    public void setOverhedge(final double overhedge) {
      _overhedge = overhedge;
    }

    public double getOverhedge() {
      return _overhedge;
    }

    public void setRelativeStrikeSmoothing(final double relativeStrikeSmoothing) {
      _relativeStrikeSmoothing = relativeStrikeSmoothing;
    }

    public double getRelativeStrikeSmoothing() {
      return _relativeStrikeSmoothing;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(FXOneLookBarrierOptionBlackDefaultPropertiesFunction.class, Double.toString(getOverhedge()), Double.toString(getRelativeStrikeSmoothing())));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXOneLookBarrierOptionBlackCurveSensitivityFunction.class));
    functions.add(functionConfiguration(FXOneLookBarrierOptionBlackGammaFunction.class));
    functions.add(functionConfiguration(FXOneLookBarrierOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(FXOneLookBarrierOptionBlackVannaFunction.class));
    functions.add(functionConfiguration(FXOneLookBarrierOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(FXOneLookBarrierOptionBlackVommaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackCurrencyExposureFunction.class));
    functions.add(functionConfiguration(FXOptionBlackForwardSlideThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackFXPresentValueFunction.class));
    functions.add(functionConfiguration(FXOptionBlackGammaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackGammaSpotFunction.class));
    functions.add(functionConfiguration(FXOptionBlackImpliedVolatilityFunction.class));
    functions.add(functionConfiguration(FXOptionBlackPresentValueCurveSensitivityFunction.class));
    functions.add(functionConfiguration(FXOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(FXOptionBlackPV01Function.class));
    functions.add(functionConfiguration(FXOptionBlackTermStructureCurrencyExposureFunction.class));
    functions.add(functionConfiguration(FXOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVannaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVegaMatrixFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVegaQuoteMatrixFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVolatilitySurfaceConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVolatilitySurfaceForwardSlideThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVommaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackYCNSFunction.class));
    functions.add(functionConfiguration(FXOptionBlackYieldCurvesConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackYieldCurvesForwardSlideThetaFunction.class));
    functions.add(functionConfiguration(FXOptionVannaVolgaPresentValueFunction.class));
  }

}
