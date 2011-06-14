/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.PublicSPI;

/**
 * Resolves a security to the appropriate time-series meta-data.
 * <p>
 * Meta data includes data source, data provider, data field and observation time.
 * The meta-data can be used to lookup the time-series itself.
 */
@PublicSPI
public interface TimeSeriesMetaDataResolver {

  /**
   * Default data field value.
   */
  String DEFAULT_DATA_FIELD = "PX_LAST";

  /**
   * Returns the default meta-data for a security.
   * <p>
   * Looks up security in security master and returns default meta-data based on security type.
   * 
   * @param securityKey  the bundle of identifiers for the security, not null
   * @param configName  the name of the configuration rules to use for resolving meta-data, not null
   * @return the default meta-data, null if the security cannot be found in security master
   */
  TimeSeriesMetaData getDefaultMetaData(IdentifierBundle securityKey, String configName);

}
