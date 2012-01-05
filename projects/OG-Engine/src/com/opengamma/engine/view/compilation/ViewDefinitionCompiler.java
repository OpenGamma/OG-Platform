/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFormatter;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.Pair;

/**
 * Ultimately produces a set of {@link DependencyGraph}s from a {@link ViewDefinition}, one for each
 * {@link ViewCalculationConfiguration}. Additional information, such as the live data requirements, is collected along
 * the way and exposed after compilation.
 * 
 * The compiled graphs are guaranteed to be calculable for at least the requested timestamp. One or more of the
 * referenced functions may not be valid at other timestamps.
 */
public final class ViewDefinitionCompiler {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewDefinitionCompiler.class);
  private static final boolean OUTPUT_DEPENDENCY_GRAPHS = false;
  private static final boolean OUTPUT_LIVE_DATA_REQUIREMENTS = false;
  private static final boolean OUTPUT_FAILURE_REPORTS = false;

  private ViewDefinitionCompiler() {
  }

  //-------------------------------------------------------------------------
  public static CompiledViewDefinitionWithGraphsImpl compile(ViewDefinition viewDefinition, ViewCompilationServices compilationServices, Instant valuationTime, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    ArgumentChecker.notNull(compilationServices, "compilationServices");

    s_logger.debug("Compiling {} for use with {}", viewDefinition.getName(), valuationTime);

    OperationTimer timer = new OperationTimer(s_logger, "Compiling ViewDefinition: {}", viewDefinition.getName());
    ViewCompilationContext viewCompilationContext = new ViewCompilationContext(viewDefinition, compilationServices, valuationTime);

    long t = -System.nanoTime();
    EnumSet<ComputationTargetType> specificTargetTypes = SpecificRequirementsCompiler.execute(viewCompilationContext);
    t += System.nanoTime();
    s_logger.debug("Added specific requirements after {}ms", (double) t / 1e6);
    t -= System.nanoTime();
    boolean requirePortfolioResolution = specificTargetTypes.contains(ComputationTargetType.PORTFOLIO_NODE) || specificTargetTypes.contains(ComputationTargetType.POSITION);
    Portfolio portfolio = PortfolioCompiler.execute(viewCompilationContext, versionCorrection, requirePortfolioResolution);
    t += System.nanoTime();
    s_logger.debug("Added portfolio requirements after {}ms", (double) t / 1e6);
    t -= System.nanoTime();
    Map<String, DependencyGraph> graphsByConfiguration = processDependencyGraphs(viewCompilationContext);
    t += System.nanoTime();
    s_logger.debug("Processed dependency graphs after {}ms", (double) t / 1e6);
    timer.finished();

    if (OUTPUT_DEPENDENCY_GRAPHS) {
      outputDependencyGraphs(graphsByConfiguration);
    }
    if (OUTPUT_LIVE_DATA_REQUIREMENTS) {
      outputLiveDataRequirements(graphsByConfiguration, compilationServices.getSecuritySource());
    }
    if (OUTPUT_FAILURE_REPORTS) {
      outputFailureReports(viewCompilationContext.getBuilders());
    }
    return new CompiledViewDefinitionWithGraphsImpl(viewDefinition, graphsByConfiguration, portfolio, compilationServices.getFunctionCompilationContext().getFunctionInitId());
  }

  private static Map<String, DependencyGraph> processDependencyGraphs(ViewCompilationContext context) {
    // TODO: support one of two modes; sequential build of the graphs (below) or parallel build using the executor service from the compilation services
    // TODO: perhaps a heuristic to determine which is better, or a global setting
    final Map<String, DependencyGraph> result = new HashMap<String, DependencyGraph>();
    final Iterator<Pair<DependencyGraphBuilder, Set<ValueRequirement>>> itr = context.getBuilders().iterator();
    while (itr.hasNext()) {
      final Pair<DependencyGraphBuilder, Set<ValueRequirement>> entry = itr.next();
      entry.getFirst().addTarget(entry.getSecond());
      result.put(entry.getFirst().getCalculationConfigurationName(), entry.getFirst().getDependencyGraph());
      // TODO: do we want to do anything with the ValueRequirement to resolved ValueSpecification data?
      itr.remove();
    }
    return result;
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

  private static void outputFailureReports(final Collection<Pair<DependencyGraphBuilder, Set<ValueRequirement>>> builders) {
    for (Pair<DependencyGraphBuilder, Set<ValueRequirement>> builder : builders) {
      outputFailureReport(builder.getFirst());
    }
  }

  private static void outputFailureReport(final DependencyGraphBuilder builder) {
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
