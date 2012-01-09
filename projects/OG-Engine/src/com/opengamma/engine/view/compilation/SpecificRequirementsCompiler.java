/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.EnumSet;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
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
   * @return the set of target types found in the specific requirements, not null
   */
  public static EnumSet<ComputationTargetType> execute(ViewCompilationContext compilationContext) {
    EnumSet<ComputationTargetType> specificTargetTypes = EnumSet.noneOf(ComputationTargetType.class);
    ResultModelDefinition resultModelDefinition = compilationContext.getViewDefinition().getResultModelDefinition();
    for (ViewCalculationConfiguration calcConfig : compilationContext.getViewDefinition().getAllCalculationConfigurations()) {
      final Set<ValueRequirement> requirements = compilationContext.getValueRequirements(calcConfig.getName());
      for (ValueRequirement requirement : calcConfig.getSpecificRequirements()) {
        ComputationTargetSpecification targetSpecification = requirement.getTargetSpecification();
        if (resultModelDefinition.getOutputMode(targetSpecification.getType()) == ResultOutputMode.NONE) {
          // We're not including this in the results, so no point it being a terminal output. It will be added
          // automatically if it is needed for some other terminal output.
          continue;
        }
        requirements.add(requirement);
        specificTargetTypes.add(requirement.getTargetSpecification().getType());
      }
    }
    return specificTargetTypes;
  }
  
}
