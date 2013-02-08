/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.util.ArgumentChecker;

/**
*
*/
public enum BlotterColumn {

  TYPE("Type"),
  PRODUCT("Product"),
  QUANTITY("Quantity"),
  START("Start Date"),
  MATURITY("Maturity Date"),
  RATE("Rate"),
  FREQUENCY("Frequency"),
  DIRECTION("Pay/Receive"),
  FLOAT_FREQUENCY("Float Frequency"),
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
