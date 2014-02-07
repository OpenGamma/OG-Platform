/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;

/**
 * Enum specifying the types of master in the system.
 */
public enum MasterType {
  /** {@link PortfolioMaster} */
  PORTFOLIO,
  /** {@link PositionMaster} */
  POSITION,
  /** {@link HolidayMaster} */
  HOLIDAY,
  /** {@link SecurityMaster} */
  SECURITY,
  /** {@link HistoricalTimeSeriesMaster} */
  TIME_SERIES,
  /** {@link ConfigMaster} */
  CONFIG,
  /** @link LegalEntityMaster} */
  ORGANIZATION,
  /** @link MarketDataSnapshotMaster} */
  MARKET_DATA_SNAPSHOT
  // TODO all the other masters
}
