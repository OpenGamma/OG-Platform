/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.PublicSPI;

/**
 * Resolves an identifier, such as a security, to the appropriate historical data information.
 * <p>
 * Time-series information includes data source, data provider, data field and observation time.
 * This can be used to lookup the time-series itself.
 */
@PublicSPI
public interface HistoricalDataInfoResolver {

  /**
   * Default data field value.
   */
  String DEFAULT_DATA_FIELD = "PX_LAST";

  /**
   * Find the time-series info for a security.
   * <p>
   * This returns suitable time-series information for the specified security.
   * 
   * @param securityKey  the bundle of identifiers for the security, not null
   * @param configName  the name of the configuration rules to use for resolving info, not null
   * @return the matched info, null if unable to find a match
   */
  HistoricalDataInfo getInfo(IdentifierBundle securityKey, String configName);

}
