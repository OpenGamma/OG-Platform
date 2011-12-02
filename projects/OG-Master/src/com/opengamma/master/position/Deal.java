/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import org.joda.beans.Bean;

import com.opengamma.util.PublicSPI;

/**
 * An empty interface for a deal.
 * <p>
 * The trade interface and class in OpenGamma is intended to model a trade
 * from the perspective of risk analytics.
 * The deal interface provides a hook to add more detail about the trade,
 * or "deal", from another perspective, such as trade booking.
 */
@PublicSPI
public interface Deal extends Bean {
  /**
   * Deal prefix key
   */
  String DEAL_PREFIX = "Deal~";
  /**
   * Deal Classname key
   */
  String DEAL_CLASSNAME = DEAL_PREFIX + "JavaClass";
  /**
   * Deal type key
   */
  String DEAL_TYPE = DEAL_PREFIX + "dealType";
}
