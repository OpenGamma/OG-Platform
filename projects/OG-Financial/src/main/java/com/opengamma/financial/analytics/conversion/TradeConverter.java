/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.position.Trade;

/**
 * Common interface for converting trades into instrument definitions.
 */
public interface TradeConverter {

  /**
   * Converts a trade into an instrument definition.
   * @param trade the trade containing the instrument.
   * @return an instrument definition instance.
   */
  InstrumentDefinition<?> convert(Trade trade);
}
