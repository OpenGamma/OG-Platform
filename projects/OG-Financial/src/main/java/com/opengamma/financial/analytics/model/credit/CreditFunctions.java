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

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpType;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaBucketedCS01CDSFunction;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaBucketedGammaCDSFunction;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaCDSBucketedCS01Defaults;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaCDSCS01Defaults;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaCDSCurveDefaults;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaCDSDV01Defaults;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaCDSPriceTypeDefaults;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaCDSRR01Defaults;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaDV01CDSFunction;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaJumpToDefaultCDSFunction;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaParallelCS01CDSFunction;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaParallelGammaCDSFunction;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaPresentValueCDSFunction;
import com.opengamma.financial.analytics.model.credit.standard.StandardVanillaRR01CDSFunction;
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

  /**
   * Function repository configuration source for the default functions contained in this package.
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
        ArgumentChecker.notNullInjected(getCurveName(), "curveName");
        ArgumentChecker.notNullInjected(getCurveCalculationConfig(), "curveCalculationConfig");
        ArgumentChecker.notNullInjected(getCurveCalculationMethod(), "curveCalculationMethod");
      }

    }

    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
    private int _nIterations = 100;
    private double _tolerance = 1e-15;
    private double _rangeMultiplier = 0.5;
    private int _nIntegrationPoints = 30;
    private double _creditSpreadCurveBump = 1;
    private SpreadBumpType _spreadBumpCurveType = SpreadBumpType.ADDITIVE_PARALLEL;
    private SpreadBumpType _bucketedSpreadBumpCurveType = SpreadBumpType.ADDITIVE;
    private double _yieldCurveBump = 1;
    private SpreadBumpType _yieldBumpCurveType = SpreadBumpType.ADDITIVE_PARALLEL;
    private SpreadBumpType _bucketedYieldBumpCurveType = SpreadBumpType.ADDITIVE;
    private double _recoveryRateBump = 0.0001;
    private RecoveryRateBumpType _recoveryRateCurveType = RecoveryRateBumpType.ADDITIVE;
    private PriceType _priceType = PriceType.CLEAN;

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

    public void setCreditSpreadCurveBump(final double creditSpreadCurveBump) {
      _creditSpreadCurveBump = creditSpreadCurveBump;
    }

    public double getCreditSpreadCurveBump() {
      return _creditSpreadCurveBump;
    }

    public void setSpreadCurveBumpType(final SpreadBumpType spreadBumpCurveType) {
      _spreadBumpCurveType = spreadBumpCurveType;
    }

    public SpreadBumpType getSpreadCurveBumpType() {
      return _spreadBumpCurveType;
    }

    public void setBucketedSpreadCurveBumpType(final SpreadBumpType bucketedSpreadBumpCurveType) {
      _bucketedSpreadBumpCurveType = bucketedSpreadBumpCurveType;
    }

    public SpreadBumpType getBucketedSpreadCurveBumpType() {
      return _bucketedSpreadBumpCurveType;
    }

    public void setYieldCurveBump(final double yieldCurveBump) {
      _yieldCurveBump = yieldCurveBump;
    }

    public double getYieldCurveBump() {
      return _yieldCurveBump;
    }

    public void setYieldCurveBumpType(final SpreadBumpType yieldBumpCurveType) {
      _yieldBumpCurveType = yieldBumpCurveType;
    }

    public SpreadBumpType getYieldCurveBumpType() {
      return _yieldBumpCurveType;
    }

    public void setBucketedYieldCurveBumpType(final SpreadBumpType bucketedYieldBumpCurveType) {
      _bucketedYieldBumpCurveType = bucketedYieldBumpCurveType;
    }

    public SpreadBumpType getBucketedYieldCurveBumpType() {
      return _bucketedYieldBumpCurveType;
    }

    public void setRecoveryRateBump(final double recoveryRateBump) {
      _recoveryRateBump = recoveryRateBump;
    }

    public double getRecoveryRateBump() {
      return _recoveryRateBump;
    }

    public void setRecoveryRateBumpType(final RecoveryRateBumpType recoveryRateCurveType) {
      _recoveryRateCurveType = recoveryRateCurveType;
    }

    public RecoveryRateBumpType getRecoveryRateBumpType() {
      return _recoveryRateCurveType;
    }

    public void setPriceType(final PriceType priceType) {
      _priceType = priceType;
    }

    public PriceType getPriceType() {
      return _priceType;
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


    protected void addISDALegacyVanillaCDSDefaults(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(ISDALegacyVanillaCDSDefaults.class, Integer.toString(getNIntegrationPoints())));
    }

    protected void addStandardVanillaCDSCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 4];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> entry : getPerCurrencyInfo().entrySet()) {
        args[i++] = entry.getKey();
        final CurrencyInfo value = entry.getValue();
        args[i++] = value.getCurveName();
        args[i++] = value.getCurveCalculationConfig();
        args[i++] = value.getCurveCalculationMethod();
      }
      functions.add(functionConfiguration(StandardVanillaCDSCurveDefaults.class, args));
    }

    protected void addStandardVanillaDV01Defaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 3];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> entry : getPerCurrencyInfo().entrySet()) {
        args[i++] = entry.getKey();
        args[i++] = Double.toString(getYieldCurveBump());
        args[i++] = getYieldCurveBumpType().name();
      }
      functions.add(functionConfiguration(StandardVanillaCDSDV01Defaults.class, args));
    }

    protected void addStandardVanillaBucketedCS01Defaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 3];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> entry : getPerCurrencyInfo().entrySet()) {
        args[i++] = entry.getKey();
        args[i++] = Double.toString(getCreditSpreadCurveBump());
        args[i++] = getBucketedSpreadCurveBumpType().name();
      }
      functions.add(functionConfiguration(StandardVanillaCDSBucketedCS01Defaults.class, args));
    }

    protected void addStandardVanillaCS01Defaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 3];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> entry : getPerCurrencyInfo().entrySet()) {
        args[i++] = entry.getKey();
        args[i++] = Double.toString(getCreditSpreadCurveBump());
        args[i++] = getSpreadCurveBumpType().name();
      }
      functions.add(functionConfiguration(StandardVanillaCDSCS01Defaults.class, args));
    }

    protected void addStandardVanillaRR01Defaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 3];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> entry : getPerCurrencyInfo().entrySet()) {
        args[i++] = entry.getKey();
        args[i++] = Double.toString(getRecoveryRateBump());
        args[i++] = getRecoveryRateBumpType().name();
      }
      functions.add(functionConfiguration(StandardVanillaCDSRR01Defaults.class, args));
    }

    protected void addStandardVanillaPriceTypeDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 2];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> entry : getPerCurrencyInfo().entrySet()) {
        args[i++] = entry.getKey();
        args[i++] = getPriceType().name();
      }
      functions.add(functionConfiguration(StandardVanillaCDSPriceTypeDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addISDAYieldCurveDefaults(functions);
      }
      addISDALegacyVanillaCDSDefaults(functions);
      addStandardVanillaCDSCurveDefaults(functions);
      addStandardVanillaCS01Defaults(functions);
      addStandardVanillaBucketedCS01Defaults(functions);
      addStandardVanillaDV01Defaults(functions);
      addStandardVanillaRR01Defaults(functions);
      addStandardVanillaPriceTypeDefaults(functions);
    }
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ISDACreditSpreadCurveFunction.class));
    functions.add(functionConfiguration(ISDALegacyVanillaCDSCleanPriceFunction.class));
    functions.add(functionConfiguration(ISDALegacyVanillaCDSDirtyPriceFunction.class));
    functions.add(functionConfiguration(ISDAYieldCurveFunction.class));
    functions.add(functionConfiguration(BucketedSpreadCurveFunction.class));
    functions.add(functionConfiguration(ISDABucketedCS01VanillaCDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaParallelCS01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaBucketedCS01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaParallelGammaCDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaBucketedGammaCDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaDV01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaRR01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaJumpToDefaultCDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaPresentValueCDSFunction.class));
  }

}
