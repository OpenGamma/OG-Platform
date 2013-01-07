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
   */
  public static final RepositoryConfigurationSource DEFAULT = (new OptionFunctions()).getObjectCreating();

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EquityOptionBAWPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackFundingCurveSensitivitiesFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackImpliedVolFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackRhoFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotDeltaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotGammaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackSpotVannaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVegaMatrixFunction.class));
    functions.add(functionConfiguration(EquityOptionBlackVommaFunction.class));
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
  }

}
