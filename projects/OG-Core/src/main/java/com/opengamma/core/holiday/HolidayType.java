/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday;

import com.opengamma.util.PublicAPI;

/**
 * The type of a holiday.
 * <p>
 * Holidays are categorized into fixed types.
 */
@PublicAPI
public enum HolidayType {

  /**
   * A retail bank holiday.
   * These occur when retail banks throughout the region are closed.
   * This often affects other businesses and government departments.
   */
  BANK,
  /**
   * An exchange trading holiday.
   * These occur when an exchange, such as the London Stock Exchange, is closed
   * and trading is not possible.
   */
  TRADING,
  /**
   * An exchange settlement holiday.
   * These occur when exchange-based trades cannot be settled.
   * This may be because the banks, clearing agency or market is closed.
   */
  SETTLEMENT,
  /**
   * A currency (foreign exchange) holiday.
   * These occur when the foreign exchange market for a currency is closed,
   * such as when the central bank or clearing agency is closed.
   */
  CURRENCY,
  /**
   * A custom holiday type.
   */
  CUSTOM

}
