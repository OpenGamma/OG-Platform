/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * A simple cycle recorder implementation that captures all component calls made during execution of the cycle.
 */
public class DefaultCycleRecorder implements CycleRecorder {

  private final ViewConfig _viewConfig;
  private final List<?> _tradeInputs;
  private final CalculationArguments _calculationArguments;
  private final ProxiedComponentMap _proxiedComponentMap;
  private final MarketDataEnvironment _marketDataEnvironment;

  /**
   * Construct the recorder.
   *
   * @param viewConfig the view config used for the cycle
   * @param tradeInputs the trades/securities used for the cycle
   * @param calculationArguments the cycle arguments used to run the cycle
   * be used whilst the cycle is running
   * @param proxiedComponentMap the components that will be used
   * whilst the cycle is running
   */
  public DefaultCycleRecorder(ViewConfig viewConfig,
                              List<?> tradeInputs,
                              CalculationArguments calculationArguments,
                              MarketDataEnvironment marketDataEnvironment,
                              ProxiedComponentMap proxiedComponentMap) {
    _marketDataEnvironment = ArgumentChecker.notNull(marketDataEnvironment, "marketDataEnvironment");
    _viewConfig = ArgumentChecker.notNull(viewConfig, "viewConfig");
    _tradeInputs = ArgumentChecker.notNull(tradeInputs, "tradeInputs");
    _calculationArguments = ArgumentChecker.notNull(calculationArguments, "calculationArguments");
    _proxiedComponentMap = ArgumentChecker.notNull(proxiedComponentMap, "proxiedComponentMap");
  }

  @Override
  public Results complete(Results results) {
    Map<ZonedDateTime, Map<Pair<ExternalIdBundle, FieldName>, Result<?>>> emptyMarketData = Collections.emptyMap();

    ViewInputs viewInputs = new ViewInputs(_tradeInputs,
                                           _viewConfig,
                                           _calculationArguments.getFunctionArguments(),
                                           _calculationArguments.getValuationTime(),
                                           emptyMarketData,
                                           _proxiedComponentMap.retrieveResults(),
                                           _proxiedComponentMap.retrieveHtsResults(),
                                           _marketDataEnvironment);
    return results.withViewInputs(viewInputs);
  }

}
