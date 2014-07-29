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

import com.opengamma.analytics.financial.credit.bumpers.InterestRateBumpType;
import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpType;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.credit.isda.ISDADateCurveDefaults;
import com.opengamma.financial.analytics.model.credit.isda.ISDAHazardRateCurveDefaults;
import com.opengamma.financial.analytics.model.credit.isda.calibration.ISDACDSHazardRateCurveFunction;
import com.opengamma.financial.analytics.model.credit.isda.calibration.ISDACDXAsSingleNameHazardRateCurveFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaAccruedCDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaBucketedCS01CDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaBucketedGammaCS01CDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaBucketedIR01CDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaCDSBucketedCS01Defaults;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaCDSBucketedIR01Defaults;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaCDSCS01Defaults;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaCDSIR01Defaults;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaCDSNetMarketValueFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaCDSRR01Defaults;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaHedgeNotionalCDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaJumpToDefaultCDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaParSpreadCDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaParallelCS01CDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaParallelGammaCS01CDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaParallelIR01CDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaPresentValueCDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cds.StandardVanillaRR01CDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cdx.ISDACDXAsSingleNameAccruedCDSFunction;
import com.opengamma.financial.analytics.model.credit.isda.cdx.ISDACDXAsSingleNameBucketedCS01Function;
import com.opengamma.financial.analytics.model.credit.isda.cdx.ISDACDXAsSingleNameBucketedGammaCS01Function;
import com.opengamma.financial.analytics.model.credit.isda.cdx.ISDACDXAsSingleNameBucketedIR01Function;
import com.opengamma.financial.analytics.model.credit.isda.cdx.ISDACDXAsSingleNameParSpreadFunction;
import com.opengamma.financial.analytics.model.credit.isda.cdx.ISDACDXAsSingleNameParallelCS01Function;
import com.opengamma.financial.analytics.model.credit.isda.cdx.ISDACDXAsSingleNameParallelGammaCS01Function;
import com.opengamma.financial.analytics.model.credit.isda.cdx.ISDACDXAsSingleNameParallelIR01Function;
import com.opengamma.financial.analytics.model.credit.isda.cdx.ISDACDXAsSingleNamePresentValueFunction;
import com.opengamma.financial.analytics.model.credit.isda.cdx.ISDACDXAsSingleNameRR01Function;
import com.opengamma.financial.analytics.model.credit.isdanew.ISDACompliantCDSFunction;
import com.opengamma.financial.analytics.model.credit.isdanew.ISDACompliantCreditCurveFunction;
import com.opengamma.financial.analytics.model.credit.isdanew.ISDACompliantYieldCurveFunction;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class CreditFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new CreditFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

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
    private InterestRateBumpType _yieldBumpCurveType = InterestRateBumpType.ADDITIVE_PARALLEL;
    private InterestRateBumpType _bucketedYieldBumpCurveType = InterestRateBumpType.ADDITIVE;
    private double _recoveryRateBump = 0.01;
    private RecoveryRateBumpType _recoveryRateCurveType = RecoveryRateBumpType.ADDITIVE;
    private String _spreadCurveShiftType = CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT_TYPE;

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

    public void setYieldCurveBumpType(final InterestRateBumpType yieldBumpCurveType) {
      _yieldBumpCurveType = yieldBumpCurveType;
    }

    public InterestRateBumpType getYieldCurveBumpType() {
      return _yieldBumpCurveType;
    }

    public void setBucketedYieldCurveBumpType(final InterestRateBumpType bucketedYieldBumpCurveType) {
      _bucketedYieldBumpCurveType = bucketedYieldBumpCurveType;
    }

    public InterestRateBumpType getBucketedYieldCurveBumpType() {
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

    public void setSpreadCurveShiftType(final String spreadCurveShiftType) {
      _spreadCurveShiftType = spreadCurveShiftType;
    }

    public String getSpreadCurveShiftType() {
      return _spreadCurveShiftType;
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

    protected void addISDADateCurveDefaults(final List<FunctionConfiguration> functions) {
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
      functions.add(functionConfiguration(ISDADateCurveDefaults.class, args));
    }

    protected void addISDAHazardRateCurveDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 2];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> entry : getPerCurrencyInfo().entrySet()) {
        args[i++] = entry.getKey();
        final CurrencyInfo value = entry.getValue();
        args[i++] = value.getCurveCalculationMethod();
      }
      functions.add(functionConfiguration(ISDAHazardRateCurveDefaults.class, args));
    }

    protected void addStandardVanillaIR01Defaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 3];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> entry : getPerCurrencyInfo().entrySet()) {
        args[i++] = entry.getKey();
        args[i++] = Double.toString(getYieldCurveBump());
        args[i++] = getYieldCurveBumpType().name();
      }
      functions.add(functionConfiguration(StandardVanillaCDSIR01Defaults.class, args));
    }

    protected void addStandardVanillaBucketedIR01Defaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 3];
      int i = 0;
      args[i++] = PriorityClass.NORMAL.name();
      for (final Map.Entry<String, CurrencyInfo> entry : getPerCurrencyInfo().entrySet()) {
        args[i++] = entry.getKey();
        args[i++] = Double.toString(getYieldCurveBump());
        args[i++] = getBucketedYieldCurveBumpType().name();
      }
      functions.add(functionConfiguration(StandardVanillaCDSBucketedIR01Defaults.class, args));
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

    protected void addSpreadCurveShiftDefaults(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(SpreadCurveShiftDefaults.class, CreditInstrumentPropertyNamesAndValues.ADDITIVE_SPREAD_CURVE_SHIFT));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addISDAYieldCurveDefaults(functions);
      }
      addISDALegacyVanillaCDSDefaults(functions);
      addISDADateCurveDefaults(functions);
      addISDAHazardRateCurveDefaults(functions);
      addStandardVanillaCS01Defaults(functions);
      addStandardVanillaBucketedCS01Defaults(functions);
      addStandardVanillaIR01Defaults(functions);
      addStandardVanillaBucketedIR01Defaults(functions);
      addStandardVanillaRR01Defaults(functions);
      addSpreadCurveShiftDefaults(functions);
    }
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ISDACreditSpreadCurveFunction.class));
    functions.add(functionConfiguration(ISDACreditSpreadCurveShiftFunction.class));
    functions.add(functionConfiguration(ISDAYieldCurveFunction.class));
    functions.add(functionConfiguration(ISDACDSHazardRateCurveFunction.class));

    functions.add(functionConfiguration(ISDACDXAsSingleNameHazardRateCurveFunction.class));
    functions.add(functionConfiguration(BucketedSpreadCurveFunction.class));
    functions.add(functionConfiguration(ISDACompliantCreditCurveFunction.class));
    functions.add(functionConfiguration(StandardVanillaParallelCS01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaBucketedCS01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaParallelGammaCS01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaBucketedGammaCS01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaParallelIR01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaBucketedIR01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaRR01CDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaJumpToDefaultCDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaPresentValueCDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaAccruedCDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaParSpreadCDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaHedgeNotionalCDSFunction.class));
    functions.add(functionConfiguration(StandardVanillaCDSNetMarketValueFunction.class));

    functions.add(functionConfiguration(ISDACDXAsSingleNameParallelCS01Function.class));
    functions.add(functionConfiguration(ISDACDXAsSingleNameBucketedCS01Function.class));
    functions.add(functionConfiguration(ISDACDXAsSingleNameParallelGammaCS01Function.class));
    functions.add(functionConfiguration(ISDACDXAsSingleNameBucketedGammaCS01Function.class));
    functions.add(functionConfiguration(ISDACDXAsSingleNameParallelIR01Function.class));
    functions.add(functionConfiguration(ISDACDXAsSingleNameBucketedIR01Function.class));
    functions.add(functionConfiguration(ISDACDXAsSingleNameRR01Function.class));
    functions.add(functionConfiguration(ISDACDXAsSingleNamePresentValueFunction.class));
    functions.add(functionConfiguration(ISDACDXAsSingleNameAccruedCDSFunction.class));
    functions.add(functionConfiguration(ISDACDXAsSingleNameParSpreadFunction.class));

    functions.add(functionConfiguration(ISDACompliantCDSFunction.class));
    functions.add(functionConfiguration(ISDACompliantYieldCurveFunction.class));
    functions.add(functionConfiguration(ISDACompliantCreditCurveFunction.class));

    functions.add(functionConfiguration(JumpToDefaultPortfolioNodeFunction.class));
  }

}
