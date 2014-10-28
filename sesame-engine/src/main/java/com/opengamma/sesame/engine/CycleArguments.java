/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.Collections;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.id.VersionCorrection;
import com.opengamma.sesame.config.FunctionArguments;
import com.opengamma.sesame.function.scenarios.ScenarioArgument;
import com.opengamma.sesame.function.scenarios.ScenarioDefinition;
import com.opengamma.sesame.marketdata.CycleMarketDataFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Arguments used to drive the calculations in a single calculation cycle.
 */
public final class CycleArguments {

  /**
   * The type of information to be captured by a trace.
   */
  public enum TraceType {
    /**
     * Do not capture any trace information.
     */
    NONE,
    /**
     * Capture method timings for all calls.
     */
    TIMINGS_ONLY,
    /**
     * Capture timings, arguments and return values but
     * convert to strings before returning. This means
     * trace information can be serialized and sent to
     * remote processes.
     */
    FULL_AS_STRING,
    /**
     * Capture timings, arguments and return values. This means
     * trace information cannot necessarily be serialized and
     * sent to remote processes.
     */
    FULL
  }

  // TODO sort out function arguments

  private final ZonedDateTime _valuationTime;
  private final CycleMarketDataFactory _cycleMarketDataFactory;
  private final VersionCorrection _configVersionCorrection;
  private final Map<Cell, TraceType> _traceCells;
  private final Map<String, TraceType> _traceOutputs;
  private final FunctionArguments _functionArguments;
  private final boolean _captureInputs;
  private final ScenarioDefinition _scenarioDefinition;

  /**
   * @deprecated this will be removed, use {@link #builder}
   */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory) {
    this(valuationTime,
         configVersionCorrection,
         cycleMarketDataFactory,
         FunctionArguments.EMPTY);
  }

  /**
   * @deprecated this will be removed, use {@link #builder}
   */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory,
                        FunctionArguments functionArguments) {
    this(valuationTime,
         configVersionCorrection,
         cycleMarketDataFactory,
         functionArguments,
         ImmutableMap.<Cell, TraceType>of(),
         ImmutableMap.<String, TraceType>of());
  }

  /**
   * @deprecated this will be removed, use {@link #builder}
   */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory,
                        FunctionArguments functionArguments,
                        Map<Cell, TraceType> traceCells,
                        Map<String, TraceType> traceOutputs) {
    this(valuationTime,
         configVersionCorrection,
         cycleMarketDataFactory,
         functionArguments,
         traceCells,
         traceOutputs,
         false,
         ScenarioDefinition.EMPTY);
  }

  /**
   * @deprecated this will be removed, use {@link #builder}
   */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory,
                        boolean captureInputs) {
    this(valuationTime,
         configVersionCorrection,
         cycleMarketDataFactory,
         FunctionArguments.EMPTY,
         ImmutableMap.<Cell, TraceType>of(),
         ImmutableMap.<String, TraceType>of(),
         captureInputs,
         ScenarioDefinition.EMPTY);
  }

  /**
   * @deprecated this will be removed, use {@link #builder}
   */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory,
                        FunctionArguments functionArguments,
                        boolean captureInputs) {
    this(valuationTime,
         configVersionCorrection,
         cycleMarketDataFactory,
         functionArguments,
         ImmutableMap.<Cell, TraceType>of(),
         ImmutableMap.<String, TraceType>of(),
         captureInputs,
         ScenarioDefinition.EMPTY);
  }

  /**
   * @deprecated this will become private, use {@link #builder}
   */
  @Deprecated
  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory,
                        FunctionArguments functionArguments,
                        Map<Cell, TraceType> traceCells,
                        Map<String, TraceType> traceOutputs,
                        boolean captureInputs,
                        ScenarioDefinition scenarioDefinition) {
    _scenarioDefinition = ArgumentChecker.notNull(scenarioDefinition, "scenarioDefinition");
    _functionArguments = ArgumentChecker.notNull(functionArguments, "functionArguments");
    _configVersionCorrection = ArgumentChecker.notNull(configVersionCorrection, "configVersionCorrection");
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
    _cycleMarketDataFactory = ArgumentChecker.notNull(cycleMarketDataFactory, "cycleMarketDataFactory");
    _traceCells = ImmutableMap.copyOf(ArgumentChecker.notNull(traceCells, "traceCells"));
    _traceOutputs = ImmutableMap.copyOf(ArgumentChecker.notNull(traceOutputs, "traceOutputs"));
    _captureInputs = captureInputs;
  }

  ZonedDateTime getValuationTime() {
    return _valuationTime;
  }

  CycleMarketDataFactory getCycleMarketDataFactory() {
    return _cycleMarketDataFactory;
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

  ScenarioDefinition getScenarioDefinition() {
    return _scenarioDefinition;
  }

  TraceType traceType(int rowIndex, int colIndex) {
    Cell cell = new Cell(rowIndex, colIndex);
    return _traceCells.containsKey(cell) ?
      _traceCells.get(cell) :
      TraceType.NONE;
  }

  boolean isCaptureInputs() {
    return _captureInputs;
  }

  /**
   * Creates a builder for building up cycle arguments.
   *
   * @param marketDataFactory the market data factory for the cycle
   * @return a builder for building up cycle arguments
   */
  public static Builder builder(CycleMarketDataFactory marketDataFactory) {
    return new Builder(marketDataFactory);
  }

  /**
   * Represents a single cell in the grid of {@link Results}.
   */
  public static class Cell {

    private final int _row;
    private final int _col;

    /**
     * @param row the index of the cell's row (zero based)
     * @param col the index of the cell's column (zero based)
     */
    public Cell(int row, int col) {
      _row = ArgumentChecker.notNegative(row, "row");
      _col = ArgumentChecker.notNegative(col, "col");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Cell cell = (Cell) o;
      return _row == cell._row && _col == cell._col;
    }

    @Override
    public int hashCode() {
      int result = _row;
      result = 31 * result + _col;
      return result;
    }
  }

  /**
   * Builder for constructing instances of {@link CycleArguments}.
   */
  public static final class Builder {

    private final CycleMarketDataFactory _cycleMarketDataFactory;

    private ZonedDateTime _valuationTime = ZonedDateTime.now();
    private Map<Cell, TraceType> _traceCells = Collections.emptyMap();
    private Map<String, TraceType> _traceOutputs = Collections.emptyMap();
    // TODO this is correct, enable it
    //private Map<Class<?>, FunctionArguments> _functionArguments = Collections.emptyMap();
    private FunctionArguments _functionArguments = FunctionArguments.EMPTY;
    private boolean _captureInputs = false;
    private VersionCorrection _versionCorrection = VersionCorrection.LATEST;
    private ScenarioDefinition _scenarioDefinition = ScenarioDefinition.EMPTY;

    private Builder(CycleMarketDataFactory cycleMarketDataFactory) {
      _cycleMarketDataFactory = ArgumentChecker.notNull(cycleMarketDataFactory, "cycleMarketDataFactory");
    }

    /**
     * @return an instance of {@code CycleArguments} containing the data in this builder
     */
    public CycleArguments build() {
      return new CycleArguments(_valuationTime,
                                _versionCorrection,
                                _cycleMarketDataFactory,
                                _functionArguments,
                                _traceCells,
                                _traceOutputs,
                                _captureInputs,
                                _scenarioDefinition);
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
