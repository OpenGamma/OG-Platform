/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;

/**
 * 
 */
public abstract class EquityInstrumentDefaultValues {
  private static final String SURFACE_INTERPOLATOR = "Spline";
  private static final String SURFACE_NAME = "BBG";
  private static final String FUTURES_PRICE_CURVE_NAME = "BBG";
  private static final String DISCOUNTING_CURVE_NAME = "OIS";
  private static final String DIVIDEND_DISCRETE = "Discrete";
  private static final String DIVIDEND_CONTINUOUS = "Continuous";
  private static final Map<String, String> EQUITY_NAMES = new HashMap<>();
  private static final Map<String, String> EXCHANGE_NAMES = new HashMap<>();
  private static final Map<String, String> DISCOUNTING_CURVE_NAMES = new HashMap<>();
  private static final Map<String, String> FORWARD_CURVE_NAMES = new HashMap<>();
  private static final Map<String, String> DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES = new HashMap<>();
  private static final Map<String, String> FORWARD_CURVE_CALCULATION_CONFIG_NAMES = new HashMap<>();
  private static final Map<String, String> FORWARD_CURVE_CALCULATION_METHOD_NAMES = new HashMap<>();
  private static final Map<String, String> VOLATILITY_SURFACE_NAMES = new HashMap<>();
  private static final Map<String, String> EX_VOLATILITY_SURFACE_NAMES = new HashMap<>();
  private static final Map<String, String> CCY_VOLATILITY_SURFACE_NAMES = new HashMap<>();
  private static final Map<String, String> INTERPOLATOR_NAMES = new HashMap<>();
  private static final Map<String, String> EX_INTERPOLATOR_NAMES = new HashMap<>();
  private static final Map<String, String> CCY_INTERPOLATOR_NAMES = new HashMap<>();
  private static final Map<String, String> VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES = new HashMap<>();
  private static final Map<String, String> EX_VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES = new HashMap<>();
  private static final Map<String, String> CCY_VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES = new HashMap<>();
  private static final Map<String, String> DIVIDEND_TYPES = new HashMap<>();
  private static final Map<String, String> CCY_DIVIDEND_TYPES = new HashMap<>();

  static {
    EQUITY_NAMES.put("DJX", "USD");
    EQUITY_NAMES.put("SPX", "USD");
    EQUITY_NAMES.put("SPXQ", "USD");
    EQUITY_NAMES.put("NDX", "USD");
    EQUITY_NAMES.put("RUY", "USD");
    EQUITY_NAMES.put("VIX", "USD");
    EQUITY_NAMES.put("NKY", "JPY");

    EQUITY_NAMES.put("IBM", "USD");
    EQUITY_NAMES.put("RBS", "USD");
    EQUITY_NAMES.put("LLOY", "GBP");
    EQUITY_NAMES.put("JPM", "USD");
    EQUITY_NAMES.put("SPY", "USD");

    EXCHANGE_NAMES.put("US", "USD");
    DISCOUNTING_CURVE_NAMES.put("USD", DISCOUNTING_CURVE_NAME);
    DISCOUNTING_CURVE_NAMES.put("JPY", DISCOUNTING_CURVE_NAME);
    DISCOUNTING_CURVE_NAMES.put("GBP", DISCOUNTING_CURVE_NAME);

    FORWARD_CURVE_NAMES.put("USD", DISCOUNTING_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("JPY", DISCOUNTING_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("GBP", DISCOUNTING_CURVE_NAME);
    DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES.put("USD", "DefaultTwoCurveUSDConfig");
    DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES.put("JPY", "DefaultTwoCurveJPYConfig");
    DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES.put("GBP", "DefaultTwoCurveGBPConfig");
    FORWARD_CURVE_CALCULATION_CONFIG_NAMES.put("USD", "DefaultTwoCurveUSDConfig");
    FORWARD_CURVE_CALCULATION_CONFIG_NAMES.put("JPY", "DefaultTwoCurveJPYConfig");
    FORWARD_CURVE_CALCULATION_CONFIG_NAMES.put("GBP", "DefaultTwoCurveGBPConfig");

    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("USD", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("JPY", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("GBP", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("DJX", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("SPX", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("NDX", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("SPXQ", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("RUY", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("NKY", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("VIX", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("IBM", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("RBS", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("LLOY", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("JPM", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("SPY", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);

    FORWARD_CURVE_NAMES.put("DJX", FUTURES_PRICE_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("SPX", FUTURES_PRICE_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("NDX", FUTURES_PRICE_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("SPXQ", FUTURES_PRICE_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("RUY", FUTURES_PRICE_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("NKY", FUTURES_PRICE_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("VIX", FUTURES_PRICE_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("IBM", DISCOUNTING_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("RBS", DISCOUNTING_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("LLOY", DISCOUNTING_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("JPM", DISCOUNTING_CURVE_NAME);
    FORWARD_CURVE_NAMES.put("SPY", DISCOUNTING_CURVE_NAME);


    VOLATILITY_SURFACE_NAMES.put("DJX", SURFACE_NAME);
    VOLATILITY_SURFACE_NAMES.put("SPX", SURFACE_NAME);
    VOLATILITY_SURFACE_NAMES.put("SPXQ", SURFACE_NAME);
    VOLATILITY_SURFACE_NAMES.put("NDX", SURFACE_NAME);
    VOLATILITY_SURFACE_NAMES.put("RUY", SURFACE_NAME);
    VOLATILITY_SURFACE_NAMES.put("NKY", SURFACE_NAME);
    VOLATILITY_SURFACE_NAMES.put("VIX", SURFACE_NAME);

    VOLATILITY_SURFACE_NAMES.put("IBM", SURFACE_NAME);
    VOLATILITY_SURFACE_NAMES.put("RBS", SURFACE_NAME);
    VOLATILITY_SURFACE_NAMES.put("LLOY", SURFACE_NAME);
    VOLATILITY_SURFACE_NAMES.put("JPM", SURFACE_NAME);
    VOLATILITY_SURFACE_NAMES.put("SPY", SURFACE_NAME);

    EX_VOLATILITY_SURFACE_NAMES.put("US", SURFACE_NAME);
    CCY_VOLATILITY_SURFACE_NAMES.put("USD", SURFACE_NAME);
    CCY_VOLATILITY_SURFACE_NAMES.put("JPY", SURFACE_NAME);
    CCY_VOLATILITY_SURFACE_NAMES.put("GBP", SURFACE_NAME);
    INTERPOLATOR_NAMES.put("DJX", SURFACE_INTERPOLATOR);
    INTERPOLATOR_NAMES.put("SPX", SURFACE_INTERPOLATOR);
    INTERPOLATOR_NAMES.put("SPXQ", SURFACE_INTERPOLATOR);
    INTERPOLATOR_NAMES.put("NDX", SURFACE_INTERPOLATOR);
    INTERPOLATOR_NAMES.put("RUY", SURFACE_INTERPOLATOR);
    INTERPOLATOR_NAMES.put("NKY", SURFACE_INTERPOLATOR);
    INTERPOLATOR_NAMES.put("VIX", SURFACE_INTERPOLATOR);

    INTERPOLATOR_NAMES.put("IBM", SURFACE_INTERPOLATOR);
    INTERPOLATOR_NAMES.put("RBS", SURFACE_INTERPOLATOR);
    INTERPOLATOR_NAMES.put("LLOY", SURFACE_INTERPOLATOR);
    INTERPOLATOR_NAMES.put("JPM", SURFACE_INTERPOLATOR);
    INTERPOLATOR_NAMES.put("SPY", SURFACE_INTERPOLATOR);

    EX_INTERPOLATOR_NAMES.put("US", SURFACE_INTERPOLATOR);
    CCY_INTERPOLATOR_NAMES.put("USD", SURFACE_INTERPOLATOR);
    CCY_INTERPOLATOR_NAMES.put("JPY", SURFACE_INTERPOLATOR);
    CCY_INTERPOLATOR_NAMES.put("GBP", SURFACE_INTERPOLATOR);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("DJX", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("SPX", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("SPXQ", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("NDX", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("RUY", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("NKY", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("VIX", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);

    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("IBM", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("RBS", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("LLOY", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("JPM", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("SPY", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);

    EX_VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("US", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    CCY_VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("USD", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    CCY_VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.put("JPY", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);

    DIVIDEND_TYPES.put("DJX", DIVIDEND_CONTINUOUS);
    DIVIDEND_TYPES.put("SPX", DIVIDEND_CONTINUOUS);
    DIVIDEND_TYPES.put("SPXQ", DIVIDEND_CONTINUOUS);
    DIVIDEND_TYPES.put("NDX", DIVIDEND_CONTINUOUS);
    DIVIDEND_TYPES.put("NKY", DIVIDEND_CONTINUOUS);
    DIVIDEND_TYPES.put("VIX", DIVIDEND_CONTINUOUS);
    DIVIDEND_TYPES.put("RUY", DIVIDEND_CONTINUOUS);
    DIVIDEND_TYPES.put("IBM", DIVIDEND_DISCRETE);
    DIVIDEND_TYPES.put("RBS", DIVIDEND_DISCRETE);
    DIVIDEND_TYPES.put("LLOY", DIVIDEND_DISCRETE);
    DIVIDEND_TYPES.put("JPM", DIVIDEND_DISCRETE);
    DIVIDEND_TYPES.put("SPY", DIVIDEND_DISCRETE);
    CCY_DIVIDEND_TYPES.put("USD", DIVIDEND_DISCRETE);
  }

  /**
   * Builder interface
   */
  public interface Builder {

    Builder useIdName();

    List<String> createPerTickerDefaults();

    com.opengamma.web.spring.defaults.EquityInstrumentDefaultValues.Builder useDividendTypes();

    Builder useDiscountingCurveNames();

    Builder useDiscountingCurveCurrency();

    Builder useDiscountingCurveCalculationConfigNames();

    Builder useForwardCurveNames();

    Builder useForwardCurveCalculationMethodNames();

    /**
     * @return the builder
     * @deprecated See useForwardCurveCalculationMethodNames
     */
    @Deprecated
    Builder useForwardCurveCalculationConfigNames();

    Builder useVolatilitySurfaceNames();

    Builder useInterpolationMethodNames();

    Builder useVolatilitySurfaceCalculationMethodNames();

    List<String> createPerEquityDefaults();

    List<String> createPerExchangeDefaults();

    List<String> createPerCurrencyDefaults();

    List<String> createAnyTickerDefaults();

  }

  private EquityInstrumentDefaultValues() {
  }

  public static Builder builder() {
    return new MyBuilder();
  }

  private static final class MyBuilder implements Builder {
    private final boolean _useIdentifierName;
    private final boolean _useDiscountingCurveNames;
    private final boolean _useDiscountingCurveCurrency;
    private final boolean _useDiscountingCurveCalculationConfigNames;
    private final boolean _useForwardCurveNames;
    private final boolean _useForwardCurveCalculationMethodNames;
    private final boolean _useForwardCurveCalculationConfigNames;
    private final boolean _useVolatilitySurfaceNames;
    private final boolean _useInterpolationMethodNames;
    private final boolean _useVolatilitySurfaceCalculationMethodNames;
    private final boolean _useDividendTypes;
    private final List<Integer> _order;

    /* package */ MyBuilder() {
      this(false, false, false, false, false, false, false, false, false, false, false, new ArrayList<Integer>());
    }

    /* package */ MyBuilder(final boolean useEquityName, final boolean useDiscountingCurveNames, final boolean useDiscountingCurveCurrency,
        final boolean useDiscountingCurveCalculationConfigNames, final boolean useForwardCurveNames, final boolean useForwardCurveCalculationNames,
        final boolean useForwardCurveCalculationConfigNames, final boolean useVolatilitySurfaceNames, final boolean useInterpolationMethodNames,
        final boolean useVolatilitySurfaceCalculationMethodNames, final boolean useDividendTypes, final List<Integer> order) {
      _useIdentifierName = useEquityName;
      _useDiscountingCurveNames = useDiscountingCurveNames;
      _useDiscountingCurveCurrency = useDiscountingCurveCurrency;
      _useDiscountingCurveCalculationConfigNames = useDiscountingCurveCalculationConfigNames;
      _useForwardCurveNames = useForwardCurveNames;
      _useForwardCurveCalculationMethodNames = useForwardCurveCalculationNames;
      _useForwardCurveCalculationConfigNames = useForwardCurveCalculationConfigNames;
      _useVolatilitySurfaceNames = useVolatilitySurfaceNames;
      _useInterpolationMethodNames = useInterpolationMethodNames;
      _useVolatilitySurfaceCalculationMethodNames = useVolatilitySurfaceCalculationMethodNames;
      _useDividendTypes = useDividendTypes;
      _order = order;
    }

    @Override
    public Builder useIdName() {
      if (_useIdentifierName == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(0);
      return new MyBuilder(true, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames,
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames,
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, _useVolatilitySurfaceCalculationMethodNames, _useDividendTypes, order);
    }

    @Override
    public Builder useDiscountingCurveNames() {
      if (_useDiscountingCurveNames == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(1);
      return new MyBuilder(_useIdentifierName, true, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames,
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames,
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, _useVolatilitySurfaceCalculationMethodNames, _useDividendTypes, order);
    }

    @Override
    public Builder useDiscountingCurveCurrency() {
      if (_useDiscountingCurveCurrency == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(2);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, true, _useDiscountingCurveCalculationConfigNames,
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames,
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, _useVolatilitySurfaceCalculationMethodNames, _useDividendTypes, order);
    }

    @Override
    public Builder useDiscountingCurveCalculationConfigNames() {
      if (_useDiscountingCurveCalculationConfigNames == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(3);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, true,
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames,
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, _useVolatilitySurfaceCalculationMethodNames, _useDividendTypes, order);
    }

    @Override
    public Builder useForwardCurveNames() {
      if (_useForwardCurveNames == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(4);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames,
          true, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames,
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, _useVolatilitySurfaceCalculationMethodNames, _useDividendTypes, order);
    }

    @Override
    public Builder useForwardCurveCalculationMethodNames() {
      if (_useForwardCurveCalculationMethodNames == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(5);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames,
          _useForwardCurveNames, true, _useForwardCurveCalculationConfigNames,
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, _useVolatilitySurfaceCalculationMethodNames, _useDividendTypes, order);
    }

    @Override
    public Builder useForwardCurveCalculationConfigNames() {
      if (_useForwardCurveCalculationConfigNames == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(6);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames,
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, true,
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, _useVolatilitySurfaceCalculationMethodNames, _useDividendTypes, order);
    }

    @Override
    public Builder useVolatilitySurfaceNames() {
      if (_useVolatilitySurfaceNames == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(7);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames,
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames,
          true, _useInterpolationMethodNames, _useVolatilitySurfaceCalculationMethodNames, _useDividendTypes, order);
    }

    @Override
    public Builder useInterpolationMethodNames() {
      if (_useInterpolationMethodNames == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(8);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames,
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames,
          _useVolatilitySurfaceNames, true, _useVolatilitySurfaceCalculationMethodNames, _useDividendTypes, order);
    }

    @Override
    public Builder useVolatilitySurfaceCalculationMethodNames() {
      if (_useVolatilitySurfaceCalculationMethodNames == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(9);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames,
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames,
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, true, _useDividendTypes, order);
    }

    @Override
    public Builder useDividendTypes() {
      if (_useForwardCurveCalculationConfigNames == true) {
        return this;
      }
      final List<Integer> order = new ArrayList<>(_order);
      order.add(10);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames,
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames,
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, _useVolatilitySurfaceCalculationMethodNames, true, order);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public List<String> createPerEquityDefaults() {
      final List<String> result = new ArrayList<>();
      for (final Map.Entry<String, String> entry : EQUITY_NAMES.entrySet()) {
        final String indexName = entry.getKey();
        final String currency = entry.getValue();
        for (final Integer field : _order) {
          switch (field) {
            case 0:
              result.add(indexName);
              break;
            case 1:
              result.add(DISCOUNTING_CURVE_NAMES.get(currency));
              break;
            case 2:
              result.add(currency);
              break;
            case 3:
              result.add(DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES.get(currency));
              break;
            case 4:
              result.add(FORWARD_CURVE_NAMES.get(indexName));
              break;
            case 5:
              result.add(FORWARD_CURVE_CALCULATION_METHOD_NAMES.get(indexName));
              break;
            case 6:
              result.add(FORWARD_CURVE_CALCULATION_CONFIG_NAMES.get(currency));
              break;
            case 7:
              result.add(VOLATILITY_SURFACE_NAMES.get(indexName));
              break;
            case 8:
              result.add(INTERPOLATOR_NAMES.get(indexName));
              break;
            case 9:
              result.add(VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.get(indexName));
              break;
            case 10:
              result.add(DIVIDEND_TYPES.get(indexName));
              break;
            default:
              throw new IllegalStateException();
          }
        }
      }
      return result;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public List<String> createPerExchangeDefaults() {
      final List<String> result = new ArrayList<>();
      for (final Map.Entry<String, String> entry : EXCHANGE_NAMES.entrySet()) {
        final String exchangeName = entry.getKey();
        final String currency = entry.getValue();
        for (final Integer field : _order) {
          switch (field) {
            case 0:
              result.add(exchangeName);
              break;
            case 1:
              result.add(DISCOUNTING_CURVE_NAMES.get(currency));
              break;
            case 2:
              result.add(currency);
              break;
            case 3:
              result.add(DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES.get(currency));
              break;
            case 4:
              result.add(FORWARD_CURVE_NAMES.get(currency));
              break;
            case 5:
              result.add(FORWARD_CURVE_CALCULATION_METHOD_NAMES.get(currency));
              break;
            case 6:
              result.add(FORWARD_CURVE_CALCULATION_CONFIG_NAMES.get(currency));
              break;
            case 7:
              result.add(EX_VOLATILITY_SURFACE_NAMES.get(exchangeName));
              break;
            case 8:
              result.add(EX_INTERPOLATOR_NAMES.get(exchangeName));
              break;
            case 9:
              result.add(EX_VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.get(exchangeName));
              break;
            case 10:
              result.add(DIVIDEND_CONTINUOUS);
              break;
            default:
              throw new IllegalStateException();
          }
        }
      }
      return result;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public List<String> createPerCurrencyDefaults() {
      final List<String> result = new ArrayList<>();
      for (final Map.Entry<String, String> entry : DISCOUNTING_CURVE_NAMES.entrySet()) {
        final String currency = entry.getKey();
        for (final Integer field : _order) {
          switch (field) {
            case 0:
              result.add(currency);
              break;
            case 1:
              result.add(DISCOUNTING_CURVE_NAMES.get(currency));
              break;
            case 2:
              result.add(currency);
              break;
            case 3:
              result.add(DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES.get(currency));
              break;
            case 4:
              result.add(FORWARD_CURVE_NAMES.get(currency));
              break;
            case 5:
              result.add(FORWARD_CURVE_CALCULATION_METHOD_NAMES.get(currency));
              break;
            case 6:
              result.add(FORWARD_CURVE_CALCULATION_CONFIG_NAMES.get(currency));
              break;
            case 7:
              result.add(CCY_VOLATILITY_SURFACE_NAMES.get(currency));
              break;
            case 8:
              result.add(CCY_INTERPOLATOR_NAMES.get(currency));
              break;
            case 9:
              result.add(CCY_VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.get(currency));
              break;
            case 10:
              result.add(DIVIDEND_CONTINUOUS);
              break;
            default:
              throw new IllegalStateException();
          }
        }
      }
      return result;
    }

    @Override
    public List<String> createAnyTickerDefaults() {
      final List<String> result = new ArrayList<>();
      for (final Integer field : _order) {
        switch (field) {
          case 0:
            throw new IllegalStateException();
          case 1:
            throw new IllegalStateException();
          case 2:
            throw new IllegalStateException();
          case 3:
            throw new IllegalStateException();
          case 4:
            result.add("Discounting");
            break;
          case 5:
            result.add(ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
            break;
          case 6:
            throw new IllegalStateException();
          case 7:
            result.add(SURFACE_NAME);
            break;
          case 8:
            result.add(SURFACE_INTERPOLATOR);
            break;
          case 9:
            result.add(BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
            break;
          case 10:
            result.add(DIVIDEND_CONTINUOUS);
            break;
          default:
            throw new IllegalStateException();
        }
      }
      return result;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public List<String> createPerTickerDefaults() {
      final List<String> result = new ArrayList<>();
      for (final Map.Entry<String, String> entry : EQUITY_NAMES.entrySet()) {
        final String indexName = entry.getKey();
        final String currency = entry.getValue();
        for (final Integer field : _order) {
          switch (field) {
            case 0:
              result.add(indexName);
              break;
            case 1:
              result.add(DISCOUNTING_CURVE_NAMES.get(currency));
              break;
            case 2:
              result.add(currency);
              break;
            case 3:
              result.add(DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES.get(currency));
              break;
            case 4:
              result.add(FUTURES_PRICE_CURVE_NAME);
              break;
            case 5:
              result.add(ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
              break;
            case 6:
              result.add(FORWARD_CURVE_CALCULATION_CONFIG_NAMES.get(currency));
              break;
            case 7:
              result.add(SURFACE_NAME);
              break;
            case 8:
              result.add(INTERPOLATOR_NAMES.get(indexName));
              break;
            case 9:
              result.add(VOLATILTY_SURFACE_CALCULATION_METHOD_NAMES.get(indexName));
              break;
            case 10:
              result.add(DIVIDEND_TYPES.get(indexName));
              break;
            default:
              throw new IllegalStateException();
          }
        }
      }
      return result;
    }
  }
}
