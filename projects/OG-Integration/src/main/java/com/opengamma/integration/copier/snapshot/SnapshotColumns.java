/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot;

import java.util.ArrayList;

/**
 * A list of snapshot columns and
 */
public enum SnapshotColumns {

  /** The Type of snapshot */
  TYPE("type"),

  /** Name of the snapshot */
  NAME("name"),

  /** Instant */
  INSTANT("instant"),

  /** Currency of the yield curve */
  YIELD_CURVE_CURRENCY("yield curve currency"),

  /** Volatility surface unique id */
  SURFACE_TARGET("surface target"),

  /** Volatility surface instrument type */
  SURFACE_INSTRUMENT_TYPE("surface instrument type"),

  /** Volatility surface quote type */
  SURFACE_QUOTE_TYPE("surface quote type"),

  /** Volatility surface quote unit */
  SURFACE_QUOTE_UNITS("surface quote units"),

  /** ExternalId Bundle */
  ID_BUNDLE("external id bundle"),

  /** ValueSnapshot name */
  VALUE_NAME("value name"),

  /** ValueSnapshot object, needed to preserve null valueSnapshots
   *  and deal with objects other than doubles and dates
   */
  VALUE_OBJECT("value object"),

  /** ValueSnapshot market value */
  MARKET_VALUE("market value"),

  /** ValueSnapshot override value  */
  OVERRIDE_VALUE("override value"),

  /** Volatility Surface x cdo-ordinate */
  SURFACE_X("surface x"),

  /** Surface y co-ordinate */
  SURFACE_Y("surface y");

  private String _text;

  SnapshotColumns(String text) {
    this._text = text;

  }

  public String get() {
    return this._text;
  }

  public static SnapshotColumns from(String text) {
    if (text != null) {
      for (SnapshotColumns column : SnapshotColumns.values()) {
        if (text.equalsIgnoreCase(column._text)) {
          return column;
        }
      }
    }
    return null;
  }

  private static String[] getColumns(ArrayList<String> list) {
    String[] columns = new String[list.size()];
    columns = list.toArray(columns);
    return columns;
  }

  /**
   * @return Snapshot columns
   *   {TYPE, NAME, INSTANT, YIELD_CURVE_CURRENCY, SURFACE_TARGET, SURFACE_INSTRUMENT_TYPE, SURFACE_QUOTE_TYPE,
   *    SURFACE_QUOTE_UNITS, ID_BUNDLE, VALUE_NAME, MARKET_VALUE, VALUE_OBJECT, OVERRIDE_VALUE, SURFACE_X, SURFACE_Y }
   */

  public static String[] columns() {
    ArrayList<String> list = new ArrayList<>();
    for (SnapshotColumns column : SnapshotColumns.values()) {
      list.add(column._text);
    }
    return getColumns(list);
  }

}
