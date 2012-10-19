/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFormatter;
import com.opengamma.engine.depgraph.Housekeeper;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;
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
      for (DependencyGraphBuilder builder : builders) {
        _buildEstimates.put(builder.getCalculationConfigurationName(), 0d);
        Housekeeper.of(builder, this, builder.buildFractionEstimate()).start();
      }
      _label = context.getViewDefinition().getName();
    }

    public double[] estimates() {
      final double[] result = new double[_buildEstimates.size()];
      int i = 0;
      for (Double estimate : _buildEstimates.values()) {
        result[i++] = estimate;
      }
      return result;
    }

    public double estimate() {
      double result = 0;
      for (Double estimate : _buildEstimates.values()) {
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

  //-------------------------------------------------------------------------
  public static Future<CompiledViewDefinitionWithGraphsImpl> compileTask(final ViewDefinition viewDefinition, final ViewCompilationServices compilationServices, final Instant valuationTime,
      final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    ArgumentChecker.notNull(compilationServices, "compilationServices");
    s_logger.debug("Compiling {} for use with {}", viewDefinition.getName(), valuationTime);
    final OperationTimer timer = new OperationTimer(s_logger, "Compiling ViewDefinition: {}", viewDefinition.getName());
    final ViewCompilationContext viewCompilationContext = new ViewCompilationContext(viewDefinition, compilationServices, valuationTime, versionCorrection);
    if (s_logger.isDebugEnabled()) {
      new CompilationCompletionEstimate(viewCompilationContext);
    }
    // TODO: return a Future that provides access to a completion metric to feedback to any interactive user
    return new Future<CompiledViewDefinitionWithGraphsImpl>() {

      private volatile CompiledViewDefinitionWithGraphsImpl _result;

      /**
       * Cancels any active builders.
       */
      @Override
      public boolean cancel(final boolean mayInterruptIfRunning) {
        boolean result = true;
        for (DependencyGraphBuilder builder : viewCompilationContext.getBuilders()) {
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
        for (DependencyGraphBuilder builder : viewCompilationContext.getBuilders()) {
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
      public CompiledViewDefinitionWithGraphsImpl get() throws InterruptedException, ExecutionException {
        final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>();
        addLoggingTargetSpecificationResolvers(viewCompilationContext, resolutions);
        long t = -System.nanoTime();
        SpecificRequirementsCompiler.execute(viewCompilationContext);
        t += System.nanoTime();
        s_logger.info("Added specific requirements after {}ms", (double) t / 1e6);
        t -= System.nanoTime();
        Portfolio portfolio = PortfolioCompiler.execute(viewCompilationContext);
        t += System.nanoTime();
        s_logger.info("Added portfolio requirements after {}ms", (double) t / 1e6);
        t -= System.nanoTime();
        Map<String, DependencyGraph> graphsByConfiguration = processDependencyGraphs(viewCompilationContext);
        t += System.nanoTime();
        s_logger.info("Processed dependency graphs after {}ms", (double) t / 1e6);
        t -= System.nanoTime();
        getPortfolioResolutions(portfolio, resolutions);
        t += System.nanoTime();
        s_logger.info("Extracted resolved identifiers after {}ms", (double) t / 1e6);
        timer.finished();
        _result = new CompiledViewDefinitionWithGraphsImpl(viewDefinition, graphsByConfiguration, resolutions, portfolio, compilationServices.getFunctionCompilationContext().getFunctionInitId());
        if (OUTPUT_DEPENDENCY_GRAPHS) {
          outputDependencyGraphs(graphsByConfiguration);
        }
        if (OUTPUT_LIVE_DATA_REQUIREMENTS) {
          outputLiveDataRequirements(graphsByConfiguration, compilationServices.getFunctionCompilationContext().getComputationTargetResolver().getSecuritySource());
        }
        if (OUTPUT_FAILURE_REPORTS) {
          outputFailureReports(viewCompilationContext.getBuilders());
        }
        return _result;
      }

      @Override
      public CompiledViewDefinitionWithGraphsImpl get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException();
      }

    };
  }

  public static CompiledViewDefinitionWithGraphsImpl compile(ViewDefinition viewDefinition, ViewCompilationServices compilationServices, Instant valuationTime, VersionCorrection versionCorrection) {
    try {
      return compileTask(viewDefinition, compilationServices, valuationTime, versionCorrection).get();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    } catch (ExecutionException e) {
      throw new OpenGammaRuntimeException("Failed", e);
    }
  }

  private static Map<String, DependencyGraph> processDependencyGraphs(final ViewCompilationContext context) {
    final Map<String, DependencyGraph> result = new HashMap<String, DependencyGraph>();
    for (DependencyGraphBuilder builder : context.getBuilders()) {
      final DependencyGraph graph = builder.getDependencyGraph();
      graph.removeUnnecessaryValues();
      result.put(builder.getCalculationConfigurationName(), graph);
      // TODO: do we want to do anything with the ValueRequirement to resolved ValueSpecification data?
    }
    return result;
  }

  /**
   * Wraps an existing specification resolver to log all of the resolutions calls. This allows any values that were considered during the compilation to be recorded and returned as part of the
   * compiled context. If the resolution of any of these would be different for a different version/correction then this compilation is no longer valid.
   */
  private static class LoggingSpecificationResolver implements ComputationTargetSpecificationResolver.AtVersionCorrection, ComputationTargetReferenceVisitor<ComputationTargetReference> {

    private final ComputationTargetSpecificationResolver.AtVersionCorrection _underlying;
    private final ConcurrentMap<ComputationTargetReference, UniqueId> _resolutions;

    public LoggingSpecificationResolver(final ComputationTargetSpecificationResolver.AtVersionCorrection underlying, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions) {
      _underlying = underlying;
      _resolutions = resolutions;
    }

    private static final ComputationTargetTypeVisitor<Void, ComputationTargetType> s_getLeafType = new ComputationTargetTypeVisitor<Void, ComputationTargetType>() {

      @Override
      public ComputationTargetType visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
        final List<ComputationTargetType> result = new ArrayList<ComputationTargetType>(types.size());
        boolean different = false;
        for (ComputationTargetType type : types) {
          final ComputationTargetType leafType = type.accept(s_getLeafType, null);
          if (leafType != null) {
            result.add(leafType);
            different = true;
          } else {
            result.add(type);
          }
        }
        if (different) {
          ComputationTargetType type = result.get(0);
          for (int i = 1; i < result.size(); i++) {
            type = type.or(result.get(i));
          }
          return type;
        } else {
          return null;
        }
      }

      @Override
      public ComputationTargetType visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
        return types.get(types.size() - 1).accept(this, null);
      }

      @Override
      public ComputationTargetType visitNullComputationTargetType(final Void data) {
        return null;
      }

      @Override
      public ComputationTargetType visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
        return null;
      }

    };

    private ComputationTargetType getLeafType(final ComputationTargetType type) {
      // TODO: Ought to reduce the type to its simplest form. This hasn't been a problem with the views & function repository I've been testing
      // with but might be necessary in the general case as there will be duplicate values in the resolver cache.
      return type.accept(s_getLeafType, null);
    }

    // ComputationTargetSpecificationResolver.AtVersionCorrection

    @Override
    public ComputationTargetSpecification getTargetSpecification(final ComputationTargetReference reference) {
      final ComputationTargetSpecification resolved = _underlying.getTargetSpecification(reference);
      final ComputationTargetReference key = reference.accept(this);
      if (key != null) {
        UniqueId resolvedId = (resolved != null) ? resolved.getUniqueId() : CompiledViewDefinitionWithGraphsImpl.NULL_RESOLVED;
        if (resolvedId == null) {
          // Handle the case of the target being resolved to ComputationTargetSpecification.NULL
          resolvedId = CompiledViewDefinitionWithGraphsImpl.NULL_RESOLVED;
        }
        final UniqueId previousId = _resolutions.putIfAbsent(key, resolvedId);
        assert (previousId == null) || previousId.equals(resolvedId);
      }
      return resolved;
    }

    // ComputationTargetReferenceVisitor

    @Override
    public ComputationTargetReference visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
      final ComputationTargetType leafType = getLeafType(requirement.getType());
      if (leafType != null) {
        return MemoryUtils.instance(new ComputationTargetRequirement(leafType, requirement.getIdentifiers()));
      } else {
        return requirement;
      }
    }

    @Override
    public ComputationTargetReference visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
      if ((specification.getUniqueId() != null) && specification.getUniqueId().isLatest()) {
        final ComputationTargetType leafType = getLeafType(specification.getType());
        if (leafType != null) {
          return MemoryUtils.instance(new ComputationTargetSpecification(leafType, specification.getUniqueId()));
        } else {
          return specification;
        }
      } else {
        return null;
      }
    }

  }

  private static void addLoggingTargetSpecificationResolvers(final ViewCompilationContext context, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions) {
    for (DependencyGraphBuilder builder : context.getBuilders()) {
      final FunctionCompilationContext functionContext = builder.getCompilationContext();
      functionContext.setComputationTargetSpecificationResolver(new LoggingSpecificationResolver(functionContext.getComputationTargetSpecificationResolver(), resolutions));
    }
  }

  private static void getPortfolioResolutions(final Portfolio portfolio, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions) {
    resolutions.putIfAbsent(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, portfolio.getRootNode().getUniqueId().toLatest()), portfolio.getRootNode().getUniqueId());
    PortfolioNodeTraverser.depthFirst(new AbstractPortfolioNodeTraversalCallback() {

      /**
       * Store details of the security link in the resolution cache. The link is assumed to be a record of the link to the object, for example is it held by strong (object id) or weak (external id)
       * reference.
       * 
       * @param link the link to store - the identifier is taken from this along with the resolved unique identifier
       */
      private void store(final SecurityLink link) {
        final ComputationTargetReference key;
        final UniqueId uid;
        if (link.getTarget() == null) {
          if (link.getObjectId() != null) {
            key = new ComputationTargetSpecification(ComputationTargetType.SECURITY, link.getObjectId().atLatestVersion());
            uid = CompiledViewDefinitionWithGraphsImpl.NULL_RESOLVED;
          } else if (!link.getExternalId().isEmpty()) {
            key = new ComputationTargetRequirement(ComputationTargetType.SECURITY, link.getExternalId());
            uid = CompiledViewDefinitionWithGraphsImpl.NULL_RESOLVED;
          } else {
            return;
          }
        } else {
          uid = link.getTarget().getUniqueId();
          if (link.getObjectId() != null) {
            key = new ComputationTargetSpecification(ComputationTargetType.SECURITY, uid.toLatest());
          } else if (!link.getExternalId().isEmpty()) {
            key = new ComputationTargetRequirement(ComputationTargetType.SECURITY, link.getExternalId());
          } else {
            return;
          }
        }
        final UniqueId existing = resolutions.putIfAbsent(MemoryUtils.instance(key), uid);
        assert (existing == null) || existing.equals(uid);
      }

      @Override
      public void preOrderOperation(final PortfolioNode node, final Position position) {
        resolutions.putIfAbsent(MemoryUtils.instance(new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getUniqueId().toLatest())), position.getUniqueId());
        store(position.getSecurityLink());
        for (Trade trade : position.getTrades()) {
          store(trade.getSecurityLink());
        }
      }

    }).traverse(portfolio.getRootNode());
  }

  private static void outputDependencyGraphs(Map<String, DependencyGraph> graphsByConfiguration) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, DependencyGraph> entry : graphsByConfiguration.entrySet()) {
      String configName = entry.getKey();
      sb.append("DepGraph for ").append(configName);

      DependencyGraph depGraph = entry.getValue();
      sb.append("\tProducing values ").append(depGraph.getOutputSpecifications());
      for (DependencyNode depNode : depGraph.getDependencyNodes()) {
        sb.append("\t\tNode:\n").append(DependencyNodeFormatter.toString(depNode));
      }
    }
    s_logger.warn("Dependency Graphs -- \n{}", sb);
  }

  private static void outputLiveDataRequirements(Map<String, DependencyGraph> graphsByConfiguration, SecuritySource secMaster) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, DependencyGraph> entry : graphsByConfiguration.entrySet()) {
      String configName = entry.getKey();
      Collection<Pair<ValueRequirement, ValueSpecification>> requiredLiveData = entry.getValue().getAllRequiredMarketData();
      if (requiredLiveData.isEmpty()) {
        sb.append(configName).append(" requires no live data.\n");
      } else {
        sb.append("Live data for ").append(configName).append("\n");
        for (Pair<ValueRequirement, ValueSpecification> liveRequirement : requiredLiveData) {
          sb.append("\t").append(liveRequirement.getFirst()).append("\n");
        }
      }
    }
    s_logger.warn("Live data requirements -- \n{}", sb);
  }

  private static void outputFailureReports(final Collection<DependencyGraphBuilder> builders) {
    for (DependencyGraphBuilder builder : builders) {
      outputFailureReport(builder);
    }
  }

  public static void outputFailureReport(final DependencyGraphBuilder builder) {
    final Map<Throwable, Integer> exceptions = builder.getExceptions();
    if (!exceptions.isEmpty()) {
      for (Map.Entry<Throwable, Integer> entry : exceptions.entrySet()) {
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
