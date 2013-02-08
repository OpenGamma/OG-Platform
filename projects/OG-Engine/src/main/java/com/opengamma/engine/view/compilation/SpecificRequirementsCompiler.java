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
  public static void execute(final ViewCompilationContext compilationContext) {
    final ResultModelDefinition resultModelDefinition = compilationContext.getViewDefinition().getResultModelDefinition();

    // Scan through the view definition's calc configs, adding all specific requirements to the relevant graph builder
    for (final ViewCalculationConfiguration calcConfig : compilationContext.getViewDefinition().getAllCalculationConfigurations()) {

      // Retrieve the current calc config's dep graph builder
      final DependencyGraphBuilder builder = compilationContext.getBuilder(calcConfig.getName());

      // Scan through the current calc config's specific requirements
      for (final ValueRequirement requirement : calcConfig.getSpecificRequirements()) {
        final ComputationTargetReference targetReference = requirement.getTargetReference();
        if (resultModelDefinition.getOutputMode(targetReference.getType()) == ResultOutputMode.NONE) {
          // We're not including this in the results, so no point it being a terminal output. It will be added
          // automatically if it is needed for some other terminal output.
          continue;
        }

        // Add the specific requirement to the current calc config's dep graph builder
        builder.addTarget(requirement);
      }
    }
  }
  
}
