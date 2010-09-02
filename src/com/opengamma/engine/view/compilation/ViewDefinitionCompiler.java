/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFormatter;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Ultimately produces a set of {@link DependencyGraph}s from a {@link ViewDefinition}, one for each
 * {@link ViewCalculationConfiguration}. Additional information, such as the live data requirements, is collected along
 * the way and exposed after compilation.
 */
public final class ViewDefinitionCompiler {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewDefinitionCompiler.class);
  private static final boolean OUTPUT_DEPENDENCY_GRAPHS = false;
  private static final boolean OUTPUT_LIVE_DATA_REQUIREMENTS = false;
  
  private ViewDefinitionCompiler() {
  }
  
  //--------------------------------------------------------------------------
  public static ViewEvaluationModel compile(ViewDefinition viewDefinition, ViewCompilationServices compilationServices) {
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    ArgumentChecker.notNull(compilationServices, "compilationServices");
        
    OperationTimer timer = new OperationTimer(s_logger, "Compiling ViewDefinition: {}", viewDefinition.getName());
    ViewCompilationContext viewCompilationContext = new ViewCompilationContext(viewDefinition, compilationServices);
    
    Portfolio portfolio = PortfolioCompiler.execute(viewCompilationContext);
    SpecificRequirementsCompiler.execute(viewCompilationContext);
    
    Map<String, DependencyGraph> graphsByConfiguration = processDependencyGraphs(viewCompilationContext);
    timer.finished();
    
    if (OUTPUT_DEPENDENCY_GRAPHS) {
      outputDependencyGraphs(graphsByConfiguration);
    }
    if (OUTPUT_LIVE_DATA_REQUIREMENTS) {
      outputLiveDataRequirements(graphsByConfiguration, compilationServices.getSecuritySource());
    }
    
    return new ViewEvaluationModel(graphsByConfiguration, portfolio);
  }
  
  //--------------------------------------------------------------------------
  private static Map<String, DependencyGraph> processDependencyGraphs(ViewCompilationContext context) {
    Map<String, DependencyGraph> result = new HashMap<String, DependencyGraph>();
    for (DependencyGraphBuilder builder : context.getBuilders().values()) {
      DependencyGraph dependencyGraph = builder.getDependencyGraph();
      dependencyGraph.removeUnnecessaryValues();
      result.put(builder.getCalculationConfigurationName(), dependencyGraph);
    }
    return result;
  }
  
  private static void outputDependencyGraphs(Map<String, DependencyGraph> graphsByConfiguration) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, DependencyGraph> entry : graphsByConfiguration.entrySet()) {
      String configName = entry.getKey();
      sb.append("DepGraph for ").append(configName);
      
      DependencyGraph depGraph = entry.getValue();
      sb.append("\tProducing values ").append(depGraph.getOutputValues());
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
      Collection<ValueSpecification> requiredLiveData = entry.getValue().getAllRequiredLiveData();
      if (requiredLiveData.isEmpty()) {
        sb.append(configName).append(" requires no live data.\n");
      } else {
        sb.append("Live data for ").append(configName).append("\n");
        for (ValueSpecification liveRequirement : requiredLiveData) {
          sb.append("\t").append(liveRequirement.getRequirementSpecification().getTargetSpecification().getRequiredLiveData(secMaster)).append("\n");
        }
      }
    }
    s_logger.warn("Live data requirements -- \n{}", sb);
  }
  
}
