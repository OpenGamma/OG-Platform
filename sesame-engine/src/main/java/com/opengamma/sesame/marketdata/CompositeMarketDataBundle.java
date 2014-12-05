/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

/**
 * Market data bundle that delegates to two underlying bundles and returns the first available value.
 */
public class CompositeMarketDataBundle implements MarketDataBundle {

  private final MarketDataBundle _bundle1;
  private final MarketDataBundle _bundle2;

  /**
   * Creates an bundle that delegates to {@code bundle1} and then {@code bundle2}.
   *
   * @param bundle1 the first delegate bundle
   * @param bundle2 the second delegate bundle, used when no value is available from {@code bundle1}
   */
  public CompositeMarketDataBundle(MarketDataBundle bundle1, MarketDataBundle bundle2) {
    _bundle1 = ArgumentChecker.notNull(bundle1, "bundle1");
    _bundle2 = ArgumentChecker.notNull(bundle2, "bundle2");
  }

  @Override
  public <T> Result<T> get(MarketDataId<T> id, Class<T> dataType) {
    Result<T> result = _bundle1.get(id, dataType);
    return result.isSuccess() ? result : _bundle2.get(id, dataType);
  }

  @Override
  public <T> Result<DateTimeSeries<LocalDate, T>> get(MarketDataId<?> id, Class<T> dataType, LocalDateRange dateRange) {
    Result<DateTimeSeries<LocalDate, T>> result = _bundle1.get(id, dataType, dateRange);
    return result.isSuccess() ? result : _bundle2.get(id, dataType, dateRange);
  }

  @Override
  public MarketDataBundle withTime(ZonedDateTime time) {
    return new CompositeMarketDataBundle(_bundle1.withTime(time), _bundle2.withTime(time));
  }

  @Override
  public MarketDataBundle withDate(LocalDate date) {
    return new CompositeMarketDataBundle(_bundle1.withDate(date), _bundle2.withDate(date));
  }
}
