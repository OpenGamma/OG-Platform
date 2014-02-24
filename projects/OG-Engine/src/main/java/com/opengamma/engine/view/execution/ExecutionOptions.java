/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Provides standard view process execution options.
 */
@PublicAPI
public class ExecutionOptions implements ViewExecutionOptions {
  
  private final ViewCycleExecutionSequence _executionSequence;
  private final EnumSet<ViewExecutionFlags> _flags;
  private final Integer _maxSuccessiveDeltaCycles;
  private final Long _marketDataTimeoutMillis;
  private final ViewCycleExecutionOptions _defaultExecutionOptions;

  //-------------------------------------------------------------------------
  /**
   * Creates a custom execution sequence.
   * 
   * @param cycleExecutionSequence the execution sequence, not null
   * @param flags the execution flags, not null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions of(ViewCycleExecutionSequence cycleExecutionSequence, EnumSet<ViewExecutionFlags> flags) {
    ArgumentChecker.notNull(cycleExecutionSequence, "cycleExecutionSequence");
    ArgumentChecker.notNull(flags, "flags");
    return of(cycleExecutionSequence, null, flags);
  }

  /**
   * Creates a custom execution sequence.
   * 
   * @param cycleExecutionSequence the execution sequence, not null
   * @param defaultCycleOptions the default view cycle execution options, may be null
   * @param flags the execution flags, not null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions of(ViewCycleExecutionSequence cycleExecutionSequence, ViewCycleExecutionOptions defaultCycleOptions, EnumSet<ViewExecutionFlags> flags) {
    ArgumentChecker.notNull(cycleExecutionSequence, "cycleExecutionSequence");
    ArgumentChecker.notNull(flags, "flags");
    return new ExecutionOptions(cycleExecutionSequence, flags, defaultCycleOptions);
  }

  /**
   * Creates an infinite execution sequence with a valuation time driven by the market data and all triggers enabled. Execution will continue for as long as there is demand.
   * <p>
   * For the classic execution sequence for real-time calculations against live market data, use
   * 
   * <pre>
   * ExecutionOptions.infinite(MarketData.live());
   * </pre>
   * 
   * @param marketDataSpec the market data specification, not null
   * @return the execution sequence, not null
   * @deprecated use list variant
   */
  @Deprecated
  public static ViewExecutionOptions infinite(MarketDataSpecification marketDataSpec) {
    return infinite(marketDataSpec, ExecutionFlags.triggersEnabled().get());
  }
  
  /**
   * Creates an infinite execution sequence with a valuation time driven by the market data and all triggers enabled. Execution will continue for as long as there is demand.
   * <p>
   * For the classic execution sequence for real-time calculations against live market data, use
   * 
   * <pre>
   * ExecutionOptions.infinite(Lists.asList(MarketData.live(),...));
   * </pre>
   * 
   * @param marketDataSpecs a list of market data specifications, not null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions infinite(List<MarketDataSpecification> marketDataSpecs) {
    return infinite(marketDataSpecs, ExecutionFlags.triggersEnabled().get());
  }

  /**
   * Creates an infinite execution sequence with a valuation time driven by the market data. Execution will continue for as long as there is demand.
   * 
   * @param marketDataSpec the market data specification, not null
   * @param flags the execution flags, not null
   * @return the execution sequence, not null
   * @deprecated use list variant
   */
  @Deprecated
  public static ViewExecutionOptions infinite(MarketDataSpecification marketDataSpec, EnumSet<ViewExecutionFlags> flags) {
    ViewCycleExecutionOptions defaultExecutionOptions = ViewCycleExecutionOptions.builder().setMarketDataSpecification(marketDataSpec).create();
    return of(new InfiniteViewCycleExecutionSequence(), defaultExecutionOptions, flags);
  }
  
  /**
   * Creates an infinite execution sequence with a valuation time driven by the market data. Execution will continue for as long as there is demand.
   * 
   * @param marketDataSpecs a list of market data specifications, not null
   * @param flags the execution flags, not null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions infinite(List<MarketDataSpecification> marketDataSpecs, EnumSet<ViewExecutionFlags> flags) {
    ViewCycleExecutionOptions defaultExecutionOptions = ViewCycleExecutionOptions.builder().setMarketDataSpecifications(marketDataSpecs).create();
    return of(new InfiniteViewCycleExecutionSequence(), defaultExecutionOptions, flags);
  }


  /**
   * Creates an infinite execution sequence with a valuation time driven by the market data. Execution will continue for as long as there is demand.
   * 
   * @param marketDataSpec the market data specification, not null
   * @param flags the execution flags, not null
   * @param versionCorrection the version-correction instants, not null
   * @return the execution sequence, not null
   * @deprecated use list variant
   */
  @Deprecated
  public static ViewExecutionOptions infinite(MarketDataSpecification marketDataSpec, EnumSet<ViewExecutionFlags> flags, VersionCorrection versionCorrection) {
    ViewCycleExecutionOptions defaultExecutionOptions = ViewCycleExecutionOptions.builder().setMarketDataSpecification(marketDataSpec).setResolverVersionCorrection(versionCorrection).create();
    return of(new InfiniteViewCycleExecutionSequence(), defaultExecutionOptions, flags);
  }
  
  /**
   * Creates an infinite execution sequence with a valuation time driven by the market data. Execution will continue for as long as there is demand.
   * 
   * @param marketDataSpecs a list of market data specifications, not null
   * @param flags the execution flags, not null
   * @param versionCorrection the version-correction instants, not null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions infinite(List<MarketDataSpecification> marketDataSpecs, EnumSet<ViewExecutionFlags> flags, VersionCorrection versionCorrection) {
    ViewCycleExecutionOptions defaultExecutionOptions = ViewCycleExecutionOptions.builder().setMarketDataSpecifications(marketDataSpecs).setResolverVersionCorrection(versionCorrection).create();
    return of(new InfiniteViewCycleExecutionSequence(), defaultExecutionOptions, flags);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an execution sequence designed for batch-mode operation. The typical next-cycle triggers are disabled; the sequence is instead configured to run as fast as possible.
   * 
   * @param valuationTimeProvider the valuation time provider, or null to use the market data time
   * @param marketDataSpec the market data specification, not null
   * @param defaultCycleOptions the default view cycle execution options, may be null
   * @return the execution sequence, not null
   * @deprecated use list variant
   */
  @Deprecated
  public static ViewExecutionOptions batch(Instant valuationTimeProvider, MarketDataSpecification marketDataSpec, ViewCycleExecutionOptions defaultCycleOptions) {
    ViewCycleExecutionOptions cycleOptions = ViewCycleExecutionOptions.builder().setValuationTime(valuationTimeProvider).setMarketDataSpecification(marketDataSpec).create();
    ViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(Arrays.asList(cycleOptions));
    return of(sequence, defaultCycleOptions, ExecutionFlags.none().batch().runAsFastAsPossible().awaitMarketData().get());
  }
  
  /**
   * Creates an execution sequence designed for batch-mode operation. The typical next-cycle triggers are disabled; the sequence is instead configured to run as fast as possible.
   * 
   * @param valuationTimeProvider the valuation time provider, or null to use the market data time
   * @param marketDataSpecs the market data specification, not null
   * @param defaultCycleOptions the default view cycle execution options, may be null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions batch(Instant valuationTimeProvider, List<MarketDataSpecification> marketDataSpecs, ViewCycleExecutionOptions defaultCycleOptions) {
    ViewCycleExecutionOptions cycleOptions = ViewCycleExecutionOptions.builder().setValuationTime(valuationTimeProvider).setMarketDataSpecifications(marketDataSpecs).create();
    ViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(Arrays.asList(cycleOptions));
    return of(sequence, defaultCycleOptions, ExecutionFlags.none().batch().runAsFastAsPossible().awaitMarketData().get());
  }

  /**
   * Creates an execution sequence designed for batch-mode operation. The typical next-cycle triggers are disabled; the sequence is instead configured to run as fast as possible.
   * 
   * @param cycleExecutionSequence the execution sequence, not null
   * @param defaultCycleOptions the default view cycle execution options, may be null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions batch(ViewCycleExecutionSequence cycleExecutionSequence, ViewCycleExecutionOptions defaultCycleOptions) {
    return of(cycleExecutionSequence, defaultCycleOptions, ExecutionFlags.none().batch().runAsFastAsPossible().awaitMarketData().get());
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an execution sequence to run a single cycle.
   * 
   * @param valuationTimeProvider the valuation time provider, or null to use the market data time
   * @param marketDataSpec the market data specification, not null
   * @return an execution sequence representing the single cycle, not null
   * @deprecated use list version
   */
  @Deprecated
  public static ViewExecutionOptions singleCycle(Instant valuationTimeProvider, MarketDataSpecification marketDataSpec) {
    return singleCycle(valuationTimeProvider, marketDataSpec, ExecutionFlags.none().runAsFastAsPossible().awaitMarketData().get());
  }
  
  /**
   * Creates an execution sequence to run a single cycle.
   * 
   * @param valuationTimeProvider the valuation time provider, or null to use the market data time
   * @param marketDataSpecs a list of the market data specifications, not null
   * @return an execution sequence representing the single cycle, not null
   */
  public static ViewExecutionOptions singleCycle(Instant valuationTimeProvider, List<MarketDataSpecification> marketDataSpecs) {
    return singleCycle(valuationTimeProvider, marketDataSpecs, ExecutionFlags.none().runAsFastAsPossible().awaitMarketData().get());
  }

  /**
   * Creates an execution sequence to run a single cycle.
   * 
   * @param valuationTimeProvider the valuation time provider, or null to use the market data time
   * @param marketDataSpec the market data specification, not null
   * @param flags the execution flags, not null
   * @return an execution sequence representing the single cycle, not null
   * @deprecated use list version
   */
  @Deprecated
  public static ViewExecutionOptions singleCycle(Instant valuationTimeProvider, MarketDataSpecification marketDataSpec, EnumSet<ViewExecutionFlags> flags) {
    ArgumentChecker.notNull(marketDataSpec, "marketDataSpec");
    ArgumentChecker.notNull(flags, "flags");
    ViewCycleExecutionOptions cycleOptions = ViewCycleExecutionOptions.builder().setValuationTime(valuationTimeProvider).setMarketDataSpecification(marketDataSpec).create();
    ViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(Arrays.asList(cycleOptions));
    return of(sequence, flags);
  }
  
  /**
   * Creates an execution sequence to run a single cycle.
   * 
   * @param valuationTimeProvider the valuation time provider, or null to use the market data time
   * @param marketDataSpecs a list of the market data specification, not null
   * @param flags the execution flags, not null
   * @return an execution sequence representing the single cycle, not null
   */
  public static ViewExecutionOptions singleCycle(Instant valuationTimeProvider, List<MarketDataSpecification> marketDataSpecs, EnumSet<ViewExecutionFlags> flags) {
    ArgumentChecker.notNull(marketDataSpecs, "marketDataSpec");
    ArgumentChecker.notNull(flags, "flags");
    ViewCycleExecutionOptions cycleOptions = ViewCycleExecutionOptions.builder().setValuationTime(valuationTimeProvider).setMarketDataSpecifications(marketDataSpecs).create();
    ViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(Arrays.asList(cycleOptions));
    return of(sequence, flags);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance. It is recommended to use a factory method instead of this constructor.
   * 
   * @param executionSequence the execution sequence, not null
   * @param flags the execution flags, not null
   */
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags) {
    this(executionSequence, flags, null, null);
  }

  /**
   * Creates an instance. It is recommended to use a factory method instead of this constructor.
   * 
   * @param executionSequence the execution sequence, not null
   * @param flags the execution flags, not null
   * @param maxSuccessiveDeltaCycles the maximum cycles, may be null
   */
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags, Integer maxSuccessiveDeltaCycles) {
    this(executionSequence, flags, maxSuccessiveDeltaCycles, null);
  }

  /**
   * Creates an instance. It is recommended to use a factory method instead of this constructor.
   * 
   * @param executionSequence the execution sequence, not null
   * @param flags the execution flags, not null
   * @param defaultExecutionOptions the default view cycle execution options, may be null
   */
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags, ViewCycleExecutionOptions defaultExecutionOptions) {
    this(executionSequence, flags, null, defaultExecutionOptions);
  }

  /**
   * @param executionSequence the execution sequence, not null
   * @param flags the execution flags, not null
   * @param maxSuccessiveDeltaCycles the maximum cycles, may be null
   * @param defaultExecutionOptions the default view cycle execution options, may be null
   */
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags,
      Integer maxSuccessiveDeltaCycles, ViewCycleExecutionOptions defaultExecutionOptions) {
    this(executionSequence, flags, maxSuccessiveDeltaCycles, null, defaultExecutionOptions);
  }
  
  /**
   * @param executionSequence the execution sequence, not null
   * @param flags the execution flags, not null
   * @param maxSuccessiveDeltaCycles the maximum cycles, may be null
   * @param marketDataTimeoutMillis the maximum time to wait for market data to become available, may be null
   * @param defaultExecutionOptions the default view cycle execution options, may be null
   */
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags,
      Integer maxSuccessiveDeltaCycles, Long marketDataTimeoutMillis, ViewCycleExecutionOptions defaultExecutionOptions) {
    ArgumentChecker.notNull(executionSequence, "executionSequence");
    ArgumentChecker.notNull(flags, "flags");
    _executionSequence = executionSequence;
    _flags = flags;
    _maxSuccessiveDeltaCycles = maxSuccessiveDeltaCycles;
    _marketDataTimeoutMillis = marketDataTimeoutMillis;
    _defaultExecutionOptions = defaultExecutionOptions;
  }

  @Override
  public ViewCycleExecutionSequence getExecutionSequence() {
    return _executionSequence;
  }

  @Override
  public Integer getMaxSuccessiveDeltaCycles() {
    return _maxSuccessiveDeltaCycles;
  }
  
  @Override
  public Long getMarketDataTimeoutMillis() {
    return _marketDataTimeoutMillis;
  }

  @Override
  public ViewCycleExecutionOptions getDefaultExecutionOptions() {
    return _defaultExecutionOptions;
  }

  @Override
  public EnumSet<ViewExecutionFlags> getFlags() {
    return _flags;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ExecutionOptions)) {
      return false;
    }
    ExecutionOptions other = (ExecutionOptions) obj;
    if (!_executionSequence.equals(other._executionSequence)) {
      return false;
    }
    if (!_flags.equals(other._flags)) {
      return false;
    }
    if (_defaultExecutionOptions == null) {
      if (other._defaultExecutionOptions != null) {
        return false;
      }
    } else if (!_defaultExecutionOptions.equals(other._defaultExecutionOptions)) {
      return false;
    }
    if (_maxSuccessiveDeltaCycles == null) {
      if (other._maxSuccessiveDeltaCycles != null) {
        return false;
      }
    } else if (!_maxSuccessiveDeltaCycles.equals(other._maxSuccessiveDeltaCycles)) {
      return false;
    }
    if (_marketDataTimeoutMillis == null) {
      if (other._marketDataTimeoutMillis != null) {
        return false;
      }
    } else if (!_marketDataTimeoutMillis.equals(other._marketDataTimeoutMillis)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_defaultExecutionOptions == null) ? 0 : _defaultExecutionOptions.hashCode());
    result = prime * result + ((_executionSequence == null) ? 0 : _executionSequence.hashCode());
    result = prime * result + ((_flags == null) ? 0 : _flags.hashCode());
    result = prime * result + ((_marketDataTimeoutMillis == null) ? 0 : _marketDataTimeoutMillis.hashCode());
    result = prime * result + ((_maxSuccessiveDeltaCycles == null) ? 0 : _maxSuccessiveDeltaCycles.hashCode());
    return result;
  }
  
  @Override
  public String toString() {
    return "ExecutionOptions [executionSequence=" + _executionSequence + ", flags=" + _flags + ", maxSuccessiveDeltaCycles=" + _maxSuccessiveDeltaCycles + ", marketDataTimeoutMillis=" +
        _marketDataTimeoutMillis + ", defaultExecutionOptions=" + _defaultExecutionOptions + "]";
  }

}
