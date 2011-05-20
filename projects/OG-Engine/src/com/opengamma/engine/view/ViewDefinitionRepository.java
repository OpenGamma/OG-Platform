/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Set;

/**
 * Allows modules, specifically the {@link ViewProcessor}, access to
 * {@link ViewDefinition}s.
 *
 * @author kirk
 */
public interface ViewDefinitionRepository {
  
  /**
   * Returns the set of all currently defined views.
   * 
   * @return the definition names, not {@code null}
   */
  Set<String> getDefinitionNames();
  
  /**
   * Returns the named definition.
   * 
   * @param definitionName the name of the view, not {@code null}
   * @return the view definition, or {@code null} if the name does not exist
   */
  ViewDefinition getDefinition(String definitionName);

}
