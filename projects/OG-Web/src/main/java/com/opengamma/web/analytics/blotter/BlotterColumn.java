/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.util.ArgumentChecker;

/**
 * The columns in the blotter.
 */
public enum BlotterColumn {

  /**
   * The type.
   */
  TYPE("Type"),
  /**
   * The product.
   */
  PRODUCT("Product"),
  /**
   * The quantity.
   */
  QUANTITY("Quantity"),
  /**
   * The start date.
   */
  START("Start Date"),
  /**
   * The maturity date.
   */
  MATURITY("Maturity Date"),
  /**
   * The rate.
   */
  RATE("Rate"),
  /**
   * The frequency.
   */
  FREQUENCY("Frequency"),
  /**
   * The direction.
   */
  DIRECTION("Pay/Receive"),
  /**
   * The frequency.
   */
  FLOAT_FREQUENCY("Float Frequency"),
  /**
   * The index.
   */
  INDEX("Index");

  private final String _name;

  private BlotterColumn(String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  public String getName() {
    return _name;
  }

}
