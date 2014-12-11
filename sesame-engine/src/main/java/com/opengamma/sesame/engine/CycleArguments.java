/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.id.VersionCorrection;
import com.opengamma.sesame.config.FunctionArguments;
import com.opengamma.sesame.function.scenarios.ScenarioArgument;
import com.opengamma.sesame.function.scenarios.ScenarioDefinition;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.util.ArgumentChecker;

/**
 * Arguments used to drive the calculations in a single calculation cycle.
 *
 * @deprecated use {@link CalculationArguments}
 */
@Deprecated
public final class CycleArguments {

  // TODO sort out function arguments

  private final ZonedDateTime _valuationTime;
  private final MarketDataEnvironment _marketDataEnvironment;
  private final VersionCorrection _configVersionCorrection;
  private final Map<Cell, TraceType> _traceCells;
  private final Map<String, TraceType> _traceOutputs;
  // TODO this should be Map<Class, FunctionArguments>
  private final FunctionArguments _functionArguments;
  private final boolean _captureInputs;

  /** @deprecated use a {@link #builder(MarketDataEnvironment) builder} instead. */
  /** Valuations times keyed by column name. If there is no value for a column {@link #_valuationTime} will be used. */
  private final Map<String, ZonedDateTime> _columnValuationTimes;

  /** @deprecated this will be removed, use {@link #builder} */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        MarketDataEnvironment marketDataEnvironment,
                        VersionCorrection configVersionCorrection) {
    this(valuationTime, marketDataEnvironment, configVersionCorrection, FunctionArguments.EMPTY);
  }

  /** @deprecated use a {@link #builder(MarketDataEnvironment) builder} instead. */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        MarketDataEnvironment marketDataEnvironment,
                        VersionCorrection configVersionCorrection,
                        FunctionArguments functionArguments) {
    this(valuationTime,
         marketDataEnvironment,
         configVersionCorrection,
         functionArguments,
         ImmutableMap.<Cell, TraceType>of(),
         ImmutableMap.<String, TraceType>of());
  }

  /** @deprecated use a {@link #builder(MarketDataEnvironment) builder} instead. */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime, MarketDataEnvironment marketDataEnvironment,
                        VersionCorrection configVersionCorrection,
                        FunctionArguments functionArguments,
                        Map<Cell, TraceType> traceCells,
                        Map<String, TraceType> traceOutputs) {
    this(valuationTime,
         marketDataEnvironment,
         configVersionCorrection,
         functionArguments,
         traceCells,
         traceOutputs,
         false,
         Collections.<String, ZonedDateTime>emptyMap());
  }

  /** @deprecated use a {@link #builder(MarketDataEnvironment) builder} instead. */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        MarketDataEnvironment marketDataEnvironment,
                        VersionCorrection configVersionCorrection,
                        boolean captureInputs) {
    this(valuationTime,
         marketDataEnvironment,
         configVersionCorrection,
         FunctionArguments.EMPTY,
         ImmutableMap.<Cell, TraceType>of(),
         ImmutableMap.<String, TraceType>of(),
         captureInputs,
         Collections.<String, ZonedDateTime>emptyMap());
  }

  /** @deprecated use a {@link #builder(MarketDataEnvironment) builder} instead. */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        MarketDataEnvironment marketDataEnvironment,
                        VersionCorrection configVersionCorrection,
                        FunctionArguments functionArguments,
                        boolean captureInputs) {
    this(valuationTime,
         marketDataEnvironment,
         configVersionCorrection,
         functionArguments,
         ImmutableMap.<Cell, TraceType>of(),
         ImmutableMap.<String, TraceType>of(),
         captureInputs,
         Collections.<String, ZonedDateTime>emptyMap());
  }

  /**
   * @deprecated this will become private, use {@link #builder}
   */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        MarketDataEnvironment marketDataEnvironment,
                        VersionCorrection configVersionCorrection,
                        FunctionArguments functionArguments,
                        Map<Cell, TraceType> traceCells,
                        Map<String, TraceType> traceOutputs,
                        boolean captureInputs,
                        Map<String, ZonedDateTime> columnValuationTimes) {
    _marketDataEnvironment = ArgumentChecker.notNull(marketDataEnvironment, "marketDataBundle");
    _columnValuationTimes = ImmutableMap.copyOf(ArgumentChecker.notNull(columnValuationTimes, "columnValuationTimes"));
    _functionArguments = ArgumentChecker.notNull(functionArguments, "functionArguments");
    _configVersionCorrection = ArgumentChecker.notNull(configVersionCorrection, "configVersionCorrection");
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
    _traceCells = ImmutableMap.copyOf(ArgumentChecker.notNull(traceCells, "traceCells"));
    _traceOutputs = ImmutableMap.copyOf(ArgumentChecker.notNull(traceOutputs, "traceOutputs"));
    _captureInputs = captureInputs;
  }

  ZonedDateTime getValuationTime() {
    return _valuationTime;
  }

  ZonedDateTime getValuationTime(String columnName) {
    if (_columnValuationTimes.containsKey(columnName)) {
      return _columnValuationTimes.get(columnName);
    } else {
      return _valuationTime;
    }
  }

  TraceType traceType(String output) {
    return _traceOutputs.containsKey(output) ?
        _traceOutputs.get(output) :
        TraceType.NONE;
  }

  VersionCorrection getConfigVersionCorrection() {
    return _configVersionCorrection;
  }

  FunctionArguments getFunctionArguments() {
    return _functionArguments;
  }

  TraceType traceType(int rowIndex, int colIndex) {
    Cell cell = Cell.of(rowIndex, colIndex);
    return _traceCells.containsKey(cell) ?
      _traceCells.get(cell) :
      TraceType.NONE;
  }

  boolean isCaptureInputs() {
    return _captureInputs;
  }

  MarketDataEnvironment getMarketDataEnvironment() {
    return _marketDataEnvironment;
  }

  Map<Cell, TraceType> getTraceCells() {
    return _traceCells;
  }

  Map<String, TraceType> getTraceOutputs() {
    return _traceOutputs;
  }

  // TODO maybe marketDataEnvironment could be a normal method too. default behaviour is to gather all requirements
  // and use functions to populate
  public static Builder builder(MarketDataEnvironment marketDataBundle) {
    return new Builder(marketDataBundle);
  }

  public static final class Builder {

    private final MarketDataEnvironment _marketDataBundle;
    private final Map<String, ZonedDateTime> _columnValuationTime = new HashMap<>();

    private ZonedDateTime _valuationTime = ZonedDateTime.now();
    private Map<Cell, TraceType> _traceCells = Collections.emptyMap();
    private Map<String, TraceType> _traceOutputs = Collections.emptyMap();
    // TODO this is correct, enable it
    //private Map<Class<?>, FunctionArguments> _functionArguments = Collections.emptyMap();
    private FunctionArguments _functionArguments = FunctionArguments.EMPTY;
    private boolean _captureInputs;
    private VersionCorrection _versionCorrection = VersionCorrection.LATEST;
    private ScenarioDefinition _scenarioDefinition = ScenarioDefinition.EMPTY;

    private Builder(MarketDataEnvironment marketDataBundle) {
      _marketDataBundle = ArgumentChecker.notNull(marketDataBundle, "marketDataBundle");
    }

    /**
     * @return an instance of {@code CycleArguments} containing the data in this builder
     */
    public CycleArguments build() {
      return new CycleArguments(_valuationTime,
                                _marketDataBundle,
                                _versionCorrection,
                                _functionArguments,
                                _traceCells,
                                _traceOutputs,
                                _captureInputs,
                                _columnValuationTime);
    }

    /**
     * Sets the cells for which tracing of function calls and values will be enabled
     *
     * @param cells the cells for which tracing will be enabled
     * @return this builder
     */
    public Builder traceCells(Map<Cell, TraceType> cells) {
      _traceCells = ArgumentChecker.notNull(cells, "cells");
      return this;
    }

    /**
     * Sets the non-portfolio outputs for which tracing of function calls and values will be enabled
     *
     * @param outputs the outputs for which tracing will be enabled
     * @return this builder
     */
    public Builder traceOutputs(Map<String, TraceType> outputs) {
      _traceOutputs = ArgumentChecker.notNull(outputs, "outputs");
      return this;
    }

    /**
     * Sets the function arguments for the cycle.
     *
     * @param arguments the function arguments for the cycle
     * @return this builder
     */
    public Builder functionArguments(FunctionArguments arguments) {
      _functionArguments = ArgumentChecker.notNull(arguments, "arguments");
      return this;
    }

    /**
     * Sets the valuation time for the calculations in the cycle
     *
     * @param valuationTime the valuation time for the calculations in the cycle
     * @return this builder
     */
    public Builder valuationTime(ZonedDateTime valuationTime) {
      _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
      return this;
    }

    /**
     * Sets the valuation time for the calculations in the cycle for a specific column.
     * <p>
     * If no value is specified for a column the default value from {@link #valuationTime(ZonedDateTime)} will be
     * used.
     *
     * @param valuationTime the valuation time for the calculations in the cycle
     * @return this builder
     */
    public Builder valuationTime(ZonedDateTime valuationTime, String columnName) {
      ArgumentChecker.notNull(valuationTime, "valuationTime");
      ArgumentChecker.notEmpty(columnName, "columnName");

      _columnValuationTime.put(columnName, valuationTime);
      return this;
    }

    // TODO this is the correct version
    // TODO but what if we want to provide different arguments for different columns?
    /* public Builder functionArguments(Map<Class<?>, FunctionArguments> arguments) {
      _functionArguments = ArgumentChecker.notNull(arguments, "arguments");
      return this;
    }*/

    /**
     * Sets the version correction used to load from the data store
     *
     * @param versionCorrection the version correction used to load from the data store
     * @return this builder
     */
    public Builder versionCorrection(VersionCorrection versionCorrection) {
      _versionCorrection = ArgumentChecker.notNull(versionCorrection, "versionCorrection");
      return this;
    }

    /**
     * Sets whether all inputs to the calculations should be captured in addition to the results.
     * <p>
     * This includes all market data and any data loaded from a data store.
     *
     * @param capture true if inputs should be captured and returned with the results
     * @return this builder
     */
    public Builder captureInputs(boolean capture) {
      _captureInputs = capture;
      return this;
    }

    /**
     * Merges {@code scenarioDefinition} with the builder's scenario definition.
     *
     * @param scenarioDefinition a scenario definition
     * @return this builder
     */
    public Builder scenarioDefinition(ScenarioDefinition scenarioDefinition) {
      ArgumentChecker.notNull(scenarioDefinition, "scenarioDefinition");
      _scenarioDefinition = _scenarioDefinition.mergedWith(scenarioDefinition);
      return this;
    }

    /**
     * Adds scenario arguments to this builder's scenario definition.
     *
     * @param args the scenario arguments
     * @return this builder
     */
    public Builder scenarioArguments(ScenarioArgument<?, ?>... args) {
      _scenarioDefinition = _scenarioDefinition.with(args);
      return this;
    }
  }
}
