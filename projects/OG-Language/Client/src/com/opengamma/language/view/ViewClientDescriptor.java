/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.id.UniqueId;

/**
 * Converts the view name and execution options to/from a string that describes them both.
 */
public final class ViewClientDescriptor {

  /**
   * Type of descriptor; which of the factory methods created it. 
   */
  public static enum Type {
    /**
     * Created by {@link #tickingMarketData}.
     */
    TICKING_MARKET_DATA,
    /**
     * Created by {@link #staticMarketData}.
     */
    STATIC_MARKET_DATA,
    /**
     * Created by {@link #historicalMarketData}.
     */
    HISTORICAL_MARKET_DATA,
    /**
     * Created by {@link #tickingSnapshot}.
     */
    TICKING_SNAPSHOT,
    /**
     * Created by {@link #staticSnapshot}.
     */
    STATIC_SNAPSHOT;
  }

  private static final char SEPARATOR = '~';
  private static final char ESCAPE_CHAR = '$';

  private static final String MARKET_DATA_HISTORICAL = "Historical";
  private static final String MARKET_DATA_LIVE = "Live";
  private static final String MARKET_DATA_USER = "User";
  private static final String STATIC = "Static";
  private static final String TICKING = "Ticking";

  /**
   * Default value of samplePeriod.
   */
  public static final int DEFAULT_SAMPLE_PERIOD = 86400;

  private final Type _type;
  private final UniqueId _viewId;
  private final ViewExecutionOptions _executionOptions;
  private final String _encoded;

  private ViewClientDescriptor(final Type type, final UniqueId descriptorId, final ViewExecutionOptions executionOptions, final String encoded) {
    _type = type;
    _viewId = descriptorId;
    _executionOptions = executionOptions;
    _encoded = encoded;
  }

  public Type getType() {
    return _type;
  }

  public UniqueId getViewId() {
    return _viewId;
  }
  
  public ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  protected static ViewClientDescriptor decode(final String str) {
    final List<String> values = split(str);
    try {
      switch (values.size()) {
        case 1:
          return tickingMarketData(UniqueId.parse(values.get(0)), null);
        case 2:
          if (MARKET_DATA_LIVE.equals(values.get(0))) {
            return tickingMarketData(UniqueId.parse(values.get(1)), null);
          }
          break;
        case 3:
          if (MARKET_DATA_LIVE.equals(values.get(0))) {
            if (STATIC.equals(values.get(2))) {
              return staticMarketData(UniqueId.parse(values.get(1)), null);
            } else {
              return tickingMarketData(UniqueId.parse(values.get(1)), values.get(2));
            }
          } else if (MARKET_DATA_USER.equals(values.get(0))) {
            return staticSnapshot(UniqueId.parse(values.get(1)), UniqueId.parse(values.get(2)));
          }
          break;
        case 4:
          if (MARKET_DATA_LIVE.equals(values.get(0)) && STATIC.equals(values.get(3))) {
            return staticMarketData(UniqueId.parse(values.get(1)), values.get(2));
          } else if (MARKET_DATA_USER.equals(values.get(0)) && TICKING.equals(values.get(3))) {
            return tickingSnapshot(UniqueId.parse(values.get(1)), UniqueId.parse(values.get(2)));
          }
          break;
        case 6:
          if (MARKET_DATA_HISTORICAL.equals(values.get(0))) {
            return historicalMarketData(UniqueId.parse(values.get(1)), Instant.ofEpochSeconds(Long.parseLong(values.get(2))), Instant.ofEpochSeconds(Long.parseLong(values.get(3))),
                DEFAULT_SAMPLE_PERIOD, values.get(4), values.get(5));
          }
          break;
        case 7:
          if (MARKET_DATA_HISTORICAL.equals(values.get(0))) {
            return historicalMarketData(UniqueId.parse(values.get(1)), Instant.ofEpochSeconds(Long.parseLong(values.get(2))), Instant.ofEpochSeconds(Long.parseLong(values.get(3))),
                Integer.parseInt(values.get(4)), values.get(5), values.get(6));
          }
          break;
      }
    } catch (IllegalArgumentException e) {
      // Ignore; drop through to try the "normal" unique identifier below
    }
    // Nothing recognized; assume it's a normal unique identifier and not one of our decorated ones
    return tickingMarketData(UniqueId.parse(str), null);
  }

  public String encode() {
    return _encoded;
  }

  /**
   * Appends a separator character, if this is not the first entry into the buffer, followed by the escaped string.
   * 
   * @param sb string buffer to update
   * @param str string to append
   * @return the updated string buffer
   */
  private static StringBuilder append(final StringBuilder sb, final String str) {
    if (sb.length() > 0) {
      sb.append(SEPARATOR);
    }
    if ((str.indexOf(SEPARATOR) < 0) && (str.indexOf(ESCAPE_CHAR) < 0)) {
      sb.append(str);
    } else {
      for (int i = 0; i < str.length(); i++) {
        final char c = str.charAt(i);
        if (c == SEPARATOR) {
          sb.append(ESCAPE_CHAR).append(c);
        } else if (c == ESCAPE_CHAR) {
          sb.append(ESCAPE_CHAR).append(c);
        } else {
          sb.append(c);
        }
      }
    }
    return sb;
  }

  private static StringBuilder append(final StringBuilder sb, final UniqueId id) {
    return append(sb, id.toString());
  }

  private static StringBuilder append(final StringBuilder sb, final int value) {
    return append(sb, Integer.toString(value));
  }

  private static StringBuilder append(final StringBuilder sb, final long value) {
    return append(sb, Long.toString(value));
  }

  private static StringBuilder append(final StringBuilder sb, final Instant instant) {
    return append(sb, instant.getEpochSeconds());
  }

  private static String escape(final String str) {
    return append(new StringBuilder(), str).toString();
  }

  /**
   * Splits a string on the given separator characters and unescapes the components.
   * 
   * @param str input string
   * @return the split and unescaped components
   */
  private static List<String> split(final String str) {
    final List<String> result = new ArrayList<String>();
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      final char c = str.charAt(i);
      if (c == SEPARATOR) {
        result.add(sb.toString());
        sb.delete(0, sb.length());
      } else if (c == ESCAPE_CHAR) {
        if ((++i) >= str.length()) {
          return Collections.emptyList();
        } else {
          sb.append(str.charAt(i));
        }
      } else {
        sb.append(c);
      }
    }
    result.add(sb.toString());
    return result;
  }

  @Override
  public String toString() {
    return "ViewClientDescriptor[" + encode() + "]";
  }

  /**
   * Returns a descriptor for ticking live market data. The view will recalculate when market data changes and obey time
   * thresholds on the view definition.
   * 
   * @param viewId  unique identifier of the view, not null
   * @param dataSource  the data source, null for the default
   * @return the descriptor
   */
  public static ViewClientDescriptor tickingMarketData(final UniqueId viewId, final String dataSource) {
    final LiveMarketDataSpecification marketDataSpec;
    String encoded;
    if (dataSource == null) {
      // Only encode if normal representation could be confused for an encoded form
      encoded = viewId.toString();
      if (encoded.startsWith(MARKET_DATA_LIVE) || encoded.startsWith(MARKET_DATA_USER)) {
        encoded = escape(encoded);
      }
      marketDataSpec = MarketData.live();
    } else {
      encoded = dataSource == null ? viewId.toString() : append(append(new StringBuilder(MARKET_DATA_LIVE), viewId), dataSource).toString();
      marketDataSpec = MarketData.live(dataSource);
    }
    return new ViewClientDescriptor(Type.TICKING_MARKET_DATA, viewId, ExecutionOptions.infinite(marketDataSpec), encoded);
  }

  /**
   * Returns a descriptor for a static live market data view. The view will recalculate when manually triggered. 
   * 
   * @param viewId  unique identifier of the view, not null
   * @param dataSource  the data source, null for the default
   * @return the descriptor
   */
  public static ViewClientDescriptor staticMarketData(final UniqueId viewId, final String dataSource) {
    final ViewCycleExecutionSequence cycleSequence = new InfiniteViewCycleExecutionSequence();
    final LiveMarketDataSpecification marketDataSpec = dataSource == null ? MarketData.live() : MarketData.live(dataSource);
    final ViewCycleExecutionOptions cycleOptions = new ViewCycleExecutionOptions(null, marketDataSpec);
    final ViewExecutionOptions options = ExecutionOptions.of(cycleSequence, cycleOptions, ExecutionFlags.none().awaitMarketData().get());
    StringBuilder encodingBuilder = append(new StringBuilder(MARKET_DATA_LIVE), viewId);
    if (dataSource != null) {
      append(encodingBuilder, dataSource);
    }
    final String encoded = append(encodingBuilder, STATIC).toString();
    return new ViewClientDescriptor(Type.STATIC_MARKET_DATA, viewId, options, encoded);
  }

  /**
   * Returns a descriptor for a view that can be manually iterated over a historical data sample. Cycles will only trigger
   * manually and will not start until the first trigger is received (allowing injected market data to be set up before the
   * first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param firstValuationTime first valuation time of the sample, not null
   * @param lastValuationTime last valuation time in the sample, not null
   * @param samplePeriod period between each samples in seconds, e.g. 86400 for a day
   * @param timeSeriesResolverKey resolution key for the time series provider, use null for a default
   * @param timeSeriesFieldResolverKey resolution key for the time series provider, use null for a default
   * @return the descriptor
   */
  public static ViewClientDescriptor historicalMarketData(final UniqueId viewId, final InstantProvider firstValuationTime, final InstantProvider lastValuationTime, final int samplePeriod,
      final String timeSeriesResolverKey, final String timeSeriesFieldResolverKey) {
    final Instant firstValuationTimeValue = Instant.of(firstValuationTime);
    final Instant lastValuationTimeValue = Instant.of(lastValuationTime);
    ViewCycleExecutionSequence executionSequence = HistoricalExecutionSequenceFunction.generate(
        firstValuationTimeValue, lastValuationTimeValue, samplePeriod, timeSeriesResolverKey, timeSeriesFieldResolverKey);
    final ViewExecutionOptions options = ExecutionOptions.of(executionSequence, null, ExecutionFlags.none().waitForInitialTrigger().get());
    StringBuilder encoded = append(append(append(new StringBuilder(MARKET_DATA_HISTORICAL), viewId), firstValuationTimeValue), lastValuationTimeValue);
    if (samplePeriod != DEFAULT_SAMPLE_PERIOD) {
      encoded = append(encoded, samplePeriod);
    }
    append(append(encoded, timeSeriesResolverKey != null ? timeSeriesResolverKey : ""), timeSeriesFieldResolverKey != null ? timeSeriesFieldResolverKey : "");
    return new ViewClientDescriptor(Type.HISTORICAL_MARKET_DATA, viewId, options, encoded.toString());
  }

  /**
   * Returns a descriptor for a view that can be manually iterated over a historical data sample. Cycles will only trigger
   * manually and will not start until the first trigger is received (allowing injected market data to be set up before the
   * first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param firstValuationTime first valuation time of the sample, not null
   * @param lastValuationTime last valuation time in the sample, not null
   * @param samplePeriod period between each samples in seconds, e.g. 86400 for a day
   * @return the descriptor
   */
  public static ViewClientDescriptor historicalMarketData(final UniqueId viewId, final InstantProvider firstValuationTime, final InstantProvider lastValuationTime, final int samplePeriod) {
    return historicalMarketData(viewId, firstValuationTime, lastValuationTime, samplePeriod, null, null);
  }

  /**
   * Returns a descriptor for a view that can be manually iterated over a historical data sample. Cycles will only trigger
   * manually and will not start until the first trigger is received (allowing injected market data to be set up before the
   * first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param firstValuationTime first valuation time of the sample, not null
   * @param lastValuationTime last valuation time in the sample, not null
   * @param timeSeriesResolverKey resolution key for the time series provider, use null for a default
   * @param timeSeriesFieldResolverKey resolution key for the time series provider, use null for a default
   * @return the descriptor
   */
  public static ViewClientDescriptor historicalMarketData(final UniqueId viewId, final InstantProvider firstValuationTime, final InstantProvider lastValuationTime, final String timeSeriesResolverKey,
      final String timeSeriesFieldResolverKey) {
    return historicalMarketData(viewId, firstValuationTime, lastValuationTime, DEFAULT_SAMPLE_PERIOD, timeSeriesResolverKey, timeSeriesFieldResolverKey);
  }

  /**
   * Returns a descriptor for a view that can be manually iterated over a historical data sample. Cycles will only trigger
   * manually and will not start until the first trigger is received (allowing injected market data to be set up before the
   * first).
   * 
   * @param viewId unique identifier of the view, not null
   * @param firstValuationTime first valuation time of the sample, not null
   * @param lastValuationTime last valuation time in the sample, not null
   * @return the descriptor
   */
  public static ViewClientDescriptor historicalMarketData(final UniqueId viewId, final InstantProvider firstValuationTime, final InstantProvider lastValuationTime) {
    return historicalMarketData(viewId, firstValuationTime, lastValuationTime, DEFAULT_SAMPLE_PERIOD, null, null);
  }

  /**
   * Returns a descriptor for a view that will use market data from a snapshot. Cycles will be triggered when the snapshot
   * changes (if an unversioned identifier is supplied) or manually.
   * 
   * @param viewId unique identifier of the view, not null
   * @param snapshotId unique identifier of the market data snapshot, not null
   * @return the descriptor 
   */
  public static ViewClientDescriptor tickingSnapshot(final UniqueId viewId, final UniqueId snapshotId) {
    final ViewCycleExecutionSequence cycleSequence = new InfiniteViewCycleExecutionSequence();
    final ViewCycleExecutionOptions cycleOptions = new ViewCycleExecutionOptions(MarketData.user(snapshotId));
    final ExecutionFlags flags = ExecutionFlags.none().triggerOnMarketData();
    final ViewExecutionOptions options = ExecutionOptions.of(cycleSequence, cycleOptions, flags.get());
    final String encoded = append(append(append(new StringBuilder(MARKET_DATA_USER), viewId), snapshotId), TICKING).toString();
    return new ViewClientDescriptor(Type.TICKING_SNAPSHOT, viewId, options, encoded);
  }

  /**
   * Returns a descriptor for a view that will use market data from a snapshot. The view will recalculate when triggered
   * manually. E.g. if created with an unversioned snapshot identifier, changes to the snapshot will not trigger a cycle
   * but will be observed (the latest version will be taken) when a cycle is manually triggered.
   * 
   * @param viewId unique identifier of the view, not null
   * @param snapshotId unique identifier of the market data snapshot, not null
   * @return the descriptor
   */
  public static ViewClientDescriptor staticSnapshot(final UniqueId viewId, final UniqueId snapshotId) {
    final ViewCycleExecutionSequence cycleSequence = new InfiniteViewCycleExecutionSequence();
    final ViewCycleExecutionOptions cycleOptions = new ViewCycleExecutionOptions(MarketData.user(snapshotId));
    final ExecutionFlags flags = ExecutionFlags.none();
    final ViewExecutionOptions options = ExecutionOptions.of(cycleSequence, cycleOptions, flags.get());
    final String encoded = append(append(new StringBuilder(MARKET_DATA_USER), viewId), snapshotId).toString();
    return new ViewClientDescriptor(Type.STATIC_SNAPSHOT, viewId, options, encoded);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ViewClientDescriptor)) {
      return false;
    }
    final ViewClientDescriptor other = (ViewClientDescriptor) o;
    return encode().equals(other.encode());
  }

  @Override
  public int hashCode() {
    return encode().hashCode();
  }

}
