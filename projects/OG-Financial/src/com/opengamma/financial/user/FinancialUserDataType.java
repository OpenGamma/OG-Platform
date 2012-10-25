/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

/**
 * Enumeration of the user supplied data masters.
 */
public enum FinancialUserDataType {

  /**
   * Value from an InterpolatedYieldCurveDefinitionMaster
   */
  INTERPOLATED_YIELD_CURVE_DEFINITION,

  /**
   * Value from a SecurityMaster.
   */
  SECURITY,

  /**
   * Value from a PositionMaster.
   */
  POSITION,

  /**
   * Value from a PortfolioMaster.
   */
  PORTFOLIO,

  /**
   * Value from a ConfigSource.
   */
  VIEW_DEFINITION,
  
  /**
   * Value from a MarketDataSnapshotMaster.
   */
  MARKET_DATA_SNAPSHOT

}
