/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.id.VersionCorrection;
import com.opengamma.sesame.config.FunctionArguments;
import com.opengamma.sesame.marketdata.CycleMarketDataFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * TODO this will probably need to be a joda bean for serialization
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

  // TODO function arguments for the output functions
  // TODO portfolio version correction

  private final ZonedDateTime _valuationTime;
  private final CycleMarketDataFactory _cycleMarketDataFactory;
  private final VersionCorrection _configVersionCorrection;
  private final Map<Pair<Integer, Integer>, TraceType> _traceCells;
  private final Map<String, TraceType> _traceOutputs;
  private final FunctionArguments _functionArguments;
  private final Map<Class<?>, Object> _scenarioArguments;
  private final boolean _captureInputs;

  // TODO use a Cell class instead of Pair<Integer, Integer>
  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory) {
    this(valuationTime,
         configVersionCorrection,
         cycleMarketDataFactory,
         FunctionArguments.EMPTY,
         ImmutableMap.<Class<?>, Object>of());
  }

  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory,
                        FunctionArguments functionArguments,
                        Map<Class<?>, Object> scenarioArguments) {
    this(valuationTime,
         configVersionCorrection,
         cycleMarketDataFactory,
         functionArguments,
         scenarioArguments,
         ImmutableMap.<Pair<Integer, Integer>, TraceType>of(),
         ImmutableMap.<String, TraceType>of());
  }

  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory,
                        FunctionArguments functionArguments,
                        Map<Class<?>, Object> scenarioArguments,
                        Map<Pair<Integer, Integer>, TraceType> traceCells,
                        Map<String, TraceType> traceOutputs) {
    this(valuationTime, configVersionCorrection, cycleMarketDataFactory, functionArguments,
         scenarioArguments, traceCells, traceOutputs, false);
  }

  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory,
                        boolean captureInputs) {
    this(valuationTime, configVersionCorrection, cycleMarketDataFactory, FunctionArguments.EMPTY,
         ImmutableMap.<Class<?>, Object>of(), ImmutableMap.<Pair<Integer, Integer>, TraceType>of(),
         ImmutableMap.<String, TraceType>of(), captureInputs);
  }

  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory,
                        FunctionArguments functionArguments,
                        Map<Class<?>, Object> scenarioArguments, boolean captureInputs) {
    this(valuationTime, configVersionCorrection, cycleMarketDataFactory, functionArguments,
         scenarioArguments, ImmutableMap.<Pair<Integer, Integer>, TraceType>of(),
         ImmutableMap.<String, TraceType>of(), captureInputs);
  }

  public CycleArguments(ZonedDateTime valuationTime,
                        VersionCorrection configVersionCorrection,
                        CycleMarketDataFactory cycleMarketDataFactory,
                        FunctionArguments functionArguments,
                        Map<Class<?>, Object> scenarioArguments,
                        Map<Pair<Integer, Integer>, TraceType> traceCells,
                        Map<String, TraceType> traceOutputs,
                        boolean captureInputs) {
    _functionArguments = ArgumentChecker.notNull(functionArguments, "functionArguments");
    _scenarioArguments = ImmutableMap.copyOf(ArgumentChecker.notNull(scenarioArguments, "scenarioArguments"));
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

  Map<Class<?>, Object> getScenarioArguments() {
    return _scenarioArguments;
  }

  TraceType traceType(int rowIndex, int colIndex) {
    Pair<Integer, Integer> cell = Pairs.of(rowIndex, colIndex);
    return _traceCells.containsKey(cell) ?
      _traceCells.get(cell) :
      TraceType.NONE;
  }

  public boolean isCaptureInputs() {
    return _captureInputs;
  }
}
