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

/**
 * 
 */
public abstract class EquityInstrumentDefaultValues {
  private static final Map<String, String> EQ_CURRENCIES = new HashMap<String, String>();
  private static final Map<String, String> EQ_DISCOUNTING_CURVES = new HashMap<String, String>();
  private static final Map<String, String> EQ_FORWARD_CURVES = new HashMap<String, String>();
  private static final Map<String, String> EQ_DISCOUNTING_CURVE_CALCULATION_CONFIGS = new HashMap<String, String>();
  private static final Map<String, String> EQ_FORWARD_CURVE_CALCULATION_CONFIGS = new HashMap<String, String>();
  private static final Map<String, String> EQ_FORWARD_CURVE_CALCULATION_METHODS = new HashMap<String, String>();
  private static final Map<String, String> EQ_VOLATILITY_SURFACES = new HashMap<String, String>();
  private static final Map<String, String> EQ_INTERPOLATORS = new HashMap<String, String>();
  private static final Map<String, String> EX_CURRENCIES = new HashMap<String, String>();
  private static final Map<String, String> EX_DISCOUNTING_CURVES = new HashMap<String, String>();
  private static final Map<String, String> EX_FORWARD_CURVES = new HashMap<String, String>();
  private static final Map<String, String> EX_DISCOUNTING_CURVE_CALCULATION_CONFIGS = new HashMap<String, String>();
  private static final Map<String, String> EX_FORWARD_CURVE_CALCULATION_CONFIGS = new HashMap<String, String>();
  private static final Map<String, String> EX_FORWARD_CURVE_CALCULATION_METHODS = new HashMap<String, String>();
  private static final Map<String, String> EX_VOLATILITY_SURFACES = new HashMap<String, String>();
  private static final Map<String, String> EX_INTERPOLATORS = new HashMap<String, String>();
  private static final Map<String, String> CCY_DISCOUNTING_CURVES = new HashMap<String, String>();
  private static final Map<String, String> CCY_FORWARD_CURVES = new HashMap<String, String>();
  private static final Map<String, String> CCY_DISCOUNTING_CURVE_CALCULATION_CONFIGS = new HashMap<String, String>();
  private static final Map<String, String> CCY_FORWARD_CURVE_CALCULATION_CONFIGS = new HashMap<String, String>();
  private static final Map<String, String> CCY_FORWARD_CURVE_CALCULATION_METHODS = new HashMap<String, String>();
  private static final Map<String, String> CCY_VOLATILITY_SURFACES = new HashMap<String, String>();
  private static final Map<String, String> CCY_INTERPOLATORS = new HashMap<String, String>();
  
  static {
    EQ_CURRENCIES.put("DJX", "USD");    
    EQ_CURRENCIES.put("SPX", "USD");    
    EQ_CURRENCIES.put("NDX", "USD");    
    EQ_CURRENCIES.put("RUY", "USD");
    EQ_CURRENCIES.put("NKY", "JPY");
    EX_CURRENCIES.put("US", "USD");
    EQ_DISCOUNTING_CURVES.put("DJX", "Discounting");    
    EQ_DISCOUNTING_CURVES.put("SPX", "Discounting");    
    EQ_DISCOUNTING_CURVES.put("NDX", "Discounting");    
    EQ_DISCOUNTING_CURVES.put("RUY", "Discounting");
    EQ_DISCOUNTING_CURVES.put("NKY", "Discounting");
    EX_DISCOUNTING_CURVES.put("US", "Discounting");
    CCY_DISCOUNTING_CURVES.put("USD", "Discounting");
    EQ_FORWARD_CURVES.put("DJX", "Discounting");
    EQ_FORWARD_CURVES.put("SPX", "Discounting");
    EQ_FORWARD_CURVES.put("NDX", "Discounting");
    EQ_FORWARD_CURVES.put("RUY", "Discounting");
    EQ_FORWARD_CURVES.put("NKY", "Discounting");
    EX_FORWARD_CURVES.put("US", "Discounting");
    CCY_FORWARD_CURVES.put("USD", "Discounting");
    EQ_DISCOUNTING_CURVE_CALCULATION_CONFIGS.put("DJX", "DefaultTwoCurveUSDConfig");
    EQ_DISCOUNTING_CURVE_CALCULATION_CONFIGS.put("SPX", "DefaultTwoCurveUSDConfig");
    EQ_DISCOUNTING_CURVE_CALCULATION_CONFIGS.put("NDX", "DefaultTwoCurveUSDConfig");
    EQ_DISCOUNTING_CURVE_CALCULATION_CONFIGS.put("RUY", "DefaultTwoCurveUSDConfig");
    EQ_DISCOUNTING_CURVE_CALCULATION_CONFIGS.put("NKY", "DefaultTwoCurveJPYConfig");
    EX_DISCOUNTING_CURVE_CALCULATION_CONFIGS.put("US", "DefaultTwoCurveUSDConfig");
    CCY_DISCOUNTING_CURVE_CALCULATION_CONFIGS.put("USD", "DefaultTwoCurveUSDConfig");
    EQ_FORWARD_CURVE_CALCULATION_CONFIGS.put("DJX", "DefaultTwoCurveUSDConfig");
    EQ_FORWARD_CURVE_CALCULATION_CONFIGS.put("SPX", "DefaultTwoCurveUSDConfig");
    EQ_FORWARD_CURVE_CALCULATION_CONFIGS.put("NDX", "DefaultTwoCurveUSDConfig");
    EQ_FORWARD_CURVE_CALCULATION_CONFIGS.put("RUY", "DefaultTwoCurveUSDConfig");
    EQ_FORWARD_CURVE_CALCULATION_CONFIGS.put("NKY", "DefaultTwoCurveJPYConfig");
    EX_FORWARD_CURVE_CALCULATION_CONFIGS.put("US", "DefaultTwoCurveUSDConfig");
    CCY_FORWARD_CURVE_CALCULATION_CONFIGS.put("USD", "DefaultTwoCurveUSDConfig");
    EQ_FORWARD_CURVE_CALCULATION_METHODS.put("DJX", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    EQ_FORWARD_CURVE_CALCULATION_METHODS.put("SPX", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    EQ_FORWARD_CURVE_CALCULATION_METHODS.put("NDX", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    EQ_FORWARD_CURVE_CALCULATION_METHODS.put("RUY", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    EQ_FORWARD_CURVE_CALCULATION_METHODS.put("NKY", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    EX_FORWARD_CURVE_CALCULATION_METHODS.put("US", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    CCY_FORWARD_CURVE_CALCULATION_METHODS.put("USD", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    EQ_VOLATILITY_SURFACES.put("DJX", "BBG");
    EQ_VOLATILITY_SURFACES.put("SPX", "BBG");
    EQ_VOLATILITY_SURFACES.put("NDX", "BBG");
    EQ_VOLATILITY_SURFACES.put("RUY", "BBG");
    EQ_VOLATILITY_SURFACES.put("NKY", "BBG");
    EX_VOLATILITY_SURFACES.put("US", "BBG");
    CCY_VOLATILITY_SURFACES.put("USD", "BBG");
    EQ_INTERPOLATORS.put("DJX", "Spline");
    EQ_INTERPOLATORS.put("SPX", "Spline");
    EQ_INTERPOLATORS.put("NDX", "Spline");
    EQ_INTERPOLATORS.put("RUY", "Spline");
    EQ_INTERPOLATORS.put("NKY", "Spline");
    EX_INTERPOLATORS.put("US", "Spline");
    CCY_INTERPOLATORS.put("USD", "Spline");
  }

  /**
   * Builder interface
   */
  public interface Builder {
    
    Builder useIdName();
    
    Builder useDiscountingCurveNames();
    
    Builder useDiscountingCurveCurrency();
    
    Builder useDiscountingCurveCalculationConfigNames();
    
    Builder useForwardCurveNames();
    
    Builder useForwardCurveCalculationMethodNames();
    
    Builder useForwardCurveCalculationConfigNames();
    
    Builder useVolatilitySurfaceNames();
    
    Builder useInterpolationMethodNames();
    
    List<String> createPerEquityDefaults();
    
    List<String> createPerExchangeDefaults();
    
    List<String> createPerCurrencyDefaults();
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
    private final List<Integer> _order;
    
    /* package */ MyBuilder() {
      this(false, false, false, false, false, false, false, false, false, new ArrayList<Integer>());
    }
    
    /* package */ MyBuilder(final boolean useEquityName, final boolean useDiscountingCurveNames, final boolean useDiscountingCurveCurrency,
        final boolean useDiscountingCurveCalculationConfigNames, final boolean useForwardCurveNames, final boolean useForwardCurveCalculationNames, 
        final boolean useForwardCurveCalculationConfigNames, final boolean useVolatilitySurfaceNames, final boolean useInterpolationMethodNames,
        final List<Integer> order) {
      _useIdentifierName = useEquityName;
      _useDiscountingCurveNames = useDiscountingCurveNames;
      _useDiscountingCurveCurrency = useDiscountingCurveCurrency;
      _useDiscountingCurveCalculationConfigNames = useDiscountingCurveCalculationConfigNames;
      _useForwardCurveNames = useForwardCurveNames;
      _useForwardCurveCalculationMethodNames = useForwardCurveCalculationNames;
      _useForwardCurveCalculationConfigNames = useForwardCurveCalculationConfigNames;
      _useVolatilitySurfaceNames = useVolatilitySurfaceNames;
      _useInterpolationMethodNames = useInterpolationMethodNames;
      _order = order;
    }
    
    @Override
    public Builder useIdName() {
      if (_useIdentifierName == true) {
        return this;
      }
      List<Integer> order = new ArrayList<Integer>(_order);
      order.add(0);
      return new MyBuilder(true, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames, 
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, order);
    }
    
    @Override
    public Builder useDiscountingCurveNames() {
      if (_useDiscountingCurveNames == true) {
        return this;
      }
      List<Integer> order = new ArrayList<Integer>(_order);
      order.add(1);
      return new MyBuilder(_useIdentifierName, true, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames, 
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, order);
    }

    @Override
    public Builder useDiscountingCurveCurrency() {
      if (_useDiscountingCurveCurrency == true) {
        return this;
      }
      List<Integer> order = new ArrayList<Integer>(_order);
      order.add(2);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, true, _useDiscountingCurveCalculationConfigNames, 
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames, 
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, order);
    }

    @Override
    public Builder useDiscountingCurveCalculationConfigNames() {
      if (_useDiscountingCurveCalculationConfigNames == true) {
        return this;
      }
      List<Integer> order = new ArrayList<Integer>(_order);
      order.add(3);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, true, 
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames, 
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, order);
    }

    @Override
    public Builder useForwardCurveNames() {
      if (_useForwardCurveNames == true) {
        return this;
      }
      List<Integer> order = new ArrayList<Integer>(_order);
      order.add(4);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
          true, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames, 
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, order);
    }

    @Override
    public Builder useForwardCurveCalculationMethodNames() {
      if (_useForwardCurveCalculationMethodNames == true) {
        return this;
      }
      List<Integer> order = new ArrayList<Integer>(_order);
      order.add(5);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
          _useForwardCurveNames, true, _useForwardCurveCalculationConfigNames, 
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, order);
    }

    @Override
    public Builder useForwardCurveCalculationConfigNames() {
      if (_useForwardCurveCalculationConfigNames == true) {
        return this;
      }
      List<Integer> order = new ArrayList<Integer>(_order);
      order.add(6);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, true, 
          _useVolatilitySurfaceNames, _useInterpolationMethodNames, order);
    }

    @Override
    public Builder useVolatilitySurfaceNames() {
      if (_useVolatilitySurfaceNames == true) {
        return this;
      }
      List<Integer> order = new ArrayList<Integer>(_order);
      order.add(7);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames, 
          true, _useInterpolationMethodNames, order);
    }
    
    @Override
    public Builder useInterpolationMethodNames() {
      if (_useInterpolationMethodNames == true) {
        return this;
      }
      List<Integer> order = new ArrayList<Integer>(_order);
      order.add(8);
      return new MyBuilder(_useIdentifierName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames, 
          _useVolatilitySurfaceNames, true, order);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public List<String> createPerEquityDefaults() {
      final List<String> result = new ArrayList<String>();
      for (Map.Entry<String, String> entry : EQ_DISCOUNTING_CURVES.entrySet()) {
        final String indexName = entry.getKey();
        for (Integer field : _order) {
          switch (field) {
            case 0: 
              result.add(indexName);
              break;
            case 1:
              result.add(entry.getValue());
              break;
            case 2: 
              result.add(EQ_CURRENCIES.get(indexName));
              break;
            case 3:
              result.add(EQ_DISCOUNTING_CURVE_CALCULATION_CONFIGS.get(indexName));
              break;
            case 4:
              result.add(EQ_FORWARD_CURVES.get(indexName));
              break;
            case 5:
              result.add(EQ_FORWARD_CURVE_CALCULATION_METHODS.get(indexName));
              break;
            case 6:
              result.add(EQ_FORWARD_CURVE_CALCULATION_CONFIGS.get(indexName));
              break;
            case 7:
              result.add(EQ_VOLATILITY_SURFACES.get(indexName));
              break;
            case 8:
              result.add(EQ_INTERPOLATORS.get(indexName));
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
      final List<String> result = new ArrayList<String>();
      for (Map.Entry<String, String> entry : EX_DISCOUNTING_CURVES.entrySet()) {
        final String exchangeName = entry.getKey();
        for (Integer field : _order) {
          switch (field) {
            case 0: 
              result.add(exchangeName);
              break;
            case 1:
              result.add(entry.getValue());
              break;
            case 2: 
              result.add(EX_CURRENCIES.get(exchangeName));
              break;
            case 3:
              result.add(EX_DISCOUNTING_CURVE_CALCULATION_CONFIGS.get(exchangeName));
              break;
            case 4:
              result.add(EX_FORWARD_CURVES.get(exchangeName));
              break;
            case 5:
              result.add(EX_FORWARD_CURVE_CALCULATION_METHODS.get(exchangeName));
              break;
            case 6:
              result.add(EX_FORWARD_CURVE_CALCULATION_CONFIGS.get(exchangeName));
              break;
            case 7:
              result.add(EX_VOLATILITY_SURFACES.get(exchangeName));
              break;
            case 8:
              result.add(EX_INTERPOLATORS.get(exchangeName));
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
      final List<String> result = new ArrayList<String>();
      for (Map.Entry<String, String> entry : CCY_DISCOUNTING_CURVES.entrySet()) {
        final String currencyName = entry.getKey();
        for (Integer field : _order) {
          switch (field) {
            case 0: 
              result.add(currencyName);
              break;
            case 1:
              result.add(entry.getValue());
              break;
            case 2: 
              result.add(currencyName);
              break;
            case 3:
              result.add(CCY_DISCOUNTING_CURVE_CALCULATION_CONFIGS.get(currencyName));
              break;
            case 4:
              result.add(CCY_FORWARD_CURVES.get(currencyName));
              break;
            case 5:
              result.add(CCY_FORWARD_CURVE_CALCULATION_METHODS.get(currencyName));
              break;
            case 6:
              result.add(CCY_FORWARD_CURVE_CALCULATION_CONFIGS.get(currencyName));
              break;
            case 7:
              result.add(CCY_VOLATILITY_SURFACES.get(currencyName));
              break;
            case 8:
              result.add(CCY_INTERPOLATORS.get(currencyName));
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
