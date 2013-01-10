/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.bond.BondFunctions.Defaults.CurrencyInfo;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class BondFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new BondFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource defaults(final Map<String, CurrencyInfo> perCurrencyInfo) {
    final Defaults factory = new Defaults();
    factory.setPerCurrencyInfo(perCurrencyInfo);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractRepositoryConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _riskFreeCurveName;
      private String _riskFreeCurveCalculationConfig;
      private String _creditCurveName;
      private String _creditCurveCalculationConfig;

      public CurrencyInfo() {
      }

      public CurrencyInfo(final String riskFreeCurveName, final String riskFreeCurveCalculationConfig, final String creditCurveName, final String creditCurveCalculationConfig) {
        setRiskFreeCurveName(riskFreeCurveName);
        setRiskFreeCurveCalculationConfig(riskFreeCurveCalculationConfig);
        setCreditCurveName(creditCurveName);
        setCreditCurveCalculationConfig(creditCurveCalculationConfig);
      }

      public void setRiskFreeCurveName(final String curveName) {
        _riskFreeCurveName = curveName;
      }

      public String getRiskFreeCurveName() {
        return _riskFreeCurveName;
      }

      public void setRiskFreeCurveCalculationConfig(final String curveCalculationConfig) {
        _riskFreeCurveCalculationConfig = curveCalculationConfig;
      }

      public String getRiskFreeCurveCalculationConfig() {
        return _riskFreeCurveCalculationConfig;
      }

      public void setCreditCurveName(final String curveName) {
        _creditCurveName = curveName;
      }

      public String getCreditCurveName() {
        return _creditCurveName;
      }

      public void setCreditCurveCalculationConfig(final String curveCalculationConfig) {
        _creditCurveCalculationConfig = curveCalculationConfig;
      }

      public String getCreditCurveCalculationConfig() {
        return _creditCurveCalculationConfig;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected("riskFreeCurveName", getRiskFreeCurveName());
        ArgumentChecker.notNullInjected("riskFreeCurveCalculationConfig", getRiskFreeCurveCalculationConfig());
        ArgumentChecker.notNullInjected("creditCurveName", getCreditCurveName());
        ArgumentChecker.notNullInjected("creditCurveCalculationConfig", getCreditCurveCalculationConfig());
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

    protected void addBondCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 5];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getRiskFreeCurveName();
        args[i++] = e.getValue().getRiskFreeCurveCalculationConfig();
        args[i++] = e.getValue().getCreditCurveName();
        args[i++] = e.getValue().getCreditCurveCalculationConfig();
      }
      functions.add(functionConfiguration(BondDefaultCurveNamesFunction.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addBondCurveDefaults(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondCouponPaymentDiaryFunction.class));
    functions.add(functionConfiguration(BondTenorFunction.class));
    functions.add(functionConfiguration(BondMarketCleanPriceFunction.class));
    functions.add(functionConfiguration(BondMarketDirtyPriceFunction.class));
    functions.add(functionConfiguration(BondMarketYieldFunction.class));
    functions.add(functionConfiguration(BondYieldFromCurvesFunction.class));
    functions.add(functionConfiguration(BondCleanPriceFromCurvesFunction.class));
    functions.add(functionConfiguration(BondDirtyPriceFromCurvesFunction.class));
    functions.add(functionConfiguration(BondMacaulayDurationFromCurvesFunction.class));
    functions.add(functionConfiguration(BondModifiedDurationFromCurvesFunction.class));
    functions.add(functionConfiguration(BondCleanPriceFromYieldFunction.class));
    functions.add(functionConfiguration(BondDirtyPriceFromYieldFunction.class));
    functions.add(functionConfiguration(BondMacaulayDurationFromYieldFunction.class));
    functions.add(functionConfiguration(BondModifiedDurationFromYieldFunction.class));
    functions.add(functionConfiguration(BondZSpreadFromCurveCleanPriceFunction.class));
    functions.add(functionConfiguration(BondZSpreadFromMarketCleanPriceFunction.class));
    functions.add(functionConfiguration(BondZSpreadPresentValueSensitivityFromCurveCleanPriceFunction.class));
    functions.add(functionConfiguration(BondZSpreadPresentValueSensitivityFromMarketCleanPriceFunction.class));
    functions.add(functionConfiguration(NelsonSiegelSvenssonBondCurveFunction.class));
  }

}
