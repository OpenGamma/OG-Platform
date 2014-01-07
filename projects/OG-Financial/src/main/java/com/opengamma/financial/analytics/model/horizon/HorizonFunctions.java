/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class HorizonFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new HorizonFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the default functions in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {
    /** The number of days to move forward */
    private int _daysForward;

    /**
     * Gets the number of days forward
     * @return The number of days to move forward
     */
    public int getDaysForward() {
      return _daysForward;
    }

    /**
     * Sets the number of days to move forward.
     * @param daysForward The number of days to move forward
     */
    public void setDaysForward(final int daysForward) {
      _daysForward = daysForward;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNegative(getDaysForward(), "days forward");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      final String[] args = new String[] {PriorityClass.ABOVE_NORMAL.name(), Integer.toString(getDaysForward())};
      functions.add(functionConfiguration(FXForwardThetaDefaults.class, args));
      functions.add(functionConfiguration(FXOptionBlackThetaDefaults.class, args));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXDigitalCallSpreadBlackConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(FXForwardConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(FXForwardConstantSpreadSingleThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackForwardSlideThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVolatilitySurfaceConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVolatilitySurfaceForwardSlideThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackYieldCurvesConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(FXOptionBlackYieldCurvesForwardSlideThetaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionConstantSpreadPositionThetaFunction.class));
    functions.add(functionConfiguration(SwapConstantSpreadThetaFunction.class));
    functions.add(functionConfiguration(SwaptionConstantSpreadThetaFunction.class));
  }

}
