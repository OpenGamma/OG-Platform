/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.id.UniqueId;

/**
 * {@link RowTarget} for a row that contains an OTC trade / position. There is one position for each OTC trade
 * so they are displayed as a single row.
 */
public class OtcTradeTarget extends RowTarget {

  /**
   * @param name The row name
   * @param id The position ID
   */
  /* package */ OtcTradeTarget(String name, UniqueId id) {
    super(name, id);
  }
}
