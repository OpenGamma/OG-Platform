/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;

/**
 * 
 */
public abstract class EquityInstrumentDefaultValues {
  private static final Map<String, String> EQUITY_NAMES = new HashMap<String, String>(); 
  private static final Map<String, String> DISCOUNTING_CURVE_NAMES = new HashMap<String, String>();
  private static final Map<String, String> FORWARD_CURVE_NAMES = new HashMap<String, String>();
  private static final Map<String, String> DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES = new HashMap<String, String>();
  private static final Map<String, String> FORWARD_CURVE_CALCULATION_CONFIG_NAMES = new HashMap<String, String>();
  private static final Map<String, String> FORWARD_CURVE_CALCULATION_METHOD_NAMES = new HashMap<String, String>();
  private static final Map<String, String> VOLATILITY_SURFACE_NAMES = new HashMap<String, String>();
  private static final Map<String, String> INTERPOLATOR_NAMES = new HashMap<String, String>();
  
  static {
    EQUITY_NAMES.put("DJX", "USD");
    EQUITY_NAMES.put("SPX", "USD");
    EQUITY_NAMES.put("NDX", "USD");
    EQUITY_NAMES.put("RUY", "USD");
    EQUITY_NAMES.put("NKY", "JPY");
    EQUITY_NAMES.put("AAPL", "USD");
    EQUITY_NAMES.put("AAPL US", "USD");
    DISCOUNTING_CURVE_NAMES.put("USD", "Discounting");
    DISCOUNTING_CURVE_NAMES.put("JPY", "Discounting");
    FORWARD_CURVE_NAMES.put("USD", "Discounting");
    FORWARD_CURVE_NAMES.put("JPY", "Discounting");
    DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES.put("USD", "DefaultTwoCurveUSDConfig");
    DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES.put("JPY", "DefaultTwoCurveJPYConfig");
    FORWARD_CURVE_CALCULATION_CONFIG_NAMES.put("USD", "DefaultTwoCurveUSDConfig");
    FORWARD_CURVE_CALCULATION_CONFIG_NAMES.put("JPY", "DefaultTwoCurveJPYConfig");
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("USD", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    FORWARD_CURVE_CALCULATION_METHOD_NAMES.put("JPY", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    VOLATILITY_SURFACE_NAMES.put("DJX", "BBG");
    VOLATILITY_SURFACE_NAMES.put("SPX", "BBG");
    VOLATILITY_SURFACE_NAMES.put("NDX", "BBG");
    VOLATILITY_SURFACE_NAMES.put("RUY", "BBG");
    VOLATILITY_SURFACE_NAMES.put("NKY", "BBG");
    VOLATILITY_SURFACE_NAMES.put("AAPL", "BBG");
    VOLATILITY_SURFACE_NAMES.put("AAPL US", "BBG");
    INTERPOLATOR_NAMES.put("DJX", "Spline");
    INTERPOLATOR_NAMES.put("SPX", "Spline");
    INTERPOLATOR_NAMES.put("NDX", "Spline");
    INTERPOLATOR_NAMES.put("RUY", "Spline");
    INTERPOLATOR_NAMES.put("NKY", "Spline");
    INTERPOLATOR_NAMES.put("AAPL", "Spline");
    INTERPOLATOR_NAMES.put("AAPL US", "Spline");
  }

  /**
   * Builder interface
   */
  public interface Builder {
    
    Builder useEquityName();
    
    Builder useDiscountingCurveNames();
    
    Builder useDiscountingCurveCurrency();
    
    Builder useDiscountingCurveCalculationConfigNames();
    
    Builder useForwardCurveNames();
    
    Builder useForwardCurveCalculationMethodNames();
    
    Builder useForwardCurveCalculationConfigNames();
    
    Builder useVolatilitySurfaceNames();
    
    Builder useInterpolationMethodNames();
    
    List<String> createDefaults();
  }
  
  private EquityInstrumentDefaultValues() {
  }

  public static Builder builder() {
    return new MyBuilder();
  }
  
  private static final class MyBuilder implements Builder {
    private final boolean _useEquityName;
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
      _useEquityName = useEquityName;
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
    public Builder useEquityName() {
      if (_useEquityName == true) {
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
      return new MyBuilder(_useEquityName, true, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
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
      return new MyBuilder(_useEquityName, _useDiscountingCurveNames, true, _useDiscountingCurveCalculationConfigNames, 
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
      return new MyBuilder(_useEquityName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, true, 
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
      return new MyBuilder(_useEquityName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
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
      return new MyBuilder(_useEquityName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
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
      return new MyBuilder(_useEquityName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
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
      return new MyBuilder(_useEquityName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
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
      return new MyBuilder(_useEquityName, _useDiscountingCurveNames, _useDiscountingCurveCurrency, _useDiscountingCurveCalculationConfigNames, 
          _useForwardCurveNames, _useForwardCurveCalculationMethodNames, _useForwardCurveCalculationConfigNames, 
          _useVolatilitySurfaceNames, true, order);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public List<String> createDefaults() {
      List<String> result = new ArrayList<String>();
      Iterator<Map.Entry<String, String>> iterator = EQUITY_NAMES.entrySet().iterator();
      for (int i = 0; i < EQUITY_NAMES.size(); i++) {
        Map.Entry<String, String> entry = iterator.next();
        String indexName = entry.getKey();
        String id = entry.getValue();
        for (Integer field : _order) {
          switch (field) {
            case 0: 
              result.add(indexName);
              break;
            case 1:
              result.add(DISCOUNTING_CURVE_NAMES.get(id));
              break;
            case 2: 
              result.add(id);
              break;
            case 3:
              result.add(DISCOUNTING_CURVE_CALCULATION_CONFIG_NAMES.get(id));
              break;
            case 4:
              result.add(FORWARD_CURVE_NAMES.get(id));
              break;
            case 5:
              result.add(FORWARD_CURVE_CALCULATION_METHOD_NAMES.get(id));
              break;
            case 6:
              result.add(FORWARD_CURVE_CALCULATION_CONFIG_NAMES.get(id));
              break;
            case 7:
              result.add(VOLATILITY_SURFACE_NAMES.get(indexName));
              break;
            case 8:
              result.add(INTERPOLATOR_NAMES.get(indexName));
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
