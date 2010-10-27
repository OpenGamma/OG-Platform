/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday;

/**
 * The type of a holiday.
 * <p>
 * Holidays are categorized into fixed types.
 */
public enum HolidayType {

  /**
   * A trading holiday, when no trades occur.
   */
  TRADING,
  /**
   * A settlement holiday, when no settlements occur.
   */
  SETTLEMENT,
  /**
   * A bank holiday, when banks are closed.
   */
  BANK,
  /**
   * A currency holiday, when no currency changes occur.
   */
  CURRENCY;

}
