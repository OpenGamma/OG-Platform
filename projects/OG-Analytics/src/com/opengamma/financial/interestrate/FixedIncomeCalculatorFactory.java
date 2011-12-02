/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public final class FixedIncomeCalculatorFactory {
  /** Present value */
  public static final String PRESENT_VALUE = "PresentValue";
  /** Present value coupon sensitivity */
  public static final String PRESENT_VALUE_COUPON_SENSITIVITY = "PresentValueCouponSensitivity";
  /** Present value sensitivity */
  public static final String PRESENT_VALUE_SENSITIVITY = "PresentValueSensitivity";
  /** PV01 */
  public static final String PV01 = "PV01";
  /** Par rate */
  public static final String PAR_RATE = "ParRate";
  /** Par rate curve sensitivity */
  public static final String PAR_RATE_CURVE_SENSITIVITY = "ParRateCurveSensitivity";
  /** Par rate parallel sensitivity */
  public static final String PAR_RATE_PARALLEL_SENSITIVITY = "ParRateParallelSensitivity";
  /** Present value calculator */
  public static final PresentValueCalculator PRESENT_VALUE_CALCULATOR = PresentValueCalculator.getInstance();
  /** Present value coupon sensitivity calculator */
  public static final PresentValueCouponSensitivityCalculator PRESENT_VALUE_COUPON_SENSITIVITY_CALCULATOR = PresentValueCouponSensitivityCalculator.getInstance();
  /** Present value sensitivity calculator */
  public static final PresentValueCurveSensitivityCalculator PRESENT_VALUE_SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  /** PV01 calculator */
  public static final PV01Calculator PV01_CALCULATOR = PV01Calculator.getInstance();
  /** Par rate calculator */
  public static final ParRateCalculator PAR_RATE_CALCULATOR = ParRateCalculator.getInstance();
  /** Par rate curve sensitivity calculator */
  public static final ParRateCurveSensitivityCalculator PAR_RATE_CURVE_SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();
  /** Par rate parallel sensitivity calculator*/
  public static final ParRateParallelSensitivityCalculator PAR_RATE_PARALLEL_SENSITIVITY_CALCULATOR = ParRateParallelSensitivityCalculator.getInstance();

  private static final Map<String, InstrumentDerivativeVisitor<?, ?>> s_instances = new HashMap<String, InstrumentDerivativeVisitor<?, ?>>();
  private static final Map<Class<?>, String> s_instanceNames = new HashMap<Class<?>, String>();

  static {
    s_instances.put(PAR_RATE, PAR_RATE_CALCULATOR);
    s_instances.put(PAR_RATE_CURVE_SENSITIVITY, PAR_RATE_CURVE_SENSITIVITY_CALCULATOR);
    s_instances.put(PAR_RATE_PARALLEL_SENSITIVITY, PAR_RATE_PARALLEL_SENSITIVITY_CALCULATOR);
    s_instances.put(PRESENT_VALUE, PRESENT_VALUE_CALCULATOR);
    s_instances.put(PRESENT_VALUE_COUPON_SENSITIVITY, PRESENT_VALUE_COUPON_SENSITIVITY_CALCULATOR);
    s_instances.put(PRESENT_VALUE_SENSITIVITY, PRESENT_VALUE_SENSITIVITY_CALCULATOR);
    s_instances.put(PV01, PV01_CALCULATOR);
    s_instanceNames.put(PAR_RATE_CALCULATOR.getClass(), PAR_RATE);
    s_instanceNames.put(PAR_RATE_CURVE_SENSITIVITY_CALCULATOR.getClass(), PAR_RATE_CURVE_SENSITIVITY);
    s_instanceNames.put(PAR_RATE_PARALLEL_SENSITIVITY_CALCULATOR.getClass(), PAR_RATE_PARALLEL_SENSITIVITY);
    s_instanceNames.put(PRESENT_VALUE_CALCULATOR.getClass(), PRESENT_VALUE);
    s_instanceNames.put(PRESENT_VALUE_COUPON_SENSITIVITY_CALCULATOR.getClass(), PRESENT_VALUE_COUPON_SENSITIVITY);
    s_instanceNames.put(PRESENT_VALUE_SENSITIVITY_CALCULATOR.getClass(), PRESENT_VALUE_SENSITIVITY);
    s_instanceNames.put(PV01_CALCULATOR.getClass(), PV01);
  }

  private FixedIncomeCalculatorFactory() {
  }

  public static InstrumentDerivativeVisitor<?, ?> getCalculator(final String name) {
    final InstrumentDerivativeVisitor<?, ?> calculator = s_instances.get(name);
    if (calculator != null) {
      return calculator;
    }
    throw new IllegalArgumentException("Could not get calculator for " + name);
  }

  public static String getCalculatorName(final InstrumentDerivativeVisitor<?, ?> calculator) {
    if (calculator == null) {
      return null;
    }
    return s_instanceNames.get(calculator.getClass());
  }
}
