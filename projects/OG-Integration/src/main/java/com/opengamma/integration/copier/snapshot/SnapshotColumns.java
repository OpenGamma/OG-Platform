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
  TYPE("Type"),

  /** Name of the snapshot */
  NAME("Name"),

  /** Instant */
  INSTANT("Instant"),

  /** Valitaility surface unique id */
  SURFACE_TARGET("Surface Target"),

  /** Volatility surface intrument type */
  SURFACE_INSTRUMENT_TYPE("Surface Instrument Type"),

  /** Volatility surface quote type */
  SURFACE_QUOTE_TYPE("Surface Quote Type"),

  /** Volatility surface quote unit */
  SURFACE_QUOTE_UNITS("Surface Quote Units"),

  /** ExternalId Bundle */
  ID_BUNDLE("External ID Bundle"),

  /** ValueSnapshot name */
  VALUE_NAME("Value Name"),

  /** ValueSnapshot market value */
  MARKET_VALUE("Market Value"),

  /** ValueSnapshot override value  */
  OVERRIDE_VALUE("Override Value"),

  /** Volatility Surface x cdo-ordinate */
  SURFACE_X("Surface X"),

  /** Surface y co-ordinate */
  SURFACE_Y("Surface Y");

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

  /**
   * @return Snapshot columns
   *   {TYPE, NAME, INSTANT, SURFACE_TARGET, SURFACE_INSTRUMENT_TYPE, SURFACE_QUOTE_TYPE,
   *    SURFACE_QUOTE_UNITS, ID_BUNDLE, VALUE_NAME, MARKET_VALUE, OVERRIDE_VALUE, SURFACE_X, SURFACE_Y }
   */

  public static ArrayList<String> columns() {
    ArrayList<String> columns = new ArrayList<>();
    for (SnapshotColumns column : SnapshotColumns.values()) {
      columns.add(column._text);
    }
    return columns;
  }
}
