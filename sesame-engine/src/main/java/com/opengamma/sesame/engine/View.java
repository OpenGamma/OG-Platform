/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.cache.CacheInvalidator;
import com.opengamma.sesame.cache.CacheProvider;
import com.opengamma.sesame.cache.CachingProxyDecorator;
import com.opengamma.sesame.cache.DefaultFunctionCache;
import com.opengamma.sesame.cache.ExecutingMethodsThreadLocal;
import com.opengamma.sesame.cache.FunctionCache;
import com.opengamma.sesame.config.FunctionArguments;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.config.NonPortfolioOutput;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.function.AvailableImplementations;
import com.opengamma.sesame.function.AvailableOutputs;
import com.opengamma.sesame.function.InvalidInputFunction;
import com.opengamma.sesame.function.InvokableFunction;
import com.opengamma.sesame.function.PermissionDeniedFunction;
import com.opengamma.sesame.function.scenarios.FilteredScenarioDefinition;
import com.opengamma.sesame.function.scenarios.ScenarioDefinition;
import com.opengamma.sesame.graph.CompositeNodeDecorator;
import com.opengamma.sesame.graph.FunctionBuilder;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.graph.Graph;
import com.opengamma.sesame.graph.GraphBuilder;
import com.opengamma.sesame.graph.GraphModel;
import com.opengamma.sesame.graph.NodeDecorator;
import com.opengamma.sesame.marketdata.GatheringMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.proxy.ExceptionWrappingProxy;
import com.opengamma.sesame.proxy.MetricsProxy;
import com.opengamma.sesame.trace.CallGraph;
import com.opengamma.sesame.trace.Tracer;
import com.opengamma.sesame.trace.TracingProxy;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * View is the main class for running calculations over a portfolio and producing results.
 * <p>
 * A view is created by a {@link ViewFactory}. It defines a set of calculations used to populate a
 * table of results when the view is run. The columns in the table are defined by a {@link ViewConfig}
 * and there is one row in the table of results for each item in the portfolio.
 * <p>
 * A view can also define a set of outputs which are calculations that are performed independently of
 * the portfolio. For example an output could be defined to return the curve used in the calculations.
 * <p>
 * A view is executed by calling one of the {@code run} or {@code runAsync} methods. A view can be run
 * repeatedly and can execute multiple runs concurrently.
 */
public class View {

  private static final Logger s_logger = LoggerFactory.getLogger(View.class);

  private final Graph _graph;
  private final ViewConfig _viewConfig;
  private final ListeningExecutorService _executor;
  private final FunctionModelConfig _systemDefaultConfig;
  private final List<String> _columnNames;
  private final GraphModel _graphModel;

  /**
   * Provider that supplies caches to this view. A cache is requested at the start of each calculation
   * cycle and used for the duration of the cycle. Normally the factory will return the same cache each
   * cycle. However if the cache is cleared the factory will create a new, empty cache which will be
   * returned at the start of the next calculation cycle.
   */
  private final CacheProvider _cacheFactory;

  /** For building new, empty caches that are only used for a single calculation cycle. */
  private final CacheBuilder<Object, Object> _cacheBuilder;

  private final Optional<MetricRegistry> _metricRegistry;
  private final ComponentMap _componentMap;
  private final CacheInvalidator _cacheInvalidator;

  /**
   * Thread local variable used to expose the cache for the current cycle to the caching proxy.
   * In order to ensure consistency in the calculations, the same cache must be used for the whole
   * of a calculation cycle. Therefore the caching proxy must have a way to get hold of the correct
   * cache when it is handling a method call. There is no way to communicate an ID for the cycle
   * between the view and the proxy, so the cache is stored in a thread local which is used by
   * the cache provider.
   */
  private final ThreadLocal<Cache<Object, Object>> _cacheThreadLocal = new ThreadLocal<>();

  /** Whether caching is enabled. */
  private final boolean _cachingEnabled;

  View(ViewConfig viewConfig,
       ExecutorService executor,
       FunctionModelConfig systemDefaultConfig,
       FunctionBuilder functionBuilder,
       EnumSet<FunctionService> services,
       ComponentMap componentMap,
       Set<Class<?>> inputTypes,
       AvailableOutputs availableOutputs,
       AvailableImplementations availableImplementations,
       CacheProvider cacheFactory,
       CacheBuilder<Object, Object> cacheBuilder,
       CacheInvalidator cacheInvalidator,
       Optional<MetricRegistry> metricRegistry) {

    // Provider that supplies the cache to the caching decorators
    // the field is updated with a cache from _cacheFactory at the start of each cycle
    CacheProvider cacheProvider = new CacheProvider() {
      @Override
      public Cache<Object, Object> get() {
        return _cacheThreadLocal.get();
      }
    };
    FunctionCache cache = new DefaultFunctionCache(cacheProvider);
    _cacheBuilder = ArgumentChecker.notNull(cacheBuilder, "cacheBuilder");
    _cachingEnabled = services.contains(FunctionService.CACHING);
    _cacheInvalidator = ArgumentChecker.notNull(cacheInvalidator, "cacheInvalidator");
    _componentMap = ArgumentChecker.notNull(componentMap, "componentMap").with(FunctionCache.class, cache);
    _viewConfig = ArgumentChecker.notNull(viewConfig, "viewConfig");
    _executor = MoreExecutors.listeningDecorator(executor);
    _systemDefaultConfig = ArgumentChecker.notNull(systemDefaultConfig, "systemDefaultConfig");
    _cacheFactory = ArgumentChecker.notNull(cacheFactory, "cacheFactory");
    _metricRegistry = ArgumentChecker.notNull(metricRegistry, "metricRegistry");
    _columnNames = columnNames(_viewConfig);

    ExecutingMethodsThreadLocal executingMethods = new ExecutingMethodsThreadLocal();

    NodeDecorator decorator = createNodeDecorator(services, cacheProvider, executingMethods);

    s_logger.debug("building graph model");
    GraphBuilder graphBuilder = new GraphBuilder(availableOutputs,
                                                 availableImplementations,
                                                 _componentMap.getComponentTypes(),
                                                 systemDefaultConfig,
                                                 decorator);
    _graphModel = graphBuilder.build(viewConfig, inputTypes);
    s_logger.debug("graph model complete, building graph");
    _graph = _graphModel.build(_componentMap, functionBuilder);
    s_logger.debug("graph complete");
  }

  private NodeDecorator createNodeDecorator(EnumSet<FunctionService> services,
                                            CacheProvider cacheProvider,
                                            ExecutingMethodsThreadLocal executingMethods) {

    ImmutableList.Builder<NodeDecorator> decorators = new ImmutableList.Builder<>();

    // Build up the proxies to be used from the outermost
    // to the innermost

    // Timing/tracing sits outside of caching so the actual
    // time taken for a request is reported. This can also
    // report on whether came from the cache or were
    // calculated (if cache there will be no child calls).
    // Only allow one tracing proxy but pick the most
    // comprehensive one
    if (services.contains(FunctionService.TRACING)) {
      decorators.add(TracingProxy.INSTANCE);
    }

    // Caching proxy memoizes requests as required so that
    // expensive calculations are not performed more
    // frequently than they need to be
    if (services.contains(FunctionService.CACHING)) {
      decorators.add(new CachingProxyDecorator(cacheProvider, executingMethods));
    }

    // Metrics records time taken to execute each function. This
    // sits inside the caching layer as we're interested in how
    // long the actual calculation takes not how long it takes to
    // get from the cache
    if (services.contains(FunctionService.METRICS)) {
      if (_metricRegistry.isPresent()) {
        decorators.add(new MetricsProxy(_metricRegistry.get()));
      } else {
        // This should be prevented by the ViewFactoryComponentFactory but is
        // here in case of programmatic misconfiguration
        s_logger.warn("Unable to create metrics proxy as no metrics repository has been configured");
      }
    }

    // Ensure we always have the exception wrapping behaviour so
    // methods returning Result<?> return Failure if an exception
    // is thrown internally.
    decorators.add(ExceptionWrappingProxy.INSTANCE);
    return CompositeNodeDecorator.compose(decorators.build());
  }

  /**
   * Runs a single calculation cycle, blocking until the results are available.
   *
   * @param cycleArguments settings for running the calculations
   * @return the calculation results
   * @deprecated use {@link #run(CalculationArguments, MarketDataEnvironment)}
   */
  @Deprecated
  public Results run(CycleArguments cycleArguments) {
    return run(cycleArguments, Collections.emptyList());
  }

  /**
   * Runs a single calculation cycle, blocking until the results are available.
   *
   * @param cycleArguments settings for running the calculations
   * @param inputs the inputs to the calculation, e.g. trades, positions, securities
   * @return the calculation results
   * @deprecated use {@link #run(CalculationArguments, MarketDataEnvironment, List)}
   */
  @Deprecated
  public Results run(CycleArguments cycleArguments, List<?> inputs) {
    try {
      CalculationArguments calculationArguments =
          CalculationArguments.builder()
              .valuationTime(cycleArguments.getValuationTime())
              .captureInputs(cycleArguments.isCaptureInputs())
              .configVersionCorrection(cycleArguments.getConfigVersionCorrection())
              .traceCells(cycleArguments.getTraceCells())
              .traceOutputs(cycleArguments.getTraceOutputs())
              .build();
      return runAsync(calculationArguments, cycleArguments.getMarketDataEnvironment(), inputs).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new OpenGammaRuntimeException("Failed to run view", e);
    }
  }

  /**
   * Runs a single calculation cycle, blocking until the results are available.
   *
   * @param cycleArguments settings for running the calculations
   * @return the calculation results
   */
  public Results run(CalculationArguments cycleArguments, MarketDataEnvironment marketData) {
    return run(cycleArguments, marketData, Collections.emptyList());
  }

  /**
   * Runs a single calculation cycle, blocking until the results are available.
   *
   * @param calculationArguments settings for running the calculations
   * @param inputs the inputs to the calculation, e.g. trades, positions, securities
   * @return the calculation results
   */
  public Results run(CalculationArguments calculationArguments, MarketDataEnvironment marketData, List<?> inputs) {
    try {
      return runAsync(calculationArguments, marketData, inputs).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new OpenGammaRuntimeException("Failed to run view", e);
    }
  }

  /**
   * Runs a single calculation cycle asynchronously, returning a future representing the pending results.
   *
   * @param calculationArguments settings for running the calculations
   * @return a future representing the calculation results
   */
  public ListenableFuture<Results> runAsync(CalculationArguments calculationArguments, MarketDataEnvironment marketData) {
    return runAsync(calculationArguments, marketData, Collections.emptyList());
  }

  /**
   * Runs a single calculation cycle asynchronously, returning a future representing the pending results.
   *
   * @param calculationArguments settings for running the calculations
   * @param inputs the inputs to the calculation, e.g. trades, positions, securities
   * @return a future representing the calculation results
   * @throws IllegalStateException if ThreadLocalServiceContext not set
   */
  public ListenableFuture<Results> runAsync(CalculationArguments calculationArguments,
                                            MarketDataEnvironment marketData,
                                            final List<?> inputs) {
    ArgumentChecker.notNull(calculationArguments, "calculationArguments");
    final Instant start = Instant.now();
    final long startInitialization = System.nanoTime();
    final long startExecution;

    /*
     * Get a cache from the factory that will be used for the duration of this calculation cycle.
     * the same cache must be used for all calculations in a cycle to ensure consistency in the results.
     *
     * The cache is never cleared during a cycle, if the user requests a clean cache then an empty cache is
     * created and returned by the factory at the start of the next cycle.
     *
     * Therefore it is possible that two cycles can be executing concurrently in the same view using a different cache.
     * It is essential to ensure that the correct cache is used for each cycle even though the caching proxy
     * might receive invocations for different cycles interleaved with each other.
     *
     * To achieve this, a thread local is used to hold the cache. there is one ThreadLocalWrapper for each cycle
     * which contains the cache for that cycle. It also contains the thread local that will hold the cache to allow
     * the caching proxy to retrieve it. The tasks that perform the calculations set the thread local with
     * the cycle's cache before executing the calculations and clearing the thread local afterwards.
     */
    Cache<Object, Object> cache = getCache();
    VersionCorrectionProvider vcProvider = getVersionCorrectionProvider(calculationArguments);
    ServiceContext originalContext = getThreadLocalServiceContext();
    ServiceContext context = originalContext.with(VersionCorrectionProvider.class, vcProvider);

    final CycleInitializer cycleInitializer = calculationArguments.isCaptureInputs() ?
        new CapturingCycleInitializer(context, _componentMap, calculationArguments,
            marketData, _graphModel, _viewConfig, _cacheBuilder, inputs) :
        new StandardCycleInitializer(context, _graph, cache);

    ServiceContext cycleContext = cycleInitializer.getServiceContext();

    ThreadLocalWrapper threadLocalWrapper =
        new ThreadLocalWrapper(cycleContext, originalContext, cycleInitializer.getCache(), _cacheThreadLocal);

    ListenableFuture<List<TaskResult>> tasksFuture =
        runAsync(calculationArguments, marketData, cycleInitializer, threadLocalWrapper, inputs);

    startExecution = System.nanoTime();

    return Futures.transform(tasksFuture, new Function<List<TaskResult>, Results>() {
      @Nullable
      @Override
      public Results apply(List<TaskResult> taskResults) {
        return buildResults(inputs, taskResults, start, startInitialization, startExecution, cycleInitializer);
      }
    });
  }

  /**
   * Collects requirements for market data that must be provided for running the calculations in this view for
   * a portfolio.
   * <p>
   * If the data is present in {@code suppliedData} it won't be included in the returned requirements.
   *
   * @param suppliedData market data that is already available and therefore doesn't need to be built or provided
   * @param calculationArguments arguments specifying how the calculations should be performed
   * @param portfolio the portfolio for which the calculations are being performed
   * @return requirements for the market data needed to run the calculations in this view for the specified portfolio
   */
  public Set<MarketDataRequirement> gatherRequirements(MarketDataEnvironment suppliedData,
                                                       CalculationArguments calculationArguments,
                                                       List<?> portfolio) {
    Cache<Object, Object> cache = getCache();
    VersionCorrectionProvider vcProvider = getVersionCorrectionProvider(calculationArguments);
    ServiceContext originalContext = getThreadLocalServiceContext();
    ServiceContext context = originalContext.with(VersionCorrectionProvider.class, vcProvider);
    CycleInitializer cycleInitializer = new StandardCycleInitializer(context, _graph, cache);
    ThreadLocalWrapper threadLocalWrapper =
        new ThreadLocalWrapper(context, originalContext, cycleInitializer.getCache(), _cacheThreadLocal);
    ZonedDateTime valuationTime = calculationArguments.getValuationTime();
    GatheringMarketDataBundle gatheringBundle = GatheringMarketDataBundle.create(suppliedData.toBundle());
    MarketDataEnvironment marketData = new GatheringMarketDataEnvironment(gatheringBundle, valuationTime);
    ListenableFuture<List<TaskResult>> tasksFuture =
        runAsync(calculationArguments, marketData, cycleInitializer, threadLocalWrapper, portfolio);
    try {
      tasksFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      // this will only happen if there is a bug, all exceptions should be caught and wrapped in a failure result
      throw new OpenGammaRuntimeException("Failed to gather requirements", e);
    }
    return gatheringBundle.getRequirements();
  }

  /**
   * Asynchronously runs the calculations in this view, returning a future representing the results.
   *
   * @param calculationArguments arguments specifying how the calculations should be performed
   * @param marketData market data used by the calculations
   * @param cycleInitializer for setting up the calculation cycle
   * @param threadLocalWrapper for setting up and tearing down thread-local state in each task
   * @param portfolio the portfolio for which the calculations should be run
   * @return a future representing the results of the calculations
   */
  private ListenableFuture<List<TaskResult>> runAsync(CalculationArguments calculationArguments,
                                                      MarketDataEnvironment marketData,
                                                      CycleInitializer cycleInitializer,
                                                      ThreadLocalWrapper threadLocalWrapper,
                                                      List<?> portfolio) {

    List<Task> tasks = new ArrayList<>();
    Graph graph = cycleInitializer.getGraph();
    ScenarioDefinition scenario = _viewConfig.getScenarioDefinition();
    tasks.addAll(portfolioTasks(calculationArguments, marketData, portfolio, graph, scenario, threadLocalWrapper));
    tasks.addAll(nonPortfolioTasks(calculationArguments, marketData, graph, scenario, threadLocalWrapper));
    List<ListenableFuture<TaskResult>> resultFutures;
    resultFutures = invokeTasks(tasks);
    return Futures.allAsList(resultFutures);
  }

  /**
   * Creates a version correction provider based on the version correction in the arguments.
   *
   * @param calculationArguments arguments containing a version correction
   * @return provider for the version correction in the arguments
   */
  private VersionCorrectionProvider getVersionCorrectionProvider(CalculationArguments calculationArguments) {
    VersionCorrection correction = calculationArguments.getConfigVersionCorrection();
    return correction == null || correction.containsLatest() ?
        new FixedInstantVersionCorrectionProvider() :
        new FixedInstantVersionCorrectionProvider(correction.getVersionAsOf());
  }

  /**
   * @return a cache, appropriate for the cache settings
   */
  private Cache<Object, Object> getCache() {
    return _cachingEnabled ? _cacheFactory.get() : new NoOpCache();
  }

  /**
   * @return the service context from {@link ThreadLocalServiceContext}
   * @throws IllegalStateException if there is no thread-local service context
   */
  private static ServiceContext getThreadLocalServiceContext() {
    ServiceContext originalContext = ThreadLocalServiceContext.getInstance();

    if (originalContext == null) {
      throw new IllegalStateException("ThreadLocalServiceContext not set");
    }
    return originalContext;
  }

  /**
   * Builds a set of results from a list of futures representing the individual pending calculation results.
   *
   * @param portfolio the inputs to the trade calculations, e.g. trades, securities
   * @param taskResults the results of the individual calculations
   * @param start the start time of the calculation cycle
   * @param startInitialization the start time of the cycle (system nano time)
   * @param startExecution the start time of the calculations (system nano time)
   * @param cycleInitializer for post-processing the results
   * @return the results of the calculations
   */
  private Results buildResults(List<?> portfolio,
                               List<TaskResult> taskResults,
                               Instant start,
                               long startInitialization,
                               long startExecution,
                               CycleInitializer cycleInitializer) {

    ResultBuilder resultsBuilder = Results.builder(portfolio, _columnNames);

    for (TaskResult result : taskResults) {
      result.addToResults(resultsBuilder);
    }
    long startResultsBuild = System.nanoTime();
    Results results = resultsBuilder.build(start, startExecution, startInitialization, startResultsBuild);
    return cycleInitializer.complete(results);
  }

  /**
   * Submits all the tasks to the executor and returns the futures. This only exists because the {@code invokeAll}
   * method of {@code ListeningExecutorService} returns {@code Future} and not {@code ListenableFuture}.
   *
   * @param tasks the tasks to execute
   * @return futures representing the pending results of the tasks
   */
  private List<ListenableFuture<TaskResult>> invokeTasks(List<Task> tasks) {
    List<ListenableFuture<TaskResult>> results = new ArrayList<>(tasks.size());

    for (Task task : tasks) {
      results.add(_executor.submit(task));
    }
    return results;
  }

  /**
   * Returns the {@link FunctionModel} of the function used to calculate the value in a column.
   * @param columnName the name of the column
   * @param inputType type of input (i.e. the security, trade or position type) for the row
   * @return the function model or null if there isn't one for the specified input type
   * @throws IllegalArgumentException if the column name isn't found
   */
  public FunctionModel getFunctionModel(String columnName, Class<?> inputType) {
    return _graphModel.getFunctionModel(columnName, inputType);
  }

  /**
   * Returns the {@link FunctionModel} of the function used to calculate a non-portfolio output.
   * @param outputName The name of the output
   * @return the function model
   * @throws IllegalArgumentException if the output name isn't found
   */
  public FunctionModel getFunctionModel(String outputName) {
    return _graphModel.getFunctionModel(outputName);
  }

  private List<Task> portfolioTasks(CalculationArguments calculationArguments,
                                    MarketDataEnvironment marketDataEnvironment,
                                    List<?> inputs,
                                    Graph graph,
                                    ScenarioDefinition scenarioDefinition,
                                    ThreadLocalWrapper threadLocalWrapper) {
    // create tasks for the portfolio outputs
    int colIndex = 0;
    List<Task> portfolioTasks = Lists.newArrayList();
    for (ViewColumn column : _viewConfig.getColumns()) {
      FilteredScenarioDefinition filteredDef = scenarioDefinition.filter(column.getName());
      Environment env =
          new EngineEnvironment(valuationTime(calculationArguments, marketDataEnvironment),
                                marketDataEnvironment.toBundle(),
                                _cacheInvalidator);
      Environment columnEnv = env.withScenarioDefinition(filteredDef);
      Map<Class<?>, InvokableFunction> functions = graph.getFunctionsForColumn(column.getName());

      int rowIndex = 0;
      for (Object input : inputs) {
        // the function that is determined from the input
        InvokableFunction function;
        // the input to the function that is determined, which can be a security when the input is a position or trade
        Object functionInput;
        // try the type of the input
        InvokableFunction inputFunction = functions.get(input.getClass());
        if (inputFunction != null) {
          function = inputFunction;
          functionInput = input;
        } else if (input instanceof PositionOrTrade) {
          // extract the security from the position or trade
          try {
            Security security = ((PositionOrTrade) input).getSecurity();
            if (security == null) {
              function = new InvalidInputFunction(
                  "Position or trade does not contain a security, column: " + column + " type: " + input.getClass().getName());
              functionInput = input;
            } else {
              function = functions.get(security.getClass());
              if (function == null) {
                function = new InvalidInputFunction(
                    "No function found for security, column: " + column + " type: " + input.getClass().getName());
              }
              functionInput = security;
            }
          } catch (AuthorizationException ex) {
            function = new PermissionDeniedFunction(ex.getMessage());
            functionInput = input;
          }
        } else {
          // input is not known by the configuration
          function = new InvalidInputFunction(
              "No function found for input, column: " + column + " type: " + input.getClass().getName());
          functionInput = input;
        }
        Tracer tracer = Tracer.create(calculationArguments.traceType(rowIndex, colIndex));

        FunctionModelConfig columnConfig = column.getFunctionConfig(functionInput.getClass());
        FunctionModelConfig functionModelConfig =
            columnConfig.mergedWith(_viewConfig.getDefaultConfig(), _systemDefaultConfig);

        Class<?> implType = function.getUnderlyingReceiver().getClass();
        Class<?> declaringType = function.getDeclaringClass();
        Map<Class<?>, FunctionArguments> functionArguments = calculationArguments.getFunctionArguments();
        FunctionArguments args = functionArguments(functionArguments, implType, declaringType, functionModelConfig);
        portfolioTasks.add(new PortfolioTask(columnEnv, functionInput, args, rowIndex++,
                                             colIndex, function, tracer, threadLocalWrapper));
      }
      colIndex++;
    }
    return portfolioTasks;
  }

  // create tasks for the non-portfolio outputs
  private List<Task> nonPortfolioTasks(CalculationArguments calculationArguments,
                                       MarketDataEnvironment marketDataEnvironment,
                                       Graph graph,
                                       ScenarioDefinition scenarioDefinition,
                                       ThreadLocalWrapper cache) {
    List<Task> tasks = Lists.newArrayList();
    for (NonPortfolioOutput output : _viewConfig.getNonPortfolioOutputs()) {
      InvokableFunction function = graph.getNonPortfolioFunction(output.getName());
      Tracer tracer = Tracer.create(calculationArguments.traceType(output.getName()));

      FunctionModelConfig outputConfig = output.getOutput().getFunctionModelConfig();
      FunctionModelConfig functionModelConfig =
          outputConfig.mergedWith(_viewConfig.getDefaultConfig(), _systemDefaultConfig);

      Class<?> implType = function.getUnderlyingReceiver().getClass();
      Class<?> declaringType = function.getDeclaringClass();
      Map<Class<?>, FunctionArguments> functionArguments = calculationArguments.getFunctionArguments();
      FunctionArguments args = functionArguments(functionArguments, implType, declaringType, functionModelConfig);
      // create an environment with scenario arguments filtered for the output
      FilteredScenarioDefinition filteredDef = scenarioDefinition.filter(output.getName());
      Environment env = new EngineEnvironment(valuationTime(calculationArguments, marketDataEnvironment), 
                                              marketDataEnvironment.toBundle(),
                                              _cacheInvalidator);
      Environment outputEnv = env.withScenarioDefinition(filteredDef);
      tasks.add(new NonPortfolioTask(outputEnv, args, output.getName(), function, tracer, cache));
    } return tasks;
  }

  private static List<String> columnNames(ViewConfig viewConfig) {
    List<String> columnNames = Lists.newArrayListWithCapacity(viewConfig.getColumns().size());
    for (ViewColumn column : viewConfig.getColumns()) {
      String columnName = column.getName();
      columnNames.add(columnName);
    }
    return columnNames;
  }

  /**
   * Returns the function arguments created by merging the arguments in the map with the arguments in the
   * function model configuration. Arguments are merged in the following order
   * <ol>
   *   <li>Function implementation type arguments from the map</li>
   *   <li>Function declaration type arguments from the map</li>
   *   <li>Function implementation type arguments from the configuration</li>
   *   <li>Function declaration type arguments from the configuration</li>
   * </ol>
   *
   * @param argMap function arguments keyed by function type
   * @param implType the type of the function implementation class
   * @param declType the type of the function interface
   * @param config the function configuration
   * @return the function arguments created by merging the arguments in the map with the arguments in the
   *   function model configuration
   */
  private static FunctionArguments functionArguments(Map<Class<?>, FunctionArguments> argMap,
                                                     Class<?> implType,
                                                     Class<?> declType,
                                                     FunctionModelConfig config) {
    FunctionArguments implArgs = argMap.containsKey(implType) ? argMap.get(implType) : FunctionArguments.EMPTY;
    FunctionArguments declArgs = argMap.containsKey(declType) ? argMap.get(declType) : FunctionArguments.EMPTY;
    return implArgs.mergedWith(declArgs, config.getFunctionArguments(implType), config.getFunctionArguments(declType));
  }

  private static ZonedDateTime valuationTime(CalculationArguments calcArgs, MarketDataEnvironment marketData) {
    if (calcArgs.getValuationTime() != null) {
      return calcArgs.getValuationTime();
    } else {
      return marketData.getValuationTime();
    }
  }

  //----------------------------------------------------------
  private interface TaskResult {

    void addToResults(ResultBuilder resultBuilder);
  }

  //----------------------------------------------------------
  private abstract static class Task implements Callable<TaskResult> {

    private final Environment _env;
    private final Object _input;
    private final InvokableFunction _invokableFunction;
    private final Tracer _tracer;
    private final FunctionArguments _args;
    private final ThreadLocalWrapper _threadLocalWrapper;

    private Task(Environment env,
                 @Nullable Object input,
                 FunctionArguments args,
                 InvokableFunction invokableFunction,
                 Tracer tracer,
                 ThreadLocalWrapper threadLocalWrapper) {
      _env = env;
      _input = input;
      _args = args;
      _invokableFunction = invokableFunction;
      _tracer = tracer;
      _threadLocalWrapper = threadLocalWrapper;
    }

    @Override
    public TaskResult call() throws Exception {
      TracingProxy.start(_tracer);
      Result<?> result = invokeFunction();
      CallGraph callGraph = TracingProxy.end();
      return createResult(result, callGraph);
    }

    private Result<?> invokeFunction() {
      // try-with-resources requires the declaration of variable even if it's not used in the body of the block
      try (ThreadLocalWrapper ignore = _threadLocalWrapper.bindToThread()) {
        Object retVal = _invokableFunction.invoke(_env, _input, _args);
        return retVal instanceof Result ? (Result<?>) retVal : Result.success(retVal);
      } catch (Exception e) {
        s_logger.warn("Failed to execute function", e);
        return Result.failure(e);
      }
    }

    protected abstract TaskResult createResult(Result<?> result, CallGraph callGraph);
  }

  //----------------------------------------------------------
  private static final class PortfolioTask extends Task {

    private final int _rowIndex;
    private final int _columnIndex;

    private PortfolioTask(Environment env,
                          Object input,
                          FunctionArguments args,
                          int rowIndex,
                          int columnIndex,
                          InvokableFunction invokableFunction,
                          Tracer tracer,
                          ThreadLocalWrapper threadLocalWrapper) {
      super(env, input, args, invokableFunction, tracer, threadLocalWrapper);
      _rowIndex = rowIndex;
      _columnIndex = columnIndex;
    }

    @Override
    protected TaskResult createResult(final Result<?> result, final CallGraph callGraph) {
      return new TaskResult() {
        @Override
        public void addToResults(ResultBuilder resultBuilder) {
          resultBuilder.add(_rowIndex, _columnIndex, result, callGraph);
        }
      };
    }
  }

  //----------------------------------------------------------
  private static final class NonPortfolioTask extends Task {

    private final String _outputValueName;

    private NonPortfolioTask(Environment env,
                             FunctionArguments args,
                             String outputValueName,
                             InvokableFunction invokableFunction,
                             Tracer tracer,
                             ThreadLocalWrapper threadLocalWrapper) {
      super(env, null, args, invokableFunction, tracer, threadLocalWrapper);
      _outputValueName = ArgumentChecker.notEmpty(outputValueName, "outputValueName");
    }

    @Override
    protected TaskResult createResult(final Result<?> result, final CallGraph callGraph) {
      return new TaskResult() {
        @Override
        public void addToResults(ResultBuilder resultBuilder) {
          resultBuilder.add(_outputValueName, result, callGraph);
        }
      };
    }
  }

  /**
   * Auto-closable wrapper around state that needs to be bound to a thread before the calculations are performed
   * and cleared when the calculations are complete.
   * <p>
   * When {@link #bindToThread()} is called the values are bound to the current thread and when {@link #close()}
   * is called they are removed.
   * <p>
   * The values need to be bound to the thread which will execute the functions, which is likely to be a
   * thread from the pool and not the one that initializes the view. The intention is that
   * tasks which execute functions should bind the values in a try-with-resources block and execute the
   * function in the body of the block.
   */
  private static class ThreadLocalWrapper implements AutoCloseable {

    private final ServiceContext _cycleServiceContext;
    private final ServiceContext _originalServiceContext;
    private final ThreadLocal<Cache<Object, Object>> _cacheThreadLocal;
    private final Cache<Object, Object> _cache;

    private ThreadLocalWrapper(ServiceContext cycleServiceContext,
                               ServiceContext originalServiceContext,
                               Cache<Object, Object> cache,
                               ThreadLocal<Cache<Object, Object>> cacheThreadLocal) {
      _cycleServiceContext = cycleServiceContext;
      _originalServiceContext = originalServiceContext;
      _cache = cache;
      _cacheThreadLocal = cacheThreadLocal;
    }

    /**
     * Binds the thread local state to the current thread.
     *
     * @return this, so it can be used in a try-with-resources block
     */
    private ThreadLocalWrapper bindToThread() {
      _cacheThreadLocal.set(_cache);
      ThreadLocalServiceContext.init(_cycleServiceContext);
      return this;
    }

    /**
     * Removes the thread local bindings.
     */
    @Override
    public void close() {
      _cacheThreadLocal.remove();
      ThreadLocalServiceContext.init(_originalServiceContext);
    }
  }
}
