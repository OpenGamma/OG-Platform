/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup;

import com.opengamma.util.ArgumentChecker;

/**
* Defines an attribute on a security. The {@link SecurityAttributeMapper} can then be used to
* map the attribute to a field on a security.
*/
public enum SecurityAttribute {

  /**
   * The security type.
   */
  TYPE("Type"),

  /**
   * The security product.
   */
  PRODUCT("Product"),

  /**
   * The quantity of the security.
   */
  QUANTITY("Quantity"),

  /**
   * The start date of the security.
   */
  START("Start Date"),

  /**
   * The maturity date of the security.
   */
  MATURITY("Maturity Date"),

  /**
   * The rate of the security.
   */
  RATE("Rate"),

  /**
   * The frequency of the security.
   */
  FREQUENCY("Frequency"),

  /**
   * The direction of the security.
   */
  DIRECTION("Pay/Receive"),

  /**
   * The float frequency of the security.
   */
  FLOAT_FREQUENCY("Float Frequency"),

  /**
   * The index of the security.
   */
  INDEX("Index");

  private final String _name;

  private SecurityAttribute(String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
