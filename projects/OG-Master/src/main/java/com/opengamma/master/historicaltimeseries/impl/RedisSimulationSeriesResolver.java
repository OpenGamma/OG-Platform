/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.master.historicaltimeseries.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;

/**
 * Implements {@link HistoricalTimeSeriesResolver} on top of any Redis HTS source.
 */
public class RedisSimulationSeriesResolver implements HistoricalTimeSeriesResolver {

  private static final Logger s_logger = LoggerFactory.getLogger(RedisSimulationSeriesResolver.class);
  private final HistoricalTimeSeriesSource _source;

  public RedisSimulationSeriesResolver(HistoricalTimeSeriesSource source) {
    _source = source;
  }

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
                                                      String resolutionKey) {
    if (identifierBundle.isEmpty()) {
      return null; // is this the correct action?
    } else if (identifierBundle.size() > 1) {
      s_logger.warn("Attempted to call RedisSimulationSeriesSource with bundle {}. Calls with more than 1 entry in ID bundle are probably misuse of this class.", identifierBundle);
    }
    ExternalId externalId = identifierBundle.getExternalIds().iterator().next();
    final UniqueId uniqueId = UniqueId.of(externalId.getScheme().getName(), externalId.getValue());
    //TODO: This should just be an existence check
    if (_source.getHistoricalTimeSeries(uniqueId, null, true, null, true) == null) {
      return null; // check timeseries actually exists
    }
    if (MarketDataRequirementNames.MARKET_VALUE != dataField) {
      s_logger.warn("Redis simulation asked for {} for {}, can only handle market value.", dataField, externalId);
      return null;
    }
    ManageableHistoricalTimeSeriesInfo htsInfo = new ManageableHistoricalTimeSeriesInfo() {
      private static final long serialVersionUID = 1L;

      @Override
      public UniqueId getUniqueId() {
        return uniqueId;
      }

      @Override
      public ExternalIdBundleWithDates getExternalIdBundle() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getDataField() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getDataSource() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getDataProvider() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getObservationTime() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public ObjectId getTimeSeriesObjectId() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

    };
    HistoricalTimeSeriesResolutionResult result = new HistoricalTimeSeriesResolutionResult(htsInfo);
    return result;
  }
}
