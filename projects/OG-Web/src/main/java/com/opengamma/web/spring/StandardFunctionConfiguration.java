/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Lists;
import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.CurrencyPairsDefaults;
import com.opengamma.financial.analytics.model.bond.BondFunctions;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionFunctions;
import com.opengamma.financial.analytics.model.credit.CreditFunctions;
import com.opengamma.financial.analytics.model.curve.forward.CommodityForwardCurveFromFuturePerCurrencyDefaults;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateFunctions;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurveYieldCurveImpliedPerCurrencyDefaults;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurvePerExchangeDefaults;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurvePerTickerDefaults;
import com.opengamma.financial.analytics.model.equity.futures.EquityDividendYieldPricingDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionInterpolatedBlackLognormalPerCurrencyDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionInterpolatedBlackLognormalPerEquityDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionInterpolatedBlackLognormalPerExchangeDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionSurfaceCalculationMethodPerCurrencyDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionSurfaceCalculationMethodPerEquityDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionSurfaceCalculationMethodPerExchangeDefaults;
import com.opengamma.financial.analytics.model.equity.option.OptionFunctions;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioTheoryFunctions;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityForwardPerEquityDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapStaticReplicationDefaults;
import com.opengamma.financial.analytics.model.fixedincome.FixedIncomeFunctions;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionFunctions;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.LocalVolatilitySurfaceDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceMixedLogNormalDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceSABRDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceSplineDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.CommodityBlackVolatilitySurfacePrimitiveDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.CommodityBlackVolatilitySurfaceSecurityDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.CommodityBlackVolatilitySurfaceTradeDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfacePerCurrencyDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfacePerExchangeDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfacePerTickerDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.FXBlackVolatilitySurfacePrimitiveDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.FXBlackVolatilitySurfaceSecurityDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.FXBlackVolatilitySurfaceTradeDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.PureBlackVolatilitySurfacePrimitiveDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.PureBlackVolatilitySurfaceSecurityDefaults;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.functional.Function1;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.spring.defaults.EquityInstrumentDefaultValues;
import com.opengamma.web.spring.defaults.GeneralBlackVolatilityInterpolationDefaults;
import com.opengamma.web.spring.defaults.GeneralLocalVolatilitySurfaceDefaults;
import com.opengamma.web.spring.defaults.TargetSpecificBlackVolatilitySurfaceDefaults;

/**
 * Constructs a standard function repository.
 * <p>
 * A sub-class should provide installation specific details relating to the data providers used.
 */
public abstract class StandardFunctionConfiguration extends AbstractRepositoryConfigurationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(StandardFunctionConfiguration.class);

  /**
   * Holds one or more values referenced by a hierarchical key.
   */
  public static class Value {

    private final Map<String, String> _values = new HashMap<>();

    public void set(final String key, final String value) {
      _values.put(key, value);
    }

    // TODO: allow wildcard matches, e.g. */discounting

    public String get(final String key) {
      final String value = _values.get(key);
      if (value != null) {
        return value;
      }
      final int separator = key.lastIndexOf('/');
      if (separator == -1) {
        return _values.get(null);
      }
      return get(key.substring(0, separator));
    }

  }

  /**
   * Constants for a particular currency.
   */
  public static class CurrencyInfo {

    private final String _currency;

    private final Value _curveConfiguration = new Value();
    private final Value _curveName = new Value();
    private final Value _surfaceName = new Value();
    private final Value _cubeName = new Value();
    private final Value _forwardCurveName = new Value();
    private final Value _forwardCurveCalculationMethod = new Value();
    private final Value _surfaceCalculationMethod = new Value();

    public CurrencyInfo(final String currency) {
      _currency = currency;
    }

    public String getCurrency() {
      return _currency;
    }

    public void setCurveConfiguration(final String key, final String curveConfiguration) {
      _curveConfiguration.set(key, curveConfiguration);
    }

    public String getCurveConfiguration(final String key) {
      return _curveConfiguration.get(key);
    }

    public void setCurveName(final String key, final String curveName) {
      _curveName.set(key, curveName);
    }

    public String getCurveName(final String key) {
      return _curveName.get(key);
    }

    public void setSurfaceName(final String key, final String surfaceName) {
      _surfaceName.set(key, surfaceName);
    }

    public String getSurfaceName(final String key) {
      return _surfaceName.get(key);
    }

    public void setCubeName(final String key, final String cubeName) {
      _cubeName.set(key, cubeName);
    }

    public String getCubeName(final String key) {
      return _cubeName.get(key);
    }

    public void setForwardCurveName(final String key, final String forwardCurveName) {
      _forwardCurveName.set(key, forwardCurveName);
    }
    
    public String getForwardCurveName(final String key) {
      return _forwardCurveName.get(key);
    }
    
    public void setForwardCurveCalculationMethod(final String key, final String forwardCurveCalculationMethod) {
      _forwardCurveCalculationMethod.set(key, forwardCurveCalculationMethod);
    }
    
    public String getForwardCurveCalculationMethod(final String key) {
      return _forwardCurveCalculationMethod.get(key);
    }
    
    public void setSurfaceCalculationMethod(final String key, final String surfaceCalculationMethod) {
      _surfaceCalculationMethod.set(key, surfaceCalculationMethod);
    }
    
    public String getSurfaceCalculationMethod(final String key) {
      return _surfaceCalculationMethod.get(key);
    }
  }

  /**
   * Constants for a particular currency pair
   */
  public static class CurrencyPairInfo {

    private final Pair<String, String> _currencies;

    private final Value _curveName = new Value();
    private final Value _surfaceName = new Value();

    public CurrencyPairInfo(final Pair<String, String> currencies) {
      _currencies = currencies;
    }

    public Pair<String, String> getCurrencies() {
      return _currencies;
    }

    public void setCurveName(final String key, final String curveName) {
      _curveName.set(key, curveName);
    }

    public String getCurveName(final String key) {
      return _curveName.get(key);
    }

    public void setSurfaceName(final String key, final String surfaceName) {
      _surfaceName.set(key, surfaceName);
    }

    public String getSurfaceName(final String key) {
      return _surfaceName.get(key);
    }

  }

  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
  private final Map<Pair<String, String>, CurrencyPairInfo> _perCurrencyPairInfo = new HashMap<>();
  private String _mark2MarketField;
  private String _costOfCarryField;

  public StandardFunctionConfiguration() {
    setDefaultCurrencyInfo();
    setDefaultCurrencyPairInfo();
  }

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
      if (entry instanceof InitializingBean) {
        try {
          ((InitializingBean) entry).afterPropertiesSet();
        } catch (final Exception ex) {
          s_logger.debug("Skipping {}", e.getKey());
          s_logger.trace("Caught exception", e);
          continue;
        }
      }
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
      if (entry instanceof InitializingBean) {
        try {
          ((InitializingBean) entry).afterPropertiesSet();
        } catch (final Exception ex) {
          s_logger.debug("Skipping {}", e.getKey());
          s_logger.trace("Caught exception", e);
          continue;
        }
      }
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

  protected CurrencyInfo defaultCurrencyInfo(final String currency) {
    return new CurrencyInfo(currency);
  }

  protected CurrencyInfo arsCurrencyInfo() {
    return defaultCurrencyInfo("ARS");
  }

  protected CurrencyInfo audCurrencyInfo() {
    return defaultCurrencyInfo("AUD");
  }

  protected CurrencyInfo brlCurrencyInfo() {
    return defaultCurrencyInfo("BRL");
  }

  protected CurrencyInfo cadCurrencyInfo() {
    return defaultCurrencyInfo("CAD");
  }

  protected CurrencyInfo chfCurrencyInfo() {
    return defaultCurrencyInfo("CHF");
  }

  protected CurrencyInfo cnyCurrencyInfo() {
    return defaultCurrencyInfo("CNY");
  }

  protected CurrencyInfo czkCurrencyInfo() {
    return defaultCurrencyInfo("CZK");
  }

  protected CurrencyInfo egpCurrencyInfo() {
    return defaultCurrencyInfo("EGP");
  }

  protected CurrencyInfo eurCurrencyInfo() {
    return defaultCurrencyInfo("EUR");
  }

  protected CurrencyInfo gbpCurrencyInfo() {
    return defaultCurrencyInfo("GBP");
  }

  protected CurrencyInfo hkdCurrencyInfo() {
    return defaultCurrencyInfo("HKD");
  }

  protected CurrencyInfo hufCurrencyInfo() {
    return defaultCurrencyInfo("HUF");
  }

  protected CurrencyInfo idrCurrencyInfo() {
    return defaultCurrencyInfo("IDR");
  }

  protected CurrencyInfo ilsCurrencyInfo() {
    return defaultCurrencyInfo("ILS");
  }

  protected CurrencyInfo inrCurrencyInfo() {
    return defaultCurrencyInfo("INR");
  }

  protected CurrencyInfo jpyCurrencyInfo() {
    return defaultCurrencyInfo("JPY");
  }

  protected CurrencyInfo krwCurrencyInfo() {
    return defaultCurrencyInfo("KRW");
  }

  protected CurrencyInfo mxnCurrencyInfo() {
    return defaultCurrencyInfo("MXN");
  }

  protected CurrencyInfo myrCurrencyInfo() {
    return defaultCurrencyInfo("MYR");
  }

  protected CurrencyInfo nokCurrencyInfo() {
    return defaultCurrencyInfo("NOK");
  }

  protected CurrencyInfo nzdCurrencyInfo() {
    return defaultCurrencyInfo("NZD");
  }

  protected CurrencyInfo phpCurrencyInfo() {
    return defaultCurrencyInfo("PHP");
  }

  protected CurrencyInfo pnlCurrencyInfo() {
    return defaultCurrencyInfo("PNL");
  }

  protected CurrencyInfo rubCurrencyInfo() {
    return defaultCurrencyInfo("RUB");
  }

  protected CurrencyInfo sekCurrencyInfo() {
    return defaultCurrencyInfo("SEK");
  }

  protected CurrencyInfo sgdCurrencyInfo() {
    return defaultCurrencyInfo("SGD");
  }

  protected CurrencyInfo tryCurrencyInfo() {
    return defaultCurrencyInfo("TRY");
  }

  protected CurrencyInfo twdCurrencyInfo() {
    return defaultCurrencyInfo("TWD");
  }

  protected CurrencyInfo usdCurrencyInfo() {
    return defaultCurrencyInfo("USD");
  }

  protected CurrencyInfo zarCurrencyInfo() {
    return defaultCurrencyInfo("ZAR");
  }

  protected void setDefaultCurrencyInfo() {
    setCurrencyInfo("ARS", arsCurrencyInfo());
    setCurrencyInfo("AUD", audCurrencyInfo());
    setCurrencyInfo("BRL", brlCurrencyInfo());
    setCurrencyInfo("CAD", cadCurrencyInfo());
    setCurrencyInfo("CHF", chfCurrencyInfo());
    setCurrencyInfo("CNY", cnyCurrencyInfo());
    setCurrencyInfo("CZK", czkCurrencyInfo());
    setCurrencyInfo("EGP", egpCurrencyInfo());
    setCurrencyInfo("EUR", eurCurrencyInfo());
    setCurrencyInfo("GBP", gbpCurrencyInfo());
    setCurrencyInfo("HKD", hkdCurrencyInfo());
    setCurrencyInfo("HUF", hufCurrencyInfo());
    setCurrencyInfo("IDR", idrCurrencyInfo());
    setCurrencyInfo("ILS", ilsCurrencyInfo());
    setCurrencyInfo("INR", inrCurrencyInfo());
    setCurrencyInfo("JPY", jpyCurrencyInfo());
    setCurrencyInfo("KRW", krwCurrencyInfo());
    setCurrencyInfo("MXN", mxnCurrencyInfo());
    setCurrencyInfo("MYR", myrCurrencyInfo());
    setCurrencyInfo("NOK", nokCurrencyInfo());
    setCurrencyInfo("NZD", nzdCurrencyInfo());
    setCurrencyInfo("PHP", phpCurrencyInfo());
    setCurrencyInfo("PLN", pnlCurrencyInfo());
    setCurrencyInfo("RUB", rubCurrencyInfo());
    setCurrencyInfo("SEK", sekCurrencyInfo());
    setCurrencyInfo("SGD", sgdCurrencyInfo());
    setCurrencyInfo("TRY", tryCurrencyInfo());
    setCurrencyInfo("TWD", twdCurrencyInfo());
    setCurrencyInfo("USD", usdCurrencyInfo());
    setCurrencyInfo("ZAR", zarCurrencyInfo());
  }

  protected CurrencyPairInfo defaultCurrencyPairInfo(final String c1, final String c2) {
    return new CurrencyPairInfo(Pair.of(c1, c2));
  }

  protected CurrencyPairInfo eurBrlCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "BRL");
  }

  protected CurrencyPairInfo eurChfCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "CHF");
  }

  protected CurrencyPairInfo eurJpyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "JPY");
  }

  protected CurrencyPairInfo eurTryCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "TRY");
  }

  protected CurrencyPairInfo sekJpyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("SEK", "JPY");
  }

  protected CurrencyPairInfo usdAudCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "AUD");
  }

  protected CurrencyPairInfo usdBrlCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "BRL");
  }

  protected CurrencyPairInfo usdCadCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "CAD");
  }

  protected CurrencyPairInfo usdChfCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "CHF");
  }

  protected CurrencyPairInfo usdCnyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "CNY");
  }

  protected CurrencyPairInfo usdEurCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "EUR");
  }

  protected CurrencyPairInfo usdGbpCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "GBP");
  }

  protected CurrencyPairInfo usdHkdCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "HKD");
  }

  protected CurrencyPairInfo usdHufCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "HUF");
  }

  protected CurrencyPairInfo usdInrCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "INR");
  }

  protected CurrencyPairInfo usdJpyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "JPY");
  }

  protected CurrencyPairInfo usdKrwCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "KRW");
  }

  protected CurrencyPairInfo usdMxnCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "MXN");
  }

  protected CurrencyPairInfo usdNokCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "NOK");
  }

  protected CurrencyPairInfo usdNzdCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "NZD");
  }

  protected CurrencyPairInfo usdSgdCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "SGD");
  }

  protected CurrencyPairInfo usdZarCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "ZAR");
  }

  protected void setDefaultCurrencyPairInfo() {
    setCurrencyPairInfo(Pair.of("EUR", "BRL"), eurBrlCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("EUR", "CHF"), eurChfCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("EUR", "JPY"), eurJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("EUR", "TRY"), eurTryCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("SEK", "JPY"), sekJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "AUD"), usdAudCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "BRL"), usdBrlCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "CAD"), usdCadCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "CHF"), usdChfCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "CNY"), usdCnyCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "EUR"), usdEurCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "GBP"), usdGbpCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "HKD"), usdHkdCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "HUF"), usdHufCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "INR"), usdInrCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "JPY"), usdJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "KRW"), usdKrwCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "MXN"), usdMxnCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "NOK"), usdNokCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "NZD"), usdNzdCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "SGD"), usdSgdCurrencyPairInfo());
    setCurrencyPairInfo(Pair.of("USD", "ZAR"), usdZarCurrencyPairInfo());

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
  }

  protected void addEquityDividendYieldFuturesDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityFutureDefaults = EquityInstrumentDefaultValues.builder()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveCalculationConfigNames()
        .useDiscountingCurveNames()
        .createPerEquityDefaults();
    final List<String> equityFutureDefaultsWithPriority = new ArrayList<>();
    equityFutureDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityFutureDefaultsWithPriority.addAll(equityFutureDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityDividendYieldPricingDefaults.class.getName(), equityFutureDefaultsWithPriority));
  }

  protected void addEquityForwardDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityForwardDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .createPerEquityDefaults();
    final List<String> equityForwardPerEquityDefaults = new ArrayList<>();
    equityForwardPerEquityDefaults.add(PriorityClass.ABOVE_NORMAL.name());
    equityForwardPerEquityDefaults.addAll(equityForwardDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardPerEquityDefaults.class.getName(), equityForwardPerEquityDefaults));
    final EquityInstrumentDefaultValues.Builder equityForwardCurveEquityAndExchangeDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useDiscountingCurveCurrency();
    final EquityInstrumentDefaultValues.Builder equityForwardCurveCurrencyDefaults = EquityInstrumentDefaultValues.builder()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames();
    final List<String> equityForwardCurvePerTickerDefaults = new ArrayList<>();
    equityForwardCurvePerTickerDefaults.add(PriorityClass.ABOVE_NORMAL.name());
    equityForwardCurvePerTickerDefaults.addAll(equityForwardCurveEquityAndExchangeDefaults.createPerEquityDefaults());
    final List<String> equityForwardCurvePerExchangeDefaults = new ArrayList<>();
    equityForwardCurvePerExchangeDefaults.add(PriorityClass.NORMAL.name());
    equityForwardCurvePerExchangeDefaults.addAll(equityForwardCurveEquityAndExchangeDefaults.createPerExchangeDefaults());
    final List<String> equityForwardCurvePerCurrencyDefaults = new ArrayList<>();
    equityForwardCurvePerCurrencyDefaults.add(PriorityClass.BELOW_NORMAL.name());
    equityForwardCurvePerCurrencyDefaults.addAll(equityForwardCurveCurrencyDefaults.createPerCurrencyDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardCurvePerTickerDefaults.class.getName(), equityForwardCurvePerTickerDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardCurvePerExchangeDefaults.class.getName(), equityForwardCurvePerExchangeDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardCurveYieldCurveImpliedPerCurrencyDefaults.class.getName(), equityForwardCurvePerCurrencyDefaults));
  }

  protected void addEquityIndexOptionBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    final EquityInstrumentDefaultValues.Builder equityDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useForwardCurveNames()
        .useForwardCurveCalculationMethodNames()
        .useVolatilitySurfaceNames();
    final List<String> perTickerDefaults = Lists.newArrayList(PriorityClass.ABOVE_NORMAL.name());
    perTickerDefaults.addAll(equityDefaults.createPerEquityDefaults());
    final List<String> perExchangeDefaults = Lists.newArrayList(PriorityClass.NORMAL.name());
    perExchangeDefaults.addAll(equityDefaults.createPerExchangeDefaults());
    final List<String> perCurrencyDefaults = Lists.newArrayList(PriorityClass.BELOW_NORMAL.name());
    perCurrencyDefaults.addAll(equityDefaults.createPerCurrencyDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfacePerTickerDefaults.class.getName(), perTickerDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfacePerExchangeDefaults.class.getName(), perExchangeDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfacePerCurrencyDefaults.class.getName(), perCurrencyDefaults));
  }

  protected void addEquityOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    final EquityInstrumentDefaultValues.Builder equityOptionSurfaceCalculationMethodDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useVolatilitySurfaceCalculationMethodNames();
    final List<String> equityOptionSurfaceCalculationMethodPerEquityDefaults = Lists.newArrayList(PriorityClass.ABOVE_NORMAL.name());
    equityOptionSurfaceCalculationMethodPerEquityDefaults.addAll(equityOptionSurfaceCalculationMethodDefaults.createPerEquityDefaults());
    final List<String> equityOptionSurfaceCalculationMethodPerExchangeDefaults = Lists.newArrayList(PriorityClass.NORMAL.name());
    equityOptionSurfaceCalculationMethodPerExchangeDefaults.addAll(equityOptionSurfaceCalculationMethodDefaults.createPerExchangeDefaults());
    final List<String> equityOptionSurfaceCalculationMethodPerCurrencyDefaults = Lists.newArrayList(PriorityClass.BELOW_NORMAL.name());
    equityOptionSurfaceCalculationMethodPerCurrencyDefaults.addAll(equityOptionSurfaceCalculationMethodDefaults.createPerCurrencyDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionSurfaceCalculationMethodPerEquityDefaults.class.getName(), equityOptionSurfaceCalculationMethodPerEquityDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionSurfaceCalculationMethodPerExchangeDefaults.class.getName(), equityOptionSurfaceCalculationMethodPerExchangeDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionSurfaceCalculationMethodPerCurrencyDefaults.class.getName(), equityOptionSurfaceCalculationMethodPerCurrencyDefaults));
    final EquityInstrumentDefaultValues.Builder equityOptionBlackSurfaceInterpolationDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .useInterpolationMethodNames()
        .useForwardCurveNames()
        .useForwardCurveCalculationMethodNames();
    final List<String> equityOptionPerEquityDefaults = Lists.newArrayList(PriorityClass.ABOVE_NORMAL.name());
    equityOptionPerEquityDefaults.addAll(equityOptionBlackSurfaceInterpolationDefaults.createPerEquityDefaults());
    final List<String> equityOptionPerExchangeDefaults = Lists.newArrayList(PriorityClass.NORMAL.name());
    equityOptionPerExchangeDefaults.addAll(equityOptionBlackSurfaceInterpolationDefaults.createPerExchangeDefaults());
    final List<String> equityOptionPerCurrencyDefaults = Lists.newArrayList(PriorityClass.BELOW_NORMAL.name());
    equityOptionPerCurrencyDefaults.addAll(equityOptionBlackSurfaceInterpolationDefaults.createPerCurrencyDefaults());
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionInterpolatedBlackLognormalPerEquityDefaults.class.getName(), equityOptionPerEquityDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionInterpolatedBlackLognormalPerExchangeDefaults.class.getName(), equityOptionPerExchangeDefaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionInterpolatedBlackLognormalPerCurrencyDefaults.class.getName(), equityOptionPerCurrencyDefaults));

  }

  protected void addEquityPureVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> defaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createPerEquityDefaults();
    functionConfigs.add(new ParameterizedFunctionConfiguration(PureBlackVolatilitySurfacePrimitiveDefaults.class.getName(), defaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(PureBlackVolatilitySurfaceSecurityDefaults.class.getName(), defaults));
  }

  protected void addEquityVarianceSwapDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityVarianceSwapStaticReplicationDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createPerEquityDefaults();
    final List<String> equityVarianceSwapStaticReplicationDefaultsWithPriority = new ArrayList<>();
    equityVarianceSwapStaticReplicationDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityVarianceSwapStaticReplicationDefaultsWithPriority.addAll(equityVarianceSwapStaticReplicationDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityVarianceSwapStaticReplicationDefaults.class.getName(), equityVarianceSwapStaticReplicationDefaultsWithPriority));
    final List<String> equityVarianceSwapDefaults = EquityInstrumentDefaultValues.builder()
        .useIdName()
        .useDiscountingCurveNames()
        .useForwardCurveNames()
        .useForwardCurveCalculationConfigNames()
        .useForwardCurveCalculationMethodNames()
        .useDiscountingCurveCurrency()
        .useVolatilitySurfaceNames()
        .createPerEquityDefaults();
    final List<String> equityVarianceSwapDefaultsWithPriority = new ArrayList<>();
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
  }

  protected RepositoryConfigurationSource getRepository(final SingletonFactoryBean<RepositoryConfigurationSource> defaults) {
    try {
      defaults.afterPropertiesSet();
    } catch (final Exception e) {
      s_logger.warn("Caught exception", e);
      return null;
    }
    return defaults.getObject();
  }

  protected void setBondFunctionDefaults(final CurrencyInfo i, final BondFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setRiskFreeCurveName(i.getCurveName("model/bond/riskFree"));
    defaults.setRiskFreeCurveCalculationConfig(i.getCurveConfiguration("model/bond/riskFree"));
    defaults.setCreditCurveName(i.getCurveName("model/bond/credit"));
    defaults.setCreditCurveCalculationConfig(i.getCurveConfiguration("model/bond/credit"));
  }

  protected void setBondFunctionDefaults(final BondFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, BondFunctions.Defaults.CurrencyInfo>() {
      @Override
      public BondFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final BondFunctions.Defaults.CurrencyInfo d = new BondFunctions.Defaults.CurrencyInfo();
        setBondFunctionDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource bondFunctions() {
    final BondFunctions.Defaults defaults = new BondFunctions.Defaults();
    setBondFunctionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setBondFutureOptionDefaults(final CurrencyInfo i, final BondFutureOptionFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfig(i.getCurveConfiguration("model/bondfutureoption"));
    defaults.setSurfaceName(i.getSurfaceName("model/bondfutureoption"));
  }

  protected void setBondFutureOptionDefaults(final BondFutureOptionFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, BondFutureOptionFunctions.Defaults.CurrencyInfo>() {
      @Override
      public BondFutureOptionFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final BondFutureOptionFunctions.Defaults.CurrencyInfo d = new BondFutureOptionFunctions.Defaults.CurrencyInfo();
        setBondFutureOptionDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource bondFutureOptionFunctions() {
    final BondFutureOptionFunctions.Defaults defaults = new BondFutureOptionFunctions.Defaults();
    setBondFutureOptionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setCDSFunctionDefaults(final CurrencyInfo i, final CreditFunctions.Defaults.CurrencyInfo defaults) {
    if ("USD".equals(i.getCurrency())) {
      defaults.setCurveCalculationConfig("ISDA");
      defaults.setCurveName("ISDA");
    }
  }

  protected void setCDSFunctionDefaults(final CreditFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, CreditFunctions.Defaults.CurrencyInfo>() {
      @Override
      public CreditFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final CreditFunctions.Defaults.CurrencyInfo d = new CreditFunctions.Defaults.CurrencyInfo();
        setCDSFunctionDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource cdsFunctions() {
    final CreditFunctions.Defaults defaults = new CreditFunctions.Defaults();
    setCDSFunctionDefaults(defaults);
    return getRepository(defaults);
  }

  protected RepositoryConfigurationSource deprecatedFunctions() {
    return null;
  }

  protected void setEquityOptionDefaults(final OptionFunctions.Defaults defaults) {
  }

  protected RepositoryConfigurationSource equityOptionFunctions() {
    final OptionFunctions.Defaults defaults = new OptionFunctions.Defaults();
    setEquityOptionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setExternalSensitivitesCalculators(final SensitivitiesFunctions.Calculators calculators) {
  }

  protected void setExternalSensitivitiesDefaults(final CurrencyInfo i, final SensitivitiesFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/sensitivities"));
  }

  protected void setExternalSensitivitiesDefaults(final SensitivitiesFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, SensitivitiesFunctions.Defaults.CurrencyInfo>() {
      @Override
      public SensitivitiesFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final SensitivitiesFunctions.Defaults.CurrencyInfo d = new SensitivitiesFunctions.Defaults.CurrencyInfo();
        setExternalSensitivitiesDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource externalSensitivitiesFunctions() {
    final SensitivitiesFunctions.Calculators calculators = new SensitivitiesFunctions.Calculators();
    setExternalSensitivitesCalculators(calculators);
    final SensitivitiesFunctions.Defaults defaults = new SensitivitiesFunctions.Defaults();
    setExternalSensitivitiesDefaults(defaults);
    return CombiningRepositoryConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  protected void setFixedIncomeDefaults(final CurrencyInfo i, final FixedIncomeFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveCalculationConfig(i.getCurveConfiguration("model/fixedincome"));
  }

  protected void setFixedIncomeDefaults(final FixedIncomeFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, FixedIncomeFunctions.Defaults.CurrencyInfo>() {
      @Override
      public FixedIncomeFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final FixedIncomeFunctions.Defaults.CurrencyInfo d = new FixedIncomeFunctions.Defaults.CurrencyInfo();
        setFixedIncomeDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource fixedIncomeFunctions() {
    final FixedIncomeFunctions.Defaults defaults = new FixedIncomeFunctions.Defaults();
    setFixedIncomeDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setForexDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/forex"));
    defaults.setDiscountingCurve(i.getCurveName("model/forex/discounting"));
  }

  protected void setForexDefaults(final CurrencyPairInfo i, final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo defaults) {
    defaults.setSurfaceName(i.getSurfaceName("model/forex"));
  }

  protected void setForexDefaults(final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setForexDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function1<CurrencyPairInfo, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo execute(final CurrencyPairInfo i) {
        final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo d =
            new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo();
        setForexDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource forexFunctions() {
    final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions defaults =
        new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions();
    setForexDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setForexOptionDefaults(final com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.Defaults defaults) {
  }

  protected RepositoryConfigurationSource forexOptionFunctions() {
    final com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.Defaults defaults = new com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.Defaults();
    setForexOptionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setForwardCurveDefaults(final CurrencyInfo i, final ForwardFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/curve/forward"));
    defaults.setDiscountingCurve(i.getCurveName("model/curve/forward/discounting"));
    defaults.setForwardCurve(i.getCurveName("model/curve/forward"));
  }

  protected void setForwardCurveDefaults(final CurrencyPairInfo i, final ForwardFunctions.Defaults.CurrencyPairInfo defaults) {
    defaults.setCurveName(i.getCurveName("model/curve/forward"));
  }

  protected void setForwardCurveDefaults(final ForwardFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, ForwardFunctions.Defaults.CurrencyInfo>() {
      @Override
      public ForwardFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final ForwardFunctions.Defaults.CurrencyInfo d = new ForwardFunctions.Defaults.CurrencyInfo();
        setForwardCurveDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function1<CurrencyPairInfo, ForwardFunctions.Defaults.CurrencyPairInfo>() {
      @Override
      public ForwardFunctions.Defaults.CurrencyPairInfo execute(final CurrencyPairInfo i) {
        final ForwardFunctions.Defaults.CurrencyPairInfo d = new ForwardFunctions.Defaults.CurrencyPairInfo();
        setForwardCurveDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource forwardCurveFunctions() {
    final ForwardFunctions.Defaults defaults = new ForwardFunctions.Defaults();
    setForwardCurveDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setFutureDefaults(final CurrencyInfo i, final FutureFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/future"));
  }

  protected void setFutureDefaults(final FutureFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, FutureFunctions.Defaults.CurrencyInfo>() {
      @Override
      public FutureFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final FutureFunctions.Defaults.CurrencyInfo d = new FutureFunctions.Defaults.CurrencyInfo();
        setFutureDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource futureFunctions() {
    final FutureFunctions.Defaults defaults = new FutureFunctions.Defaults();
    setFutureDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setFutureOptionDefaults(final CurrencyInfo i, final FutureOptionFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveName(i.getCurveName("model/futureoption"));
    defaults.setCurveCalculationConfig(i.getCurveConfiguration("model/futureoption"));
    defaults.setSurfaceName(i.getSurfaceName("model/futureoption"));
    defaults.setForwardCurveName(i.getForwardCurveName("model/futureoption"));
    defaults.setForwardCurveCalculationMethodName(i.getForwardCurveCalculationMethod("model/futureoption"));
    defaults.setSurfaceCalculationMethod(i.getSurfaceCalculationMethod("model/futureoption"));
  }

  protected void setFutureOptionDefaults(final FutureOptionFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, FutureOptionFunctions.Defaults.CurrencyInfo>() {
      @Override
      public FutureOptionFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final FutureOptionFunctions.Defaults.CurrencyInfo d = new FutureOptionFunctions.Defaults.CurrencyInfo();
        setFutureOptionDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource futureOptionFunctions() {
    final FutureOptionFunctions.Defaults defaults = new FutureOptionFunctions.Defaults();
    setFutureOptionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setInterestRateDefaults(final InterestRateFunctions.Defaults defaults) {
    defaults.setApplicableCurrencies(getPerCurrencyInfo().keySet());
  }

  protected RepositoryConfigurationSource interestRateFunctions() {
    final InterestRateFunctions.Defaults defaults = new InterestRateFunctions.Defaults();
    setInterestRateDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setIRFutureOptionDefaults(final CurrencyInfo i, final IRFutureOptionFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/irfutureoption"));
    defaults.setSurfaceName(i.getSurfaceName("model/irfutureoption"));
  }

  protected void setIRFutureOptionDefaults(final IRFutureOptionFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, IRFutureOptionFunctions.Defaults.CurrencyInfo>() {
      @Override
      public IRFutureOptionFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final IRFutureOptionFunctions.Defaults.CurrencyInfo d = new IRFutureOptionFunctions.Defaults.CurrencyInfo();
        setIRFutureOptionDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource irFutureOptionFunctions() {
    final IRFutureOptionFunctions.Defaults defaults = new IRFutureOptionFunctions.Defaults();
    setIRFutureOptionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setLocalVolatilityDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/volatility/local"));
    defaults.setDiscountingCurve(i.getCurveName("model/volatility/local/discounting"));
  }

  protected void setLocalVolatilityDefaults(final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setLocalVolatilityDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource localVolatilityFunctions() {
    final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions defaults =
        new com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions();
    setLocalVolatilityDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setPNLFunctionCalculators(final PNLFunctions.Calculators calculators) {
    calculators.setCostOfCarryField(getCostOfCarryField());
    calculators.setMark2MarketField(getMark2MarketField());
  }

  protected void setPNLFunctionDefaults(final CurrencyInfo i, final PNLFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/pnl"));
    defaults.setDiscountingCurve(i.getCurveName("model/pnl/discounting"));
    defaults.setSurfaceName(i.getSurfaceName("model/pnl"));
  }

  protected void setPNLFunctionDefaults(final CurrencyPairInfo i, final PNLFunctions.Defaults.CurrencyPairInfo defaults) {
    defaults.setSurfaceName(i.getSurfaceName("model/pnl"));
  }

  protected void setPNLFunctionDefaults(final PNLFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, PNLFunctions.Defaults.CurrencyInfo>() {
      @Override
      public PNLFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final PNLFunctions.Defaults.CurrencyInfo d = new PNLFunctions.Defaults.CurrencyInfo();
        setPNLFunctionDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function1<CurrencyPairInfo, PNLFunctions.Defaults.CurrencyPairInfo>() {
      @Override
      public PNLFunctions.Defaults.CurrencyPairInfo execute(final CurrencyPairInfo i) {
        final PNLFunctions.Defaults.CurrencyPairInfo d = new PNLFunctions.Defaults.CurrencyPairInfo();
        setPNLFunctionDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource pnlFunctionDefaults() {
    final PNLFunctions.Defaults defaults = new PNLFunctions.Defaults();
    setPNLFunctionDefaults(defaults);
    return getRepository(defaults);
  }

  protected RepositoryConfigurationSource pnlFunctions() {
    final PNLFunctions.Calculators calculators = new PNLFunctions.Calculators();
    setPNLFunctionCalculators(calculators);
    final PNLFunctions.Defaults defaults = new PNLFunctions.Defaults();
    setPNLFunctionDefaults(defaults);
    return CombiningRepositoryConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  protected void setPortfolioTheoryCalculators(final PortfolioTheoryFunctions.Calculators calculators) {
  }

  protected void setPortfolioTheoryDefaults(final PortfolioTheoryFunctions.Defaults defaults) {
  }

  protected RepositoryConfigurationSource portfolioTheoryFunctions() {
    final PortfolioTheoryFunctions.Calculators calculators = new PortfolioTheoryFunctions.Calculators();
    setPortfolioTheoryCalculators(calculators);
    final PortfolioTheoryFunctions.Defaults defaults = new PortfolioTheoryFunctions.Defaults();
    setPortfolioTheoryDefaults(defaults);
    return CombiningRepositoryConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  protected void setSABRCubeDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/sabrcube"));
    defaults.setCubeName(i.getCubeName("model/sabrcube"));
  }

  protected void setSABRCubeDefaults(final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setSABRCubeDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource sabrCubeFunctions() {
    final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions defaults =
        new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions();
    setSABRCubeDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setSwaptionDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfig(i.getCurveConfiguration("model/swaption/black"));
    defaults.setSurfaceName(i.getSurfaceName("model/swaption/black"));
  }

  protected void setSwaptionDefaults(final com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo();
        setSwaptionDefaults(i, d);
        return d;
      }
    }));
  }

  protected RepositoryConfigurationSource swaptionFunctions() {
    final com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults defaults = new com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults();
    setSwaptionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setVaRDefaults(final VaRFunctions.Defaults defaults) {
  }

  protected RepositoryConfigurationSource varFunctions() {
    final VaRFunctions.Defaults defaults = new VaRFunctions.Defaults();
    setVaRDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setVolatilitySurfaceDefaults(final com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.Defaults defaults) {
  }

  protected void setVolatilitySurfaceDefaults(final com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.Defaults defaults) {
  }

  protected RepositoryConfigurationSource volatilitySurfaceFunctions() {
    final com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.Defaults d1 = new com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.Defaults();
    setVolatilitySurfaceDefaults(d1);
    final com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.Defaults d2 = new com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.Defaults();
    setVolatilitySurfaceDefaults(d2);
    return CombiningRepositoryConfigurationSource.of(getRepository(d1), getRepository(d2));
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return CombiningRepositoryConfigurationSource.of(super.createObject(), bondFunctions(), bondFutureOptionFunctions(), cdsFunctions(), deprecatedFunctions(), equityOptionFunctions(),
        externalSensitivitiesFunctions(), fixedIncomeFunctions(), forexFunctions(), forexOptionFunctions(), forwardCurveFunctions(), futureFunctions(), futureOptionFunctions(),
        interestRateFunctions(), irFutureOptionFunctions(), localVolatilityFunctions(), pnlFunctions(), portfolioTheoryFunctions(), sabrCubeFunctions(), swaptionFunctions(), varFunctions(),
        volatilitySurfaceFunctions());
  }

}
