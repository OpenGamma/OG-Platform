/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.Arrays;
import java.util.EnumSet;

import javax.time.InstantProvider;

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
  private final ViewCycleExecutionOptions _defaultExecutionOptions;
  private final VersionCorrection _versionCorrection;
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags) {
    this(executionSequence, flags, null, null);
  }
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags, Integer maxSuccessiveDeltaCycles) {
    this(executionSequence, flags, maxSuccessiveDeltaCycles, null);
  }
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags, ViewCycleExecutionOptions defaultExecutionOptions) {
    this(executionSequence, flags, null, defaultExecutionOptions);
  }
  
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags,
      Integer maxSuccessiveDeltaCycles, ViewCycleExecutionOptions defaultExecutionOptions) {
    this(executionSequence, flags, maxSuccessiveDeltaCycles, defaultExecutionOptions, VersionCorrection.LATEST);
  }
    
  public ExecutionOptions(ViewCycleExecutionSequence executionSequence, EnumSet<ViewExecutionFlags> flags,
      Integer maxSuccessiveDeltaCycles, ViewCycleExecutionOptions defaultExecutionOptions, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(executionSequence, "executionSequence");
    ArgumentChecker.notNull(flags, "flags");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    _executionSequence = executionSequence;
    _flags = flags;
    _maxSuccessiveDeltaCycles = maxSuccessiveDeltaCycles;
    _defaultExecutionOptions = defaultExecutionOptions;
    _versionCorrection = versionCorrection;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public ViewCycleExecutionSequence getExecutionSequence() {
    return _executionSequence;
  }

  @Override
  public Integer getMaxSuccessiveDeltaCycles() {
    return _maxSuccessiveDeltaCycles;
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
  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _executionSequence.hashCode();
    result = prime * result + _flags.hashCode();
    result = prime * result + ((_defaultExecutionOptions == null) ? 0 : _defaultExecutionOptions.hashCode());
    result = prime * result + ((_maxSuccessiveDeltaCycles == null) ? 0 : _maxSuccessiveDeltaCycles.hashCode());
    result = prime * result + _versionCorrection.hashCode();
    return result;
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
    } else if (!_versionCorrection.equals(other._versionCorrection)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ExecutionOptions [executionSequence=" + _executionSequence + ", flags=" + _flags + ", maxSuccessiveDeltaCycles=" + _maxSuccessiveDeltaCycles + ", defaultExecutionOptions=" +
        _defaultExecutionOptions + ", versionCorrection=" + _versionCorrection + "]";
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a custom execution sequence.
   * 
   * @param cycleExecutionSequence  the execution sequence, not null
   * @param flags  execution flags, not null
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
   * @param cycleExecutionSequence  the execution sequence, not null
   * @param defaultCycleOptions  the default view cycle execution options, may be null
   * @param flags  the execution flags, not null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions of(ViewCycleExecutionSequence cycleExecutionSequence, ViewCycleExecutionOptions defaultCycleOptions, EnumSet<ViewExecutionFlags> flags) {
    ArgumentChecker.notNull(cycleExecutionSequence, "cycleExecutionSequence");
    ArgumentChecker.notNull(flags, "flags");
    return new ExecutionOptions(cycleExecutionSequence, flags, defaultCycleOptions);
  }
  
  /**
   * Creates an infinite execution sequence with a valuation time driven by the market data and all triggers enabled.
   * Execution will continue for as long as there is demand.
   * <p>
   * For the classic execution sequence for real-time calculations against live market data, use
   * <pre>
   *  ExecutionOptions.infinite(MarketData.live());
   * </pre>
   * 
   * @param marketDataSpec  the market data specification, not null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions infinite(MarketDataSpecification marketDataSpec) {
    return infinite(marketDataSpec, ExecutionFlags.triggersEnabled().get());
  }
  
  /**
   * Creates an infinite execution sequence with a valuation time driven by the market data. Execution will continue
   * for as long as there is demand.
   * 
   * @param marketDataSpec  the market data specification, not null
   * @param flags  the execution flags, not null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions infinite(MarketDataSpecification marketDataSpec, EnumSet<ViewExecutionFlags> flags) {
    ViewCycleExecutionOptions defaultExecutionOptions = new ViewCycleExecutionOptions();
    defaultExecutionOptions.setMarketDataSpecification(marketDataSpec);
    return of(new InfiniteViewCycleExecutionSequence(), defaultExecutionOptions, flags);
  }
  
  /**
   * Creates an execution sequence designed for batch-mode operation. The typical next-cycle triggers are disabled; the
   * sequence is instead configured to run as fast as possible.
   * 
   * @param cycleExecutionSequence  the execution sequence, not null
   * @param defaultCycleOptions  the default view cycle execution options, may be null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions batch(InstantProvider valuationTimeProvider, MarketDataSpecification marketDataSpec, ViewCycleExecutionOptions defaultCycleOptions) {
    ViewCycleExecutionOptions cycleOptions = new ViewCycleExecutionOptions(marketDataSpec);
    if (valuationTimeProvider != null) {
      cycleOptions.setValuationTime(valuationTimeProvider);
    }
    ViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(Arrays.asList(cycleOptions));
    return of(sequence, defaultCycleOptions, ExecutionFlags.none().batch().runAsFastAsPossible().awaitMarketData().get());
  }

  /**
   * Creates an execution sequence designed for batch-mode operation. The typical next-cycle triggers are disabled; the
   * sequence is instead configured to run as fast as possible.
   *
   * @param cycleExecutionSequence  the execution sequence, not null
   * @param defaultCycleOptions  the default view cycle execution options, may be null
   * @return the execution sequence, not null
   */
  public static ViewExecutionOptions batch(ViewCycleExecutionSequence cycleExecutionSequence, ViewCycleExecutionOptions defaultCycleOptions) {
    return of(cycleExecutionSequence, defaultCycleOptions, ExecutionFlags.none().batch().runAsFastAsPossible().awaitMarketData().get());
  }

  /**
   * Creates an execution sequence to run a single cycle.
   * 
   * @param valuationTimeProvider  the valuation time provider, or null to use the market data time
   * @param marketDataSpec  the market data specification, not null
   * @return an execution sequence representing the single cycle, not null
   */
  public static ViewExecutionOptions singleCycle(InstantProvider valuationTimeProvider, MarketDataSpecification marketDataSpec) {
    return singleCycle(valuationTimeProvider, marketDataSpec, ExecutionFlags.none().runAsFastAsPossible().awaitMarketData().get());
  }

  /**
   * Creates an execution sequence to run a single cycle.
   * 
   * @param valuationTimeProvider  the valuation time provider, or null to use the market data time
   * @param marketDataSpec  the market data specification, not null
   * @param flags  the execution flags, not null
   * @return an execution sequence representing the single cycle, not null
   */
  public static ViewExecutionOptions singleCycle(InstantProvider valuationTimeProvider, MarketDataSpecification marketDataSpec, EnumSet<ViewExecutionFlags> flags) {
    ArgumentChecker.notNull(marketDataSpec, "marketDataSpec");
    ArgumentChecker.notNull(flags, "flags");
    ViewCycleExecutionOptions cycleOptions = new ViewCycleExecutionOptions(marketDataSpec);
    if (valuationTimeProvider != null) {
      cycleOptions.setValuationTime(valuationTimeProvider);
    }
    ViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(Arrays.asList(cycleOptions));
    return of(sequence, flags);
  }
  
}
