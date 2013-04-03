/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.localvol;

import java.util.List;

import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class LocalVolFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   * 
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new LocalVolFunctions().getObjectCreating();
  }

  protected void addPDEFunction(final List<FunctionConfiguration> functions, final Class<? extends FunctionDefinition> function) {
    functions.add(functionConfiguration(function, BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL));
    functions.add(functionConfiguration(function, BlackVolatilitySurfacePropertyNamesAndValues.SABR));
    functions.add(functionConfiguration(function, BlackVolatilitySurfacePropertyNamesAndValues.SPLINE));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEDualDeltaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEDualGammaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEForwardDeltaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEForwardGammaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEForwardVegaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEForwardVannaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEForwardVommaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEGridDualDeltaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEGridDualGammaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEGridForwardDeltaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEGridForwardGammaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEGridForwardVegaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEGridForwardVannaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEGridForwardVommaFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEGridImpliedVolatilityFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEGridPipsPresentValueFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEImpliedVolatilityFunction.class);
    addPDEFunction(functions, FXOptionLocalVolatilityForwardPDEPipsPresentValueFunction.class);
  }

}
