/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.CurrencyPairsDefaults;
import com.opengamma.financial.analytics.model.bond.BondFunctions;
import com.opengamma.financial.analytics.model.curve.CurveFunctions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateFunctions;
import com.opengamma.financial.analytics.model.equity.option.OptionFunctions;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioTheoryFunctions;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunctions;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.LocalVolatilitySurfaceDefaults;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.function.Function;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.web.spring.defaults.GeneralLocalVolatilitySurfaceDefaults;

/**
 * Constructs a standard function repository.
 * <p>
 * A sub-class should provide installation specific details relating to the data providers used.
 */
@SuppressWarnings("deprecation")
public abstract class StandardFunctionConfiguration extends AbstractFunctionConfigurationBean {

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
    /** The currency string */
    private final String _currency;
    /** Usually the default value of the {@link ValuePropertyNames#CURVE_CONSTRUCTION_CONFIG} property */
    private final Value _curveConfiguration = new Value();
    /** Usually the default value of the {@link ValuePropertyNames#CURVE} property */
    private final Value _curveName = new Value();
    /** Usually the default value of the {@link ValuePropertyNames#CURVE_CALCULATION_METHOD} property */
    private final Value _curveCalculationMethodName = new Value();
    /** Usually the default value of the {@link ValuePropertyNames#SURFACE} property */
    private final Value _surfaceName = new Value();
    /** Usually the default value of the {@link ValuePropertyNames#CUBE} property */
    private final Value _cubeName = new Value();
    /** The forward curve name */
    private final Value _forwardCurveName = new Value();
    /** The forward curve calculation method */
    private final Value _forwardCurveCalculationMethod = new Value();
    /** The surface calculation method */
    private final Value _surfaceCalculationMethod = new Value();
    /** Usually the default value of the {@link SurfaceAndCubePropertyNames#PROPERTY_CUBE_DEFINITION} property */
    private final Value _cubeDefinitionName = new Value();
    /** Usually the default value of the {@link SurfaceAndCubePropertyNames#PROPERTY_CUBE_SPECIIFICATION} property */
    private final Value _cubeSpecificationName = new Value();
    /** Usually the default value of the {@link SurfaceAndCubePropertyNames#PROPERTY_SURFACE_DEFINITION} property */
    private final Value _surfaceDefinitionName = new Value();
    /** Usually the default value of the {@link SurfaceAndCubePropertyNames#PROPERTY_SURFACE_SPECIIFICATION} property */
    private final Value _surfaceSpecificationName = new Value();

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

    public void setCurveCalculationMethodName(final String key, final String curveCalculationMethodName) {
      _curveCalculationMethodName.set(key, curveCalculationMethodName);
    }

    public String getCurveCalculationMethodName(final String key) {
      return _curveCalculationMethodName.get(key);
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

    /**
     * Gets the cube definition name for a key.
     * @param key The key
     * @return The cube definition name
     */
    public String getCubeDefinitionName(final String key) {
      return _cubeDefinitionName.get(key);
    }

    /**
     * Sets a cube definition name for a key.
     * @param key The key
     * @param cubeDefinitionName The cube definition name
     */
    public void setCubeDefinitionName(final String key, final String cubeDefinitionName) {
      _cubeDefinitionName.set(key, cubeDefinitionName);
    }

    /**
     * Gets the cube specification name for a key.
     * @param key The key
     * @return The cube specification name
     */
    public String getCubeSpecificationName(final String key) {
      return _cubeSpecificationName.get(key);
    }

    /**
     * Sets a cube specification name for a key.
     * @param key The key
     * @param cubeSpecificationName The cube specification name
     */
    public void setCubeSpecificationName(final String key, final String cubeSpecificationName) {
      _cubeSpecificationName.set(key, cubeSpecificationName);
    }

    /**
     * Gets the surface definition name for a key.
     * @param key The key
     * @return The surface definition name
     */
    public String getSurfaceDefinitionName(final String key) {
      return _surfaceDefinitionName.get(key);
    }

    /**
     * Sets a surface definition name for a key.
     * @param key The key
     * @param surfaceDefinitionName The surface definition name
     */
    public void setSurfaceDefinitionName(final String key, final String surfaceDefinitionName) {
      _surfaceDefinitionName.set(key, surfaceDefinitionName);
    }

    /**
     * Gets the surface specification name for a key.
     * @param key The key
     * @return The surface specification name
     */
    public String getSurfaceSpecificationName(final String key) {
      return _surfaceSpecificationName.get(key);
    }

    /**
     * Sets a surface specification name for a key.
     * @param key The key
     * @param surfaceSpecificationName The surface specification name
     */
    public void setSurfaceSpecificationName(final String key, final String surfaceSpecificationName) {
      _surfaceSpecificationName.set(key, surfaceSpecificationName);
    }
  }

  /**
   * Constants for a particular currency pair
   */
  public static class CurrencyPairInfo {

    private final Pair<String, String> _currencies;

    private final Value _curveName = new Value();
    private final Value _curveCalculationMethod = new Value();
    private final Value _surfaceName = new Value();
    private final Value _forwardCurveName = new Value();

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

    public void setCurveCalculationMethod(final String key, final String curveCalculationMethod) {
      _curveCalculationMethod.set(key, curveCalculationMethod);
    }

    public String getCurveCalculationMethod(final String key) {
      return _curveCalculationMethod.get(key);
    }

    public void setSurfaceName(final String key, final String surfaceName) {
      _surfaceName.set(key, surfaceName);
    }

    public String getSurfaceName(final String key) {
      return _surfaceName.get(key);
    }

    public String getForwardCurveName(final String key) {
      return _forwardCurveName.get(key);
    }

    public void setForwardCurveName(final String key, final String forwardCurveName) {
      _forwardCurveName.set(key, forwardCurveName);
    }
  }

  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
  private final Map<Pair<String, String>, CurrencyPairInfo> _perCurrencyPairInfo = new HashMap<>();
  private String _mark2MarketField;
  private String _costOfCarryField;
  private double _absoluteTolerance;
  private double _relativeTolerance;
  private int _maxIterations;

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

  protected <T> Map<String, T> getCurrencyInfo(final Function<CurrencyInfo, T> filter) {
    final Map<String, T> result = new HashMap<String, T>();
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      final T entry = filter.apply(e.getValue());
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

  protected <T> Map<Pair<String, String>, T> getCurrencyPairInfo(final Function<CurrencyPairInfo, T> filter) {
    final Map<Pair<String, String>, T> result = new HashMap<Pair<String, String>, T>();
    for (final Map.Entry<Pair<String, String>, CurrencyPairInfo> e : getPerCurrencyPairInfo().entrySet()) {
      final T entry = filter.apply(e.getValue());
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

  /**
   * Sets the absolute tolerance for the curve root-finder.
   * @param absoluteTolerance The absolute tolerance, greater than zero
   */
  public void setAbsoluteTolerance(final double absoluteTolerance) {
    _absoluteTolerance = absoluteTolerance;
  }

  /**
   * Gets the absolute tolerance for the curve root-finder.
   * @return The absolute tolerance
   */
  public double getAbsoluteTolerance() {
    return _absoluteTolerance;
  }

  /**
   * Sets the relative tolerance for the curve root-finder.
   * @param relativeTolerance The relative tolerance, greater than zero
   */
  public void setRelativeTolerance(final double relativeTolerance) {
    _relativeTolerance = relativeTolerance;
  }

  /**
   * Gets the relative tolerance for the curve root-finder.
   * @return The relative tolerance
   */
  public double getRelativeTolerance() {
    return _relativeTolerance;
  }

  /**
   * Sets the maximum number of iterations for the curve root-finder.
   * @param maxIterations The maximum iterations, greater than zero
   */
  public void setMaximumIterations(final int maxIterations) {
    _maxIterations = maxIterations;
  }

  /**
   * Gets the maximum number of iterations for the curve root-finder.
   * @return The maximum iterations
   */
  public int getMaximumIterations() {
    return _maxIterations;
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

  protected CurrencyInfo plnCurrencyInfo() {
    return defaultCurrencyInfo("PLN");
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
    setCurrencyInfo("PLN", plnCurrencyInfo());
    setCurrencyInfo("RUB", rubCurrencyInfo());
    setCurrencyInfo("SEK", sekCurrencyInfo());
    setCurrencyInfo("SGD", sgdCurrencyInfo());
    setCurrencyInfo("TRY", tryCurrencyInfo());
    setCurrencyInfo("TWD", twdCurrencyInfo());
    setCurrencyInfo("USD", usdCurrencyInfo());
    setCurrencyInfo("ZAR", zarCurrencyInfo());
  }

  protected CurrencyPairInfo defaultCurrencyPairInfo(final String c1, final String c2) {
    return new CurrencyPairInfo(Pairs.of(c1, c2));
  }

  protected CurrencyPairInfo audKrwCurrencyPairInfo() {
    return defaultCurrencyPairInfo("AUD", "KRW");
  }

  protected CurrencyPairInfo chfJpyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("CHF", "JPY");
  }

  protected CurrencyPairInfo eurBrlCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "BRL");
  }

  protected CurrencyPairInfo eurChfCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "CHF");
  }

  protected CurrencyPairInfo eurGbpCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "GBP");
  }

  protected CurrencyPairInfo eurJpyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "JPY");
  }

  protected CurrencyPairInfo eurTryCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "TRY");
  }

  protected CurrencyPairInfo jpyKrwCurrencyPairInfo() {
    return defaultCurrencyPairInfo("JPY", "KRW");
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
    setCurrencyPairInfo(Pairs.of("AUD", "KRW"), audKrwCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("CHF", "JPY"), chfJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("EUR", "BRL"), eurBrlCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("EUR", "CHF"), eurChfCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("EUR", "GBP"), eurGbpCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("EUR", "JPY"), eurJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("EUR", "TRY"), eurTryCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("JPY", "KRW"), jpyKrwCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("SEK", "JPY"), sekJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "AUD"), usdAudCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "BRL"), usdBrlCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "CAD"), usdCadCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "CHF"), usdChfCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "CNY"), usdCnyCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "EUR"), usdEurCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "GBP"), usdGbpCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "HKD"), usdHkdCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "HUF"), usdHufCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "INR"), usdInrCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "JPY"), usdJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "KRW"), usdKrwCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "MXN"), usdMxnCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "NOK"), usdNokCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "NZD"), usdNzdCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "SGD"), usdSgdCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "ZAR"), usdZarCurrencyPairInfo());
  }

  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(CurrencyPairsDefaults.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS));
  }

  protected void addLocalVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(LocalVolatilitySurfaceDefaults.class.getName(),
        GeneralLocalVolatilitySurfaceDefaults.getLocalVolatilitySurfaceDefaults()));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    addCurrencyConversionFunctions(functions);
    addLocalVolatilitySurfaceDefaults(functions);
  }

  protected FunctionConfigurationSource getRepository(final SingletonFactoryBean<FunctionConfigurationSource> defaults) {
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
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, BondFunctions.Defaults.CurrencyInfo>() {
      @Override
      public BondFunctions.Defaults.CurrencyInfo apply(final CurrencyInfo i) {
        final BondFunctions.Defaults.CurrencyInfo d = new BondFunctions.Defaults.CurrencyInfo();
        setBondFunctionDefaults(i, d);
        return d;
      }
    }));
  }

  protected FunctionConfigurationSource bondFunctions() {
    final BondFunctions.Defaults defaults = new BondFunctions.Defaults();
    setBondFunctionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setXCcySwapFunctionDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveCalculationConfig(i.getCurveName("model/xccyswap"));
  }

  protected void setXCcySwapFunctionDefaults(final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo apply(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo();
        setXCcySwapFunctionDefaults(i, d);
        return d;
      }
    }));
  }

  protected FunctionConfigurationSource xCcySwapFunctions() {
    final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults defaults =
        new com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults();
    setXCcySwapFunctionDefaults(defaults);
    return getRepository(defaults);
  }

  protected FunctionConfigurationSource deprecatedFunctions() {
    return null;
  }

  protected void setEquityOptionDefaults(final OptionFunctions.Defaults defaults) {
  }

  protected FunctionConfigurationSource equityOptionFunctions() {
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
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, SensitivitiesFunctions.Defaults.CurrencyInfo>() {
      @Override
      public SensitivitiesFunctions.Defaults.CurrencyInfo apply(final CurrencyInfo i) {
        final SensitivitiesFunctions.Defaults.CurrencyInfo d = new SensitivitiesFunctions.Defaults.CurrencyInfo();
        setExternalSensitivitiesDefaults(i, d);
        return d;
      }
    }));
  }

  protected FunctionConfigurationSource externalSensitivitiesFunctions() {
    final SensitivitiesFunctions.Calculators calculators = new SensitivitiesFunctions.Calculators();
    setExternalSensitivitesCalculators(calculators);
    final SensitivitiesFunctions.Defaults defaults = new SensitivitiesFunctions.Defaults();
    setExternalSensitivitiesDefaults(defaults);
    return CombiningFunctionConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  protected void setFixedIncomeDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveCalculationConfig(i.getCurveConfiguration("model/fixedincome"));
  }

  protected void setFixedIncomeDefaults(final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo apply(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo();
        setFixedIncomeDefaults(i, d);
        return d;
      }
    }));
  }

  protected FunctionConfigurationSource fixedIncomeFunctions() {
    final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults defaults =
        new com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults();
    setFixedIncomeDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setForexDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/forex"));
    defaults.setDiscountingCurve(i.getCurveName("model/forex/discounting"));
  }

  protected void setForexDefaults(final CurrencyPairInfo i, final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo defaults) {
    defaults.setSurfaceName(i.getSurfaceName("model/forex"));
    defaults.setForwardCurveName(i.getForwardCurveName("model/forex/forward"));
  }

  protected void setForexDefaults(final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo apply(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setForexDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function<CurrencyPairInfo, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo apply(final CurrencyPairInfo i) {
        final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo d =
            new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo();
        setForexDefaults(i, d);
        return d;
      }
    }));
  }

  protected FunctionConfigurationSource forexFunctions() {
    final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions defaults =
        new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions();
    setForexDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setForexOptionDefaults(final com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.Defaults defaults) {
  }

  protected FunctionConfigurationSource forexOptionFunctions() {
    final com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.Defaults defaults = new com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.Defaults();
    setForexOptionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setForexDigitalDefaults(final com.opengamma.financial.analytics.model.forex.option.callspreadblack.CallSpreadBlackFunctions.Defaults defaults) {
  }

  protected FunctionConfigurationSource forexDigitalFunctions() {
    final com.opengamma.financial.analytics.model.forex.option.callspreadblack.CallSpreadBlackFunctions.Defaults defaults =
        new com.opengamma.financial.analytics.model.forex.option.callspreadblack.CallSpreadBlackFunctions.Defaults();
    setForexDigitalDefaults(defaults);
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
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, ForwardFunctions.Defaults.CurrencyInfo>() {
      @Override
      public ForwardFunctions.Defaults.CurrencyInfo apply(final CurrencyInfo i) {
        final ForwardFunctions.Defaults.CurrencyInfo d = new ForwardFunctions.Defaults.CurrencyInfo();
        setForwardCurveDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function<CurrencyPairInfo, ForwardFunctions.Defaults.CurrencyPairInfo>() {
      @Override
      public ForwardFunctions.Defaults.CurrencyPairInfo apply(final CurrencyPairInfo i) {
        final ForwardFunctions.Defaults.CurrencyPairInfo d = new ForwardFunctions.Defaults.CurrencyPairInfo();
        setForwardCurveDefaults(i, d);
        return d;
      }
    }));
  }

  protected FunctionConfigurationSource forwardCurveFunctions() {
    final ForwardFunctions.Defaults defaults = new ForwardFunctions.Defaults();
    setForwardCurveDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setFutureDefaults(final CurrencyInfo i, final FutureFunctions.Deprecated.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/future"));
  }

  protected void setFutureDefaults(final FutureFunctions.Deprecated defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, FutureFunctions.Deprecated.CurrencyInfo>() {
      @Override
      public FutureFunctions.Deprecated.CurrencyInfo apply(final CurrencyInfo i) {
        final FutureFunctions.Deprecated.CurrencyInfo d = new FutureFunctions.Deprecated.CurrencyInfo();
        setFutureDefaults(i, d);
        return d;
      }
    }));
  }

  protected void setFutureFunctionCalculators(final FutureFunctions.Calculators calculators) {
    calculators.setClosingPriceField(getMark2MarketField());
  }

  protected FunctionConfigurationSource futureFunctions() {
    final FutureFunctions.Calculators calculators = new FutureFunctions.Calculators();
    setFutureFunctionCalculators(calculators);
    final FutureFunctions.Deprecated defaults = new FutureFunctions.Deprecated();
    setFutureDefaults(defaults);
    return CombiningFunctionConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  protected void setFutureOptionDefaults(final CurrencyInfo i, final FutureOptionFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveName(i.getCurveName("model/futureoption"));
    defaults.setCurveCalculationConfig(i.getCurveConfiguration("model/futureoption"));
    defaults.setSurfaceName(i.getSurfaceName("model/futureoption"));
    defaults.setForwardCurveName(i.getForwardCurveName("model/futureoption"));
    String v = i.getForwardCurveCalculationMethod("model/futureoption");
    if (v != null) {
      defaults.setForwardCurveCalculationMethodName(v);
    }
    v = i.getSurfaceCalculationMethod("model/futureoption");
    if (v != null) {
      defaults.setSurfaceCalculationMethod(v);
    }
  }

  protected void setFutureOptionDefaults(final FutureOptionFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, FutureOptionFunctions.Defaults.CurrencyInfo>() {
      @Override
      public FutureOptionFunctions.Defaults.CurrencyInfo apply(final CurrencyInfo i) {
        final FutureOptionFunctions.Defaults.CurrencyInfo d = new FutureOptionFunctions.Defaults.CurrencyInfo();
        setFutureOptionDefaults(i, d);
        return d;
      }
    }));
  }

  protected FunctionConfigurationSource futureOptionFunctions() {
    final FutureOptionFunctions.Defaults defaults = new FutureOptionFunctions.Defaults();
    setFutureOptionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setInterestRateDefaults(final InterestRateFunctions.Defaults defaults) {
    defaults.setApplicableCurrencies(getPerCurrencyInfo().keySet());
  }

  protected FunctionConfigurationSource interestRateFunctions() {
    final InterestRateFunctions.Defaults defaults = new InterestRateFunctions.Defaults();
    setInterestRateDefaults(defaults);
    return getRepository(defaults);
  }

  protected FunctionConfigurationSource curveFunctions() {
    final CurveFunctions.Defaults defaults = new CurveFunctions.Defaults();
    setCurveDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setCurveDefaults(final CurveFunctions.Defaults defaults) {
    defaults.setAbsoluteTolerance(_absoluteTolerance);
    defaults.setRelativeTolerance(_relativeTolerance);
    defaults.setMaximumIterations(_maxIterations);
  }

  protected void setMultiCurvePricingDefaults(final MultiCurvePricingFunctions.Defaults defaults) {
    defaults.setAbsoluteTolerance(_absoluteTolerance);
    defaults.setRelativeTolerance(_relativeTolerance);
    defaults.setMaximumIterations(_maxIterations);
  }

  protected void setLocalVolatilityDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/volatility/local"));
    defaults.setDiscountingCurve(i.getCurveName("model/volatility/local/discounting"));
  }

  protected void setLocalVolatilityDefaults(final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo apply(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setLocalVolatilityDefaults(i, d);
        return d;
      }
    }));
  }

  protected FunctionConfigurationSource localVolatilityFunctions() {
    final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions defaults =
        new com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions();
    setLocalVolatilityDefaults(defaults);
    return getRepository(defaults);
  }

  protected FunctionConfigurationSource multicurvePricingFunctions() {
    final MultiCurvePricingFunctions.Defaults defaults = new MultiCurvePricingFunctions.Defaults();
    setMultiCurvePricingDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setPNLFunctionCalculators(final PNLFunctions.Calculators calculators) {
    calculators.setCostOfCarryField(getCostOfCarryField());
    calculators.setMark2MarketField(getMark2MarketField());
  }

  protected void setPNLFunctionDefaults(final CurrencyInfo i, final PNLFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/pnl"));
    defaults.setDiscountingCurve(i.getCurveName("model/pnl/discounting"));
  }

  protected void setPNLFunctionDefaults(final CurrencyPairInfo i, final PNLFunctions.Defaults.CurrencyPairInfo defaults) {
    defaults.setSurfaceName(i.getSurfaceName("model/pnl"));
  }

  protected void setPNLFunctionDefaults(final PNLFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, PNLFunctions.Defaults.CurrencyInfo>() {
      @Override
      public PNLFunctions.Defaults.CurrencyInfo apply(final CurrencyInfo i) {
        final PNLFunctions.Defaults.CurrencyInfo d = new PNLFunctions.Defaults.CurrencyInfo();
        setPNLFunctionDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function<CurrencyPairInfo, PNLFunctions.Defaults.CurrencyPairInfo>() {
      @Override
      public PNLFunctions.Defaults.CurrencyPairInfo apply(final CurrencyPairInfo i) {
        final PNLFunctions.Defaults.CurrencyPairInfo d = new PNLFunctions.Defaults.CurrencyPairInfo();
        setPNLFunctionDefaults(i, d);
        return d;
      }
    }));
  }

  protected FunctionConfigurationSource pnlFunctions() {
    final PNLFunctions.Calculators calculators = new PNLFunctions.Calculators();
    setPNLFunctionCalculators(calculators);
    final PNLFunctions.Defaults defaults = new PNLFunctions.Defaults();
    setPNLFunctionDefaults(defaults);
    return CombiningFunctionConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  protected void setPortfolioTheoryCalculators(final PortfolioTheoryFunctions.Calculators calculators) {
  }

  protected void setPortfolioTheoryDefaults(final PortfolioTheoryFunctions.Defaults defaults) {
  }

  protected FunctionConfigurationSource portfolioTheoryFunctions() {
    final PortfolioTheoryFunctions.Calculators calculators = new PortfolioTheoryFunctions.Calculators();
    setPortfolioTheoryCalculators(calculators);
    final PortfolioTheoryFunctions.Defaults defaults = new PortfolioTheoryFunctions.Defaults();
    setPortfolioTheoryDefaults(defaults);
    return CombiningFunctionConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  protected void setSABRCubeDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/sabrcube"));
    defaults.setCubeDefinitionName(i.getCubeDefinitionName("model/sabrcube"));
    defaults.setCubeSpecificationName(i.getCubeSpecificationName("model/sabrcube"));
    defaults.setSurfaceDefinitionName(i.getSurfaceDefinitionName("model/sabrcube"));
    defaults.setSurfaceSpecificationName(i.getSurfaceSpecificationName("model/sabrcube"));
  }

  protected void setSABRCubeDefaults(final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo apply(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setSABRCubeDefaults(i, d);
        return d;
      }
    }));
    @SuppressWarnings("unused")
    final Object temp = getCurrencyInfo(new Function<CurrencyInfo, com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo apply(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setSABRCubeDefaults(i, d);
        return d;
      }
    });
  }

  protected FunctionConfigurationSource sabrCubeFunctions() {
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
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo apply(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo();
        setSwaptionDefaults(i, d);
        return d;
      }
    }));
  }

  protected FunctionConfigurationSource swaptionFunctions() {
    final com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults defaults = new com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults();
    setSwaptionDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setVaRDefaults(final VaRFunctions.Defaults defaults) {
  }

  protected FunctionConfigurationSource varFunctions() {
    final VaRFunctions.Defaults defaults = new VaRFunctions.Defaults();
    setVaRDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setVolatilitySurfaceDefaults(final com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.Defaults defaults) {
  }

  protected void setVolatilitySurfaceBlackDefaults(final CurrencyInfo i,
      final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveName(i.getForwardCurveName("model/volatility/surface/black"));
    final String v = i.getForwardCurveCalculationMethod("model/volatility/surface/black");
    if (v != null) {
      defaults.setCurveCalculationMethod(v);
    }
    defaults.setSurfaceName(i.getSurfaceName("model/volatility/surface/black"));
  }

  protected void setVolatilitySurfaceBlackDefaults(final CurrencyPairInfo i,
      final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo defaults) {
    defaults.setCurveName(i.getCurveName("model/volatility/surface/black"));
    final String v = i.getCurveCalculationMethod("model/volatility/surface/black");
    if (v != null) {
      defaults.setCurveCalculationMethod(v);
    }
    defaults.setSurfaceName(i.getSurfaceName("model/volatility/surface/black"));
  }

  protected void setVolatilitySurfaceBlackDefaults(final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults
    .setPerCurrencyInfo(getCurrencyInfo(new Function<CurrencyInfo, com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo apply(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setVolatilitySurfaceBlackDefaults(i, d);
        return d;
      }
    }));
    defaults
    .setPerCurrencyPairInfo(getCurrencyPairInfo(new Function<CurrencyPairInfo,
        com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo apply(final CurrencyPairInfo i) {
        final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo d =
            new com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo();
        setVolatilitySurfaceBlackDefaults(i, d);
        return d;
      }
    }));
  }

  protected void setVolatilitySurfaceDefaults(final com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.Defaults defaults) {
  }

  protected FunctionConfigurationSource volatilitySurfaceFunctions() {
    final com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.Defaults d1 = new com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.Defaults();
    setVolatilitySurfaceDefaults(d1);
    final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions d2 =
        new com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions();
    setVolatilitySurfaceBlackDefaults(d2);
    final com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.Defaults d3 = new com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.Defaults();
    setVolatilitySurfaceDefaults(d3);
    return CombiningFunctionConfigurationSource.of(getRepository(d1), getRepository(d2), getRepository(d3));
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), bondFunctions(), // bondFutureOptionFunctions(), 
        forexDigitalFunctions(),
        deprecatedFunctions(), equityOptionFunctions(), externalSensitivitiesFunctions(), fixedIncomeFunctions(), forexFunctions(), forexOptionFunctions(),
        forwardCurveFunctions(), futureFunctions(), futureOptionFunctions(), interestRateFunctions(), // irFutureOptionFunctions(),
        localVolatilityFunctions(), pnlFunctions(), portfolioTheoryFunctions(), sabrCubeFunctions(), swaptionFunctions(), varFunctions(),
        volatilitySurfaceFunctions(), xCcySwapFunctions());
  }

}
