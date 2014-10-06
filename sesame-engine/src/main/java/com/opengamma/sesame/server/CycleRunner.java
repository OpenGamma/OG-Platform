/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.server;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Duration;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.sesame.config.FunctionArguments;
import com.opengamma.sesame.engine.CycleArguments;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.engine.View;
import com.opengamma.sesame.marketdata.CycleMarketDataFactory;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.MarketDataFactory;
import com.opengamma.sesame.marketdata.StrategyAwareMarketDataSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Responsible for running a cycles of the engine.
 * <p>
 * This involves checking the source of market data and ensuring that
 * all required data is retrieved. To achieve this for data sources which
 * do not eagerly retrieve data (e.g. most live data providers), a cycle may
 * be run multiple times until all its market data requirements are fulfilled.
 */
public class CycleRunner {

  /**
   * A market data source used for setting up cycles.
   * It is not expected to be used to access market data but merely
   * provides a suitable initial value.
   */
  public static final StrategyAwareMarketDataSource INITIAL_MARKET_DATA_SOURCE = new InitialMarketDataSource();

  /**
   * The view to be executed, not null.
   */
  private final View _view;
  /**
   * The manager of the market data sources, not null.
   */
  private final MarketDataFactory _marketDataFactory;
  /**
   * The cycle options determining how the cycles of the view
   * should be executed, not null.
   */
  private final CycleOptions _cycleOptions;
  /**
   * The trades/securities to execute the cycles with, not null
   * but may be empty.
   */
  private final List<Object> _inputs;
  /**
   * Handles the results produced by each cycle of the engine, not null.
   */
  private final CycleResultsHandler _handler;
  /**
   * Schedules the cycles in a manner appropriate to the use case. This
   * may involve running as fast as possible, or throttling cycles
   * to reduce overheads.
   */
  private final CycleScheduler _cycleScheduler;

  /**
   * Creates the new cycle runner. Intended for views where throttling is
   * required such that there is a minimum period between each cycle.
   *
   * @param view  the view to be executed, not null
   * @param marketDataFactory  the factory for market data sources, not null
   * @param cycleOptions  the cycle options determining how the cycles of the
   * view should be executed, not null
   * @param inputs  the trades/securities to execute the cycles with,
   *   not null but may be empty
   * @param handler  handler for the results produced by each cycle of the
   *   engine, not null
   * @param cycleTerminator  determines whether the execution of the cycles
   *   should be terminated, not null
   * @param minimumTimeBetweenCycles  minimum duration between cycles when
   *   streaming, not null
   */
  public CycleRunner(View view,
                     MarketDataFactory marketDataFactory,
                     CycleOptions cycleOptions,
                     List<Object> inputs,
                     CycleResultsHandler handler,
                     CycleTerminator cycleTerminator,
                     Duration minimumTimeBetweenCycles) {

    this(view, marketDataFactory, cycleOptions, inputs, handler,
         createScheduler(
             ArgumentChecker.notNull(cycleTerminator, "cycleTerminator"),
             minimumTimeBetweenCycles));
  }

  /**
   * Creates the new cycle runner. Intended for views where the results
   * for each cycle are wanted as fast as possible.
   *
   * @param view  the view to be executed, not null
   * @param marketDataFactory  the factory for market data sources, not null
   * @param cycleOptions  the cycle options determining how the cycles of the
   * view should be executed, not null
   * @param inputs  the trades/securities to execute the cycles with,
   *   not null but may be empty
   * @param handler  handler for the results produced by each cycle of the
   *   engine, not null
   * @param cycleTerminator  determines whether the execution of the cycles
   *   should be terminated, not null
   */
  public CycleRunner(View view,
                     MarketDataFactory marketDataFactory,
                     CycleOptions cycleOptions,
                     List<Object> inputs,
                     CycleResultsHandler handler,
                     CycleTerminator cycleTerminator) {

    this(view, marketDataFactory, cycleOptions, inputs, handler,
         new DefaultCycleScheduler(ArgumentChecker.notNull(cycleTerminator, "cycleTerminator")));
  }

  /**
   * Common private constructor for use by the two public ones.
   */
  private CycleRunner(View view,
                      MarketDataFactory marketDataFactory,
                      CycleOptions cycleOptions,
                      List<Object> inputs,
                      CycleResultsHandler handler,
                      CycleScheduler cycleScheduler) {
    _view = ArgumentChecker.notNull(view, "view");
    _marketDataFactory = ArgumentChecker.notNull(marketDataFactory, "marketDataFactory");
    _cycleOptions = ArgumentChecker.notNull(cycleOptions, "cycleOptions");
    _inputs = ArgumentChecker.notNull(inputs, "inputs");
    _handler = ArgumentChecker.notNull(handler, "handler");
    _cycleScheduler = cycleScheduler;
  }

  private static CycleScheduler createScheduler(CycleTerminator cycleTerminator, Duration minimumTimeBetweenCycles) {
    ArgumentChecker.notNull(minimumTimeBetweenCycles, "minimumTimeBetweenCycles");
    ArgumentChecker.notNull(cycleTerminator, "cycleTerminator");
    return minimumTimeBetweenCycles.isZero() ?
        new DefaultCycleScheduler(cycleTerminator) :
        new ThrottledCycleScheduler(minimumTimeBetweenCycles, cycleTerminator);
  }

  //-------------------------------------------------------------------------
  /**
   * Execute the view with each of the cycle options, checking if
   * early termination is required. We keep track of the market data
   * source used as this may be used to help setup the next cycle.
   */
  public void execute() {

    // We keep track of the market data being used so that when required we can
    // track the changes in market data between cycles
    final CycleMarketDataFactory cycleMarketDataFactory =
        new DefaultCycleMarketDataFactory(_marketDataFactory, INITIAL_MARKET_DATA_SOURCE);

    // Iterate over the cycle options. As they may be infinite (e.g. streaming),
    // we need to check the terminator to see if external events mean we should stop.
    final Iterator<IndividualCycleOptions> cycleOptions = _cycleOptions.iterator();

    _cycleScheduler.run(cycleMarketDataFactory, cycleOptions, _handler, this);
  }

  private Pair<Results, CycleMarketDataFactory> cycleUntilResultsAvailable(
      IndividualCycleOptions cycleOptions, CycleMarketDataFactory previousFactory) {

    // We first run a cycle, then check it to see if any market data is
    // pending. Where we are using a non-lazy data source (i.e. not
    // live data), then we will not need to do anything more and can just
    // return the results we have

    CycleMarketDataFactory factory =
        createPrimedCycleMarketDataFactory(previousFactory, cycleOptions.getMarketDataSpec());

    Results result = executeCycle(cycleOptions, factory);

    while (result.isPendingMarketData()) {

      // If there is market data pending then we ask for a market data
      // source primed with the missing results and retry. It is possible
      // that the subsequent run then wants additional market market data
      // so we keep repeating until no data is pending.
      factory = factory.withPrimedMarketDataSource();
      result = executeCycle(cycleOptions, factory);
    }

    return Pairs.of(result, factory);
  }

  private CycleMarketDataFactory createPrimedCycleMarketDataFactory(CycleMarketDataFactory previousFactory,
                                                                    MarketDataSpecification marketDataSpecification) {

    // TODO - this cast suggests a design problem - get rid of it
    StrategyAwareMarketDataSource previousSource =
        (StrategyAwareMarketDataSource) previousFactory.getPrimaryMarketDataSource();
    if (previousSource.isCompatible(marketDataSpecification)) {
      return previousFactory.withPrimedMarketDataSource();
    } else {
      previousSource.dispose();
      // This is a new source, so it doesn't need to be primed (that
      // will happen automatically on the next cycle of appropriate)
      return previousFactory.withMarketDataSpecification(marketDataSpecification);
    }
  }

  private Results executeCycle(IndividualCycleOptions cycleOptions,
                               CycleMarketDataFactory cycleMarketDataFactory) {
    CycleArguments cycleArguments = createCycleArguments(cycleOptions, cycleMarketDataFactory);
    return _view.run(cycleArguments, _inputs);
  }

  private CycleArguments createCycleArguments(IndividualCycleOptions cycleOptions,
                                              CycleMarketDataFactory cycleMarketDataFactory) {

    // todo - we may want a method whereby we can get the delta of data that has changed since the previous cycle
    // todo - pass the delta in through the cycle arguments
    // todo - version correction should be coming from somewhere - cycle options?

    // todo - these need real values
    FunctionArguments functionArguments = FunctionArguments.EMPTY;
    VersionCorrection configVersionCorrection = VersionCorrection.LATEST;

    return new CycleArguments(cycleOptions.getValuationTime(),
                              configVersionCorrection,
                              cycleMarketDataFactory,
                              functionArguments,
                              cycleOptions.isCaptureInputs());
  }

  /**
   * A market data source used for setting up engine cycles. It is not
   * expected to be used to access market data but merely provides a
   * suitable initial value.
   */
  private static class InitialMarketDataSource implements StrategyAwareMarketDataSource {

    @Override
    public Result<?> get(ExternalIdBundle id, FieldName fieldName) {
      throw new UnsupportedOperationException("get not supported");
    }

    @Override
    public StrategyAwareMarketDataSource createPrimedSource() {
      throw new UnsupportedOperationException("should never be called");
    }

    @Override
    public boolean isCompatible(MarketDataSpecification specification) {
      // Always false - want a real source on the next cycle
      return false;
    }

    @Override
    public void dispose() {
    }
  }

  /**
   * Private interface, responsible for the iteration/scheduling of
   * cycles for the view.
   */
  private interface CycleScheduler {

    /**
     * Run the cycles, iterating over the cycle options.
     *
     * @param cycleMarketDataFactory  the source of marketdata
     * @param cycleOptions  the cycle options determining how the cycles of the
     *   view should be executed, not null
     * @param handler  handler for the results produced by each cycle of the
     *   engine, not null
     * @param cycleRunner  the parent cycle runner
     */
    void run(CycleMarketDataFactory cycleMarketDataFactory,
             Iterator<IndividualCycleOptions> cycleOptions,
             CycleResultsHandler handler, CycleRunner cycleRunner);
  }

  /**
   * Cycle scheduler which uses a Java scheduler to cue up the
   * next cycle in the streaming case. This means that the cycles
   * can be run with a minimum time gap between them. In scenarions
   * where the market data is not ticking (fast), this means the
   * engine does not spin, repeatedly returning the same results.
   */
  private static class ThrottledCycleScheduler implements CycleScheduler {

    /**
     * Minimum time period between cycles when streaming, not null. If a series of
     * cycles are to be run and the first starts at time <code>t</code>, then the next cycle
     * will not be started until at least time <code>t + minimumTimeBetweenCycles</code>.
     * This prevents the server "spinning" for cases where the cycles execute quickly
     * as the underlying data has not changed since the last run.
     */
    private final Duration _minimumTimeBetweenCycles;

    /**
     * Determines whether the execution of the cycles should be terminated, not null.
     * This is generally used when an infinite set of cycles has been requested
     * and we want to stop processing (e.g. a UI using streaming data, which the
     * user then decides they have finished with).
     */
    private final CycleTerminator _cycleTerminator;

    /**
     * Create the cycle scheduler.
     *
     * @param minimumTimeBetweenCycles  the minimum time between cycles
     * @param cycleTerminator  determines whether the execution of the cycles
     *   should be terminated
     */
    public ThrottledCycleScheduler(Duration minimumTimeBetweenCycles, CycleTerminator cycleTerminator) {
      _minimumTimeBetweenCycles = minimumTimeBetweenCycles;
      _cycleTerminator = cycleTerminator;
    }

    @Override
    public void run(CycleMarketDataFactory cycleMarketDataFactory,
                    Iterator<IndividualCycleOptions> cycleOptions,
                    CycleResultsHandler handler, CycleRunner cycleRunner) {

      // Scheduler to be used for executing the next cycle after
      // the minimum period has passed.
      ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

      // We create an initial task and execute it immediately. The task
      // itself will then schedule the next task, which in turn creates
      // the next etc. There are alternative implementations that could
      // be used and these should be reassessed:
      //   - create a simple timed task that periodically enables a
      //     semaphore/latch. Loop over cycle options and await the
      //     semaphore. When acquired, execute the cycle
      //   - use the scheduler to periodically schedule a job. Job
      //     picks up the next cycle option and executes
      scheduler.execute(createNextCycleTask(cycleMarketDataFactory, cycleOptions, handler, scheduler, cycleRunner));

      // Wait for all work to be completed before exiting
      try {
        scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
      } catch (InterruptedException e) {
        // Thread was interrupted, just exit with no action
      }
    }

    private Runnable createNextCycleTask(final CycleMarketDataFactory cycleMarketDataFactory,
                                         final Iterator<IndividualCycleOptions> cycleOptions,
                                         final CycleResultsHandler handler,
                                         final ScheduledExecutorService scheduler,
                                         final CycleRunner cycleRunner) {
      return new Runnable() {
        @Override
        public void run() {
          if (_cycleTerminator.shouldContinue() && cycleOptions.hasNext()) {
            long cycleStart = System.currentTimeMillis();
            Pair<Results, CycleMarketDataFactory> result =
                cycleRunner.cycleUntilResultsAvailable(cycleOptions.next(), cycleMarketDataFactory);
            handler.handleResults(result.getFirst());
            CycleMarketDataFactory cycleMarketDataFactory = result.getSecond();

            // Now schedule the next execution, negative value means
            // it will be executed immediately
            long delay = cycleStart + _minimumTimeBetweenCycles.toMillis() - System.currentTimeMillis();
            Runnable task =
                createNextCycleTask(cycleMarketDataFactory, cycleOptions, handler, scheduler, cycleRunner);
            scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
          } else {
            // No more work to be done, shut down the scheduler which
            // will allow the execute method to complete
            scheduler.shutdown();
          }
        }
      };
    }
  }

  /**
   * Cycle scheduler which executes cycles as fast as possible, one
   * after another.
   */
  private static class DefaultCycleScheduler implements CycleScheduler {

    /**
     * Determines whether the execution of the cycles should be terminated, not null.
     * This is generally used when an infinite set of cycles has been requested
     * and we want to stop processing (e.g. a UI using streaming data, which the
     * user then decides they have finished with).
     */
    private final CycleTerminator _cycleTerminator;

    /**
     * Create the cycle scheduler.
     *
     * @param cycleTerminator  determines whether the execution of the cycles
     *   should be terminated
     */
    public DefaultCycleScheduler(CycleTerminator cycleTerminator) {
      _cycleTerminator = cycleTerminator;
    }

    @Override
    public void run(CycleMarketDataFactory cycleMarketDataFactory,
                    Iterator<IndividualCycleOptions> cycleOptions,
                    CycleResultsHandler handler,
                    CycleRunner cycleRunner) {

      // Iterate over the cycle options. As they may be infinite (e.g. streaming),
      // we check the terminator to see if external events mean we should stop.
      while (_cycleTerminator.shouldContinue() && cycleOptions.hasNext()) {

        Pair<Results, CycleMarketDataFactory> result =
            cycleRunner.cycleUntilResultsAvailable(cycleOptions.next(), cycleMarketDataFactory);
        handler.handleResults(result.getFirst());
        cycleMarketDataFactory = result.getSecond();
      }
    }
  }

}
