/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.CurrencyPairsDefaults;
import com.opengamma.financial.analytics.model.bond.BondFunctions;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionFunctions;
import com.opengamma.financial.analytics.model.credit.CreditFunctions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateFunctions;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurveDefaults;
import com.opengamma.financial.analytics.model.equity.futures.EquityDividendYieldPricingDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionDefaults;
import com.opengamma.financial.analytics.model.equity.option.OptionFunctions;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioTheoryFunctions;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityForwardCalculationDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapStaticReplicationDefaults;
import com.opengamma.financial.analytics.model.fixedincome.FixedIncomeFunctions;
import com.opengamma.financial.analytics.model.forex.ForexFunctions;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionFunctions;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sabrcube.SABRCubeFunctions;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.cube.CubeFunctions;
import com.opengamma.financial.analytics.model.volatility.local.LocalFunctions;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.LocalVolatilitySurfaceDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceMixedLogNormalDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceSABRDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceSplineDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.CommodityBlackVolatilitySurfacePrimitiveDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.CommodityBlackVolatilitySurfaceSecurityDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.CommodityBlackVolatilitySurfaceTradeDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfacePrimitiveDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfaceSecurityDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfaceTradeDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.FXBlackVolatilitySurfacePrimitiveDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.FXBlackVolatilitySurfaceSecurityDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.FXBlackVolatilitySurfaceTradeDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.PureBlackVolatilitySurfaceDefaults;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.functional.Function1;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.spring.defaults.EquityInstrumentDefaultValues;
import com.opengamma.web.spring.defaults.GeneralBlackVolatilityInterpolationDefaults;
import com.opengamma.web.spring.defaults.GeneralLocalVolatilitySurfaceDefaults;
import com.opengamma.web.spring.defaults.TargetSpecificBlackVolatilitySurfaceDefaults;

/**
 * Constructs a standard function repository.
 * <p>
 * This should be replaced by something that loads the functions from the configuration database
 */
public class DemoStandardFunctionConfiguration extends AbstractRepositoryConfigurationBean {

  /**
   * Constants for a particular currency.
   */
  public static class CurrencyInfo {

    private String _defaultCurveConfiguration;
    private String _defaultCurve;
    private String _isdaCurveConfiguration;
    private String _isdaCurve;
    private String _bondFutureOptionSurface;
    private String _futureOptionSurface;
    private String _irFutureOptionSurface;
    private String _swaptionSurface;
    private String _defaultCube;

    public String getDefaultCurveConfiguration() {
      return _defaultCurveConfiguration;
    }

    public void setDefaultCurveConfiguration(final String defaultCurveConfiguration) {
      _defaultCurveConfiguration = defaultCurveConfiguration;
    }

    public String getDefaultCurve() {
      return _defaultCurve;
    }

    public void setDefaultCurve(final String defaultCurve) {
      _defaultCurve = defaultCurve;
    }

    public String getDiscountingCurve() {
      return getDefaultCurve();
    }

    public String getCreditCurveConfiguration() {
      return getDefaultCurveConfiguration();
    }

    public String getCreditCurve() {
      return getDefaultCurve();
    }

    public String getRiskFreeCurveConfiguration() {
      return getDefaultCurveConfiguration();
    }

    public String getRiskFreeCurve() {
      return getDefaultCurve();
    }

    public void setISDACurveConfiguration(final String isdaCurveConfiguration) {
      _isdaCurveConfiguration = isdaCurveConfiguration;
    }

    public String getISDACurveConfiguration() {
      return _isdaCurveConfiguration;
    }

    public void setISDACurve(final String isdaCurve) {
      _isdaCurve = isdaCurve;
    }

    public String getISDACurve() {
      return _isdaCurve;
    }

    public void setBondFutureOptionSurface(final String bondFutureOptionSurface) {
      _bondFutureOptionSurface = bondFutureOptionSurface;
    }

    public String getBondFutureOptionSurface() {
      return _bondFutureOptionSurface;
    }

    public void setFutureOptionSurface(final String futureOptionSurface) {
      _futureOptionSurface = futureOptionSurface;
    }

    public String getFutureOptionSurface() {
      return _futureOptionSurface;
    }

    public void setIRFutureOptionSurface(final String irFutureOptionSurface) {
      _irFutureOptionSurface = irFutureOptionSurface;
    }

    public String getIRFutureOptionSurface() {
      return _irFutureOptionSurface;
    }

    public void setSwaptionSurface(final String swaptionSurface) {
      _swaptionSurface = swaptionSurface;
    }

    public String getSwaptionSurface() {
      return _swaptionSurface;
    }

    public void setDefaultCube(final String cubeName) {
      _defaultCube = cubeName;
    }

    public String getDefaultCube() {
      return _defaultCube;
    }

  }

  /**
   * Constants for a particular currency pair
   */
  public static class CurrencyPairInfo {

    private String _defaultCurve;
    private String _defaultSurface;

    public void setDefaultCurve(final String curve) {
      _defaultCurve = curve;
    }

    public String getDefaultCurve() {
      return _defaultCurve;
    }

    public void setDefaultSurface(final String surface) {
      _defaultSurface = surface;
    }

    public String getDefaultSurface() {
      return _defaultSurface;
    }

  }

  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<String, CurrencyInfo>();
  private final Map<Pair<String, String>, CurrencyPairInfo> _perCurrencyPairInfo = new HashMap<Pair<String, String>, CurrencyPairInfo>();
  private String _mark2MarketField = "PX_LAST";
  private String _costOfCarryField = "COST_OF_CARRY";

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

  protected <T> Map<String, T> getCurrencyInfo(final Function1<CurrencyInfo, T> filter) {
    final Map<String, T> result = new HashMap<String, T>();
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      final T entry = filter.execute(e.getValue());
      if (entry != null) {
        result.put(e.getKey(), entry);
      }
    }
    return result;
  }

  public void setPerCurrencyPairInfo(final Map<Pair<String, String>, CurrencyPairInfo> perCurrencyPairInfo) {
    _perCurrencyPairInfo.clear();
    _perCurrencyPairInfo.putAll(perCurrencyPairInfo);
  }

  public Map<Pair<String, String>, CurrencyPairInfo> getPerCurrencyPairInfo() {
    return _perCurrencyPairInfo;
  }

  public void setCurrencyPairInfo(final Pair<String, String> currencyPair, final CurrencyPairInfo info) {
    _perCurrencyPairInfo.put(currencyPair, info);
  }

  public CurrencyPairInfo getCurrencyPairInfo(final Pair<String, String> currencyPair) {
    return _perCurrencyPairInfo.get(currencyPair);
  }

  protected <T> Map<Pair<String, String>, T> getCurrencyPairInfo(final Function1<CurrencyPairInfo, T> filter) {
    final Map<Pair<String, String>, T> result = new HashMap<Pair<String, String>, T>();
    for (final Map.Entry<Pair<String, String>, CurrencyPairInfo> e : getPerCurrencyPairInfo().entrySet()) {
      final T entry = filter.execute(e.getValue());
      if (entry != null) {
        result.put(e.getKey(), entry);
      }
    }
    return result;
  }

  public void setMark2MarketField(final String mark2MarketField) {
    _mark2MarketField = mark2MarketField;
  }

  public String getMark2MarketField() {
    return _mark2MarketField;
  }

  public void setCostOfCarryField(final String costOfCarryField) {
    _costOfCarryField = costOfCarryField;
  }

  public String getCostOfCarryField() {
    return _costOfCarryField;
  }

  public static RepositoryConfigurationSource instance() {
    return new DemoStandardFunctionConfiguration().getObjectCreating();
  }

  public DemoStandardFunctionConfiguration() {
    setDefaultCurrencyInfo();
    setDefaultCurrencyPairInfo();
  }

  protected CurrencyInfo audDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveAUDConfig");
    i.setDefaultCurve("Discounting");
    i.setDefaultCube("BLOOMBERG");
    return i;
  }

  protected CurrencyInfo brlDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveBRLConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  protected CurrencyInfo cadDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveCADConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  protected CurrencyInfo chfDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveCHFConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  protected CurrencyInfo eurDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveEURConfig");
    i.setDefaultCurve("Discounting");
    i.setIRFutureOptionSurface("DEFAULT_PRICE");
    i.setDefaultCube("BLOOMBERG");
    i.setSwaptionSurface("DEFAULT");
    return i;
  }

  protected CurrencyInfo gbpDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveGBPConfig");
    i.setDefaultCurve("Discounting");
    i.setDefaultCube("BLOOMBERG");
    return i;
  }

  protected CurrencyInfo hkdDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveHKDConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  protected CurrencyInfo hufDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveHUFConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  protected CurrencyInfo jpyDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveJPYConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  protected CurrencyInfo krwDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveKRWConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  protected CurrencyInfo mxnDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveMXNConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  protected CurrencyInfo nzdDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveNZDConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  protected CurrencyInfo rubDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultCashCurveRUBConfig");
    i.setDefaultCurve("Cash");
    return i;
  }

  protected CurrencyInfo usdDefaultCurrencyInfo() {
    final CurrencyInfo i = new CurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveUSDConfig");
    i.setDefaultCurve("Discounting");
    i.setISDACurveConfiguration("ISDA");
    i.setISDACurve("ISDA");
    i.setBondFutureOptionSurface("BBG");
    i.setFutureOptionSurface("BBG_S ");
    i.setIRFutureOptionSurface("DEFAULT_PRICE");
    i.setDefaultCube("BLOOMBERG");
    return i;
  }

  protected void setDefaultCurrencyInfo() {
    setCurrencyInfo("ARS", new CurrencyInfo());
    setCurrencyInfo("AUD", audDefaultCurrencyInfo());
    setCurrencyInfo("BRL", brlDefaultCurrencyInfo());
    setCurrencyInfo("CAD", cadDefaultCurrencyInfo());
    setCurrencyInfo("CHF", chfDefaultCurrencyInfo());
    setCurrencyInfo("CNY", new CurrencyInfo());
    setCurrencyInfo("CZK", new CurrencyInfo());
    setCurrencyInfo("EGP", new CurrencyInfo());
    setCurrencyInfo("EUR", eurDefaultCurrencyInfo());
    setCurrencyInfo("GBP", gbpDefaultCurrencyInfo());
    setCurrencyInfo("HKD", hkdDefaultCurrencyInfo());
    setCurrencyInfo("HUF", hufDefaultCurrencyInfo());
    setCurrencyInfo("IDR", new CurrencyInfo());
    setCurrencyInfo("ILS", new CurrencyInfo());
    setCurrencyInfo("INR", new CurrencyInfo());
    setCurrencyInfo("JPY", jpyDefaultCurrencyInfo());
    setCurrencyInfo("KRW", krwDefaultCurrencyInfo());
    setCurrencyInfo("MXN", mxnDefaultCurrencyInfo());
    setCurrencyInfo("MYR", new CurrencyInfo());
    setCurrencyInfo("NOK", new CurrencyInfo());
    setCurrencyInfo("NZD", nzdDefaultCurrencyInfo());
    setCurrencyInfo("PHP", new CurrencyInfo());
    setCurrencyInfo("PLN", new CurrencyInfo());
    setCurrencyInfo("RUB", rubDefaultCurrencyInfo());
    setCurrencyInfo("SEK", new CurrencyInfo());
    setCurrencyInfo("SGD", new CurrencyInfo());
    setCurrencyInfo("TRY", new CurrencyInfo());
    setCurrencyInfo("TWD", new CurrencyInfo());
    setCurrencyInfo("USD", usdDefaultCurrencyInfo());
    setCurrencyInfo("ZAR", new CurrencyInfo());
  }

  protected CurrencyPairInfo eurChfCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo eurJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdAudCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdBrlCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdCadCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdChfCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdEurCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultCurve("DiscountingImplied");
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdGbpCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdHkdCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdHufCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdKrwCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdMxnCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected CurrencyPairInfo usdNzdCurrencyPairInfo() {
    final CurrencyPairInfo i = new CurrencyPairInfo();
    i.setDefaultSurface("TULLETT");
    return i;
  }

  protected void setDefaultCurrencyPairInfo() {
    setCurrencyPairInfo(Pair.of("EUR", "CHF"), eurChfCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("EUR", "JPY"), eurJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "AUD"), usdAudCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "BRL"), usdBrlCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "CAD"), usdCadCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "CHF"), usdChfCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "EUR"), usdEurCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "GBP"), usdGbpCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "HKD"), usdHkdCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "HUF"), usdHufCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "JPY"), usdJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "KRW"), usdKrwCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "MXN"), usdMxnCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "NZD"), usdNzdCurrencyPairInfo());
  }

  protected void addBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSABRDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getSABRInterpolationDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceMixedLogNormalDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getMixedLogNormalInterpolationDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSplineDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getSplineInterpolationDefaults()));
  }

  protected void addCommodityBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfacePrimitiveDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfaceSecurityDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfaceTradeDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
  }

  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(CurrencyPairsDefaults.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.BLOOMBERG_LIVE_DATA));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  protected void addEquityDividendYieldFuturesDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityFutureDefaults = EquityInstrumentDefaultValues.builder()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveCalculationConfigNames()
        .useDiscountingCurveNames()
        .createDefaults();
    final List<String> equityFutureDefaultsWithPriority = new ArrayList<String>();
    equityFutureDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityFutureDefaultsWithPriority.addAll(equityFutureDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityDividendYieldPricingDefaults.class.getName(), equityFutureDefaultsWithPriority));
  }

  protected void addEquityForwardDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityForwardDefaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .createDefaults();
    final List<String> equityForwardDefaultsWithPriority = new ArrayList<String>();
    equityForwardDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityForwardDefaultsWithPriority.addAll(equityForwardDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardCalculationDefaults.class.getName(), equityForwardDefaultsWithPriority));
    final List<String> equityForwardCurveDefaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useDiscountingCurveCurrency()
        .createDefaults();
    final List<String> equityForwardCurveDefaultsWithPriority = new ArrayList<String>();
    equityForwardCurveDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityForwardCurveDefaultsWithPriority.addAll(equityForwardCurveDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardCurveDefaults.class.getName(), equityForwardCurveDefaultsWithPriority));
  }

  protected void addEquityIndexOptionBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> defaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useForwardCurveNames()
        .useForwardCurveCalculationMethodNames()
        .useDiscountingCurveCurrency()
        .useForwardCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createDefaults();
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfacePrimitiveDefaults.class.getName(), defaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfaceSecurityDefaults.class.getName(), defaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfaceTradeDefaults.class.getName(), defaults));
  }

  protected void addEquityOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityIndexOptionDefaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useForwardCurveCalculationConfigNames()
        .useForwardCurveNames()
        .useVolatilitySurfaceNames()
        .useInterpolationMethodNames()
        .createDefaults();
    final List<String> equityIndexOptionDefaultsWithPriority = new ArrayList<String>();
    equityIndexOptionDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityIndexOptionDefaultsWithPriority.addAll(equityIndexOptionDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionDefaults.class.getName(), equityIndexOptionDefaultsWithPriority));
  }

  protected void addEquityPureVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> defaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createDefaults();
    functionConfigs.add(new ParameterizedFunctionConfiguration(PureBlackVolatilitySurfaceDefaults.class.getName(), defaults));
  }

  protected void addEquityVarianceSwapDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityVarianceSwapStaticReplicationDefaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createDefaults();
    final List<String> equityVarianceSwapStaticReplicationDefaultsWithPriority = new ArrayList<String>();
    equityVarianceSwapStaticReplicationDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityVarianceSwapStaticReplicationDefaultsWithPriority.addAll(equityVarianceSwapStaticReplicationDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityVarianceSwapStaticReplicationDefaults.class.getName(), equityVarianceSwapStaticReplicationDefaultsWithPriority));
    final List<String> equityVarianceSwapDefaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useForwardCurveNames()
        .useForwardCurveCalculationConfigNames()
        .useForwardCurveCalculationMethodNames()
        .useDiscountingCurveCurrency()
        .useVolatilitySurfaceNames()
        .createDefaults();
    final List<String> equityVarianceSwapDefaultsWithPriority = new ArrayList<String>();
    equityVarianceSwapDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityVarianceSwapDefaultsWithPriority.addAll(equityVarianceSwapDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityVarianceSwapDefaults.class.getName(), equityVarianceSwapDefaultsWithPriority));
  }

  protected void addFXOptionBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXBlackVolatilitySurfacePrimitiveDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllFXDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXBlackVolatilitySurfaceSecurityDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllFXDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXBlackVolatilitySurfaceTradeDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllFXDefaults()));
  }

  protected void addLocalVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(LocalVolatilitySurfaceDefaults.class.getName(),
        GeneralLocalVolatilitySurfaceDefaults.getLocalVolatilitySurfaceDefaults()));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    addBlackVolatilitySurfaceDefaults(functions);
    addCommodityBlackVolatilitySurfaceDefaults(functions);
    addCurrencyConversionFunctions(functions);
    addEquityDividendYieldFuturesDefaults(functions);
    addEquityForwardDefaults(functions);
    addEquityIndexOptionBlackVolatilitySurfaceDefaults(functions);
    addEquityOptionDefaults(functions);
    addEquityPureVolatilitySurfaceDefaults(functions);
    addEquityVarianceSwapDefaults(functions);
    addFXOptionBlackVolatilitySurfaceDefaults(functions);
    addLocalVolatilitySurfaceDefaults(functions);
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "FUNDING"));
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));
  }

  protected RepositoryConfigurationSource bondFunctions() {
    return BondFunctions.defaults(getCurrencyInfo(new Function1<CurrencyInfo, BondFunctions.Defaults.CurrencyInfo>() {
      @Override
      public BondFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        if ((i.getRiskFreeCurve() == null) || (i.getRiskFreeCurveConfiguration() == null) || (i.getCreditCurve() == null) || (i.getCreditCurveConfiguration() == null)) {
          return null;
        }
        return new BondFunctions.Defaults.CurrencyInfo(i.getRiskFreeCurve(), i.getRiskFreeCurveConfiguration(), i.getCreditCurve(), i.getCreditCurveConfiguration());
      }
    }));
  }

  protected RepositoryConfigurationSource bondFutureOptionFunctions() {
    return BondFutureOptionFunctions.defaults(getCurrencyInfo(new Function1<CurrencyInfo, BondFutureOptionFunctions.Defaults.CurrencyInfo>() {
      @Override
      public BondFutureOptionFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        if ((i.getDefaultCurveConfiguration() == null) || (i.getBondFutureOptionSurface() == null)) {
          return null;
        }
        return new BondFutureOptionFunctions.Defaults.CurrencyInfo(i.getDefaultCurveConfiguration(), i.getBondFutureOptionSurface());
      }
    }));
  }

  protected RepositoryConfigurationSource cdsFunctions() {
    return CreditFunctions.defaults(getCurrencyInfo(new Function1<CurrencyInfo, CreditFunctions.Defaults.CurrencyInfo>() {
      @Override
      public CreditFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        if ((i.getISDACurve() == null) || (i.getISDACurveConfiguration() == null)) {
          return null;
        }
        return new CreditFunctions.Defaults.CurrencyInfo(i.getISDACurve(), i.getISDACurveConfiguration());
      }
    }));
  }

  protected RepositoryConfigurationSource cubeFunctions() {
    return CubeFunctions.defaults();
  }

  protected RepositoryConfigurationSource equityOptionFunctions() {
    return OptionFunctions.defaults();
  }

  protected RepositoryConfigurationSource externalSensitivitiesFunctions() {
    return SensitivitiesFunctions.defaults(getCurrencyInfo(new Function1<CurrencyInfo, SensitivitiesFunctions.Defaults.CurrencyInfo>() {
      @Override
      public SensitivitiesFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        if (i.getDefaultCurveConfiguration() == null) {
          return null;
        }
        return new SensitivitiesFunctions.Defaults.CurrencyInfo(i.getDefaultCurveConfiguration());
      }
    }));
  }

  protected RepositoryConfigurationSource fixedIncomeFunctions() {
    return FixedIncomeFunctions.defaults(getCurrencyInfo(new Function1<CurrencyInfo, FixedIncomeFunctions.Defaults.CurrencyInfo>() {
      @Override
      public FixedIncomeFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        if (i.getDefaultCurveConfiguration() == null) {
          return null;
        }
        return new FixedIncomeFunctions.Defaults.CurrencyInfo(i.getDefaultCurveConfiguration());
      }
    }));
  }

  protected RepositoryConfigurationSource forexFunctions() {
    final Map<String, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo> curveDefaults =
        getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
          @Override
          public com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
            if ((i.getDefaultCurveConfiguration() == null) || (i.getDiscountingCurve() == null)) {
              return null;
            }
            return new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo(i.getDefaultCurveConfiguration(), i.getDiscountingCurve());
          }
        });
    final Map<Pair<String, String>, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo> surfaceDefaults =
        getCurrencyPairInfo(new Function1<CurrencyPairInfo, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo>() {
          @Override
          public com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo execute(final CurrencyPairInfo i) {
            if (i.getDefaultSurface() == null) {
              return null;
            }
            return new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo(i.getDefaultSurface());
          }
        });
    return ForexFunctions.defaults(curveDefaults, surfaceDefaults);
  }

  protected RepositoryConfigurationSource forexOptionFunctions() {
    return com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.defaults();
  }

  protected RepositoryConfigurationSource forwardCurveFunctions() {
    final Map<String, ForwardFunctions.Defaults.CurrencyInfo> ccyDefaults = getCurrencyInfo(new Function1<CurrencyInfo, ForwardFunctions.Defaults.CurrencyInfo>() {
      @Override
      public ForwardFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        if ((i.getDefaultCurveConfiguration() == null) || (i.getDiscountingCurve() == null)) {
          return null;
        }
        return new ForwardFunctions.Defaults.CurrencyInfo(i.getDefaultCurveConfiguration(), i.getDiscountingCurve());
      }
    });
    final Map<Pair<String, String>, ForwardFunctions.Defaults.CurrencyPairInfo> ccypDefaults = getCurrencyPairInfo(new Function1<CurrencyPairInfo, ForwardFunctions.Defaults.CurrencyPairInfo>() {
      @Override
      public ForwardFunctions.Defaults.CurrencyPairInfo execute(final CurrencyPairInfo i) {
        if (i.getDefaultCurve() == null) {
          return null;
        }
        return new ForwardFunctions.Defaults.CurrencyPairInfo(i.getDefaultCurve());
      }
    });
    return ForwardFunctions.defaults(ccyDefaults, ccypDefaults);
  }

  protected RepositoryConfigurationSource futureFunctions() {
    return FutureFunctions.defaults(getCurrencyInfo(new Function1<CurrencyInfo, FutureFunctions.Defaults.CurrencyInfo>() {
      @Override
      public FutureFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        if (i.getDefaultCurveConfiguration() == null) {
          return null;
        }
        return new FutureFunctions.Defaults.CurrencyInfo(i.getDefaultCurveConfiguration());
      }
    }));
  }

  protected RepositoryConfigurationSource futureOptionFunctions() {
    return FutureOptionFunctions.defaults(getCurrencyInfo(new Function1<CurrencyInfo, FutureOptionFunctions.Defaults.CurrencyInfo>() {
      @Override
      public FutureOptionFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        if ((i.getDiscountingCurve() == null) || (i.getDefaultCurveConfiguration() == null) || (i.getFutureOptionSurface() == null)) {
          return null;
        }
        return new FutureOptionFunctions.Defaults.CurrencyInfo(i.getDiscountingCurve(), i.getDefaultCurveConfiguration(), i.getFutureOptionSurface());
      }
    }));
  }

  protected RepositoryConfigurationSource interestRateFunctions() {
    return InterestRateFunctions.defaults(getPerCurrencyInfo().keySet());
  }

  protected RepositoryConfigurationSource irFutureOptionFunctions() {
    return IRFutureOptionFunctions.defaults(getCurrencyInfo(new Function1<CurrencyInfo, IRFutureOptionFunctions.Defaults.CurrencyInfo>() {
      @Override
      public IRFutureOptionFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        if ((i.getDefaultCurveConfiguration() == null) || (i.getIRFutureOptionSurface() == null)) {
          return null;
        }
        return new IRFutureOptionFunctions.Defaults.CurrencyInfo(i.getDefaultCurveConfiguration(), i.getIRFutureOptionSurface());
      }
    }));
  }

  protected RepositoryConfigurationSource localVolatilityFunctions() {
    return LocalFunctions.defaults(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
        if ((i.getDefaultCurveConfiguration() == null) || (i.getDiscountingCurve() == null)) {
          return null;
        }
        return new com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo(i.getDefaultCurveConfiguration(), i.getDiscountingCurve());
      }
    }));
  }

  protected RepositoryConfigurationSource pnlFunctions() {
    return new CombiningRepositoryConfigurationSource(PNLFunctions.calculators(getMark2MarketField(), getCostOfCarryField()),
        PNLFunctions.defaults("FUNDING", "FUNDING", "FUNDING"));
  }

  protected RepositoryConfigurationSource portfolioTheoryFunctions() {
    return new CombiningRepositoryConfigurationSource(PortfolioTheoryFunctions.calculators(), PortfolioTheoryFunctions.defaults());
  }

  protected RepositoryConfigurationSource sabrCubeFunctions() {
    return SABRCubeFunctions.defaults(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
        if ((i.getDefaultCurveConfiguration() == null) || (i.getDefaultCube() == null)) {
          return null;
        }
        return new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo(i.getDefaultCurveConfiguration(), i.getDefaultCube());
      }
    }));
  }

  protected RepositoryConfigurationSource swaptionFunctions() {
    return com.opengamma.financial.analytics.model.swaption.black.BlackFunctions
        .defaults(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo>() {
          @Override
          public com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
            if ((i.getDefaultCurveConfiguration() == null) || (i.getSwaptionSurface() == null)) {
              return null;
            }
            return new com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo(i.getDefaultCurveConfiguration(), i.getSwaptionSurface());
          }
        }));
  }

  protected RepositoryConfigurationSource varFunctions() {
    return VaRFunctions.defaults();
  }

  protected RepositoryConfigurationSource volatilitySurfaceFunctions() {
    return new CombiningRepositoryConfigurationSource(com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.defaults(),
        com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.defaults());
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), bondFunctions(), cdsFunctions(), cubeFunctions(), equityOptionFunctions(), externalSensitivitiesFunctions(),
        fixedIncomeFunctions(), forexFunctions(), forexOptionFunctions(), forwardCurveFunctions(), futureFunctions(), futureOptionFunctions(), interestRateFunctions(), localVolatilityFunctions(),
        pnlFunctions(), portfolioTheoryFunctions(), swaptionFunctions(), varFunctions(), volatilitySurfaceFunctions());
  }

}
