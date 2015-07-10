/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class SurfaceFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new SurfaceFunctions().getObjectCreating();
  }

  public static FunctionConfigurationSource defaults() {
    final Defaults factory = new Defaults();
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  public static FunctionConfigurationSource defaults(final String leftXExtrapolatorName, final String rightXExtrapolatorName, final String xInterpolatorName, final String leftYExtrapolatorName,
      final String rightYExtrapolatorName, final String yInterpolatorName) {
    final Defaults factory = new Defaults();
    factory.setLeftXExtrapolatorName(leftXExtrapolatorName);
    factory.setRightXExtrapolatorName(rightXExtrapolatorName);
    factory.setXInterpolatorName(xInterpolatorName);
    factory.setLeftYExtrapolatorName(leftYExtrapolatorName);
    factory.setRightYExtrapolatorName(rightYExtrapolatorName);
    factory.setYInterpolatorName(yInterpolatorName);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the functions contained in this package and sub-packages.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    private String _leftXExtrapolatorName = "FlatExtrapolator";
    private String _rightXExtrapolatorName = "FlatExtrapolator";
    private String _xInterpolatorName = "Linear";
    private String _leftYExtrapolatorName = "FlatExtrapolator";
    private String _rightYExtrapolatorName = "FlatExtrapolator";
    private String _yInterpolatorName = "Linear";

    public String getLeftXExtrapolatorName() {
      return _leftXExtrapolatorName;
    }

    public void setLeftXExtrapolatorName(final String leftXExtrapolatorName) {
      _leftXExtrapolatorName = leftXExtrapolatorName;
    }

    public String getRightXExtrapolatorName() {
      return _rightXExtrapolatorName;
    }

    public void setRightXExtrapolatorName(final String rightXExtrapolatorName) {
      _rightXExtrapolatorName = rightXExtrapolatorName;
    }

    public String getXInterpolatorName() {
      return _xInterpolatorName;
    }

    public void setXInterpolatorName(final String xInterpolatorName) {
      _xInterpolatorName = xInterpolatorName;
    }

    public String getLeftYExtrapolatorName() {
      return _leftYExtrapolatorName;
    }

    public void setLeftYExtrapolatorName(final String leftYExtrapolatorName) {
      _leftYExtrapolatorName = leftYExtrapolatorName;
    }

    public String getRightYExtrapolatorName() {
      return _rightYExtrapolatorName;
    }

    public void setRightYExtrapolatorName(final String rightYExtrapolatorName) {
      _rightYExtrapolatorName = rightYExtrapolatorName;
    }

    public String getYInterpolatorName() {
      return _yInterpolatorName;
    }

    public void setYInterpolatorName(final String yInterpolatorName) {
      _yInterpolatorName = yInterpolatorName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNull(getLeftXExtrapolatorName(), "leftXExtrapolatorName");
      ArgumentChecker.notNull(getRightXExtrapolatorName(), "rightXExtrapolatorName");
      ArgumentChecker.notNull(getXInterpolatorName(), "xInterpolatorName");
      ArgumentChecker.notNull(getLeftYExtrapolatorName(), "leftYExtrapolatorName");
      ArgumentChecker.notNull(getRightYExtrapolatorName(), "rightYExtrapolatorName");
      ArgumentChecker.notNull(getYInterpolatorName(), "yInterpolatorName");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(InterpolatedVolatilitySurfaceDefaultPropertiesFunction.class, getLeftXExtrapolatorName(), getRightXExtrapolatorName(), getXInterpolatorName(),
          getLeftYExtrapolatorName(), getRightYExtrapolatorName(), getYInterpolatorName()));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(CommodityOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(DefaultVolatilitySurfaceShiftFunction.class));
    functions.add(functionConfiguration(EquityFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(EquityOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(IRFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawBondFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawEquityFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawEquityOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawIRFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawFXVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawCommodityFutureOptionVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(RawSwaptionATMVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(SwaptionATMVolatilitySurfaceDataFunction.class));
    functions.add(functionConfiguration(VolatilitySurfaceShiftFunction.class));
    functions.add(functionConfiguration(RawSurfaceDataFunction.class));
  }

}
