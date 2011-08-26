/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.ArrayList;
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
  // TODO: this is only public because OG-Excel references it

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
     * Created by {@link #tickingSnapshot}.
     */
    TICKING_SNAPSHOT,
    /**
     * Created by {@link #staticSnapshot}.
     */
    STATIC_SNAPSHOT
  }

  private static final char SEPARATOR = '~';
  private static final char ESCAPE_CHAR = '$';

  private static final String STATIC = "Static";
  private static final String SNAPSHOT = "Snapshot";
  private static final String TICKING = "Ticking";

  private final Type _type;
  private final String _viewName;
  private final ViewExecutionOptions _executionOptions;
  private final String _encoded;

  private ViewClientDescriptor(final Type type, final String viewName, final ViewExecutionOptions executionOptions, final String encoded) {
    _type = type;
    _viewName = viewName;
    _executionOptions = executionOptions;
    _encoded = encoded;
  }

  public Type getType() {
    return _type;
  }

  public String getViewName() {
    return _viewName;
  }
  
  public ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  protected static ViewClientDescriptor decode(final String str) {
    final List<String> values = split(str);
    switch (values.size()) {
      case 1:
        return tickingMarketData(values.get(0));
      case 3:
        if (STATIC.equals(values.get(1))) {
          return staticMarketData(values.get(0), Instant.ofEpochSeconds(Long.parseLong(values.get(2))));
        } else if (SNAPSHOT.equals(values.get(1))) {
          return staticSnapshot(values.get(0), UniqueId.parse(values.get(2)));
        } else {
          throw new IllegalArgumentException(str);
        }
      case 4:
        if (SNAPSHOT.equals(values.get(1))) {
          if (TICKING.equals(values.get(3))) {
            return tickingSnapshot(values.get(0), UniqueId.parse(values.get(2)));
          } else {
            throw new IllegalArgumentException(str);
          }
        } else {
          throw new IllegalArgumentException(str);
        }
      default:
        throw new IllegalArgumentException(str);
    }
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
        sb.append(str.charAt(++i));
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

  public static ViewClientDescriptor tickingMarketData(final String viewName) {
    return new ViewClientDescriptor(Type.TICKING_MARKET_DATA, viewName, ExecutionOptions.infinite(MarketData.live()), escape(viewName));
  }

  public static ViewClientDescriptor staticMarketData(final String viewName, final InstantProvider valuationTime) {
    final Instant valuationTimeValue = Instant.of(valuationTime);
    final ViewExecutionOptions options = ExecutionOptions.singleCycle(valuationTimeValue, MarketData.live());
    final String encoded = append(append(append(new StringBuilder(), viewName), STATIC), Long.toString(valuationTimeValue.toEpochMillisLong() / 1000L)).toString();
    return new ViewClientDescriptor(Type.STATIC_MARKET_DATA, viewName, options, encoded);
  }

  public static ViewClientDescriptor tickingSnapshot(final String viewName, final UniqueId snapshotId) {
    final ViewCycleExecutionSequence cycleSequence = new InfiniteViewCycleExecutionSequence();
    final ViewCycleExecutionOptions cycleOptions = new ViewCycleExecutionOptions(MarketData.user(snapshotId));
    final ExecutionFlags flags = ExecutionFlags.none().triggerOnMarketData();
    final ViewExecutionOptions options = ExecutionOptions.of(cycleSequence, cycleOptions, flags.get());
    final String encoded = append(append(append(append(new StringBuilder(), viewName), SNAPSHOT), snapshotId.toString()), TICKING).toString();
    return new ViewClientDescriptor(Type.TICKING_SNAPSHOT, viewName, options, encoded);
  }

  public static ViewClientDescriptor staticSnapshot(final String viewName, final UniqueId snapshotId) {
    final ViewCycleExecutionSequence cycleSequence = new InfiniteViewCycleExecutionSequence();
    final ViewCycleExecutionOptions cycleOptions = new ViewCycleExecutionOptions(MarketData.user(snapshotId));
    final ExecutionFlags flags = ExecutionFlags.none();
    final ViewExecutionOptions options = ExecutionOptions.of(cycleSequence, cycleOptions, flags.get());
    final String encoded = append(append(append(new StringBuilder(), viewName), SNAPSHOT), snapshotId.toString()).toString();
    return new ViewClientDescriptor(Type.STATIC_SNAPSHOT, viewName, options, encoded);
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
