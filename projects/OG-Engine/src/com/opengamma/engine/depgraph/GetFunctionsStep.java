/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/* package */final class GetFunctionsStep extends ResolveTask.State {

  private static final Logger s_logger = LoggerFactory.getLogger(GetFunctionsStep.class);

  public GetFunctionsStep(final ResolveTask task) {
    super(task);
  }

  @Override
  protected void run(final GraphBuildingContext context) {
    switch (context.getMarketDataAvailabilityProvider().getAvailability(getValueRequirement())) {
      case AVAILABLE:
        s_logger.info("Found live data for {}", getValueRequirement());
        final MarketDataSourcingFunction function = new MarketDataSourcingFunction(getValueRequirement());
        final ResolvedValue result = createResult(function.getResult(), new ParameterizedFunction(function, function.getDefaultParameters()), Collections.<ValueSpecification>emptySet(), Collections
            .singleton(MemoryUtils.instance(function.getResult())));
        context.declareProduction(result);
        if (!pushResult(context, result, true)) {
          throw new IllegalStateException(result + " rejected by pushResult");
        }
        // Leave in current state; will go to finished after being pumped
        break;
      case NOT_AVAILABLE:
        final ComputationTarget target = getComputationTarget(context);
        if (target != null) {
          final Iterator<Pair<ParameterizedFunction, ValueSpecification>> itr = context.getFunctionResolver().resolveFunction(getValueRequirement(), target);
          if (itr.hasNext()) {
            s_logger.debug("Found functions for {}", getValueRequirement());
            setRunnableTaskState(new NextFunctionStep(getTask(), itr), context);
          } else {
            s_logger.info("No functions for {}", getValueRequirement());
            storeFailure(context.noFunctions(getValueRequirement()));
            setTaskStateFinished(context);
          }
        } else {
          s_logger.info("No functions for unresolved target {}", getValueRequirement());
          storeFailure(context.noFunctions(getValueRequirement()));
          setTaskStateFinished(context);
        }
        break;
      case MISSING:
        s_logger.info("Missing market data for {}", getValueRequirement());
        storeFailure(context.marketDataMissing(getValueRequirement()));
        setTaskStateFinished(context);
        break;
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  protected void pump(final GraphBuildingContext context) {
    // Only had one market data result so go to finished state
    setTaskStateFinished(context);
  }

  @Override
  protected boolean isActive() {
    // Get functions has no background behavior - if run isn't called, nothing will happen
    return false;
  }

  @Override
  public String toString() {
    return "GET_FUNCTIONS" + getObjectId();
  }

}
