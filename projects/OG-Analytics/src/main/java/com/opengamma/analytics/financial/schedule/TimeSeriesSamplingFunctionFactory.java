/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class TimeSeriesSamplingFunctionFactory {
  /** No padding */
  public static final String NO_PADDING = "NoPadding";
  /** Pad with previous value */
  public static final String PREVIOUS_VALUE_PADDING = "PreviousValuePadding";
  /** Pad with previous value, pad with first value in series if there is insufficient data */
  public static final String PREVIOUS_AND_FIRST_VALUE_PADDING = "PreviousAndFirstValuePadding";
  /** No padding calculator */
  public static final NoPaddingTimeSeriesSamplingFunction NO_PADDING_FUNCTION = new NoPaddingTimeSeriesSamplingFunction();
  /** Previous value padding calculator */
  public static final PreviousValuePaddingTimeSeriesSamplingFunction PREVIOUS_VALUE_FUNCTION = new PreviousValuePaddingTimeSeriesSamplingFunction();
  /** Pad with previous value, pad with first value in series if there is insufficient data */
  public static final PreviousAndFirstValuePaddingTimeSeriesSamplingFunction PREVIOUS_AND_FIRST_VALUE_FUNCTION = new PreviousAndFirstValuePaddingTimeSeriesSamplingFunction();

  private static Map<String, TimeSeriesSamplingFunction> s_instances = new HashMap<>();

  static {
    s_instances.put(NO_PADDING, NO_PADDING_FUNCTION);
    s_instances.put(PREVIOUS_VALUE_PADDING, PREVIOUS_VALUE_FUNCTION);
    s_instances.put(PREVIOUS_AND_FIRST_VALUE_PADDING, PREVIOUS_AND_FIRST_VALUE_FUNCTION);
  }

  public static TimeSeriesSamplingFunction getFunction(final String name) {
    final TimeSeriesSamplingFunction function = s_instances.get(name);
    if (function == null) {
      throw new IllegalArgumentException("Could not get function with name " + name);
    }
    return function;
  }
}
