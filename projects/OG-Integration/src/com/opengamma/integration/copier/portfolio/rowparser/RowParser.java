/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.rowparser;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * An abstract row parser class, to be specialised for parsing a specific security/trade/position type from a row's data
 */
public abstract class RowParser {

  // CSOFF
  /** Standard date-time formatter for the input. */
  protected DateTimeFormatter CSV_DATE_FORMATTER;
  /** Standard date-time formatter for the output. */
  protected DateTimeFormatter OUTPUT_DATE_FORMATTER;
  /** Standard rate formatter. */
  protected DecimalFormat RATE_FORMATTER = new DecimalFormat("0.###%");
  /** Standard notional formatter. */
  protected DecimalFormat NOTIONAL_FORMATTER = new DecimalFormat("0,000");
  // CSON
  
  {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyy-MM-dd");
    CSV_DATE_FORMATTER = builder.toFormatter();
    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyy-MM-dd");
    OUTPUT_DATE_FORMATTER = builder.toFormatter();
  }
  
  /**
   * Constructs a row from the supplied trade.
   * @param trade The trade to convert
   * @return      The mapping from column names to contents of the current row
  */
  public Map<String, String> constructRow(ManageableTrade trade) {
    return new HashMap<String, String>();
  }

  /**
   * Constructs a row from the supplied position.
   * @param position The position to convert
   * @return      The mapping from column names to contents of the current row
  */
  public Map<String, String> constructRow(ManageablePosition position) {
    return new HashMap<String, String>();
  }

  /**
   * Constructs a row from the supplied security.
   * @param security The security to convert
   * @return      The mapping from column names to contents of the current row
  */
  public Map<String, String> constructRow(ManageableSecurity security) {
    return new HashMap<String, String>();
  }
  
  /**
   * Constructs a row from the supplied security, position and trade.
   * @param security  The security to convert
   * @param position  The position to convert
   * @param trade     The trade to convert
   * @return          The mapping from column names to contents of the current row
   */
  public Map<String, String> constructRow(ManageableSecurity security, ManageablePosition position, ManageableTrade trade) {
    
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(trade, "trade");
    
    Map<String, String> result = new HashMap<String, String>();
    Map<String, String> securityRow = constructRow(security);
    Map<String, String> positionRow = constructRow(position);
    Map<String, String> tradeRow = constructRow(trade);
    if (securityRow != null) {
      result.putAll(securityRow);
    }
    if (positionRow != null) {
      result.putAll(positionRow);
    }
    if (tradeRow != null) {
      result.putAll(tradeRow);
    }
    return result;
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
    
    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");
    
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

  /**
   * Gets the list of column names that this particular row parser knows of
   * @return  A string array containing the column names
   */
  public abstract String[] getColumns();

  public int getSecurityHashCode() {
    return 0;
  }
  
 
  public static String getWithException(Map<String, String> fieldValueMap, String fieldName) {
    
    String result = fieldValueMap.get(fieldName);
    if (result == null) {
      System.err.println(fieldValueMap);
      throw new IllegalArgumentException("Could not find field '" + fieldName + "'");
    }
    return result;
  }

  public LocalDate getDateWithException(Map<String, String> fieldValueMap, String fieldName) {
    return getDateWithException(fieldValueMap, fieldName, CSV_DATE_FORMATTER);
  }

  public static LocalDate getDateWithException(Map<String, String> fieldValueMap, String fieldName, DateTimeFormatter formatter) {
    return LocalDate.parse(getWithException(fieldValueMap, fieldName), formatter);
  }


  public static void addValueIfNotNull(Map<String, String> map, String key, Object value) {
    if (value != null) {
      map.put(key, value.toString());
    }
  }

}
