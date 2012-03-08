/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.rowparser;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;

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
  
  private ToolContext _toolContext;
  
  {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern("dd/MM/yyyy");
    CSV_DATE_FORMATTER = builder.toFormatter();
    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyy-MM-dd");
    OUTPUT_DATE_FORMATTER = builder.toFormatter();
  }

  public RowParser(ToolContext toolContext) {
    _toolContext = toolContext;
  }
  
  /**
   * Creates a new row parser for the specified security type and tool context
   * @param securityName  the type of the security for which a row parser is to be created
   * @param toolContext   the tool context for the row parser (for access to masters and sources)
   * @return              the RowParser class for the specified security type, or null if unable to identify a suitable parser
   */
  public static RowParser newRowParser(String securityName, ToolContext toolContext) {
    // Now using the experimental JodaBean parser
    return new JodaBeanParser(securityName, toolContext);
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

  public ToolContext getToolContext() {
    return _toolContext;
  }

  public void setToolContext(ToolContext toolContext) {
    _toolContext = toolContext;
  }

  public abstract String[] getColumns();
  
}
