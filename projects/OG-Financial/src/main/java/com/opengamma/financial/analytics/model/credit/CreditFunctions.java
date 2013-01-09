/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class CreditFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new CreditFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource defaults(final Map<String, Defaults.CurrencyInfo> perCurrencyInfo) {
    final Defaults factory = new Defaults();
    factory.setPerCurrencyInfo(perCurrencyInfo);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  public static RepositoryConfigurationSource defaults(final int nIterations, final double tolerance, final double rangeMultiplier, final int nIntegrationPoints,
      final Map<String, Defaults.CurrencyInfo> perCurrencyInfo) {
    final Defaults factory = new Defaults();
    factory.setNIterations(nIterations);
    factory.setTolerance(tolerance);
    factory.setRangeMultiplier(rangeMultiplier);
    factory.setNIntegrationPoints(nIntegrationPoints);
    factory.setPerCurrencyInfo(perCurrencyInfo);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the functions contained in this package.
   */
  public static class Defaults extends AbstractRepositoryConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private int _offset; /* = 0; */
      private String _curveName;
      private String _curveCalculationConfig;
      private String _curveCalculationMethod;

      public CurrencyInfo() {
      }

      public CurrencyInfo(final String curveName, final String curveCalculationConfig, final String curveCalculationMethod) {
        setCurveName(curveName);
        setCurveCalculationConfig(curveCalculationConfig);
        setCurveCalculationMethod(curveCalculationMethod);
      }

      public CurrencyInfo(final int offset, final String curveName, final String curveCalculationConfig, final String curveCalculationMethod) {
        this(curveName, curveCalculationConfig, curveCalculationMethod);
        setOffset(offset);
      }

      public void setOffset(final int offset) {
        _offset = offset;
      }

      public int getOffset() {
        return _offset;
      }

      public void setCurveName(final String curveName) {
        _curveName = curveName;
      }

      public String getCurveName() {
        return _curveName;
      }

      public void setCurveCalculationConfig(final String curveCalculationConfig) {
        _curveCalculationConfig = curveCalculationConfig;
      }

      public String getCurveCalculationConfig() {
        return _curveCalculationConfig;
      }

      public void setCurveCalculationMethod(final String curveCalculationMethod) {
        _curveCalculationMethod = curveCalculationMethod;
      }

      public String getCurveCalculationMethod() {
        return _curveCalculationMethod;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected("curveName", getCurveName());
        ArgumentChecker.notNullInjected("curveCalculationConfig", getCurveCalculationConfig());
        ArgumentChecker.notNullInjected("curveCalculationMethod", getCurveCalculationMethod());
      }

    }

    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<String, CurrencyInfo>();
    private int _nIterations = 100;
    private double _tolerance = 1e-15;
    private double _rangeMultiplier = 0.5;
    private int _nIntegrationPoints = 30;

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

    public void setNIterations(final int nIterations) {
      _nIterations = nIterations;
    }

    public int getNIterations() {
      return _nIterations;
    }

    public void setTolerance(final double tolerance) {
      _tolerance = tolerance;
    }

    public double getTolerance() {
      return _tolerance;
    }

    public void setRangeMultiplier(final double rangeMultiplier) {
      _rangeMultiplier = rangeMultiplier;
    }

    public double getRangeMultiplier() {
      return _rangeMultiplier;
    }

    public void setNIntegrationPoints(final int nIntegrationPoints) {
      _nIntegrationPoints = nIntegrationPoints;
    }

    public int getNIntegrationPoints() {
      return _nIntegrationPoints;
    }

    protected void addISDAYieldCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 2];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = Integer.toString(e.getValue().getOffset());
      }
      functions.add(functionConfiguration(ISDAYieldCurveDefaults.class, args));
    }

    protected void addISDALegacyCDSHazardCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[4 + getPerCurrencyInfo().size() * 4];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      args[i++] = Integer.toString(getNIterations());
      args[i++] = Double.toString(getTolerance());
      args[i++] = Double.toString(getRangeMultiplier());
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveName();
        args[i++] = e.getValue().getCurveCalculationConfig();
        args[i++] = e.getValue().getCurveCalculationMethod();
      }
      functions.add(functionConfiguration(ISDALegacyCDSHazardCurveDefaults.class, args));
    }

    protected void addISDALegacyVanillaCDSDefaults(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(ISDALegacyVanillaCDSDefaults.class, PriorityClass.NORMAL.name(), Integer.toString(getNIntegrationPoints())));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      addISDAYieldCurveDefaults(functions);
      addISDALegacyCDSHazardCurveDefaults(functions);
      addISDALegacyVanillaCDSDefaults(functions);
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ISDALegacyCDSHazardCurveFunction.class));
    functions.add(functionConfiguration(ISDALegacyVanillaCDSCleanPriceFunction.class));
    functions.add(functionConfiguration(ISDALegacyVanillaCDSDirtyPriceFunction.class));
    functions.add(functionConfiguration(ISDAYieldCurveFunction.class));
  }

}
