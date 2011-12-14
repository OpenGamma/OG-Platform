/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.historical.HistoricalMarketDataNormalizer;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * A {@link MarketDataSnapshot} backed by historical data.
 */
public class HistoricalMarketDataSnapshot implements MarketDataSnapshot {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalMarketDataSnapshot.class);

  private final HistoricalMarketDataSpecification _marketDataSpec;
  private final HistoricalTimeSeriesSource _timeSeriesSource;
  private final HistoricalMarketDataFieldResolver _fieldResolver;
  private final HistoricalMarketDataNormalizer _normalizer;
  
  public HistoricalMarketDataSnapshot(HistoricalMarketDataSpecification marketDataSpec, HistoricalTimeSeriesSource timeSeriesSource, HistoricalMarketDataFieldResolver fieldResolver,
      HistoricalMarketDataNormalizer normalizer) {
    _marketDataSpec = marketDataSpec;
    _timeSeriesSource = timeSeriesSource;
    _fieldResolver = fieldResolver;
    _normalizer = normalizer;
  }
  
  @Override
  public Instant getSnapshotTimeIndication() {
    // TODO something better thought-out here
    return getMarketDataSpec().getSnapshotDate().atMidnight().atZone(TimeZone.UTC).toInstant();
  }

  @Override
  public void init() {
    // Nothing to do as we query on-the-fly.
  }
  
  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    // Nothing to do as we query on-the-fly. Unavailable historical values will not become available by waiting, so
    // ignore the timeout.
  }

  @Override
  public Instant getSnapshotTime() {
    return getSnapshotTimeIndication();
  }

  @Override
  public Object query(ValueRequirement requirement) {
    final LocalDate date = getMarketDataSpec().getSnapshotDate();
    final ExternalId identifier = requirement.getTargetSpecification().getIdentifier();
    final ExternalIdBundle identifiers = ExternalIdBundle.of(identifier);
    HistoricalTimeSeries hts = getTimeSeriesSource().getHistoricalTimeSeries(
        getFieldResolver().resolve(requirement.getValueName(), getMarketDataSpec().getTimeSeriesFieldResolverKey()),
        identifiers,
        getMarketDataSpec().getTimeSeriesResolverKey(),
        date,
        true, 
        date, 
        true);
    if (hts == null || hts.getTimeSeries().isEmpty()) {
      s_logger.info("No data for {}", requirement);
      return null;
    }
    final Object rawValue = hts.getTimeSeries().getValue(getMarketDataSpec().getSnapshotDate());
    final Object normalizedValue = getNormalizer().normalize(identifiers, requirement.getValueName(), rawValue);
    if (normalizedValue == null) {
      s_logger.info("Normalization failed for {}, raw value = {}", requirement, rawValue);
      return null;
    } else {
      s_logger.debug("Normalized value for {} = {}", requirement, normalizedValue);
    }
    return normalizedValue;
  }
  
  // TODO: bulk operation could be useful; even if the HTS source doesn't support it, the normalizers could

  //-------------------------------------------------------------------------
  private HistoricalMarketDataSpecification getMarketDataSpec() {
    return _marketDataSpec;
  }
  
  private HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

  private HistoricalMarketDataFieldResolver getFieldResolver() {
    return _fieldResolver;
  }

  private HistoricalMarketDataNormalizer getNormalizer() {
    return _normalizer;
  }

}
