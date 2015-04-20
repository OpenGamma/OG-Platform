/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.server;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.marketdata.CompositeMarketDataSpecification;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;

/**
 * Server capable of executing view requests.
 *
 * @deprecated use {@link Engine} directly
 */
@Deprecated
public class DefaultFunctionServer implements FunctionServer {

  private final Engine _engine;

  /**
   * @param engine engine that performs the calculation cycles
   */
  @Inject
  public DefaultFunctionServer(Engine engine) {
    _engine = engine;
  }

  @Override
  public Results executeSingleCycle(FunctionServerRequest<IndividualCycleOptions> request) {
    IndividualCycleOptions cycleOptions = request.getCycleOptions();
    CalculationArguments calculationArguments =
        CalculationArguments.builder()
            .valuationTime(cycleOptions.getValuationTime())
            .captureInputs(cycleOptions.isCaptureInputs())
            .marketDataSpecification(CompositeMarketDataSpecification.of(cycleOptions.getMarketDataSpecs()))
            .build();
    return _engine.runView(request.getViewConfig(),
                           calculationArguments,
                           MarketDataEnvironmentBuilder.empty(),
                           request.getInputs());
  }

  @Override
  public List<Results> executeMultipleCycles(FunctionServerRequest<GlobalCycleOptions> request) {
    List<Results> resultsList = new ArrayList<>();

    for (IndividualCycleOptions cycleOptions : request.getCycleOptions()) {
      CalculationArguments calculationArguments =
          CalculationArguments.builder()
              .valuationTime(cycleOptions.getValuationTime())
              .captureInputs(cycleOptions.isCaptureInputs())
              .marketDataSpecification(CompositeMarketDataSpecification.of(cycleOptions.getMarketDataSpecs()))
              .build();
      Results results = _engine.runView(request.getViewConfig(),
                                        calculationArguments,
                                        MarketDataEnvironmentBuilder.empty(),
                                        request.getInputs());
      resultsList.add(results);
    }
    return resultsList;
  }
}
