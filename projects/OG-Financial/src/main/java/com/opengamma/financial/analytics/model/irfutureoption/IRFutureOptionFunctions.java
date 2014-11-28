/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.horizon.InterestRateFutureOptionBlackThetaDefaults;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class IRFutureOptionFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new IRFutureOptionFunctions().getObjectCreating();
  }

  public static FunctionConfigurationSource deprecated() {
    return new Deprecated().getObjectCreating();
  }

  /**
   * Function repository configuration source for the deprecated functions contained in this package.
   */
  public static class Deprecated extends AbstractFunctionConfigurationBean {

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      // TODO: add functions
    }

  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveName;
      private String _curveConfiguration;
      private String _surfaceName;
      private String _smileFittingMethod = SmileFittingPropertyNamesAndValues.NON_LINEAR_LEAST_SQUARES;

      public String getCurveName() {
        return _curveName;
      }

      public void setCurveName(final String curveName) {
        _curveName = curveName;
      }

      public String getCurveConfiguration() {
        return _curveConfiguration;
      }

      public void setCurveConfiguration(final String curveConfiguration) {
        _curveConfiguration = curveConfiguration;
      }

      public String getSurfaceName() {
        return _surfaceName;
      }

      public void setSurfaceName(final String surfaceName) {
        _surfaceName = surfaceName;
      }

      public String getSmileFittingMethod() {
        return _smileFittingMethod;
      }

      public void setSmileFittingMethod(final String smileFittingMethod) {
        _smileFittingMethod = smileFittingMethod;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveConfiguration(), "curveConfiguration");
        ArgumentChecker.notNullInjected(getSurfaceName(), "surfaceName");
        ArgumentChecker.notNullInjected(getSmileFittingMethod(), "smileFittingMethod");
      }
    }

    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<String, CurrencyInfo>();

    public void setPerCurrencyInfo(final Map<String, CurrencyInfo> perCurrencyInfo) {
      _perCurrencyInfo.clear();
      _perCurrencyInfo.putAll(perCurrencyInfo);
    }

    public Map<String, CurrencyInfo> getPerCurrencyInfo() {
      return _perCurrencyInfo;
    }

    public void setCurrencyInfo(final String currency, final CurrencyInfo info) {
      _perCurrencyInfo.put(currency, info);
    }

    public CurrencyInfo getCurrencyInfo(final String currency) {
      return _perCurrencyInfo.get(currency);
    }

    protected void addIRFutureOptionBlackDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 3];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
        args[i++] = e.getValue().getSurfaceName();
      }
      functions.add(functionConfiguration(InterestRateFutureOptionBlackDefaults.class, args));
    }

    // TODO Needs improvement: defaultNumberOfDaysForward should not be hardcoded here
    protected void addIRFutureOptionBlackThetaDefaults(final List<FunctionConfiguration> functions) {

      final int defaultNumberOfDaysForward = 1;       // TODO !!! Hardcode

      final String[] daysPlusBlackArgs = new String[getPerCurrencyInfo().size() * 3 + 1];
      int i = 0;
      daysPlusBlackArgs[i++] = Integer.toString(defaultNumberOfDaysForward);
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        daysPlusBlackArgs[i++] = e.getKey();
        daysPlusBlackArgs[i++] = e.getValue().getCurveConfiguration();
        daysPlusBlackArgs[i++] = e.getValue().getSurfaceName();
      }
      functions.add(functionConfiguration(InterestRateFutureOptionBlackThetaDefaults.class, daysPlusBlackArgs));
    }

    // TODO Default is hardcoded here. Where should this be done?
    protected void addIRFutureOptionBlackPositionDeltaGammaScaleDefaults(final List<FunctionConfiguration> functions) {

      final double defaultScaleFactor = 0.0001; // scale to basis point moves in underlying

      final String[] scalePlusBlackArgs = new String[getPerCurrencyInfo().size() * 3 + 1];
      int i = 0;
      scalePlusBlackArgs[i++] = Double.toString(defaultScaleFactor);
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        scalePlusBlackArgs[i++] = e.getKey();
        scalePlusBlackArgs[i++] = e.getValue().getCurveConfiguration();
        scalePlusBlackArgs[i++] = e.getValue().getSurfaceName();
      }
      functions.add(functionConfiguration(InterestRateFutureOptionBlackPositionDeltaGammaScaleDefaults.class, scalePlusBlackArgs));
    }

    protected void addIRFutureOptionBlackCurveSpecificDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 4];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveName();
        args[i++] = e.getValue().getCurveConfiguration();
        args[i++] = e.getValue().getSurfaceName();
      }
      functions.add(functionConfiguration(InterestRateFutureOptionBlackCurveSpecificDefaults.class, args));
    }

    protected void addIRFutureOptionSABRDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 4];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
        args[i++] = e.getValue().getSurfaceName();
        args[i++] = e.getValue().getSmileFittingMethod();
      }
      functions.add(functionConfiguration(IRFutureOptionSABRDefaults.class, args));
    }

    protected void addIRFutureOptionHestonDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 3];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
        args[i++] = e.getValue().getSurfaceName();
      }
      functions.add(functionConfiguration(InterestRateFutureOptionHestonDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addIRFutureOptionBlackDefaults(functions);
        addIRFutureOptionBlackCurveSpecificDefaults(functions);
        addIRFutureOptionBlackThetaDefaults(functions);
        addIRFutureOptionBlackPositionDeltaGammaScaleDefaults(functions);
        addIRFutureOptionSABRDefaults(functions);
        addIRFutureOptionHestonDefaults(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(InterestRateFutureOptionMarketUnderlyingPriceFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackVolatilitySensitivityFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackImpliedVolatilityFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackGammaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackPriceFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackDeltaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackPositionGammaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackPositionWeightedVegaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackForwardFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackValueDeltaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackValueGammaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackValueGammaPFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackValueThetaFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionHestonPresentValueFunction.class));
    functions.add(functionConfiguration(IRFutureOptionSABRPresentValueFunction.class));
    functions.add(functionConfiguration(IRFutureOptionSABRSensitivitiesFunction.class));
    functions.add(functionConfiguration(IRFutureOptionSABRYCNSFunction.class));
  }

}
