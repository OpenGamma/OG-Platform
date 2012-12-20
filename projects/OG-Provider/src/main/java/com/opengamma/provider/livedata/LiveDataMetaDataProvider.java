/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.livedata;

import com.opengamma.util.PublicSPI;

/**
 * A provider of live data meta-data.
 * <p>
 * This provides access to a data source for live data.
 * For example, major data sources provide ticking equity prices.
 * <p>
 * Since live data is best expressed via JMS, this provider only provides a basic
 * description of how to access the underlying data source.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface LiveDataMetaDataProvider {

  /**
   * Queries the meta-data for the live data server.
   * <p>
   * This is typically used to obtain the connection information.
   * 
   * @return the requested meta-data, not null
   * @throws RuntimeException if a problem occurs
   */
  LiveDataMetaData metaData();

  /**
   * Queries the meta-data for the live data server.
   * <p>
   * This is typically used to obtain the connection information.
   * 
   * @param request  the meta-data request, not null
   * @return the requested meta-data, not null
   * @throws RuntimeException if a problem occurs
   */
  LiveDataMetaDataProviderResult metaData(LiveDataMetaDataProviderRequest request);

}
