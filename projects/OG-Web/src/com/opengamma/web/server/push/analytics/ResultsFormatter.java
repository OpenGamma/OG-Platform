/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.conversion.ResultConverter;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 *
 */
public class ResultsFormatter {

  private static final Logger s_logger = LoggerFactory.getLogger(ResultsFormatter.class);

  /** Maximum string length for values that are sent to the grid. */
  private static final int MAX_VALUE_LENGTH = 100;

  private final ResultConverterCache _converters;

  public ResultsFormatter(ResultConverterCache converters) {
    ArgumentChecker.notNull(converters, "converters");
    _converters = converters;
  }

  /**
   * Returns a formatted version of a value suitable for display in the UI.
   * @param value The value
   * @param valueSpec The value's specification
   * @return {@code null} if the value is {@code null}, otherwise a formatted version of a value suitable
   * for display in the UI.
   */
  /* package */ Object formatValueForDisplay(Object value, ValueSpecification valueSpec) {
    if (value == null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    ResultConverter<Object> converter = (ResultConverter<Object>) _converters.getConverterForType(value.getClass());
    if (converter == null) {
      return trim(value.toString());
    }
    try {
      // TODO mode
      return converter.convertForDisplay(_converters, valueSpec, value, ConversionMode.SUMMARY);
    } catch (Exception e) {
      s_logger.error("Exception when converting: ", e);
      return "Conversion Error";
    }
  }

  /* package */ Object formatValueForHistory(Object value, ValueSpecification valueSpec) {
    throw new UnsupportedOperationException();
  }

  /**
   * If a string is shorter than {@link #MAX_VALUE_LENGTH} it is returned, otherwise it is trimmed and
   * "..." is appended.
   * @param str The string to be trimmed.
   * @return The trimmed string.
   */
  private static String trim(String str) {
    if (str.length() < MAX_VALUE_LENGTH) {
      return str;
    } else {
      return str.substring(0, MAX_VALUE_LENGTH) + "...";
    }
  }
}
