/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.tuple.Pair;

/**
 * A {@link MarketDataSnapshot} backed by historical data.
 */
public class HistoricalMarketDataSnapshot extends AbstractMarketDataSnapshot {

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
  public Instant getSnapshotTime() {
    return getSnapshotTimeIndication();
  }

  private Object query(final ExternalIdBundle identifiers, final String valueName) {
    final LocalDate date = getMarketDataSpec().getSnapshotDate();
    HistoricalTimeSeries hts = getTimeSeriesSource().getHistoricalTimeSeries(
        getFieldResolver().resolve(valueName, getMarketDataSpec().getTimeSeriesFieldResolverKey()),
        identifiers,
        getMarketDataSpec().getTimeSeriesResolverKey(),
        date,
        true, 
        date, 
        true);
    if (hts == null || hts.getTimeSeries().isEmpty()) {
      s_logger.info("No time-series for {}, {}", identifiers, valueName);
      return null;
    }
    return hts.getTimeSeries().getValue(getMarketDataSpec().getSnapshotDate());
  }

  @Override
  public Object query(ValueRequirement requirement) {
    final ExternalIdBundle identifiers = ExternalIdBundle.of(requirement.getTargetSpecification().getIdentifier());
    final Object rawValue = query(identifiers, requirement.getValueName());
    if (rawValue == null) {
      s_logger.info("No data point for {}", requirement);
      return null;
    }
    final Object normalizedValue = getNormalizer().normalize(identifiers, requirement.getValueName(), rawValue);
    if (normalizedValue == null) {
      s_logger.info("Normalization failed for {}, raw value = {}", requirement, rawValue);
      return null;
    }
    s_logger.debug("Normalized value for {} = {}", requirement, normalizedValue);
    return normalizedValue;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Map<ValueRequirement, Object> query(final Set<ValueRequirement> requirements) {
    // The raw values are fetched sequentially, but the normalization can be done with a bulk operation.
    final Map<ValueRequirement, Object> result = Maps.newHashMapWithExpectedSize(requirements.size());
    final Map<Pair<ExternalIdBundle, String>, Object> request = Maps.newHashMapWithExpectedSize(requirements.size());
    for (ValueRequirement requirement : requirements) {
      final ExternalIdBundle identifiers = ExternalIdBundle.of(requirement.getTargetSpecification().getIdentifier());
      final Object rawValue = query(identifiers, requirement.getValueName());
      if (rawValue != null) {
        final Pair<ExternalIdBundle, String> key = Pair.of(identifiers, requirement.getValueName());
        request.put(key, rawValue);
        result.put(requirement, key);
      }
    }
    s_logger.debug("Raw values = {}", request);
    final Map<Pair<ExternalIdBundle, String>, Object> response = getNormalizer().normalize(request);
    for (ValueRequirement requirement : requirements) {
      final Pair<ExternalIdBundle, String> key = (Pair<ExternalIdBundle, String>) result.remove(requirement);
      if (key != null) {
        final Object normalizedValue = response.get(key);
        if (normalizedValue != null) {
          result.put(requirement, normalizedValue);
        }
      }
    }
    s_logger.debug("Normalized values = {}", result);
    return result;
  }

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
