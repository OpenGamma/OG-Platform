/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot;

/**
 * A list of snapshot columns and
 */
public enum SnapshotType {

  /** The Type snapshot name */
  NAME("name"),

  /** The Type snapshot name */
  BASIS_NAME("basis name"),

  /** The Type snapshot name */
  CURVE("curve"),

  /** The Type snapshot name */
  YIELD_CURVE("yield curve"),

  /** The Type snapshot name */
  GLOBAL_VALUES("global values"),

  /** The Type snapshot name */
  VOL_SURFACE("volatility surface");

  private String _text;

  SnapshotType(String text) {
    this._text = text;

  }

  public String get() {
    return this._text;
  }

  public static SnapshotType from(String text) {
    if (text != null) {
      for (SnapshotType column : SnapshotType.values()) {
        if (text.equalsIgnoreCase(column._text)) {
          return column;
        }
      }
    }
    return null;
  }

}
