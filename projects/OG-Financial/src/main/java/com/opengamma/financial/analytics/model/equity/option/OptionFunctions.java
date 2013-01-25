/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class OptionFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new OptionFunctions().getObjectCreating();
  }

  /**
   * Gets the default values for calculations
   * @return The repository with equity option defaults set
   */
  public static RepositoryConfigurationSource defaults() {
    final Defaults factory = new Defaults();
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * @param overhedge The overhedge to use for equity barrier options
   * @param callSpreadFullWidth The width of the call spread to use for barrier options
   * @return The repository with equity barrier option defaults set
   */
  public static RepositoryConfigurationSource defaults(final double overhedge, final double callSpreadFullWidth) {
    final Defaults factory = new Defaults();
    factory.setOverhedge(overhedge);
    factory.setCallSpreadFullWidth(callSpreadFullWidth);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractRepositoryConfigurationBean {

    private double _overhedge; /* = 0.0; */
    private double _callSpreadFullWidth = 0.001;

    public void setOverhedge(final double overhedge) {
      _overhedge = overhedge;
    }

    public double getOverhedge() {
      return _overhedge;
    }

    public void setCallSpreadFullWidth(final double callSpreadFullWidth) {
      _callSpreadFullWidth = callSpreadFullWidth;
    }

    public double getCallSpreadFullWidth() {
      return _callSpreadFullWidth;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(EquityVanillaBarrierOptionDefaults.class, Double.toString(getOverhedge()), Double.toString(getCallSpreadFullWidth())));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EquityOptionBAWGreeksFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBAWValueGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandGreeksFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBjerksundStenslandValueGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionPDEPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackFundingCurveSensitivitiesFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackImpliedVolFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackRhoFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotVannaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVegaMatrixFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVommaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackValueDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackValueGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionForwardValueFunction.class));
    functions.add(functionConfiguration(EquityOptionSpotIndexFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionForwardValueFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionFundingCurveSensitivitiesFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionPresentValueFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionRhoFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotDeltaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotGammaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotIndexFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionSpotVannaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionVegaMatrixFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionVegaFunction.class));
    functions.add(functionConfiguration(EquityVanillaBarrierOptionVommaFunction.class));
    functions.add(functionConfiguration(WeightedVegaFunction.class));
  }

}
