/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/* package */final class GetFunctionsStep extends ResolveTask.State {

  private static final Logger s_logger = LoggerFactory.getLogger(GetFunctionsStep.class);

  public GetFunctionsStep(final ResolveTask task) {
    super(task);
  }

  private static final class LiveDataResolvedValueProducer implements ResolvedValueProducer {

    private final ValueRequirement _valueRequirement;
    private final ResolvedValue _resolvedValue;
    private int _refCount = 1;

    public LiveDataResolvedValueProducer(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue) {
      _valueRequirement = valueRequirement;
      _resolvedValue = resolvedValue;
    }

    @Override
    public Cancelable addCallback(final GraphBuildingContext context, final ResolvedValueCallback callback) {
      final AtomicReference<ResolvedValueCallback> callbackRef = new AtomicReference<ResolvedValueCallback>(callback);
      context.resolved(callback, _valueRequirement, _resolvedValue, new ResolutionPump() {

        @Override
        public void pump(final GraphBuildingContext context) {
          final ResolvedValueCallback callback = callbackRef.getAndSet(null);
          if (callback != null) {
            // No error information to push; just that there are no additional value requirements
            context.failed(callback, _valueRequirement, null);
          }
        }

        @Override
        public void close(final GraphBuildingContext context) {
          callbackRef.set(null);
        }

      });
      return new Cancelable() {
        @Override
        public boolean cancel(final GraphBuildingContext context) {
          return callbackRef.getAndSet(null) != null;
        }
      };
    }

    @Override
    public synchronized void addRef() {
      assert _refCount > 0;
      _refCount++;
    }

    @Override
    public synchronized int release(final GraphBuildingContext context) {
      assert _refCount > 0;
      return --_refCount;
    }

  }

  @Override
  protected void run(final GraphBuildingContext context) {
    switch (context.getMarketDataAvailabilityProvider().getAvailability(getValueRequirement())) {
      case AVAILABLE:
        s_logger.info("Found live data for {}", getValueRequirement());
        final MarketDataSourcingFunction function = new MarketDataSourcingFunction(getValueRequirement());
        final ResolvedValue result = createResult(function.getResult(), new ParameterizedFunction(function, function.getDefaultParameters()), Collections.<ValueSpecification>emptySet(), Collections
            .singleton(function.getResult()));
        final LiveDataResolvedValueProducer producer = new LiveDataResolvedValueProducer(getValueRequirement(), result);
        final ResolvedValueProducer existing = context.declareTaskProducing(function.getResult(), getTask(), producer);
        if (!pushResult(context, result)) {
          throw new IllegalStateException(result + " rejected by pushResult");
        }
        producer.release(context);
        existing.release(context);
        // Leave in current state; will go to finished after being pumped
        break;
      case NOT_AVAILABLE:
        final Iterator<Pair<ParameterizedFunction, ValueSpecification>> itr = context.getFunctionResolver().resolveFunction(getValueRequirement(), getComputationTarget());
        if (itr.hasNext()) {
          s_logger.debug("Found functions for {}", getValueRequirement());
          setRunnableTaskState(new NextFunctionStep(getTask(), itr), context);
        } else {
          s_logger.info("No functions for {}", getValueRequirement());
          storeFailure(ResolutionFailure.noFunctions(getValueRequirement()));
          setTaskStateFinished(context);
        }
        break;
      case MISSING:
        s_logger.info("Missing market data for {}", getValueRequirement());
        storeFailure(ResolutionFailure.marketDataMissing(getValueRequirement()));
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
