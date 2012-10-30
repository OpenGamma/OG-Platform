/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.marketdata.availability.MarketDataNotSatisfiableException;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.BlockingOperation;
import com.opengamma.util.tuple.Triple;

/* package */final class GetFunctionsStep extends ResolveTask.State {

  private static final Logger s_logger = LoggerFactory.getLogger(GetFunctionsStep.class);

  public GetFunctionsStep(final ResolveTask task) {
    super(task);
  }

  @Override
  protected boolean run(final GraphBuildingContext context) {
    boolean missing = false;
    ValueSpecification marketDataSpec = null;
    BlockingOperation.off();
    try {
      marketDataSpec = context.getMarketDataAvailabilityProvider().getAvailability(getValueRequirement());
    } catch (BlockingOperation e) {
      return false;
    } catch (MarketDataNotSatisfiableException e) {
      missing = true;
    } finally {
      BlockingOperation.on();
    }
    if (marketDataSpec != null) {
      s_logger.info("Found live data for {}", getValueRequirement());
      if (getValueRequirement().isSatisfiedBy(marketDataSpec)) {
        final MarketDataSourcingFunction function = new MarketDataSourcingFunction(getValueRequirement(), marketDataSpec);
        final ResolvedValue result = createResult(function.getResult(), new ParameterizedFunction(function, function.getDefaultParameters()), Collections.<ValueSpecification>emptySet(), Collections
              .singleton(MemoryUtils.instance(function.getResult())));
        context.declareProduction(result);
        if (!pushResult(context, result, true)) {
          throw new IllegalStateException(result + " rejected by pushResult");
        }
        // Leave in current state; will go to finished after being pumped
      } else {
        // A well behaved market data provider shouldn't do this, treat as missing market data
        s_logger.warn("Live data {} cannot satisfy {}", marketDataSpec, getValueRequirement());
        storeFailure(context.marketDataMissing(getValueRequirement()));
        setTaskStateFinished(context);
        }
    } else {
      if (missing) {
        s_logger.info("Missing market data for {}", getValueRequirement());
        storeFailure(context.marketDataMissing(getValueRequirement()));
        setTaskStateFinished(context);
      } else {
        final ComputationTarget target = getComputationTarget(context);
        if (target != null) {
          final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> itr = context.getFunctionResolver().resolveFunction(getValueRequirement(), target);
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
          storeFailure(context.couldNotResolve(getValueRequirement()));
          setTaskStateFinished(context);
        }
      }
    }
    return true;
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
