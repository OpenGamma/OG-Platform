/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

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
  SECURITY
  // TODO all the other masters
}
