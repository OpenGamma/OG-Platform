/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFormatter;
import com.opengamma.engine.depgraph.Housekeeper;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.tuple.Pair;

/**
 * Ultimately produces a set of {@link DependencyGraph}s from a {@link ViewDefinition}, one for each {@link ViewCalculationConfiguration}. Additional information, such as the live data requirements,
 * is collected along the way and exposed after compilation.
 * <p>
 * The compiled graphs are guaranteed to be calculable for at least the requested timestamp. One or more of the referenced functions may not be valid at other timestamps.
 */
public final class ViewDefinitionCompiler {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewDefinitionCompiler.class);
  private static final boolean OUTPUT_DEPENDENCY_GRAPHS = false;
  private static final boolean OUTPUT_LIVE_DATA_REQUIREMENTS = false;
  private static final boolean OUTPUT_FAILURE_REPORTS = false;

  private ViewDefinitionCompiler() {
  }

  /**
   * Exposure of the completion status of a graph compilation. This is currently for debugging/diagnostic purposes. A full implementation should incorporate the "cancel"/"get" behavior of the Future
   * returned by {@link #compileTask} with something that can return the progress of the task.
   */
  protected static final class CompilationCompletionEstimate implements Housekeeper.Callback<Supplier<Double>> {

    private final String _label;
    private final ConcurrentMap<String, Double> _buildEstimates;

    private CompilationCompletionEstimate(final ViewCompilationContext context) {
      final Collection<DependencyGraphBuilder> builders = context.getBuilders();
      _buildEstimates = new ConcurrentHashMap<String, Double>();
      for (final DependencyGraphBuilder builder : builders) {
        _buildEstimates.put(builder.getCalculationConfigurationName(), 0d);
        Housekeeper.of(builder, this, builder.buildFractionEstimate()).start();
      }
      _label = context.getViewDefinition().getName();
    }

    public double[] estimates() {
      final double[] result = new double[_buildEstimates.size()];
      int i = 0;
      for (final Double estimate : _buildEstimates.values()) {
        result[i++] = estimate;
      }
      return result;
    }

    public double estimate() {
      double result = 0;
      for (final Double estimate : _buildEstimates.values()) {
        result += estimate;
      }
      return result / _buildEstimates.size();
    }

    @Override
    public boolean tick(final DependencyGraphBuilder builder, final Supplier<Double> estimate) {
      final Double estimateValue = estimate.get();
      s_logger.debug("{}/{} building at {}", new Object[] {_label, builder.getCalculationConfigurationName(), estimateValue });
      _buildEstimates.put(builder.getCalculationConfigurationName(), estimateValue);
      return estimateValue < 1d;
    }

    @Override
    public boolean cancelled(final DependencyGraphBuilder builder, final Supplier<Double> estimate) {
      return false;
    }

    @Override
    public boolean completed(final DependencyGraphBuilder builder, final Supplier<Double> estimate) {
      return estimate.get() < 1d;
    }

  }

  // TODO: return something that provides the caller with access to a completion metric to feedback to any interactive user
  private abstract static class CompilationTask implements Future<CompiledViewDefinitionWithGraphsImpl> {

    private final ViewCompilationContext _viewCompilationContext;
    private volatile CompiledViewDefinitionWithGraphsImpl _result;
    private final ConcurrentMap<ComputationTargetReference, UniqueId> _resolutions;

    protected CompilationTask(final ViewDefinition viewDefinition, final ViewCompilationServices compilationServices, final Instant valuationTime,
        final VersionCorrection versionCorrection, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions) {
      s_logger.info("Compiling {} for use at {}", viewDefinition.getName(), valuationTime);
      _viewCompilationContext = new ViewCompilationContext(viewDefinition, compilationServices, valuationTime, versionCorrection);
      _resolutions = resolutions;
      if (s_logger.isDebugEnabled()) {
        new CompilationCompletionEstimate(_viewCompilationContext);
      }
    }

    protected ViewCompilationContext getContext() {
      return _viewCompilationContext;
    }

    protected ConcurrentMap<ComputationTargetReference, UniqueId> getResolutions() {
      return _resolutions;
    }

    protected abstract Portfolio compile();

    private void removeUnusedResolutions(final Map<String, DependencyGraph> graphsByConfiguration, final Portfolio portfolio) {
      final Set<UniqueId> validIdentifiers = new HashSet<UniqueId>();
      if (portfolio != null) {
        validIdentifiers.add(portfolio.getUniqueId());
      }
      for (final DependencyGraph graph : graphsByConfiguration.values()) {
        for (final ComputationTargetSpecification target : graph.getAllComputationTargets()) {
          validIdentifiers.add(target.getUniqueId());
        }
      }
      getResolutions().values().retainAll(validIdentifiers);
    }

    /**
     * Cancels any active builders.
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      boolean result = true;
      for (final DependencyGraphBuilder builder : getContext().getBuilders()) {
        result &= builder.cancel(mayInterruptIfRunning);
      }
      return result;
    }

    /**
     * Tests if any of the builders have been canceled.
     */
    @Override
    public boolean isCancelled() {
      boolean result = false;
      for (final DependencyGraphBuilder builder : getContext().getBuilders()) {
        result |= builder.isCancelled();
      }
      return result;
    }

    /**
     * Tests if all of the builders have completed.
     */
    @Override
    public boolean isDone() {
      return _result != null;
    }

    @Override
    public CompiledViewDefinitionWithGraphsImpl get() {
      for (final DependencyGraphBuilder builder : getContext().getBuilders()) {
        final FunctionCompilationContext functionContext = builder.getCompilationContext();
        final ComputationTargetResolver.AtVersionCorrection resolver = functionContext.getComputationTargetResolver();
        functionContext.setComputationTargetResolver(TargetResolutionLogger.of(resolver, getResolutions()));
      }
      long t = -System.nanoTime();
      final Portfolio portfolio = compile();
      final Map<String, DependencyGraph> graphsByConfiguration = processDependencyGraphs(getContext());
      t += System.nanoTime();
      s_logger.info("Processed dependency graphs after {}ms", t / 1e6);
      removeUnusedResolutions(graphsByConfiguration, portfolio);
      _result = new CompiledViewDefinitionWithGraphsImpl(getContext().getViewDefinition(), graphsByConfiguration, getResolutions(), portfolio, getContext().getServices()
          .getFunctionCompilationContext().getFunctionInitId());
      if (OUTPUT_DEPENDENCY_GRAPHS) {
        outputDependencyGraphs(graphsByConfiguration);
      }
      if (OUTPUT_LIVE_DATA_REQUIREMENTS) {
        outputLiveDataRequirements(graphsByConfiguration);
      }
      if (OUTPUT_FAILURE_REPORTS) {
        outputFailureReports(_viewCompilationContext.getBuilders());
      }
      return _result;
    }

    @Override
    public CompiledViewDefinitionWithGraphsImpl get(final long timeout, final TimeUnit unit) {
      throw new UnsupportedOperationException();
    }

  }

  private static class FullCompilationTask extends CompilationTask {

    protected FullCompilationTask(final ViewDefinition viewDefinition, final ViewCompilationServices compilationServices, final Instant valuationTime,
        final VersionCorrection versionCorrection) {
      super(viewDefinition, compilationServices, valuationTime, versionCorrection, new ConcurrentHashMap<ComputationTargetReference, UniqueId>());
    }

    @Override
    protected Portfolio compile() {
      s_logger.info("Performing full compilation");
      SpecificRequirementsCompiler.execute(getContext());
      return PortfolioCompiler.executeFull(getContext(), getResolutions());
    }

  }

  private static class IncrementalCompilationTask extends CompilationTask {

    private final Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> _previousGraphs;
    private final boolean _portfolioFull;

    protected IncrementalCompilationTask(final ViewDefinition viewDefinition, final ViewCompilationServices compilationServices, final Instant valuationTime,
        final VersionCorrection versionCorrection, final Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> previousGraphs,
        final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions, final boolean portfolioFull) {
      super(viewDefinition, compilationServices, valuationTime, versionCorrection, resolutions);
      _previousGraphs = previousGraphs;
      _portfolioFull = portfolioFull;
    }

    @Override
    public Portfolio compile() {
      for (final DependencyGraphBuilder builder : getContext().getBuilders()) {
        final Pair<DependencyGraph, Set<ValueRequirement>> graph = _previousGraphs.get(builder.getCalculationConfigurationName());
        if (graph != null) {
          builder.setDependencyGraph(graph.getFirst());
          if (graph.getSecond().isEmpty()) {
            s_logger.debug("No incremental work for {}", graph.getFirst());
          } else {
            s_logger.info("{} incremental resolutions required for {}", graph.getSecond().size(), graph.getFirst());
            builder.addTarget(graph.getSecond());
          }
        }
      }
      if (_portfolioFull) {
        return PortfolioCompiler.executeFull(getContext(), getResolutions());
      } else {
        return PortfolioCompiler.executeIncremental(getContext(), getResolutions());
      }
    }

  }

  public static Future<CompiledViewDefinitionWithGraphsImpl> fullCompileTask(final ViewDefinition viewDefinition, final ViewCompilationServices compilationServices, final Instant valuationTime,
      final VersionCorrection versionCorrection) {
    return new FullCompilationTask(viewDefinition, compilationServices, valuationTime, versionCorrection);
  }

  public static Future<CompiledViewDefinitionWithGraphsImpl> incrementalCompileTask(final ViewDefinition viewDefinition, final ViewCompilationServices compilationServices,
      final Instant valuationTime, final VersionCorrection versionCorrection, final Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> previousGraphs,
      final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions, final boolean portfolioFull) {
    return new IncrementalCompilationTask(viewDefinition, compilationServices, valuationTime, versionCorrection, previousGraphs, resolutions, portfolioFull);
  }

  public static CompiledViewDefinitionWithGraphsImpl compile(final ViewDefinition viewDefinition, final ViewCompilationServices compilationServices,
      final Instant valuationTime, final VersionCorrection versionCorrection) {
    try {
      return fullCompileTask(viewDefinition, compilationServices, valuationTime, versionCorrection).get();
    } catch (final InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    } catch (final ExecutionException e) {
      throw new OpenGammaRuntimeException("Failed", e);
    }
  }

  private static Map<String, DependencyGraph> processDependencyGraphs(final ViewCompilationContext context) {
    final Map<String, DependencyGraph> result = new HashMap<String, DependencyGraph>();
    for (final DependencyGraphBuilder builder : context.getBuilders()) {
      final DependencyGraph graph = builder.getDependencyGraph();
      graph.removeUnnecessaryValues();
      result.put(builder.getCalculationConfigurationName(), graph);
      // TODO: do we want to do anything with the ValueRequirement to resolved ValueSpecification data? I don't like it being in the graph
      // as it's more specific to how the graph is used. Having it in the graph with the terminal outputs data is convenient for taking
      // sub-graphs to initialise an incremental graph builder with though.
    }
    return result;
  }

  private static void outputDependencyGraphs(final Map<String, DependencyGraph> graphsByConfiguration) {
    final StringBuilder sb = new StringBuilder();
    for (final Map.Entry<String, DependencyGraph> entry : graphsByConfiguration.entrySet()) {
      final String configName = entry.getKey();
      sb.append("DepGraph for ").append(configName);

      final DependencyGraph depGraph = entry.getValue();
      sb.append("\tProducing values ").append(depGraph.getOutputSpecifications());
      for (final DependencyNode depNode : depGraph.getDependencyNodes()) {
        sb.append("\t\tNode:\n").append(DependencyNodeFormatter.toString(depNode));
      }
    }
    s_logger.warn("Dependency Graphs -- \n{}", sb);
  }

  private static void outputLiveDataRequirements(final Map<String, DependencyGraph> graphsByConfiguration) {
    final StringBuilder sb = new StringBuilder();
    for (final Map.Entry<String, DependencyGraph> entry : graphsByConfiguration.entrySet()) {
      final String configName = entry.getKey();
      final Collection<Pair<ValueRequirement, ValueSpecification>> requiredLiveData = entry.getValue().getAllRequiredMarketData();
      if (requiredLiveData.isEmpty()) {
        sb.append(configName).append(" requires no live data.\n");
      } else {
        sb.append("Live data for ").append(configName).append("\n");
        for (final Pair<ValueRequirement, ValueSpecification> liveRequirement : requiredLiveData) {
          sb.append("\t").append(liveRequirement.getFirst()).append("\n");
        }
      }
    }
    s_logger.warn("Live data requirements -- \n{}", sb);
  }

  private static void outputFailureReports(final Collection<DependencyGraphBuilder> builders) {
    for (final DependencyGraphBuilder builder : builders) {
      outputFailureReport(builder);
    }
  }

  public static void outputFailureReport(final DependencyGraphBuilder builder) {
    final Map<Throwable, Integer> exceptions = builder.getExceptions();
    if (!exceptions.isEmpty()) {
      for (final Map.Entry<Throwable, Integer> entry : exceptions.entrySet()) {
        final Throwable exception = entry.getKey();
        final Integer count = entry.getValue();
        if (exception.getCause() != null) {
          if (s_logger.isDebugEnabled()) {
            s_logger.debug("Nested exception raised " + count + " time(s)", exception);
          }
        } else {
          if (s_logger.isWarnEnabled()) {
            s_logger.warn("Exception raised " + count + " time(s)", exception);
          }
        }
      }
    } else {
      s_logger.info("No exceptions raised for configuration {}", builder.getCalculationConfigurationName());
    }
  }

}
