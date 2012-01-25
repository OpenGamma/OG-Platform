/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.portfolioloader;

import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * An abstract row parser class, to be specialised for parsing a specific security/trade/position type from a row's data
 */
public abstract class RowParser {

  /** Standard date-time formatter for the input */
  private static final DateTimeFormatter CSV_DATE_FORMATTER;
  /** Standard date-time formatter for the output */
  private static final DateTimeFormatter OUTPUT_DATE_FORMATTER;

  static {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern("dd/MM/yyyy");
    CSV_DATE_FORMATTER = builder.toFormatter();
    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyy-MM-dd");
    OUTPUT_DATE_FORMATTER = builder.toFormatter();
  }

  public abstract ManageableSecurity constructSecurity(Map<String, String> eqFutureDetails);
  
  public abstract ManageableTrade constructTrade(Map<String, String> eqFutureDetails, ManageableSecurity security);
  
  
  protected static String getWithException(Map<String, String> fieldValueMap, String fieldName) {
    String result = fieldValueMap.get(fieldName);
    if (result == null) {
      System.err.println(fieldValueMap);
      throw new IllegalArgumentException("Could not find field '" + fieldName + "'");
    }
    return result;
  }

  protected static LocalDate getDateWithException(Map<String, String> fieldValueMap, String fieldName) {
    return LocalDate.parse(getWithException(fieldValueMap, fieldName), CSV_DATE_FORMATTER);
  }

}
