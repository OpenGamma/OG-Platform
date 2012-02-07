/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * An abstract row parser class, to be specialised for parsing a specific security/trade/position type from a row's data
 */
public abstract class RowParser {

  /** Standard date-time formatter for the input */
  protected DateTimeFormatter CSV_DATE_FORMATTER;
  /** Standard date-time formatter for the output */
  protected DateTimeFormatter OUTPUT_DATE_FORMATTER;
  /** Standard rate formatter */
  protected DecimalFormat RATE_FORMATTER = new DecimalFormat("0.###%");
  /** Standard notional formatter */
  protected DecimalFormat NOTIONAL_FORMATTER = new DecimalFormat("0,000");

  {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern("dd/MM/yyyy");
    CSV_DATE_FORMATTER = builder.toFormatter();
    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyy-MM-dd");
    OUTPUT_DATE_FORMATTER = builder.toFormatter();
  }

  /**
   * Constructs one or more securities associated with the supplied row. As a convention, the underlying security
   * is returned at array location 0.
   * @param row The mapping between column names and contents for the current row
   * @return An array of securities constructed from the current row's data; underlying is at index 0
   */
  public abstract ManageableSecurity[] constructSecurity(Map<String, String> row);
  
  /**
   * Constructs a position associated with the supplied row. 
   * @param row The mapping between column names and contents for the current row
   * @param security  The associated security
   * @return The constructed position
   */
  public ManageablePosition constructPosition(Map<String, String> row, ManageableSecurity security) {
    return new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
  }
  
  /**
   * Constructs a trade associated with the supplied row.
   * @param row The mapping between column names and contents for the current row
   * @param security  The associated security
   * @param position  The associated position
   * @return  The constructed trade
   */
  public ManageableTrade constructTrade(Map<String, String> row, ManageableSecurity security, ManageablePosition position) {
    return null;
  }
  
  protected String getWithException(Map<String, String> fieldValueMap, String fieldName) {
    String result = fieldValueMap.get(fieldName);
    if (result == null) {
      System.err.println(fieldValueMap);
      throw new IllegalArgumentException("Could not find field '" + fieldName + "'");
    }
    return result;
  }

  protected LocalDate getDateWithException(Map<String, String> fieldValueMap, String fieldName) {
    return LocalDate.parse(getWithException(fieldValueMap, fieldName), CSV_DATE_FORMATTER);
  }

}
