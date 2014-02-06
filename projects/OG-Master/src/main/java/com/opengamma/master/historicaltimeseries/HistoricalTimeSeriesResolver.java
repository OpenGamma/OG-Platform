/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import org.threeten.bp.LocalDate;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.PublicSPI;

/**
 * Resolves identifiers, such as those of a security, together with a requested data field, to the appropriate historical time-series.
 */
@PublicSPI
public interface HistoricalTimeSeriesResolver extends com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolver, ChangeProvider {

  /**
   * Resolves a time-series from a bundle of identifiers and a data field.
   * <p>
   * The desired series is specified by identifier bundle, typically a security, and the data field name, such as "price" or "volume". However, the underlying sources of data may contain multiple
   * matching time-series. The resolver allows the preferred series to be chosen based on a key. The meaning of the key is resolver-specific, and it might be treated as a DSL or a configuration key.
   * 
   * @param identifierBundle the bundle of identifiers to resolve, not null
   * @param identifierValidityDate the date that the identifier must be valid on, null to use all identifiers
   * @param dataSource the data source name associated with the time-series, null for any
   * @param dataProvider the data provider name associated with the time-series, null for any
   * @param dataField the type of data that the time-series represents, not null
   * @param resolutionKey a key defining how the resolution is to occur, null for the default best match
   * @return a resolution result for the best match, null if unable to find a match
   */
  @Override
  HistoricalTimeSeriesResolutionResult resolve(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, String resolutionKey);

  // REVIEW 2012-07-10 Andrew -- Should this be in OG-Core rather than OG-Master? Should it be possible to obtain the resolver from the HistoricalTimeSeriesSource?

}
