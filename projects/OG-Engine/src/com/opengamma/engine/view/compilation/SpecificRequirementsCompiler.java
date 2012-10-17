/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;

/**
 * Compiles specific requirements into the dependency graphs
 */
public final class SpecificRequirementsCompiler {

  private SpecificRequirementsCompiler() {
  }
  
  /**
   * Adds any specific requirements mentioned in the view calculation configurations to the dependency graphs.
   * 
   * @param compilationContext  the context of the view definition compilation
   */
  public static void execute(ViewCompilationContext compilationContext) {
    ResultModelDefinition resultModelDefinition = compilationContext.getViewDefinition().getResultModelDefinition();
    for (ViewCalculationConfiguration calcConfig : compilationContext.getViewDefinition().getAllCalculationConfigurations()) {
      final DependencyGraphBuilder builder = compilationContext.getBuilder(calcConfig.getName());
      for (ValueRequirement requirement : calcConfig.getSpecificRequirements()) {
        ComputationTargetReference targetReference = requirement.getTargetReference();
        if (resultModelDefinition.getOutputMode(targetReference.getType()) == ResultOutputMode.NONE) {
          // We're not including this in the results, so no point it being a terminal output. It will be added
          // automatically if it is needed for some other terminal output.
          continue;
        }
        builder.addTarget(requirement);
      }
    }
  }
  
}
