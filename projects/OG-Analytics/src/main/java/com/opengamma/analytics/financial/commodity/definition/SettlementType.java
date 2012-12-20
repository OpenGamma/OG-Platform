/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

/**
 * Settlement types for {@code CommodityFutureDefinition}
 */
public enum SettlementType {
  /**
   * cash settlement - i.e. no delivery
   */
  CASH,
  /**
   * physical delivery - i.e. delivery
   */
  PHYSICAL,
}
