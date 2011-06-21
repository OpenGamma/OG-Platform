/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * Resolves an identifier, such as a security, to the appropriate historical time-series information.
 * <p>
 * Time-series information includes data source, data provider, data field and observation time.
 * This can be used to lookup the time-series itself.
 */
@PublicSPI
public interface HistoricalTimeSeriesResolver {

  /**
   * Find the best matching time-series for an identifier and data field.
   * <p>
   * The desired series is specified by identifier bundle, typically a security,
   * and the data type, such as "price" or "volume".
   * However, the underlying sources of data may contain multiple matching time-series.
   * The resolver allows the preferred series to be chosen based on a key.
   * The meaning of the key is resolver specific, and it might be treated as a DSL or a configuration key.
   * 
   * @param identifierBundle  the bundle of identifiers to resolve, not null
   * @param type  the type of data that the time-series represents, not null
   * @param resolutionKey  a key defining how the resolution is to occur, null for the default best match
   * @return the best matching time-series unique identifier, null if unable to find a match
   */
  UniqueIdentifier resolve(String type, IdentifierBundle identifierBundle, String resolutionKey);

}
