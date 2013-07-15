/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.util.CalculationMode;

/**
 * 
 */
public class TimeSeriesReturnCalculatorFactory {
  /** Label for continuous relative return calculator, strict mode */
  public static final String CONTINUOUS_RELATIVE_STRICT = "ContinuousRelativeReturnStrict";
  /** Label for continuous return calculator, strict mode */
  public static final String CONTINUOUS_STRICT = "ContinuousReturnStrict";
  /** Label for excess continuous return calculator, strict mode */
  public static final String EXCESS_CONTINUOUS_STRICT = "ExcessContinuousReturnStrict";
  /** Label for excess simple return calculator, strict mode */
  public static final String EXCESS_SIMPLE_NET_STRICT = "ExcessSimpleNetReturnStrict";
  /** Label for simple gross return calculator, strict mode */
  public static final String SIMPLE_GROSS_STRICT = "SimpleGrossReturnStrict";
  /** Label for simple net return calculator, strict mode */
  public static final String SIMPLE_NET_STRICT = "SimpleNetReturnStrict";
  /** Label for simple net relative return calculator, strict mode */
  public static final String SIMPLE_NET_RELATIVE_STRICT = "SimpleNetRelativeReturnStrict";
  /** Label for continuous relative return calculator, lenient mode */
  public static final String CONTINUOUS_RELATIVE_LENIENT = "ContinuousRelativeReturnLenient";
  /** Label for continuous return calculator, lenient mode */
  public static final String CONTINUOUS_LENIENT = "ContinuousReturnLenient";
  /** Label for excess continuous return calculator, lenient mode */
  public static final String EXCESS_CONTINUOUS_LENIENT = "ExcessContinuousReturnLenient";
  /** Label for excess simple return calculator, lenient mode */
  public static final String EXCESS_SIMPLE_NET_LENIENT = "ExcessSimpleNetReturnLenient";
  /** Label for simple gross return calculator, lenient mode */
  public static final String SIMPLE_GROSS_LENIENT = "SimpleGrossReturnLenient";
  /** Label for simple net return calculator, lenient mode */
  public static final String SIMPLE_NET_LENIENT = "SimpleNetReturnLenient";
  /** Label for simple net relative return calculator, lenient mode */
  public static final String SIMPLE_NET_RELATIVE_LENIENT = "SimpleNetRelativeReturnLenient";
  /** Continuous relative return calculator, strict mode */
  public static final ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator CONTINUOUS_RELATIVE_STRICT_CALCULATOR = new ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator(
      CalculationMode.STRICT);
  /** Continuous return calculator, strict mode */
  public static final ContinuouslyCompoundedTimeSeriesReturnCalculator CONTINUOUS_STRICT_CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Continuous return calculator, strict mode */
  public static final ExcessContinuouslyCompoundedTimeSeriesReturnCalculator EXCESS_CONTINUOUS_STRICT_CALCULATOR = new ExcessContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Excess simple return calculator, strict mode */
  public static final ExcessSimpleNetTimeSeriesReturnCalculator EXCESS_SIMPLE_NET_STRICT_CALCULATOR = new ExcessSimpleNetTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Simple gross return calculator, strict mode */
  public static final SimpleGrossTimeSeriesReturnCalculator SIMPLE_GROSS_STRICT_CALCULATOR = new SimpleGrossTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Simple net return calculator, strict mode */
  public static final SimpleNetTimeSeriesReturnCalculator SIMPLE_NET_STRICT_CALCULATOR = new SimpleNetTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Simple net relative return calculator, strict mode */
  public static final SimpleNetRelativeTimeSeriesReturnCalculator SIMPLE_NET_RELATIVE_STRICT_CALCULATOR = new SimpleNetRelativeTimeSeriesReturnCalculator(CalculationMode.STRICT);
  /** Continuous relative return calculator, lenient mode */
  public static final ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator CONTINUOUS_RELATIVE_LENIENT_CALCULATOR = new ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator(
      CalculationMode.LENIENT);
  /** Continuous return calculator, lenient mode */
  public static final ContinuouslyCompoundedTimeSeriesReturnCalculator CONTINUOUS_LENIENT_CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  /** Continuous return calculator, lenient mode */
  public static final ExcessContinuouslyCompoundedTimeSeriesReturnCalculator EXCESS_CONTINUOUS_LENIENT_CALCULATOR = new ExcessContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  /** Excess simple return calculator, lenient mode */
  public static final ExcessSimpleNetTimeSeriesReturnCalculator EXCESS_SIMPLE_NET_LENIENT_CALCULATOR = new ExcessSimpleNetTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  /** Simple gross return calculator, lenient mode */
  public static final SimpleGrossTimeSeriesReturnCalculator SIMPLE_GROSS_LENIENT_CALCULATOR = new SimpleGrossTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  /** Simple net return calculator, lenient mode */
  public static final SimpleNetTimeSeriesReturnCalculator SIMPLE_NET_LENIENT_CALCULATOR = new SimpleNetTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  /** Simple net relative return calculator, lenient mode */
  public static final SimpleNetRelativeTimeSeriesReturnCalculator SIMPLE_NET_RELATIVE_LENIENT_CALCULATOR = new SimpleNetRelativeTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  private static final Map<String, TimeSeriesReturnCalculator> s_staticStrictInstances;
  private static final Map<Class<?>, String> s_instanceStrictNames;
  private static final Map<String, TimeSeriesReturnCalculator> s_staticLenientInstances;
  private static final Map<Class<?>, String> s_instanceLenientNames;

  static {
    s_staticStrictInstances = new HashMap<>();
    s_instanceStrictNames = new HashMap<>();
    s_staticLenientInstances = new HashMap<>();
    s_instanceLenientNames = new HashMap<>();
    s_staticLenientInstances.put(CONTINUOUS_LENIENT, CONTINUOUS_LENIENT_CALCULATOR);
    s_instanceLenientNames.put(CONTINUOUS_LENIENT_CALCULATOR.getClass(), CONTINUOUS_LENIENT);
    s_staticLenientInstances.put(CONTINUOUS_RELATIVE_LENIENT, CONTINUOUS_RELATIVE_LENIENT_CALCULATOR);
    s_instanceLenientNames.put(CONTINUOUS_RELATIVE_LENIENT_CALCULATOR.getClass(), CONTINUOUS_RELATIVE_LENIENT);
    s_staticStrictInstances.put(CONTINUOUS_RELATIVE_STRICT, CONTINUOUS_RELATIVE_STRICT_CALCULATOR);
    s_instanceStrictNames.put(CONTINUOUS_RELATIVE_STRICT_CALCULATOR.getClass(), CONTINUOUS_RELATIVE_STRICT);
    s_staticStrictInstances.put(CONTINUOUS_STRICT, CONTINUOUS_STRICT_CALCULATOR);
    s_instanceStrictNames.put(CONTINUOUS_STRICT_CALCULATOR.getClass(), CONTINUOUS_STRICT);
    s_staticLenientInstances.put(EXCESS_CONTINUOUS_LENIENT, EXCESS_CONTINUOUS_LENIENT_CALCULATOR);
    s_instanceLenientNames.put(EXCESS_CONTINUOUS_LENIENT_CALCULATOR.getClass(), EXCESS_CONTINUOUS_LENIENT);
    s_staticStrictInstances.put(EXCESS_CONTINUOUS_STRICT, EXCESS_CONTINUOUS_STRICT_CALCULATOR);
    s_instanceStrictNames.put(EXCESS_CONTINUOUS_STRICT_CALCULATOR.getClass(), EXCESS_CONTINUOUS_STRICT);
    s_staticLenientInstances.put(EXCESS_SIMPLE_NET_LENIENT, EXCESS_SIMPLE_NET_LENIENT_CALCULATOR);
    s_instanceLenientNames.put(EXCESS_SIMPLE_NET_LENIENT_CALCULATOR.getClass(), EXCESS_SIMPLE_NET_LENIENT);
    s_staticStrictInstances.put(EXCESS_SIMPLE_NET_STRICT, EXCESS_SIMPLE_NET_STRICT_CALCULATOR);
    s_instanceStrictNames.put(EXCESS_SIMPLE_NET_STRICT_CALCULATOR.getClass(), EXCESS_SIMPLE_NET_STRICT);
    s_staticLenientInstances.put(SIMPLE_GROSS_LENIENT, SIMPLE_GROSS_LENIENT_CALCULATOR);
    s_instanceLenientNames.put(SIMPLE_GROSS_LENIENT_CALCULATOR.getClass(), SIMPLE_GROSS_LENIENT);
    s_staticStrictInstances.put(SIMPLE_GROSS_STRICT, SIMPLE_GROSS_STRICT_CALCULATOR);
    s_instanceStrictNames.put(SIMPLE_GROSS_STRICT_CALCULATOR.getClass(), SIMPLE_GROSS_STRICT);
    s_staticLenientInstances.put(SIMPLE_NET_LENIENT, SIMPLE_NET_LENIENT_CALCULATOR);
    s_instanceLenientNames.put(SIMPLE_NET_LENIENT_CALCULATOR.getClass(), SIMPLE_NET_LENIENT);
    s_staticStrictInstances.put(SIMPLE_NET_STRICT, SIMPLE_NET_STRICT_CALCULATOR);
    s_instanceStrictNames.put(SIMPLE_NET_STRICT_CALCULATOR.getClass(), SIMPLE_NET_STRICT);
    s_staticLenientInstances.put(SIMPLE_NET_RELATIVE_LENIENT, SIMPLE_NET_RELATIVE_LENIENT_CALCULATOR);
    s_instanceLenientNames.put(SIMPLE_NET_RELATIVE_LENIENT_CALCULATOR.getClass(), SIMPLE_NET_RELATIVE_LENIENT);
    s_staticStrictInstances.put(SIMPLE_NET_RELATIVE_STRICT, SIMPLE_NET_RELATIVE_STRICT_CALCULATOR);
    s_instanceStrictNames.put(SIMPLE_NET_RELATIVE_STRICT_CALCULATOR.getClass(), SIMPLE_NET_RELATIVE_STRICT);
  }

  public static String getReturnCalculatorName(final TimeSeriesReturnCalculator calculator) {
    if (calculator == null) {
      return null;
    }
    final CalculationMode mode = calculator.getMode();
    if (mode == CalculationMode.STRICT) {
      return s_instanceStrictNames.get(calculator.getClass());
    } else if (mode == CalculationMode.LENIENT) {
      return s_instanceLenientNames.get(calculator.getClass());
    } else {
      throw new IllegalArgumentException("Do not have calculator for " + calculator.getClass().getName() + " with calculation mode " + mode);
    }
  }

  public static String getReturnCalculatorName(final TimeSeriesReturnCalculator calculator, final CalculationMode mode) {
    if (calculator == null) {
      return null;
    }
    switch (mode) {
      case STRICT:
        return s_instanceStrictNames.get(calculator.getClass());
      case LENIENT:
        return s_instanceLenientNames.get(calculator.getClass());
      default:
        throw new IllegalArgumentException("Do not have name for " + calculator.getClass().getName() + " with calculation mode " + mode);
    }
  }

  public static TimeSeriesReturnCalculator getReturnCalculator(final String calculatorName) {
    if (s_staticLenientInstances.containsKey(calculatorName)) {
      return s_staticLenientInstances.get(calculatorName);
    }
    if (s_staticStrictInstances.containsKey(calculatorName)) {
      return s_staticStrictInstances.get(calculatorName);
    }
    throw new IllegalArgumentException("Do not have calculator for " + calculatorName);
  }

  public static TimeSeriesReturnCalculator getReturnCalculator(final String calculatorName, final CalculationMode mode) {
    TimeSeriesReturnCalculator calculator;
    switch (mode) {
      case STRICT:
        calculator = s_staticStrictInstances.get(calculatorName);
        break;
      case LENIENT:
        calculator = s_staticLenientInstances.get(calculatorName);
        break;
      default:
        throw new IllegalArgumentException("Do not have calculator for " + calculatorName + " with mode " + mode);
    }
    if (calculator == null) {
      throw new IllegalArgumentException("Do not have calculator for " + calculatorName + " with mode " + mode);
    }
    return calculator;
  }
}
