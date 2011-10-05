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
     * Created by {@link #sampleMarketData}.
     */
    SAMPLE_MARKET_DATA,
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

  private static final String MARKET_DATA_LIVE = "LiveMarketData";
  private static final String MARKET_DATA_USER = "UserMarketData";
  private static final String TICKING = "Ticking";

  private static final int DEFAULT_SAMPLE_PERIOD = 86400;

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
          return tickingMarketData(UniqueId.parse(values.get(0)));
        case 2:
          if (MARKET_DATA_LIVE.equals(values.get(0))) {
            return tickingMarketData(UniqueId.parse(values.get(1)));
          }
          break;
        case 3:
          if (MARKET_DATA_LIVE.equals(values.get(0))) {
            return staticMarketData(UniqueId.parse(values.get(1)), Instant.ofEpochSeconds(Long.parseLong(values.get(2))));
          } else if (MARKET_DATA_USER.equals(values.get(0))) {
            return staticSnapshot(UniqueId.parse(values.get(1)), UniqueId.parse(values.get(2)));
          }
          break;
        case 4:
          if (MARKET_DATA_LIVE.equals(values.get(0))) {
            return sampleMarketData(UniqueId.parse(values.get(1)), Instant.ofEpochSeconds(Long.parseLong(values.get(2))), Instant.ofEpochSeconds(Long.parseLong(values.get(3))), DEFAULT_SAMPLE_PERIOD);
          } else if (MARKET_DATA_USER.equals(values.get(0)) && TICKING.equals(values.get(3))) {
            return tickingSnapshot(UniqueId.parse(values.get(1)), UniqueId.parse(values.get(2)));
          }
          break;
        case 5:
          if (MARKET_DATA_LIVE.equals(values.get(0))) {
            return sampleMarketData(UniqueId.parse(values.get(1)), Instant.ofEpochSeconds(Long.parseLong(values.get(2))), Instant.ofEpochSeconds(Long.parseLong(values.get(3))),
                Integer.parseInt(values.get(4)));
          }
          break;
      }
    } catch (IllegalArgumentException e) {
      // Ignore; drop through to try the "normal" unique identifier below
    }
    // Nothing recognized; assume it's a normal unique identifier and not one of our decorated ones
    return tickingMarketData(UniqueId.parse(str));
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
    return append(sb, instant.toEpochMillisLong() / 1000L);
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

  public static ViewClientDescriptor tickingMarketData(final UniqueId viewId) {
    String encoded = viewId.toString();
    // Only encode if normal representation could be confused for an encoded form
    if (encoded.startsWith(MARKET_DATA_LIVE) || encoded.startsWith(MARKET_DATA_USER)) {
      encoded = escape(encoded);
    }
    return new ViewClientDescriptor(Type.TICKING_MARKET_DATA, viewId, ExecutionOptions.infinite(MarketData.live()), encoded);
  }

  public static ViewClientDescriptor staticMarketData(final UniqueId viewId, final InstantProvider valuationTime) {
    final Instant valuationTimeValue = Instant.of(valuationTime);
    final ViewExecutionOptions options = ExecutionOptions.singleCycle(valuationTimeValue, MarketData.live());
    final String encoded = append(append(new StringBuilder(MARKET_DATA_LIVE), viewId), valuationTimeValue).toString();
    return new ViewClientDescriptor(Type.STATIC_MARKET_DATA, viewId, options, encoded);
  }

  public static ViewClientDescriptor sampleMarketData(final UniqueId viewId, final InstantProvider firstValuationTime, final InstantProvider lastValuationTime, final int samplePeriod) {
    final Instant firstValuationTimeValue = Instant.of(firstValuationTime);
    final Instant lastValuationTimeValue = Instant.of(lastValuationTime);
    // TODO: want a batch of execution for the valuation sample period
    final ViewExecutionOptions options = ExecutionOptions.singleCycle(firstValuationTimeValue, MarketData.live());
    StringBuilder encoded = append(append(append(new StringBuilder(MARKET_DATA_LIVE), viewId), firstValuationTimeValue), lastValuationTimeValue);
    if (samplePeriod != DEFAULT_SAMPLE_PERIOD) {
      encoded = append(encoded, samplePeriod);
    }
    return new ViewClientDescriptor(Type.STATIC_MARKET_DATA, viewId, options, encoded.toString());
  }

  public static ViewClientDescriptor sampleMarketData(final UniqueId viewId, final InstantProvider firstValuationTime, final InstantProvider lastValuationTime) {
    return sampleMarketData(viewId, firstValuationTime, lastValuationTime, DEFAULT_SAMPLE_PERIOD);
  }

  public static ViewClientDescriptor tickingSnapshot(final UniqueId viewId, final UniqueId snapshotId) {
    final ViewCycleExecutionSequence cycleSequence = new InfiniteViewCycleExecutionSequence();
    final ViewCycleExecutionOptions cycleOptions = new ViewCycleExecutionOptions(MarketData.user(snapshotId));
    final ExecutionFlags flags = ExecutionFlags.none().triggerOnMarketData();
    final ViewExecutionOptions options = ExecutionOptions.of(cycleSequence, cycleOptions, flags.get());
    final String encoded = append(append(append(new StringBuilder(MARKET_DATA_USER), viewId), snapshotId), TICKING).toString();
    return new ViewClientDescriptor(Type.TICKING_SNAPSHOT, viewId, options, encoded);
  }

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
