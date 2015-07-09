/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.sesame.marketdata.GatheringMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.timeseries.date.DateTimeSeries;

/**
 * Market data environment that only exists to return a {@link GatheringMarketDataBundle}.
 * <p>
 * The only method that works is {@link #toBundle()}, the others all throw {@code UnsupportedOperationException}.
 */
class GatheringMarketDataEnvironment implements MarketDataEnvironment {

  private final GatheringMarketDataBundle _bundle;
  private final ZonedDateTime _valuationTime;

  GatheringMarketDataEnvironment(GatheringMarketDataBundle bundle, ZonedDateTime valuationTime) {
    _bundle = bundle;
    _valuationTime = valuationTime.withZoneSameInstant(ZoneOffset.UTC);
  }

  @Override
  public Map<SingleValueRequirement, Object> getData() {
    throw new UnsupportedOperationException("getData not implemented");
  }

  @Override
  public Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> getTimeSeries() {
    throw new UnsupportedOperationException("getTimeSeries not implemented");
  }

  @Override
  public ZonedDateTime getValuationTime() {
    return _valuationTime;
  }

  @Override
  public MarketDataEnvironmentBuilder toBuilder() {
    throw new UnsupportedOperationException("toBuilder not implemented");
  }

  @Override
  public MarketDataBundle toBundle() {
    return _bundle;
  }
}
