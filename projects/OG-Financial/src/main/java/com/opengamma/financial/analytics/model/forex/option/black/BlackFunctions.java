/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.forex.option.vannavolga.FXOptionVannaVolgaPresentValueFunction;

/**
 * Function repository configuration source for the functions contained in this package.
 * @deprecated This class adds deprecated functions
 */
@Deprecated
public class BlackFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new BlackFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the defaults functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

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
    functions.add(functionConfiguration(FXOptionBlackForwardDeltaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackSpotDeltaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackForwardGammaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackSpotGammaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackForwardVegaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackForwardDriftlessThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackForwardThetaTheoreticalFunction.class));
    functions.add(functionConfiguration(FXOptionBlackValueDeltaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackValueGammaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackValueGammaSpotFunction.class));
    functions.add(functionConfiguration(FXOptionBlackImpliedVolatilityFunction.class));
    functions.add(functionConfiguration(FXOptionBlackPV01Function.class));
    functions.add(functionConfiguration(FXOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVannaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackValuePhiFunction.class));
    functions.add(functionConfiguration(FXOptionBlackValueRhoFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVegaMatrixFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVegaQuoteMatrixFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVommaFunction.class));
    functions.add(functionConfiguration(FXOptionVannaVolgaPresentValueFunction.class));
  }

}
